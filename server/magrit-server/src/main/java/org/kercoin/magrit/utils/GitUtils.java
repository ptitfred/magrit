package org.kercoin.magrit.utils;

import java.io.IOException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.inject.Singleton;

@Singleton
public class GitUtils {

	public RevCommit getCommit(Repository repo, String sha1)
			throws MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, IOException {
		RevWalk walk = new RevWalk(repo);
		return walk.parseCommit(repo.resolve(sha1));
	}
}
