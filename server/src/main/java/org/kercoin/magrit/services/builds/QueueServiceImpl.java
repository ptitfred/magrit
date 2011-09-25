package org.kercoin.magrit.services.builds;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.utils.TimeService;
import org.kercoin.magrit.utils.Pair;
import org.kercoin.magrit.utils.UserIdentity;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class QueueServiceImpl implements QueueService {

	private final Context context;
	private final TimeService timeService;
	
	private final ExecutorService executorService;

	private final Map<Pair<Repository, String>, Task> pendings;
	private final Map<Pair<Repository, String>, Task> workplace;
	private final StatusesService statusService;

	class PingBackExecutorService extends ThreadPoolExecutor {
		public PingBackExecutorService() {
			super(1, 1, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			if (r instanceof Task) {
				fireStarted(((Task) r).getTarget());
			}
			super.beforeExecute(t, r);
		}
		
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			if (r instanceof FutureTask<?>) {
//				fireEnded(((BuildTask) r).getTarget());
			}
			super.afterExecute(r, t);
		}
	}

	@Inject
	public QueueServiceImpl(Context context, TimeService timeService, StatusesService statusService) {
		this.context = context;
		this.timeService = timeService;
		this.workplace = new ConcurrentHashMap<Pair<Repository,String>, Task>();
		this.pendings = new ConcurrentHashMap<Pair<Repository, String>, Task>();
		this.executorService = new PingBackExecutorService();
		this.statusService = statusService;
	}

	@Override
	public Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository, String sha1, boolean force) throws Exception {
		if (!shouldBuild(repository, sha1, force)) {
			return null;
		}
		
		context.getRepositoryGuard().acquire(repository.getDirectory().getAbsolutePath());
		Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
		Task task = new Task(this.context, committer, timeService, repository, target);
		pendings.put(target, task);
		fireScheduled(target);
		return executorService.submit(task);
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

	private Set<BuildLifeCycleListener> callbacks = new HashSet<BuildLifeCycleListener>();
	
	@Override
	public void addCallback(BuildLifeCycleListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeCallback(BuildLifeCycleListener callback) {
		callbacks.remove(callback);
	}
	
	void fireScheduled(Pair<Repository, String> e) {
		for (BuildLifeCycleListener callback : callbacks) {
			callback.buildScheduled(e.getT(), e.getU());
		}
	}
	
	void fireStarted(Pair<Repository, String> e) {
		workplace.put(e, pendings.remove(e));
		for (BuildLifeCycleListener callback : callbacks) {
			callback.buildStarted(e.getT(), e.getU());
		}
	}

	void fireEnded(Pair<Repository, String> e) {
		workplace.remove(e);
		for (BuildLifeCycleListener callback : callbacks) {
			callback.buildEnded(e.getT(), e.getU(), Status.OK);
		}
	}

	@Override
	public Collection<Pair<Repository, String>> getCurrentTasks() {
		return workplace.keySet();
	}
	
	@Override
	public Collection<Pair<Repository, String>> getScheduledTasks() {
		return pendings.keySet();
	}

}
