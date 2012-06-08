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
package org.kercoin.magrit.core.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.BuildResult;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.TimeService;

import com.google.inject.Inject;

public class BuildDAOImpl implements BuildDAO {

	private final GitUtils gitUtils;
	private final TimeService timeService;

	@Inject
	public BuildDAOImpl(GitUtils gitUtils, TimeService timeService) {
		this.gitUtils = gitUtils;
		this.timeService = timeService;
	}

	@Override
	public BuildResult getLast(Repository repo, String sha1) {
		Note note = readNote(repo, sha1);
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
		Note note = readNote(repo, sha1);
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

	private static final char NL = '\n';
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Override
	public void save(BuildResult buildResult, Repository repo, String userName, Pair<Long,Integer> when) {
		ObjectInserter db = null;
		try {
			db = repo.getObjectDatabase().newInserter();
			RevCommit commitId = gitUtils.getCommit(repo, buildResult.getCommitSha1());
			
			ObjectId logSha1 = writeBlob(db, buildResult.getLog());
			String content = serializeResult(buildResult, userName, when, logSha1);
			ObjectId resultBlobId = writeBlob(db, content.getBytes(UTF8));

			String noteContent = buildNoteContent(repo, commitId, resultBlobId);
			writeNote(repo, commitId, noteContent);

			db.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.release();
			}
		}
	}

	private String buildNoteContent(Repository repo, RevCommit commitId, ObjectId resultBlobId)
			throws AmbiguousObjectException, IOException {
		String noteContent = serializeBuildNote(resultBlobId);
		Note note = readNote(repo, commitId);
		if (note != null) {
			String previousNoteContent = gitUtils.show(repo, note.getData().name());
			return concatenateNotes(noteContent, previousNoteContent);
		}
		return noteContent;
	}

	String concatenateNotes(String noteContent, String previousNoteContent) {
		return noteContent + NL + NL + previousNoteContent;
	}

	String serializeBuildNote(ObjectId resultBlobId) {
		return "magrit:built-by "+resultBlobId.name();
	}

	String serializeResult(BuildResult buildResult, String userName,
			Pair<Long, Integer> when, ObjectId logSha1)
			throws UnsupportedEncodingException {
		StringBuilder content = new StringBuilder();
		content.append("build ").append(buildResult.getCommitSha1()).append(NL);
		content.append("log ").append(logSha1.name()).append(NL);
		content.append("return-code ").append(buildResult.getExitCode()).append(NL);
		content.append("author ").append(userName).append(NL);
		content.append("when ").append(when.getT()).append(" ").append(timeService.offsetToString(when.getU())).append(NL);
		return content.toString();
	}

	private Note readNote(Repository repository, String commitSha1) {
		try {
			RevCommit commit = gitUtils.getCommit(repository, commitSha1);
			return readNote(repository, commit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Note readNote(Repository repo, RevCommit commitId) {
		if (commitId == null || commitId.getTree() == null) {
			return null;
		}
		return wrap(repo)
				.notesShow()
				.setObjectId(commitId.getTree()).call();
	}
	
	private void writeNote(Repository repo, RevCommit commitId,
			final String noteText) {
		wrap(repo)
				.notesAdd()
				.setMessage(noteText)
				.setObjectId(commitId.getTree())
				.call();
	}

	private ObjectId writeBlob(ObjectInserter db, byte[] bytes)
			throws IOException {
		return db.insert(Constants.OBJ_BLOB, bytes);
	}

	Git wrap(Repository repo) {
		return Git.wrap(repo);
	}

}
