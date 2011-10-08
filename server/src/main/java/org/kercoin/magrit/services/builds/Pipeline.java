package org.kercoin.magrit.services.builds;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.google.inject.ImplementedBy;

/**
 * Pipeline API
 */
@ImplementedBy(PipelineImpl.class)
public interface Pipeline {

	public static interface CriticalResource {
		Lock getLock();
	}

	public static interface Task<E> extends Callable<E> {
		Key getKey();
		void setKey(Key k);
		Date getSubmitDate();
		void setSubmitDate(Date d);
		CriticalResource getUnderlyingResource();
		InputStream openStdout();
	}

	public static interface Listener {
		void onSubmit(Key k);
		void onStart(Key k);
		void onDone(Key k);
	}

	public static interface Key extends Comparable<Key> {
		/**
		 * An uniq id, doesn't have any sense for client of the pipeline.
		 * @return
		 */
		int uniqId();
		/**
		 * Tells if the key is valid or not. A non valid task avoids the use of <code>null</code> magic value.
		 * @return
		 */
		boolean isValid();
	}

	public static interface Filter {

		/**
		 * Allows the user refine the list of tasks he is looking for.
		 * @param s
		 * @param submissionDate
		 * @return
		 * @see Pipeline#list(Filter...)
		 */
		boolean matches(boolean isRunning, Date submissionDate);
	}

	/**
	 * Submits tasks to the pipeline. If the tasks is accepted, the returned key will be valid.
	 * @param task
	 * @return
	 */
	Key submit(Task<BuildResult> task);

	Task<BuildResult> get(Key taskId);

	void addListener(Listener listener);
	void removeListener(Listener listener);

	void waitFor(Key... k) throws InterruptedException;
	void waitFor(long time, TimeUnit unit, Key... k) throws InterruptedException;

	/**
	 * @param taskId
	 */
	void cancel(Key taskId);

	List<Key> list(Filter... filters);

	/**
	 * Returns a replicated outputstream to the stdout of the task identified by the {@link Key}.
	 * @param task
	 * @return
	 */
	InputStream cat(Key task);

	Future<BuildResult> getFuture(Key k);
}
