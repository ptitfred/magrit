package org.kercoin.magrit.services;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
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
import org.kercoin.magrit.utils.UserIdentity;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BuildQueueServiceImpl implements BuildQueueService {

	private final Context context;
	private final GitUtils gitUtils;
	private final TimeService timeService;
	
	private final ExecutorService executorService;

	private final Map<Pair<Repository, String>, BuildTask> pendings;
	private final Map<Pair<Repository, String>, BuildTask> workplace;

	class PingBackExecutorService extends ThreadPoolExecutor {
		public PingBackExecutorService() {
			super(1, 1, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			if (r instanceof BuildTask) {
				fireStarted(((BuildTask) r).getTarget());
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
	public BuildQueueServiceImpl(Context context, GitUtils gitUtils, TimeService timeService) {
		this.context = context;
		this.gitUtils = gitUtils;
		this.timeService = timeService;
		this.workplace = new ConcurrentHashMap<Pair<Repository,String>, BuildTask>();
		this.pendings = new ConcurrentHashMap<Pair<Repository, String>, BuildTask>();
		this.executorService = new PingBackExecutorService();
	}

	@Override
	public Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository, String sha1) throws Exception {
		Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
		BuildTask task = new BuildTask(this.gitUtils, committer, timeService, repository, target);
		pendings.put(target, task);
		fireScheduled(target);
		return executorService.submit(task);
	}
	
	private Repository findBuildPlace(Repository repository) throws IOException {
		String originalPath = repository.getDirectory().getAbsolutePath();
		String targetPath = originalPath.replaceFirst(context.configuration().getRepositoriesHomeDir().getAbsolutePath(), context.configuration().getWorkHomeDir().getAbsolutePath());
		File workTree = new File(targetPath);
		workTree.mkdirs();
		Repository workRepo = new RepositoryBuilder().setWorkTree(workTree).build();
		if (!workRepo.getDirectory().exists()) {
			workRepo.create(false);
		}
		return workRepo;
	}

	private Set<BuildCallback> callbacks = new HashSet<BuildCallback>();
	
	@Override
	public void addCallback(BuildCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeCallback(BuildCallback callback) {
		callbacks.remove(callback);
	}
	
	void fireScheduled(Pair<Repository, String> e) {
		for (BuildCallback callback : callbacks) {
			callback.buildScheduled(e.getT(), e.getU());
		}
	}
	
	void fireStarted(Pair<Repository, String> e) {
		workplace.put(e, pendings.remove(e));
		for (BuildCallback callback : callbacks) {
			callback.buildStarted(e.getT(), e.getU());
		}
	}

	void fireEnded(Pair<Repository, String> e) {
		workplace.remove(e);
		for (BuildCallback callback : callbacks) {
			callback.buildEnded(e.getT(), e.getU(), BuildStatus.CLEAN);
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
