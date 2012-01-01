/*
Copyright 2011 Frederic Menou

This file is part of Magrit.

Magrit is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Magrit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with Magrit.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.kercoin.magrit.core.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.build.Pipeline.CriticalResource;

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
