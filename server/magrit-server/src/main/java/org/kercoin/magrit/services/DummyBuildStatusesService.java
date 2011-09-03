package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;

public class DummyBuildStatusesService implements BuildStatusesService {

	@Override
	public BuildStatus getStatus(Repository repository, String sha1) {
		return BuildStatus.FAILED;
	}

}
