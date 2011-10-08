package org.kercoin.magrit.services.builds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.mina.util.ConcurrentHashSet;
import org.kercoin.magrit.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PipelineImpl implements Pipeline {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Semaphore slots;

	private AtomicInteger keyId = new AtomicInteger(0);

	private ExecutorService dispatcher;
	private ExecutorService edt;

	private volatile Map<Key, Future<BuildResult>> futures;
	// @GuardedBy(main)
	private volatile Map<Key, Task<BuildResult>> tasks;
	private volatile Set<Key> workings;

	@Inject
	public PipelineImpl(Context ctx) {
		log.info("{} cores", ctx.configuration().getSlots());
		slots = new Semaphore(ctx.configuration().getSlots());
		tasks = new ConcurrentHashMap<Pipeline.Key, Pipeline.Task<BuildResult>>();
		futures = new ConcurrentHashMap<Pipeline.Key, Future<BuildResult>>();
		workings = new ConcurrentHashSet<Pipeline.Key>();
		dispatcher = new ThreadPoolExecutor(ctx.configuration().getSlots(), Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>()) {
			@Override
			protected <T> RunnableFuture<T> newTaskFor(
					Callable<T> callable) {
				return new DispatchTask<T>(callable);
			}
		};
		edt = Executors.newSingleThreadExecutor();
	}

	class DispatchTask<T> extends FutureTask<T> implements Comparable<DispatchTask<T>> {

		private Key key;

		@SuppressWarnings("rawtypes")
		public DispatchTask(Callable<T> callable) {
			super(callable);
			if (callable instanceof Worker) {
				this.key = ((Worker) callable).getKey();
			}
		}

		@Override
		public int compareTo(DispatchTask<T> o) {
			if (key==null && o.key == null) {
				return 0;
			}
			if (key == null) {
				return -1;
			}
			if (o.key == null) {
				return 1;
			}
			return key.compareTo(o.key);
		}

	}

	static class ValidKey implements Key {

		private final int id;

		ValidKey(int id) {
			this.id = id;
		}

		@Override
		public int uniqId() {
			return id;
		}

		@Override
		public int compareTo(Key o) {
			return uniqId() - o.uniqId();
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public String toString() {
			return String.format("{%s}", id);
		}

	}

	<U> Worker<U> w(Task<U> t) {
		return new Worker<U>(t);
	}

	class Worker<V> implements Callable<V> {

		final Task<V> t;

		public Worker(Task<V> t) {
			this.t = t;
		}
		
		Key getKey() {
			return t.getKey();
		}

		@Override
		public V call() throws Exception {
			Lock lock = t.getUnderlyingResource().getLock();
			try {
				lock.lockInterruptibly();
				Key k = t.getKey();
				try {
					while (!slots.tryAcquire(5, TimeUnit.SECONDS)) {}
					main.writeLock().lockInterruptibly();
					workings.add(k);
					main.writeLock().unlock();
					justStarted(k);
					V v = t.call();
					return v;
				} finally {
					if (!main.writeLock().isHeldByCurrentThread()) {
						main.writeLock().lockInterruptibly();
					}
					tasks.remove(k);
					workings.remove(k);
					main.writeLock().unlock();
					slots.release();
					justEnded(k);
				}
			} finally {
				lock.unlock();
			}
		}

	}

	private Key nextValid() {
		return new ValidKey(keyId.incrementAndGet());
	}

	@Override
	public Key submit(Task<BuildResult> task) {
		try {
			main.writeLock().lock();
			Key k = nextValid();
			task.setKey(k);
			task.setSubmitDate(new Date());
			tasks.put(k, task);

			futures.put(k, dispatcher.submit(w(task)));
			justSubmitted(k);
			return k;
		} finally {
			main.writeLock().unlock();
		}
	}

	@Override
	public Task<BuildResult> get(Key taskId) {
		return tasks.get(taskId);
	}

	private ReentrantReadWriteLock main = new ReentrantReadWriteLock(true);

	@Override
	public void cancel(Key taskId) {
		try {
			main.writeLock().lock();
			tasks.remove(taskId);
			futures.remove(taskId).cancel(false);
			workings.remove(taskId);
		} finally {
			main.writeLock().unlock();
		}
	}

	@Override
	public List<Key> list(Filter... filters) {
		List<Key> list = new ArrayList<Key>();
		Collection<Task<BuildResult>> cTasks = null;
		Set<Key> cWorkings = null;
		try {
			main.readLock().lock();
			cTasks = new ArrayList<Task<BuildResult>>(tasks.values());
			cWorkings = new HashSet<Key>(workings);
		} finally {
			main.readLock().unlock();
		}
		for (Task<?> t : cTasks) {
			for (Filter f : filters) {
				boolean running = cWorkings.contains(t.getKey());
				if (f.matches(running, t.getSubmitDate())) {
					list.add(t.getKey());
					break;
				}
			}
		}
		return list;
	}

	@Override
	public InputStream cat(Key task) {
		if (!workings.contains(task)) {
			return null;
		}
		if (tasks.get(task) == null) {
			return null;
		}
		return tasks.get(task).openStdout();
	}

	private static final Filter ANY = new Filter() {
		
		@Override
		public boolean matches(boolean isRunning, Date submissionDate) {
			return true;
		}
	};
	private static final Filter PENDING = new Filter() {
		public boolean matches(boolean r, Date d) {
			return !r;
		}
	};
	private static final Filter RUNNING = new Filter() {
		public boolean matches(boolean r, Date d) {
			return r;
		}
	};
	
	public static Filter any() {
		return ANY;
	}

	public static Filter pending() {
		return PENDING;
	}

	public static Filter running() {
		return RUNNING;
	}

	public static Filter since(final Date from) {
		return between(from, null);
	}

	public static Filter until(final Date to) {
		return between(null, to);
	}

	public static Filter between(final Date from, final Date to) {
		if (from == null && to == null) {
			throw new NullPointerException("Both dates can't be null.");
		}
		if (from != null && to != null && from.after(to)) {
			throw new IllegalArgumentException("from must be before to");
		}
		return new Filter() {
			@Override
			public boolean matches(boolean isRunning, Date submissionDate) {
				return (from == null || submissionDate.after(from))
						&& (to == null || submissionDate.before(to));
			}
		};
	}

	void justStarted(Key k) {
		fire(EventType.STARTED, k);
	}
	void justEnded(Key k) {
		fire(EventType.ENDED, k);
	}
	void justSubmitted(Key k) {
		fire(EventType.SUBMITTED, k);
	}

	void fire(EventType t, Key k) {
		edt.execute(new Event(t, k));
	}

	enum EventType {
		STARTED, ENDED, SUBMITTED
	}

	class Event implements Runnable {

		private EventType type;
		private Key key;

		public Event(EventType type, Key key) {
			super();
			this.type = type;
			this.key = key;
		}

		@Override
		public void run() {
			synchronized (listeners) {
				for (Listener l : listeners) {
					switch (type) {
					case STARTED:
						l.onStart(key);
						break;
					case ENDED:
						l.onDone(key);
						break;
					case SUBMITTED:
						l.onSubmit(key);
						break;
					}
				}
			}
		}

	}

	private Set<Listener> listeners = new HashSet<Listener>();

	@Override
	public void addListener(Listener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private final Lock waitForHeartBeat = new ReentrantLock();
	private final Condition waitForNotifier = waitForHeartBeat.newCondition();

	private Listener accumulator = new Listener() {

		@Override public void onSubmit(Key k) {
		}

		@Override public void onStart(Key k) {
		}

		private void signal() {
			waitForHeartBeat.lock();
			waitForNotifier.signalAll();
			waitForHeartBeat.unlock();
		}

		@Override public void onDone(Key k) {
			signal();
		}
	};

	{
		addListener(accumulator);
	}

	@Override
	public void waitFor(Key... k) throws InterruptedException {
		waitFor(0, null, k);
	}

	@Override
	public void waitFor(long time, TimeUnit unit, Key... ks) throws InterruptedException {
		long ns0 = System.nanoTime();
		if (ks == null || ks.length == 0) {
			return;
		}
		long waitNS = 0L;
		if (time >0 && unit != null) {
			waitNS = unit.toNanos(time);
		}
		Set<Key> keys = new HashSet<Key>(Arrays.asList(ks));
		keys.retainAll(this.tasks.keySet());

		boolean mustWait = keys.size()>0;
		while (mustWait) {
			waitForHeartBeat.lock();
			long ns1 = System.nanoTime();
			if (waitNS>0) {
				waitForNotifier.await(waitNS - ns1 + ns0, TimeUnit.NANOSECONDS);
			} else {
				waitForNotifier.await();
			}
			waitForHeartBeat.unlock();
			keys.retainAll(this.tasks.keySet());
			mustWait = keys.size() > 0;
		}
	}
	
	@Override
	public Future<BuildResult> getFuture(Key k) {
		main.readLock().lock();
		try {
			return this.futures.get(k);
		} finally {
			main.readLock().unlock();
		}
	}

}
