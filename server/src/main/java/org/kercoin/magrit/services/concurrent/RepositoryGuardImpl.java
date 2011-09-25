package org.kercoin.magrit.services.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RepositoryGuardImpl implements RepositoryGuard {

	private Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();
	
	@Override
	public void acquire(String repoPath) throws InterruptedException {
		repoPath = repoPath.trim();
		synchronized (locks) {
			if (locks.get(repoPath) == null) {
				locks.put(repoPath, new ReentrantLock(true));
			}
		}
		locks.get(repoPath).lockInterruptibly();
	}

	@Override
	public void release(String repoPath) {
		repoPath = repoPath.trim();
		locks.get(repoPath).unlock();
	}

}
