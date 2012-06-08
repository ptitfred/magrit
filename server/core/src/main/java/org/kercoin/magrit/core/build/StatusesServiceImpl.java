/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.pipeline.Filter;
import org.kercoin.magrit.core.build.pipeline.Filters;
import org.kercoin.magrit.core.build.pipeline.Key;
import org.kercoin.magrit.core.build.pipeline.Pipeline;
import org.kercoin.magrit.core.build.pipeline.Task;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.utils.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Thead-safe
 * @author ptitfred
 *
 */
public class StatusesServiceImpl implements StatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;
	private final BuildDAO dao;
	private final Pipeline pipeline;

	@Inject
	public StatusesServiceImpl(GitUtils gitUtils, BuildDAO dao, Pipeline pipeline) {
		super();
		this.gitUtils = gitUtils;
		this.dao = dao;
		this.pipeline = pipeline;
	}

	private static enum State { RUNNING, PENDING, NONE }

	@Override
	public List<Status> getStatus(Repository repository, String sha1) {
		if (repository == null) {
			throw new NullPointerException("Repository can't be null");
		}
		if (sha1 == null) {
			throw new NullPointerException("SHA1 can't be null");
		}

		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		if (!containsCommit(repository, sha1)) {
			log.warn(String.format("Commit %s unknown in repository %s", sha1, repository.getDirectory()));
			return Arrays.asList(Status.UNKNOWN);
		}

		List<Status> statuses = new ArrayList<Status>();

		for (BuildResult result: dao.getAll(repository, sha1)) {
			statuses.add(result.getExitCode() == 0 ? Status.OK : Status.ERROR);
		}

		switch (getState(sha1)) {
		case RUNNING:
			statuses.add(Status.RUNNING);
			break;
		case PENDING:
			statuses.add(Status.PENDING);
			break;
		case NONE:
			if (statuses.isEmpty()) {
				statuses.add(Status.NEW);
			}
			break;
		}

		return statuses;
	}

	private boolean containsCommit(Repository repository, String sha1) {
		return gitUtils.containsCommit(repository, sha1);
	}

	private State getState(String sha1) {
		if (isInPipeline(sha1, Filters.pending())) {
			return State.PENDING;
		}
		if (isInPipeline(sha1, Filters.running())) {
			return State.RUNNING;
		}
		return State.NONE;
	}
	
	private boolean isInPipeline(String commitSha1, Filter filter) {
		for(Key k : pipeline.list(filter)) {
			Task<BuildResult> task = pipeline.get(k);
			if (task instanceof BuildTask) {
				Pair<Repository, String> target = ((BuildTask) task).getTarget();
				final Repository repository = target.getT();
				final String myTreeSha1 = getTreeSha1(repository, commitSha1);
				final String otherTreeSha1 = getTreeSha1(repository, target.getU());
				if (myTreeSha1 != null && myTreeSha1.equals(otherTreeSha1)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getTreeSha1(Repository repo, String commitSha1) {
		return gitUtils.getTree(repo, commitSha1);
	}

}
