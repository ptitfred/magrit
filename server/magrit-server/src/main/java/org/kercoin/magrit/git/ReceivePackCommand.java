package org.kercoin.magrit.git;

import java.io.IOException;
import java.util.Collection;

import org.apache.sshd.server.Command;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.kercoin.magrit.services.BuildQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Wraps a JGit {@link ReceivePack} as a Mina SSHD {@link Command}.</p>
 * @author ptitfred
 * @see ReceivePack
 */
public class ReceivePackCommand extends AbstractCommand implements PostReceiveHook {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private ReceivePack receivePack;

	private BuildQueueService buildQueueService;
	
	public ReceivePackCommand(Context ctx, String command, BuildQueueService buildQueueService) throws IOException {
		super(ctx);
		this.receivePack = new ReceivePack(parse(command));
		this.receivePack.setBiDirectionalPipe(true);
		this.receivePack.setTimeout(5);
		this.buildQueueService = buildQueueService;
	}

	Repository parse(String command) throws IOException {
		if (!command.startsWith("git-receive-pack") &&
			!command.startsWith("git receive-pack")) {
			throw new IllegalArgumentException("Must be a git-receive-pack command.");
		}
		
		String parts[] = command.substring(17).split(" ");
		if (parts.length != 1) {
			throw new IllegalArgumentException("Illegal git-receive-pack invokation ; the repository must be supplied");
		}
		
		String repoPath = parts[0].substring(1, parts[0].length()-1);
		
		return createRepository(repoPath);
	}
		
	@Override
	protected String getName() {
		return "ReceivePackCommand";
	}
	
	@Override
	public void destroy() {
		this.receivePack = null;
		super.destroy();
	}

	@Override
	public void run() {
		try {
			receivePack.setPostReceiveHook(this);
			receivePack.receive(in, out, err);
			callback.onExit(0);
		} catch (java.io.InterruptedIOException iioe) {
			receivePack.sendError(iioe.getMessage());
			callback.onExit(1, iioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			receivePack.sendError(e.getMessage());
			callback.onExit(1, e.getMessage());
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
		String msg = String.format("Triggering build for commit %s on repository %s.", newId.getName(), rp.getRepository().getDirectory());
		rp.sendMessage(msg);
		log.info(msg);
		buildQueueService.enqueueBuild(rp.getRepository(), newId.getName());
	}

}
