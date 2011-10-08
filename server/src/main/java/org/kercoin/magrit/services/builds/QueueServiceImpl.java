package org.kercoin.magrit.services.builds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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
	public Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository, String sha1, boolean force) throws Exception {
		if (!shouldBuild(repository, sha1, force)) {
			return null;
		}

		Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
		Task<BuildResult> task = new BuildTask(context, guard, committer, timeService, repository, target);
		Key k = pipeline.submit(task);
		tracker.put(k, target);
		return pipeline.getFuture(k);
	}

	private boolean shouldBuild(Repository repository, String sha1,
			boolean force) {
		if (force) return true;
		List<Status> statuses = statusService.getStatus(repository, sha1);
		if (statuses.isEmpty()) {
			return true;
		}
		final EnumSet<Status> aggreg = EnumSet.copyOf(statuses);
		if (aggreg.size()==1) {
			if (aggreg.contains(Status.UNKNOWN)) {
				return false;
			}
			if (aggreg.contains(Status.LOCAL)) {
				return true;
			}
		}
		if (aggreg.contains(Status.RUNNING)) {
			return false;
		}
		return !aggreg.contains(Status.OK);
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
