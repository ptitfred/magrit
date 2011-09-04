package org.kercoin.magrit.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildTask.Callback;
import org.kercoin.magrit.utils.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BuildQueueServiceImpl implements BuildQueueService, Callback<Pair<Repository,String>> {

	private final ExecutorService executorService;

	private Map<Pair<Repository, String>, Future<BuildResult>> workplace;

	private Context context;

	@Inject
	public BuildQueueServiceImpl(Context context) {
		this.context = context;
		this.workplace = new HashMap<Pair<Repository,String>, Future<BuildResult>>();
		this.executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void enqueueBuild(Repository repository, String sha1) {
		try {
			Pair<Repository, String> target = new Pair<Repository, String>(findBuildPlace(repository), sha1);
			workplace.put(target, executorService.submit(new BuildTask(repository, target, this)));
			fireStarted(target);
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public void finished(Pair<Repository,String> e) {
		fireEnded(e);
	}

	void fireStarted(Pair<Repository, String> e) {
		for (BuildCallback callback : callbacks) {
			callback.buildStarted(e.getT(), e.getU());
		}
	}

	void fireEnded(Pair<Repository, String> e) {
		for (BuildCallback callback : callbacks) {
			callback.buildEnded(e.getT(), e.getU(), BuildStatus.CLEAN);
		}
	}

}
