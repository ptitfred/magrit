package org.kercoin.magrit.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kercoin.magrit.utils.LoggerInputStream;
import org.kercoin.magrit.utils.LoggerOutputStream;

public abstract class AbstractCommand<C extends AbstractCommand<C>> implements Command, Runnable {

	protected InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	protected ExitCallback callback;
	protected final Context ctx;

	public AbstractCommand(Context ctx) {
		this.ctx = ctx;
	}
	
	public abstract C command(String command) throws IOException;
	
	protected Repository createRepository(String repoPath) throws IOException {
		if (repoPath.charAt(0) == '/') {
			repoPath = repoPath.substring(1);
		}
		
		RepositoryBuilder builder = new RepositoryBuilder();
		builder.setGitDir(new File(ctx.getRepositoriesHomeDir(), repoPath));
		return builder.build();
	}
	
	protected RevCommit getCommit(Repository repo, String sha1)
			throws MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, IOException {
		RevWalk walk = new RevWalk(repo);
		return walk.parseCommit(repo.resolve(sha1));
	}

	@Override
	public void setInputStream(InputStream in) {
		if (!(in instanceof LoggerInputStream)) {
			this.in = new LoggerInputStream(in);
		} else {
			this.in = in;
		}
	}

	@Override
	public void setOutputStream(OutputStream out) {
		if (!(out instanceof LoggerOutputStream)) {
			this.out = new LoggerOutputStream(out);
		} else {
			this.out = out;
		}
	}

	@Override
	public void setErrorStream(OutputStream err) {
		if (!(err instanceof LoggerOutputStream)) {
			this.err = new LoggerOutputStream(err);
		} else {
			this.err = err;
		}
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		new Thread(this, getName()).start();
	}
	
	protected abstract String getName();

	@Override
	public void destroy() {
		this.out = null;
		this.in = null;
		this.err = null;
		this.callback = null;
	}

}
