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
package org.kercoin.magrit.services.builds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.Pipeline.Filter;
import org.kercoin.magrit.services.builds.Pipeline.Key;
import org.kercoin.magrit.services.builds.Pipeline.Listener;
import org.kercoin.magrit.services.builds.Pipeline.Task;
import org.kercoin.magrit.services.utils.TimeService;
import org.kercoin.magrit.utils.Pair;
import org.kercoin.magrit.utils.UserIdentity;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class QueueServiceImpl implements QueueService {

	private final Context context;
	private final TimeService timeService;

	private final StatusesService statusService;

	private final Pipeline pipeline;

	private final RepositoryGuard guard;

	@Inject
	public QueueServiceImpl(Context context, TimeService timeService,
			StatusesService statusService, Pipeline pipeline, RepositoryGuard guard) {
		this.context = context;
		this.timeService = timeService;
		this.statusService = statusService;
		this.pipeline = pipeline;
		pipeline.addListener(new Tracker());
		this.guard = guard;
	}

	private Map<Key, Pair<Repository, String>> tracker = new HashMap<Key, Pair<Repository, String>>();

	class Tracker implements Listener {

		public void onSubmit(Key k) {}
		public void onStart(Key k) {}

		@Override
		public void onDone(Key k) {
			tracker.remove(k);
		}

	}

	@Override
	public Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository,
			String sha1, String command, boolean force) throws Exception {
		if (!shouldBuild(repository, sha1, force)) {
			return null;
		}

		Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
		Task<BuildResult> task = new BuildTask(context, guard, committer, timeService, repository, target, command);
		Key k = pipeline.submit(task);
		tracker.put(k, target);
		return pipeline.getFuture(k);
	}

	boolean shouldBuild(Repository repository, String sha1,
			boolean force) {
		if (force) return true;
		return shouldBuild(statusService.getStatus(repository, sha1));
	}

	boolean shouldBuild(List<Status> statuses) {
		if (statuses.isEmpty()) {
			return true;
		}
		Status last = statuses.get(statuses.size() - 1);
		if (statuses.size()==1) {
			switch (last) {
			case UNKNOWN:
				return false;
			case LOCAL:
			case NEW:
				return true;
			}
		}
		switch (last) {
		case RUNNING:
		case PENDING:
		case OK:
			return false;
		default:
			return true;
		}
	}

	private Repository findBuildPlace(Repository repository) throws IOException {
		String originalPath = repository.getDirectory().getAbsolutePath();
		String targetPath = originalPath.replaceFirst(
				context.configuration().getRepositoriesHomeDir().getAbsolutePath(),
				context.configuration().getWorkHomeDir().getAbsolutePath());
		File workTree = new File(targetPath);
		workTree.mkdirs();
		Repository workRepo = new RepositoryBuilder().setWorkTree(workTree).build();
		if (!workRepo.getDirectory().exists()) {
			workRepo.create(false);
		}
		return workRepo;
	}

	class DelegateListener implements Listener {

		BuildLifeCycleListener callback;

		DelegateListener(BuildLifeCycleListener callback) {
			this.callback = callback;
		}

		@Override
		public void onSubmit(Key k) {
			Pair<Repository, String> pair = tracker.get(k);
			callback.buildScheduled(tracker.get(k).getT(), pair.getU());
		}

		@Override
		public void onStart(Key k) {
			Pair<Repository, String> pair = tracker.get(k);
			callback.buildStarted(pair.getT(), pair.getU());
		}

		@Override
		public void onDone(Key k) {
			Pair<Repository, String> pair = tracker.get(k);
			callback.buildEnded(pair.getT(), pair.getU(), Status.UNKNOWN);
		}

	}

	Map<BuildLifeCycleListener, DelegateListener> callbackDelegates =
			new HashMap<BuildLifeCycleListener, QueueServiceImpl.DelegateListener>();

	@Override
	public void addCallback(BuildLifeCycleListener callback) {
		DelegateListener delegate = new DelegateListener(callback);
		callbackDelegates.put(callback, delegate);
		pipeline.addListener(delegate);
	}

	@Override
	public void removeCallback(BuildLifeCycleListener callback) {
		DelegateListener listener = callbackDelegates.get(callback);
		if (listener != null) {
			pipeline.removeListener(listener);
		}
	}

	@Override
	public Collection<Pair<Repository, String>> getCurrentTasks() {
		return get(PipelineImpl.running());
	}

	private Collection<Pair<Repository, String>> get(Filter filter) {
		List<Key> keys = pipeline.list(PipelineImpl.running());
		Collection<Pair<Repository, String>> pairs = new ArrayList<Pair<Repository, String>>();
		for (Key k : keys) {
			Pair<Repository, String> pair = tracker.get(k);
			if (pair != null) {
				pairs.add(pair);
			}
		}
		return pairs;
	}

	@Override
	public Collection<Pair<Repository, String>> getScheduledTasks() {
		return get(PipelineImpl.pending());
	}

}
