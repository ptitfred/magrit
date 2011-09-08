package org.kercoin.magrit.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildTask implements Callable<BuildResult> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;
	
	private Repository remote;
	private Pair<Repository,String> target;
	private Repository repository;
	private RevCommit commit;

	public BuildTask(GitUtils gitUtils, Repository remote, Pair<Repository,String> target) {
		this.gitUtils = gitUtils;
		this.remote = remote;
		this.target = target;
		this.repository = target.getT();
	}
	
	public Pair<Repository, String> getTarget() {
		return target;
	}
	
	public void before() throws Exception {
		lock();
	}
	
	public void after() {
		unlock();
	}

	@Override
	public BuildResult call() throws Exception {
		if (this.repository.isBare()) {
			throw new IllegalArgumentException(
					"Repository is bare, can't build on this.");
		}

		ObjectId commitId = this.repository.resolve(target.getU());
		if (commitId == null) {
			throw new IllegalArgumentException(
					String.format(
							"Supplied sha1 %s doesn't match any commit the repository %s",
							target.getU(), repository.getDirectory()));
		}
		
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		RevWalk walk = new RevWalk(repository);
		try {
			commit = walk.parseCommit(commitId);
		} catch (MissingObjectException e) {
			gitUtils.addRemote(this.repository, "magrit", this.remote);
			Git.wrap(this.repository).fetch().setRemote("magrit").call();
			commit = walk.parseCommit(commitId);
		}

		if (!this.repository.getRepositoryState().canCheckout()) {
			throw new IllegalStateException(String.format(
					"Can't checkout on this repository %s", this.repository
					.getDirectory().getAbsolutePath()));
		}

		String sha1 = commit.getName();

		BuildResult buildResult = new BuildResult();
		buildResult.setStartDate(new Date());

		PrintStream printOut = new PrintStream(stdout);
		String branchName = "magrit/build/" + sha1;
		printOut.println(String.format("Checking out sha1 %s as %s", sha1, branchName));
		try {
			Git.wrap(repository).checkout()
			.setCreateBranch(true)
			.setName(branchName)
			.setStartPoint(commit).call();
		} catch (RefAlreadyExistsException e) {
			// It's ok!
		}

		String command = findCommand();
		printOut.println(String.format("Starting build with command '%s'", command));

		CommandLine cmdLine = CommandLine.parse(command);
		DefaultExecutor executable = new DefaultExecutor();
		executable.setWorkingDirectory(repository.getDirectory().getParentFile());
		executable.setStreamHandler(new PumpStreamHandler(stdout));

		int exitCode = executable.execute(cmdLine);

		new DefaultExecutor().execute(CommandLine.parse("notify-send 'Magrit' 'Build ended'"));

		buildResult.setEndDate(new Date());
		buildResult.setSuccess(exitCode == 0);
		buildResult.setExitCode(exitCode);
		buildResult.setLog(stdout.toByteArray());

		return buildResult;
	}

	private void lock() throws IOException {
		File lockFile = getLockFile();
		
		if (lockFile.exists()) {
			throw new IllegalStateException(String.format("Can't start a build on this repository %s, a build is already running here...", repository.getDirectory()));
		}
		
		lockFile.createNewFile();
		PrintStream out = new PrintStream(new FileOutputStream(lockFile));
		out.println(this.target.getU());
		out.close();
	}

	private void unlock() {
		File lockFile = getLockFile();
		String lockPath = lockFile.getAbsolutePath();
		
		if (!lockFile.exists()) {
			log.warn("Strange! the build lock file {} has disapparead while build progress...", lockPath);
			return;
		}
		
		if (!lockFile.delete()) {
			log.warn("Couldn't delete build lock file {}", lockPath);
		}
	}
	
	private File getLockFile() {
		return new File(repository.getDirectory(), "MAGRIT_BUILD");
	}
	
	String findCommand() throws AmbiguousObjectException, IOException {
		return gitUtils.show(this.repository, this.target.getU() + ":" + ".magrit");
	}
	
}
