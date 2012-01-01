/*
Copyright 2011 Frederic Menou

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


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildResult;
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

	Map<String, CriticalResource> locks = new HashMap<String, CriticalResource>();

	private CriticalResource getLock(String repoPath) {
		if (locks.get(repoPath) == null) {
			locks.put(repoPath, new CR(repoPath));
		}
		return locks.get(repoPath);
	}

	class TestTask implements Task<BuildResult> {

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

	private Task<BuildResult> createTask(String path, int durationMs) {
		return new TestTask(path, durationMs);
	}

	@Before
	public void setUp() throws Exception {
		given(ctx.configuration().getSlots()).willReturn(2);
		pipeline = new PipelineImpl(ctx);
	}

	@Test
	public void cancel() throws Exception {
		Task<BuildResult> t = createTask("/", 5000);
		assertThat(pipeline.list(Filters.pending())).isEmpty();
		Key k = pipeline.submit(t);
		Thread.sleep(20); // Let the scheduler start the task
		assertThat(pipeline.list(Filters.running())).containsOnly(k);
		assertThat(pipeline.list(Filters.pending())).isEmpty();
		pipeline.cancel(t.getKey());
		assertThat(pipeline.list(Filters.running())).isEmpty();
		assertThat(pipeline.list(Filters.pending())).isEmpty();
		long l0 = System.currentTimeMillis();
		pipeline.waitFor(1, TimeUnit.SECONDS, k);
		long l1 = System.currentTimeMillis();
		assertThat(l1 - l0).isLessThan(1000);
	}

	@Test
	public void scenario() throws InterruptedException {
		// given ---------------------------------
		final String R1 = "/R1";
		final String R2 = "/R2";
		final String R3 = "/R3";
		Task<BuildResult> t1 = createTask(R1, 110);
		Task<BuildResult> t2 = createTask(R2, 110);
		Task<BuildResult> t3 = createTask(R2, 110);
		Task<BuildResult> t4 = createTask(R3, 110);

		LogListener logger = new LogListener(LogListener.SUBMIT | LogListener.START | LogListener.DONE);
		pipeline.addListener(logger);
		
		// when ----------------------------------
		Key k1 = pipeline.submit(t1);
		Key k2 = pipeline.submit(t2);
		Key k3 = pipeline.submit(t3);
		Key k4 = pipeline.submit(t4);
		
		Thread.sleep(5);
		
		List<Key> running1 = pipeline.list(Filters.running());
		List<Key> pending1 = pipeline.list(Filters.pending());
		
		pipeline.waitFor(k1, k2);
		Thread.sleep(5);

		List<Key> running2 = pipeline.list(Filters.running());
		List<Key> pending2 = pipeline.list(Filters.pending());

		pipeline.waitFor(k3, k4);
		Thread.sleep(5);

		List<Key> running3 = pipeline.list(Filters.running());
		List<Key> pending3 = pipeline.list(Filters.pending());

		// then ----------------------------------
		assertThat(pending1).containsOnly(k3, k4);
		assertThat(running1).containsOnly(k1, k2);

		assertThat(running2).excludes(k1, k2);
		assertThat(pending2).isEmpty();

		assertThat(running3).isEmpty();
		assertThat(pending3).isEmpty();
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
		assertThat(pipeline.list(Filters.running())).isEmpty();
		assertThat(pipeline.list(Filters.pending())).containsOnly(k1, k2);
		repo1.getLock().unlock();
		Thread.sleep(5); // give time for the task to start
		assertThat(pipeline.list(Filters.running())).containsOnly(k1);
		assertThat(pipeline.list(Filters.pending())).containsOnly(k2);
		repo2.getLock().unlock();
		pipeline.waitFor(k1);
		assertThat(pipeline.list(Filters.pending())).isEmpty();
		assertThat(pipeline.list(Filters.running())).containsOnly(k2);
		pipeline.waitFor(k2);
		assertThat(pipeline.list(Filters.running())).isEmpty();
		assertThat(pipeline.list(Filters.pending())).isEmpty();
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
		boolean timeout = pipeline.awaitEventDispatching(250, TimeUnit.MILLISECONDS);
		assertThat(timeout).isFalse();
		assertThat(listener.log.toString()).isEqualTo("START 1\nDONE 1\nSTART 2\nDONE 2\n");
	}

}
