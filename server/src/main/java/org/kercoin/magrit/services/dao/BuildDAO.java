package org.kercoin.magrit.services.dao;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.services.builds.BuildResult;

public interface BuildDAO {
	BuildResult getLast(Repository repo, String sha1);
	List<BuildResult> getAll(Repository repo, String sha1);
}
