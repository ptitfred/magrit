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

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.kercoin.magrit.core.build.BuildResult;

import com.google.inject.ImplementedBy;

/**
 * Pipeline API
 */
@ImplementedBy(PipelineImpl.class)
public interface Pipeline {

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
