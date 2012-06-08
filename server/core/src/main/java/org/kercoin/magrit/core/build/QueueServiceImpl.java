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
package org.kercoin.magrit.core.build;

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
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.pipeline.Filter;
import org.kercoin.magrit.core.build.pipeline.Filters;
import org.kercoin.magrit.core.build.pipeline.Key;
import org.kercoin.magrit.core.build.pipeline.Listener;
import org.kercoin.magrit.core.build.pipeline.Pipeline;
import org.kercoin.magrit.core.build.pipeline.Task;
import org.kercoin.magrit.core.user.UserIdentity;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class QueueServiceImpl implements QueueService {

	private final Context context;
	private final StatusesService statusService;
	private final Pipeline pipeline;
	private final BuildTaskProvider builder;

	@Inject
	public QueueServiceImpl(Context context, StatusesService statusService, Pipeline pipeline,
			BuildTaskProvider builder) {
		this.context = context;
		this.statusService = statusService;
		this.pipeline = pipeline;
		this.builder = builder;
	}

	private Map<Key, Pair<Repository, String>> tracker = new HashMap<Key, Pair<Repository, String>>();

	@Override
	public Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository,
			String sha1, String command, boolean force) throws Exception {
		if (!shouldBuild(repository, sha1, force)) {
			return null;
		}

		Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
		Task<BuildResult> task = builder.get(committer, repository, target, command);
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
		DelegateListener listener = callbackDelegates.remove(callback);
		if (listener != null) {
			pipeline.removeListener(listener);
		}
	}

	@Override
	public Collection<Pair<Repository, String>> getCurrentTasks() {
		return get(Filters.running());
	}

	private Collection<Pair<Repository, String>> get(Filter filter) {
		List<Key> keys = pipeline.list(filter);
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
		return get(Filters.pending());
	}

}
