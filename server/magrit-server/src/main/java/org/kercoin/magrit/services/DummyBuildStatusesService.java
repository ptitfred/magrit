package org.kercoin.magrit.services;

import java.io.IOException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.git.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DummyBuildStatusesService implements BuildStatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private GitUtils gitUtils;
	
	@Inject
	public DummyBuildStatusesService(GitUtils gitUtils) {
		super();
		this.gitUtils = gitUtils;
	}


	@Override
	public BuildStatus getStatus(Repository repository, String sha1) {
		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		try {
			@SuppressWarnings("unused")
			RevCommit commit = gitUtils.getCommit(repository, sha1);
			// le commit est connu dans la base, il est au moins NEW
			return BuildStatus.NEW;
			// TODO chercher les informations de build via les notes
		} catch (MissingObjectException e) {
			return BuildStatus.UNKNOWN;
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return BuildStatus.UNKNOWN;
	}

}
