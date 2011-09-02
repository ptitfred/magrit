package org.kercoin.magrit.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.kercoin.magrit.utils.LoggerInputStream;
import org.kercoin.magrit.utils.LoggerOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Wraps a JGit {@link ReceivePack} as a Mina SSHD {@link Command}.</p>
 * @author ptitfred
 * @see ReceivePack
 */
public class ReceivePackCommand implements Command, Runnable, PostReceiveHook {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private ReceivePack receivePack;
	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private ExitCallback exitCB;
	
	public ReceivePackCommand(Context ctx, String command) throws IOException {
		this.receivePack = new ReceivePack(parse(ctx, command));
		this.receivePack.setBiDirectionalPipe(true);
		this.receivePack.setTimeout(5);
	}

	Repository parse(Context context, String command) throws IOException {
		String parts[] = command.split(" ");
		
		if (!"git-receive-pack".equals(parts[0]) &&
			!"git receive-pack".equals(parts[0])) {
			throw new IllegalArgumentException("Must be a git-receive-pack command.");
		}
		if (parts.length != 2) {
			throw new IllegalArgumentException("Illegal git-receive-pack invokation ; the repository must be supplied");
		}
		
		String repoPath = parts[1].substring(1, parts[1].length()-1);
		if (repoPath.charAt(0) == '/') {
			repoPath = repoPath.substring(1);
		}
		
		RepositoryBuilder builder = new RepositoryBuilder();
		builder.setGitDir(new File(context.getRepositoriesHomeDir(), repoPath));
		return builder.build();
	}
	
	@Override
	public void setInputStream(InputStream in) {
		this.in = new LoggerInputStream(in);
	}
	
	@Override
	public void setOutputStream(OutputStream out) {
		this.out = new LoggerOutputStream(out);
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.exitCB = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		new Thread(this, "ReceivePackCommand").start();
	}

	@Override
	public void destroy() {
		this.out = null;
		this.in = null;
		this.err = null;
		this.exitCB = null;
		this.receivePack = null;
	}

	@Override
	public void run() {
		try {
			receivePack.setPostReceiveHook(this);
			receivePack.receive(in, out, err);
			exitCB.onExit(0);
		} catch (java.io.InterruptedIOException iioe) {
			receivePack.sendError(iioe.getMessage());
			exitCB.onExit(1, iioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			receivePack.sendError(e.getMessage());
			exitCB.onExit(1, e.getMessage());
		}
	}

	@Override
	public void onPostReceive(ReceivePack rp,
			Collection<ReceiveCommand> commands) {
		for (ReceiveCommand cmd : commands) {
			sendBuild(rp, cmd.getNewId());
		}
	}
	
	private void sendBuild(ReceivePack rp, ObjectId newId) {
		String msg = String.format("Triggering build for commit %s.", newId.getName());
		rp.sendMessage(msg);
		log.info(msg);
	}

}
