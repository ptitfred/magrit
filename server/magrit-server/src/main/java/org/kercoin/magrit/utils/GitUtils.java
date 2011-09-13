package org.kercoin.magrit.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
		toConfigure.getConfig().setString("remote", name, "fetch", refSpec );
		toConfigure.getConfig().setString("remote", name, "url", dest.getAbsolutePath());
		// write down configuration in .git/config
		toConfigure.getConfig().save();
	}

	public RevCommit getCommit(Repository repo, String revstr)
			throws MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, IOException {
		RevWalk walk = new RevWalk(repo);
		ObjectId ref = repo.resolve(revstr);
		if (ref==null) return null;
		return walk.parseCommit(ref);
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
		return Pattern.compile("[0-9a-z]{40}").matcher(candidate).matches();
	}
}
