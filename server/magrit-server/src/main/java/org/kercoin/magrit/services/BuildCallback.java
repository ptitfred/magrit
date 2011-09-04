package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;

public interface BuildCallback {
	void buildStarted(Repository repo, String sha1);

	void buildEnded(Repository repo, String sha1, BuildStatus status);
}