package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;

public interface BuildStatusesService {
	BuildStatus getStatus(Repository repository, String sha1);
}
