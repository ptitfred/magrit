package org.kercoin.magrit.services.builds;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

import com.google.inject.ImplementedBy;

@ImplementedBy(StatusesServiceImpl.class)
public interface StatusesService {
	List<Status> getStatus(Repository repository, String sha1);
}
