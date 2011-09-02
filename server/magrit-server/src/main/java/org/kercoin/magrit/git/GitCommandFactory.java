package org.kercoin.magrit.git;

import java.io.IOException;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;

/**
 * <p>
 * A git push command sent over ssh is equivalent to this:
 * <pre>$ ssh -x user@hostname "git-receive-pack 'path/to/repository.git'"</pre>
 * </p>
 * <p>Please read <a href="http://progit.org/book/ch9-6.html#uploading_data">Uploading data (progit.org)</a></p>
 * @author ptitfred
 * @see ReceivePackCommand
 */
public class GitCommandFactory implements CommandFactory {

	private Context ctx;
	
	public GitCommandFactory(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public Command createCommand(String command) {
		if (command != null && (command.startsWith("git-receive-pack ")||
								 command.startsWith("git receive-pack "))) {
			try {
				return new ReceivePackCommand(ctx, command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new UnknownCommand(command);
	}

}
