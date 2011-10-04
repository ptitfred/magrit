package org.kercoin.magrit.services.builds;


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.Pipeline.CriticalResource;
import org.kercoin.magrit.services.builds.Pipeline.Filter;
import org.kercoin.magrit.services.builds.Pipeline.Key;
import org.kercoin.magrit.services.builds.Pipeline.Listener;
import org.kercoin.magrit.services.builds.Pipeline.Task;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PipelineImplTest {

	PipelineImpl pipeline;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Context ctx;

	static class CR implements CriticalResource {

		private String pseudoPath;

		private Lock lock;

		private CR(String pseudoPath) {
			this.pseudoPath = pseudoPath;
			lock = new ReentrantLock(true);
		}

		@Override
		public Lock getLock() {
			return lock;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((pseudoPath == null) ? 0 : pseudoPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CR other = (CR) obj;
			if (pseudoPath == null) {
				if (other.pseudoPath != null)
					return false;
			} else if (!pseudoPath.equals(other.pseudoPath))
				return false;
			return true;
		}

	}

	Map<String, CriticalResource> locks = new HashMap<String, Pipeline.CriticalResource>();

	private CriticalResource getLock(String repoPath) {
		if (locks.get(repoPath) == null) {
			locks.put(repoPath, new CR(repoPath));
		}
		return locks.get(repoPath);
	}

	class TestTask implements Pipeline.Task<BuildResult> {

		private final int ms;
		private final CriticalResource lock;

		private Date submit;
		private Key key;

		private TestTask(String repoPath, int ms) {
			this.ms = ms;
			this.lock = getLock(repoPath);
		}

		@Override
		public void setSubmitDate(Date d) {
			this.submit = d;
		}

		@Override
		public Date getSubmitDate() {
			return submit;
		}

		@Override
		public BuildResult call() throws Exception {
			Thread.sleep(ms);
			return null;
		}

		@Override
		public int compareTo(Task<BuildResult> o) {
			return (int) (o.getKey().uniqId() - getKey().uniqId());
		}

		@Override
		public Key getKey() {
			return key;
		}

		@Override
		public void setKey(Key k) {
			this.key = k;
		}

		@Override
		public CriticalResource getUnderlyingResource() {
			return lock;
		}

		@Override
		public InputStream openStdout() {
			return new ByteArrayInputStream("42".getBytes());
		}

	}

	private Pipeline.Task<BuildResult> createTask(String path, int durationMs) {
		return new TestTask(path, durationMs);
	}

	@Before
	public void setUp() throws Exception {
		given(ctx.configuration().getSlots()).willReturn(2);
		pipeline = new PipelineImpl(ctx);
	}

	Date now() {
		try {
			return new Date();
		} finally {
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		}
	}

	@Test
	public void cancel() throws Exception {
		Task<BuildResult> t = createTask("/", 5000);
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
		Key k = pipeline.submit(t);
		Thread.sleep(20); // Let the scheduler start the task
		assertThat(pipeline.list(PipelineImpl.running())).containsOnly(k);
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
		pipeline.cancel(t.getKey());
		assertThat(pipeline.list(PipelineImpl.running())).isEmpty();
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
		long l0 = System.currentTimeMillis();
		pipeline.waitFor(1, TimeUnit.SECONDS, k);
		long l1 = System.currentTimeMillis();
		assertThat(l1 - l0).isLessThan(1000);
	}

	@Test
	public void scenario() throws InterruptedException {
		final String R1 = "/R1";
		final String R2 = "/R2";
		final String R3 = "/R3";
		Task<BuildResult> t1 = createTask(R1, 200);
		Task<BuildResult> t2 = createTask(R2, 200);
		Task<BuildResult> t3 = createTask(R2, 200);
		Task<BuildResult> t4 = createTask(R3, 200);

		Key k1 = pipeline.submit(t1);
		Key k2 = pipeline.submit(t2);
		Key k3 = pipeline.submit(t3);
		Key k4 = pipeline.submit(t4);

		assertThat(pipeline.list(PipelineImpl.running())).hasSize(2).containsOnly(k1, k2);
		assertThat(pipeline.list(PipelineImpl.pending())).containsOnly(k3, k4);

		pipeline.waitFor(k1, k2);

		assertThat(pipeline.list(PipelineImpl.running())).excludes(k1, k2);
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();

		pipeline.waitFor(k3, k4);

		assertThat(pipeline.list(PipelineImpl.running())).isEmpty();
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
	}

	@Test
	public void intrication() throws InterruptedException {
		final String R1 = "/R1";
		final String R2 = "/R2";
		CriticalResource repo1 = getLock(R1);
		CriticalResource repo2 = getLock(R2);
		repo1.getLock().lock();
		repo2.getLock().lock();

		Task<BuildResult> t1 = createTask(R1, 100);
		Task<BuildResult> t2 = createTask(R2, 100);
		Key k1 = pipeline.submit(t1);
		Key k2 = pipeline.submit(t2);
		assertThat(pipeline.list(PipelineImpl.running())).isEmpty();
		assertThat(pipeline.list(PipelineImpl.pending())).containsOnly(k1, k2);
		repo1.getLock().unlock();
		Thread.sleep(5); // give time for the task to start
		assertThat(pipeline.list(PipelineImpl.running())).containsOnly(k1);
		assertThat(pipeline.list(PipelineImpl.pending())).containsOnly(k2);
		repo2.getLock().unlock();
		pipeline.waitFor(k1);
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
		assertThat(pipeline.list(PipelineImpl.running())).containsOnly(k2);
		pipeline.waitFor(k2);
		assertThat(pipeline.list(PipelineImpl.running())).isEmpty();
		assertThat(pipeline.list(PipelineImpl.pending())).isEmpty();
	}

	@Test
	public void cat() throws Exception {
		CriticalResource repo = getLock("/");
		repo.getLock().lock();
		Task<BuildResult> t1 = createTask("/", 100);
		Key k1 = pipeline.submit(t1);
		assertThat(pipeline.cat(k1)).isNull();
		repo.getLock().unlock();
		Thread.sleep(5); // give time to the task to start
		assertThat(pipeline.cat(k1)).isNotNull();
		Thread.sleep(100); // task should have done
		assertThat(pipeline.cat(k1)).isNull();
	}

	@Test
	public void filters_byDates() throws InterruptedException {
		Date d0 = now();
		Date ref1 = now();
		Date d1 = now();
		Date ref2 = now();
		Date d2 = now();

		Filter between = PipelineImpl.between(ref1, ref2);
		assertThat(between.matches(true, d0)).isFalse();
		assertThat(between.matches(true, d1)).isTrue();
		assertThat(between.matches(true, d2)).isFalse();

		Filter since = PipelineImpl.since(ref1);
		assertThat(since.matches(true, d0)).isFalse();
		assertThat(since.matches(true, d1)).isTrue();
		assertThat(since.matches(true, d2)).isTrue();

		Filter until = PipelineImpl.until(ref1);
		assertThat(until.matches(true, d0)).isTrue();
		assertThat(until.matches(true, d1)).isFalse();
		assertThat(until.matches(true, d2)).isFalse();
	}

	class LogListener implements Listener {

		private static final int SUBMIT = 1<<1;
		private static final int START = 1<<2;
		private static final int DONE = 1<<3;

		private final int mask;

		private LogListener(int evtMask) {
			mask = evtMask;
		}

		StringBuilder log = new StringBuilder();

		private boolean check(int evt) {
			return (mask & evt) == evt;
		}

		public void onSubmit(Key k) {
			if (check(SUBMIT)) {
				log(k, "SUBMIT");
			}
		}

		public void onStart(Key k) {
			if (check(START)) {
				log(k, "START");
			}
		}

		public void onDone(Key k) {
			if (check(DONE)) {
				log(k, "DONE");
			}
		}

		private void log(Key k, String text) {
			log.append(text).append(" ").append(k.uniqId()).append("\n");
		}

	}

	@Test
	public void listeners() throws InterruptedException {
		LogListener listener = new LogListener(LogListener.START | LogListener.DONE);
		pipeline.addListener(listener);
		pipeline.submit(createTask("/", 100));
		Key k2 = pipeline.submit(createTask("/", 100));
		pipeline.waitFor(1, TimeUnit.SECONDS, k2);
		assertThat(listener.log.toString()).isEqualTo("START 1\nDONE 1\nSTART 2\nDONE 2\n");
	}

}
