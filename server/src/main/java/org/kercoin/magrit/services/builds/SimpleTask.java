package org.kercoin.magrit.services.builds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.utils.TimeService;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.Pair;
import org.kercoin.magrit.utils.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class SimpleTask implements Callable<BuildResult> {

	private static final char NL = '\n';

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;
	private final UserIdentity user;
	private final TimeService timeService;
	
	private Repository remote;
	private Pair<Repository,String> target;
	private Repository repository;
	private RevCommit commit;

	public SimpleTask(Context ctx,
			UserIdentity user, TimeService timeService, Repository remote, Pair<Repository,String> target) {
		this.gitUtils = ctx.getGitUtils();
		this.user = user;
		this.timeService = timeService;
		this.remote = remote;
		this.target = target;
		this.repository = target.getT();
	}
	
	public Pair<Repository, String> getTarget() {
		return target;
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

		BuildResult buildResult = new BuildResult(this.target.getU());
		try {
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
			endOfTreatment(buildResult, exitCode, true);
			return buildResult;
		} catch (ExecuteException ex) {
			endOfTreatment(buildResult, ex.getExitValue(), false);
			return buildResult;
		} finally {
			buildResult.setLog(stdout.toByteArray());
			writeToRepository(buildResult);
		}
	}
	
	void endOfTreatment(BuildResult buildResult, int exitCode, boolean success) {
		buildResult.setEndDate(new Date());
		buildResult.setSuccess(success);
		buildResult.setExitCode(exitCode);
		
		try {
			String message = "";
			if (success) {
				message = String.format("notify-send \"Magrit\" \"Build successful\"", exitCode);
			} else {
				message = String.format("notify-send \"Magrit\" \"Build failed %s\"", exitCode);
			}
			new DefaultExecutor().execute(CommandLine.parse(message));
		} catch (ExecuteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void writeToRepository(BuildResult buildResult) {
		ObjectInserter db = null;
		try {
			db = remote.getObjectDatabase().newInserter();
			ObjectId logSha1 = db.insert(Constants.OBJ_BLOB, buildResult.getLog());
			StringBuilder content = new StringBuilder();
			content.append("build ").append(buildResult.getCommitSha1()).append(NL);
			content.append("log ").append(logSha1.name()).append(NL);
			content.append("return-code ").append(buildResult.getExitCode()).append(NL);
			content.append("author ").append(this.user.toString()).append(NL);
			Pair<Long,Integer> time = timeService.now();
			content.append("when ").append(time.getT()).append(" ").append(timeService.offsetToString(time.getU())).append(NL);
			ObjectId resultBlobId = db.insert(Constants.OBJ_BLOB, content.toString().getBytes("UTF-8"));
			
			StringBuilder noteContent = new StringBuilder();
			noteContent.append("magrit:built-by ").append(resultBlobId.name());

			RevCommit commitId = gitUtils.getCommit(remote,
					buildResult.getCommitSha1());
			Note note = wrap(remote)
					.notesShow()
					.setObjectId(commitId).call();
			if (note != null) {
				String previousNoteContent = gitUtils.show(remote, note.getData().name());
				noteContent.append(NL).append(NL).append(previousNoteContent);
			}
			wrap(remote)
					.notesAdd()
					.setMessage(noteContent.toString())
					.setObjectId(
							commitId)
					.call();
			
			db.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.release();
			}
		}
	}
	
	Git wrap(Repository repo) {
		return Git.wrap(repo);
	}

	String findCommand() throws AmbiguousObjectException, IOException {
		return gitUtils.show(this.repository, this.target.getU() + ":" + ".magrit");
	}
	
}
