package org.kercoin.magrit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.utils.GitUtils;

import com.google.inject.Inject;

public class BuildDAOImpl implements BuildDAO {

	private final GitUtils gitUtils;
	
	@Inject
	public BuildDAOImpl(GitUtils gitUtils) {
		this.gitUtils = gitUtils;
	}

	@Override
	public BuildResult getLast(Repository repo, String sha1) {
		Note note = getNote(repo, sha1);
		if (note == null) return null;
		try {
			List<String> sha1s = parseNoteListing(gitUtils.show(repo, note.getData().getName()));
			if (sha1s.size() == 0) { return null; }
			String data = gitUtils.show(repo, sha1s.get(0));
			return parseNote(repo, sha1, data);
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	Note getNote(Repository repository, String commitSha1) {
		try {
			RevCommit commit = gitUtils.getCommit(repository, commitSha1);
			if (commit == null) return null;
			Note note = Git.wrap(repository).notesShow().setObjectId(commit).call();
			return note;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	List<String> parseNoteListing(String data) {
		List<String> notes = new ArrayList<String>();
		Scanner s = new Scanner(data);
		s.useDelimiter("\\n");
		String row;
		Pattern p = Pattern.compile("magrit:built-by ([0-9a-f]{40})");
		while (s.hasNext()) {
			row = s.next();
			
			Matcher matcher = p.matcher(row);
			if (matcher.matches()) {
				notes.add(matcher.group(1));
			}
		}
		
		return notes;
	}
	
	BuildResult parseNote(Repository repository, String sha1, String data) throws AmbiguousObjectException, IOException {
		BuildResult r= new BuildResult(sha1);
		Scanner s=new Scanner(data);
		s.useDelimiter("\\s{1,}");
		String c,d;
		while (s.hasNext()) {
			c = s.next();
			d = s.next();
			if ("log".equals(c)) {
				try {
					r.setLog(gitUtils.showBytes(repository, d));
				} catch (MissingObjectException e) {
					r.setLog("Log are missing ...".getBytes());
				}
			} else if ("return-code".equals(c)) {
				r.setExitCode(Integer.parseInt(d));
			}
		}
		return r;
	}

	@Override
	public List<BuildResult> getAll(Repository repo, String sha1) {
		List<BuildResult> results = new ArrayList<BuildResult>();
		Note note = getNote(repo, sha1);
		if (note == null) return Collections.emptyList();
		try {
			List<String> sha1s = parseNoteListing(gitUtils.show(repo, note.getData().getName()));
			if (sha1s.size() == 0) { return results; }
			for (String noteItem : sha1s) {
				String data = gitUtils.show(repo, noteItem);
				// reversing order of notes
				results.add(0, parseNote(repo, sha1, data));
			}
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;

	}

}
