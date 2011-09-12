package org.kercoin.magrit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.utils.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Thead-safe
 * @author ptitfred
 *
 */
public class BuildStatusesServiceImpl implements BuildStatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;

	@Inject
	public BuildStatusesServiceImpl(GitUtils gitUtils) {
		super();
		this.gitUtils = gitUtils;
	}

	@Override
	public List<BuildStatus> getStatus(Repository repository, String sha1) {
		if (repository == null) {
			throw new NullPointerException("Repository can't be null");
		}
		if (sha1 == null) {
			throw new NullPointerException("SHA1 can't be null");
		}
		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		try {
			RevCommit commit = gitUtils.getCommit(repository, sha1);
			
			if (commit == null) {
				return Arrays.asList(BuildStatus.UNKNOWN);
			}
			Note note = Git.wrap(repository).notesShow().setObjectId(commit).call();
			if (note == null) {
				// TODO check with the build queue to test if the build is currently running
				return Arrays.asList(BuildStatus.UNKNOWN);
			}
			return readStatuses(repository, note.getData().name());
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		
		return Arrays.asList(BuildStatus.UNKNOWN);
	}

	private List<BuildStatus> readStatuses(Repository repository, String noteSha1) throws IOException {
		List<BuildStatus> list = new ArrayList<BuildStatus>();
		
		String s = gitUtils.show(repository, noteSha1);
		
		Pattern pattern = Pattern.compile("magrit:built-by [0-9a-f]{40}");
		
		Scanner scanner = new Scanner(s).useDelimiter("\n");
		while(scanner.hasNext()) {
			String note = scanner.next();
			if (pattern.matcher(note).matches()) {
				String sha1 = note.substring("magrit:built-by ".length());
				list.add(0, readStatus(repository, sha1));
			}
		}
		
		return Collections.unmodifiableList(list);
	}
	
	private BuildStatus readStatus(Repository repository, String sha1) {
		try {
			String data = gitUtils.show(repository, sha1);
			Scanner scanner = new Scanner(data).useDelimiter("\n");
			Pattern p = Pattern.compile("return-code [0-9]{1,}");
			while (scanner.hasNext()) {
				String exitCode = scanner.next();
				if (p.matcher(exitCode).matches()) {
					return Integer.parseInt(exitCode.substring("return-code ".length())) == 0 ?
							BuildStatus.OK : BuildStatus.ERROR;
				}
			}
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return BuildStatus.UNKNOWN;
	}

}