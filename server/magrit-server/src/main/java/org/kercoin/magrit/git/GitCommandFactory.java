package org.kercoin.magrit.git;

import java.io.IOException;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;
import org.kercoin.magrit.git.GetStatusCommand.GetStatusCommandProvider;
import org.kercoin.magrit.git.ReceivePackCommand.ReceivePackCommandProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * <p>
 * A git push command sent over ssh is equivalent to this:
 * <pre>$ ssh -x user@hostname "git-receive-pack 'path/to/repository.git'"</pre>
 * </p>
 * <p>Please read <a href="http://progit.org/book/ch9-6.html#uploading_data">Uploading data (progit.org)</a></p>
 * @author ptitfred
 * @see ReceivePackCommand
 */
@Singleton
public class GitCommandFactory implements CommandFactory {

	private ReceivePackCommandProvider receivePackCommandProvider;
	private GetStatusCommandProvider getStatusCommandProvider;
	
	@Inject
	public GitCommandFactory(ReceivePackCommandProvider receivePackCommandProvider,
			GetStatusCommandProvider getStatusCommandProvider) {
		super();
		this.receivePackCommandProvider = receivePackCommandProvider;
		this.getStatusCommandProvider = getStatusCommandProvider;
	}

	@Override
	public Command createCommand(String command) {
		if (command == null || command.length() == 0) {
			throw new IllegalArgumentException();
		}
		try {
			if (command.startsWith("git-receive-pack ") ||
				command.startsWith("git receive-pack ")) {
				return receivePackCommandProvider.get().command(command);
			}
			if (command.startsWith("magrit status ")) {
				return getStatusCommandProvider.get().command(command);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new UnknownCommand(command);
	}

}
