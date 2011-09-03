package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyBuildStatusesService implements BuildStatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public BuildStatus getStatus(Repository repository, String sha1) {
		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		return BuildStatus.FAILED;
	}

}
