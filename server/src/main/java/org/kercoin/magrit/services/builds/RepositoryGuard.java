package org.kercoin.magrit.services.builds;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.services.builds.Pipeline.CriticalResource;

import com.google.inject.Singleton;

@Singleton
public class RepositoryGuard {
	
	Map<File, CriticalResource> locks = new HashMap<File, CriticalResource>();
	
	static class CR implements CriticalResource {

		final Lock l = new ReentrantLock();
		
		public Lock getLock() {
			return l;
		}
		
	}
	
	CriticalResource get(Repository repo) {
		CriticalResource cr = locks.get(repo.getDirectory());
		if (cr == null) {
			cr = new CR();
			locks.put(repo.getDirectory(), cr);
		}
		return cr;
	}
	
}
