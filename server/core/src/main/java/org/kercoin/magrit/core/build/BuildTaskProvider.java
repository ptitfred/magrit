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

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.utils.TimeService;

import com.google.inject.Inject;

/**
 * @author ptitfred
 *
 */
public class BuildTaskProvider {

	private final Context context;
	private final RepositoryGuard guard;
	private final TimeService timeService;
	private final BuildDAO dao;

	@Inject
	public BuildTaskProvider(Context context, RepositoryGuard guard, TimeService timeService, BuildDAO dao) {
		this.context = context;
		this.guard = guard;
		this.timeService = timeService;
		this.dao = dao;
	}

	public BuildTask get(UserIdentity committer, Repository repository,Pair<Repository, String> target, String command) {
		return new BuildTask(context, guard, committer, timeService, dao, repository, target, command);
	}

}
