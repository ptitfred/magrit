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

import java.util.Collection;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.model.Pair;
import org.kercoin.magrit.core.model.UserIdentity;

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
