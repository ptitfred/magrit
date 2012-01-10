/*
Copyright 2011 Frederic Menou

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
package org.kercoin.magrit.core.build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.pipeline.CriticalResource;
import org.kercoin.magrit.core.build.pipeline.Key;
import org.kercoin.magrit.core.build.pipeline.Task;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.TimeService;

public class BuildTask implements Task<BuildResult> {

	private final GitUtils gitUtils;
	private final RepositoryGuard guard;
	private final UserIdentity user;
	private final TimeService timeService;

	private Repository remote;
	private Pair<Repository,String> target;
	private String commandTreeSHA1;
	private Repository repository;
	private RevCommit commit;

	private Key key;

	private Date submitDate;

	private BuildDAO dao;

	public BuildTask(Context ctx, RepositoryGuard guard,
			UserIdentity user, TimeService timeService, BuildDAO dao,
			Repository remote, Pair<Repository,String> target,
			String commandTreeSha1) {
		this.gitUtils = ctx.getGitUtils();
		this.guard = guard;
		this.user = user;
		this.timeService = timeService;
		this.dao = dao;
		this.remote = remote;
		this.target = target;
		this.repository = target.getT();
		this.commandTreeSHA1 = commandTreeSha1;
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

		BuildResult buildResult = new BuildResult(this.target.getU());
		try {
			buildResult.setStartDate(new Date());

			PrintStream printOut = new PrintStream(stdout);
			checkout(printOut);

			int exitCode = build(stdout, printOut);
			
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

	private int build(ByteArrayOutputStream stdout, PrintStream printOut) throws IOException {
		String command = findCommand();
		printOut.println(String.format("Starting build with command '%s'", command));

		CommandLine cmdLine = CommandLine.parse(command);
		DefaultExecutor executable = new DefaultExecutor();
		executable.setWorkingDirectory(repository.getDirectory().getParentFile());
		executable.setStreamHandler(new PumpStreamHandler(stdout));

		return executable.execute(cmdLine);
	}

	private void checkout(PrintStream printOut) throws RefNotFoundException,
			InvalidRefNameException {
		String sha1 = commit.getName();
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
	}

	void endOfTreatment(BuildResult buildResult, int exitCode, boolean success) {
		buildResult.setEndDate(new Date());
		buildResult.setSuccess(success);
		buildResult.setExitCode(exitCode);

		gnomeNotifySend(exitCode, success);
	}

	private void gnomeNotifySend(int exitCode, boolean success) {
		try {
			String message = "";
			if (success) {
				message = String.format("notify-send \"Magrit\" \"Build successful\"");
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
		dao.save(buildResult, remote, user.toString(), timeService.now());
	}

	String findCommand() throws IOException {
		return gitUtils.show(this.repository, this.commandTreeSHA1 + ":" + ".magrit");
	}

	@Override
	public Key getKey() {
		return key;
	}

	@Override
	public void setKey(Key k) {
		this.key = k;
	}

	@Override
	public Date getSubmitDate() {
		if (submitDate == null) {
			return null;
		}
		return new Date(this.submitDate.getTime());
	}
	
	public Pair<Repository, String> getTarget() {
		return target;
	}

	@Override
	public void setSubmitDate(Date d) {
		if (d == null) {
			this.submitDate = null;
		} else {
			this.submitDate = new Date(d.getTime());
		}
	}

	@Override
	public CriticalResource getUnderlyingResource() {
		return guard.get(repository);
	}

	@Override
	public InputStream openStdout() {
		return null;
	}

}
