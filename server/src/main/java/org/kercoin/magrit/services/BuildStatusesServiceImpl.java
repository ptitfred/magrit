package org.kercoin.magrit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.utils.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Thead-safe
 * @author ptitfred
 *
 */
public class BuildStatusesServiceImpl implements BuildStatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;
	private final BuildDAO dao;

	@Inject
	public BuildStatusesServiceImpl(GitUtils gitUtils, BuildDAO dao) {
		super();
		this.gitUtils = gitUtils;
		this.dao = dao;
	}

	@Override
	public List<BuildStatus> getStatus(Repository repository, String sha1) {
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
				return Arrays.asList(BuildStatus.UNKNOWN);
			}

			List<BuildResult> results = dao.getAll(repository, sha1);
			if (results.size() == 0) {
				// check if it is running
				return Arrays.asList(BuildStatus.NEW);
			}
			
			List<BuildStatus> statuses = new ArrayList<BuildStatus>(results.size());
			for (BuildResult result: results) {
				statuses.add(result.getExitCode() == 0 ? BuildStatus.OK : BuildStatus.ERROR);
			}
			return statuses;
			
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		
		return Arrays.asList(BuildStatus.UNKNOWN);
	}

}
