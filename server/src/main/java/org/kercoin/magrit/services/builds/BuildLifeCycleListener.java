package org.kercoin.magrit.services.builds;

import org.eclipse.jgit.lib.Repository;

public interface BuildLifeCycleListener {
	
	void buildScheduled(Repository repo, String sha1);
	
	void buildStarted(Repository repo, String sha1);

	void buildEnded(Repository repo, String sha1, Status status);
}
