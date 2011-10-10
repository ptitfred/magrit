package org.kercoin.magrit.services.builds;

import java.util.Collection;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.utils.UserIdentity;
import org.kercoin.magrit.utils.Pair;

import com.google.inject.ImplementedBy;

@ImplementedBy(QueueServiceImpl.class)
public interface QueueService {
	Future<BuildResult> enqueueBuild(UserIdentity committer, Repository repository,
			String sha1, String command, boolean force) throws Exception;
	
	void addCallback(BuildLifeCycleListener callback);

	void removeCallback(BuildLifeCycleListener callback);
	
	Collection<Pair<Repository, String>> getScheduledTasks();
	Collection<Pair<Repository, String>> getCurrentTasks();
	
}
