package org.kercoin.magrit.services;

import java.util.Collection;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.utils.UserIdentity;
import org.kercoin.magrit.utils.Pair;

public interface BuildQueueService {
	Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository, String sha1, boolean force) throws Exception;
	
	void addCallback(BuildCallback callback);

	void removeCallback(BuildCallback callback);
	
	Collection<Pair<Repository, String>> getScheduledTasks();
	Collection<Pair<Repository, String>> getCurrentTasks();
	
}
