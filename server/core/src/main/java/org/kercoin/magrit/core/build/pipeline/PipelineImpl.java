/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

This file is part of Magrit.

Magrit is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Magrit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with Magrit.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.kercoin.magrit.core.build.pipeline;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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

import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PipelineImpl implements Pipeline {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Semaphore slots;

	private AtomicInteger keyId = new AtomicInteger(0);

	private final ExecutorService dispatcher;
	private final Notifier notifier = new Notifier();

	private volatile Map<Key, Future<BuildResult>> futures;
	// @GuardedBy(main)
	private volatile Map<Key, Task<BuildResult>> tasks;
	private volatile Set<Key> workings;

	@Inject
	public PipelineImpl(Context ctx) {
		log.info("{} cores", ctx.configuration().getSlots());
		slots = new Semaphore(ctx.configuration().getSlots());
		tasks = new ConcurrentHashMap<Key, Task<BuildResult>>();
		futures = new ConcurrentHashMap<Key, Future<BuildResult>>();
		workings = Collections.newSetFromMap(new ConcurrentHashMap<Key, Boolean>());
		dispatcher = new DispatcherThreadPool(ctx.configuration().getSlots(), new PriorityBlockingQueue<Runnable>());
		addListener(accumulator);
	}

	private static final class DispatcherThreadPool extends ThreadPoolExecutor {

		private static final long KEEP_ALIVE_TIME = 60L;

		private DispatcherThreadPool(int corePoolSize, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, Integer.MAX_VALUE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue);
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
			return new DispatchTask<T>(callable);
		}
	}

	private static final class DispatchTask<T> extends FutureTask<T> implements Comparable<DispatchTask<T>> {

		private Key key;

		@SuppressWarnings("rawtypes")
		private DispatchTask(Callable<T> callable) {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			DispatchTask<?> other = (DispatchTask<?>) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			return true;
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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ValidKey other = (ValidKey) obj;
			if (id != other.id) {
				return false;
			}
			return true;
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

		private static final int WORKER_TIMER_SECONDS = 5;

		private final Task<V> t;

		Worker(Task<V> t) {
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
				boolean hasAcquired = false;
				try {
					do {
						hasAcquired = slots.tryAcquire(WORKER_TIMER_SECONDS, TimeUnit.SECONDS);
					} while (!hasAcquired);
					main.writeLock().lockInterruptibly();
					workings.add(k);
					main.writeLock().unlock();
					notifier.justStarted(k);
					return t.call();
				} finally {
					if (hasAcquired) {
						if (!main.writeLock().isHeldByCurrentThread()) {
							main.writeLock().lockInterruptibly();
						}
						tasks.remove(k);
						workings.remove(k);
						main.writeLock().unlock();
						slots.release();
						notifier.justEnded(k);
					}
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
			notifier.justSubmitted(k);
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

	@Override
	public void addListener(Listener listener) {
		notifier.addListener(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		notifier.removeListener(listener);
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
			try {
				boolean timeout = false;
				long ns1 = System.nanoTime();
				if (waitNS>0) {
					timeout = !waitForNotifier.await(waitNS - ns1 + ns0, TimeUnit.NANOSECONDS);
				} else {
					waitForNotifier.await();
				}
				if (!timeout) {
					keys.retainAll(this.tasks.keySet());
					mustWait = keys.size() > 0;
				} else {
					mustWait = false;
				}
			} finally {
				waitForHeartBeat.unlock();
			}
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

	boolean awaitEventDispatching(long timeout, TimeUnit unit) throws InterruptedException {
		return notifier.await(timeout, unit);
	}
}
