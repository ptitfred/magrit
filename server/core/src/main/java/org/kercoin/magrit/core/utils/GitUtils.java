/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

This file is part of Magrit.

Magrit is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Magrit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with Magrit.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.kercoin.magrit.core.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.inject.Singleton;

@Singleton
public class GitUtils {

	private static final String REF_SPEC_PATTERN = "+refs/heads/*:refs/remotes/%s/*";

	public void addRemote(Repository toConfigure, String name, Repository remoteRepo) throws IOException {
		final String refSpec = String.format(REF_SPEC_PATTERN, name);
		File dest = remoteRepo.getDirectory();
		if (!remoteRepo.isBare()) {
			dest = dest.getParentFile();
		}
		synchronized (toConfigure) {
			toConfigure.getConfig().setString("remote", name, "fetch", refSpec );
			toConfigure.getConfig().setString("remote", name, "url", dest.getAbsolutePath());
			// write down configuration in .git/config
			toConfigure.getConfig().save();
		}
	}

	public void fetch(Repository repository, String remote) throws JGitInternalException, InvalidRemoteException {
		Git.wrap(repository).fetch().setRemote(remote).call();
	}

	public RevCommit getCommit(Repository repo, String revstr)
			throws MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, IOException {
		ObjectId ref = repo.resolve(revstr);
		if (ref==null) return null;
		RevWalk walk = new RevWalk(repo);
		try {
			return walk.parseCommit(ref);
		} finally {
			walk.dispose();
		}
	}

	public boolean containsCommit(Repository repository, String revstr) {
		try {
			return getCommit(repository, revstr) != null;
		} catch (IOException e) {
			return false;
		}
	}

	public byte[] showBytes(Repository repository, String revstr) throws AmbiguousObjectException, IOException {
		ObjectId ref = repository.resolve(revstr);
		if (ref == null) {
			return null;
		}
		return repository.getObjectDatabase().newReader().open(ref).getBytes();
	}
	
	public String show(Repository repository, String revstr) throws AmbiguousObjectException, IOException {
		byte[] bytes = showBytes(repository, revstr);
		if (bytes == null) {
			return null;
		}
		return new String(bytes, "UTF-8");
	}
	
	public boolean isSha1(String candidate) {
		if (candidate.length()!=40) {
			return false;
		}
		return Pattern.compile("[0-9a-f]{40}").matcher(candidate).matches();
	}

	public Repository createRepository(File fullPath) throws IOException {
		RepositoryBuilder builder = new RepositoryBuilder();
		builder.setGitDir(fullPath);
		return builder.build();
	}

	public String getTree(Repository repo, String commitSha1) {
		try {
			RevCommit commit = getCommit(repo, commitSha1);
			if (commit == null) {
				return null;
			}
			final RevTree tree = commit.getTree();
			if (tree == null) {
				return null;
			}
			return tree.getName();
		} catch (IOException e) {
			return null;
		}
	}

	public void checkoutAsBranch(Repository repository, String commitSha1,
			String branchName) throws RefNotFoundException,
			InvalidRefNameException {
		try {
			Git.wrap(repository).checkout().setCreateBranch(true)
					.setName(branchName).setStartPoint(commitSha1).call();
		} catch (RefAlreadyExistsException e) {
			// It's ok!
		}
	}
}
