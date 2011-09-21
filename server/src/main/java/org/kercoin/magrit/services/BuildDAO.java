package org.kercoin.magrit.services;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

public interface BuildDAO {
	BuildResult getLast(Repository repo, String sha1);
	List<BuildResult> getAll(Repository repo, String sha1);
}
