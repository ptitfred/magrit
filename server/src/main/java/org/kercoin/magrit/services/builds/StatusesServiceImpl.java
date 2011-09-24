package org.kercoin.magrit.services.builds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.services.dao.BuildDAO;
import org.kercoin.magrit.utils.GitUtils;
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

	@Inject
	public StatusesServiceImpl(GitUtils gitUtils, BuildDAO dao) {
		super();
		this.gitUtils = gitUtils;
		this.dao = dao;
	}

	@Override
	public List<Status> getStatus(Repository repository, String sha1) {
		if (repository == null) {
			throw new NullPointerException("Repository can't be null");
		}
		if (sha1 == null) {
			throw new NullPointerException("SHA1 can't be null");
		}
		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		try {
			RevCommit commit = gitUtils.getCommit(repository, sha1);
			if (commit == null) {
				return Arrays.asList(Status.UNKNOWN);
			}

			List<BuildResult> results = dao.getAll(repository, sha1);
			if (results.size() == 0) {
				// check if it is running
				return Arrays.asList(Status.NEW);
			}
			
			List<Status> statuses = new ArrayList<Status>(results.size());
			for (BuildResult result: results) {
				statuses.add(result.getExitCode() == 0 ? Status.OK : Status.ERROR);
			}
			return statuses;
			
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		
		return Arrays.asList(Status.UNKNOWN);
	}

}
