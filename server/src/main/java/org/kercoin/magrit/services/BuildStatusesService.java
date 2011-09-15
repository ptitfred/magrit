package org.kercoin.magrit.services;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

public interface BuildStatusesService {
	List<BuildStatus> getStatus(Repository repository, String sha1);
}
