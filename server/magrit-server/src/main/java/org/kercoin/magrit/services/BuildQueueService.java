package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;

public interface BuildQueueService {
	void enqueueBuild(Repository repository, String sha1);
	
	void addCallback(BuildCallback callback);

	void removeCallback(BuildCallback callback);
}
