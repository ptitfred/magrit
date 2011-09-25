package org.kercoin.magrit.services.concurrent;

import com.google.inject.ImplementedBy;

@ImplementedBy(RepositoryGuardImpl.class)
public interface RepositoryGuard {
	void acquire(String repoPath) throws InterruptedException;
	void release(String repoPath);
}
