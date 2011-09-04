package org.kercoin.magrit;

import java.io.IOException;
import java.util.Collection;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;
import org.kercoin.magrit.commands.AbstractCommand;
import org.kercoin.magrit.commands.CommandsProvider;
import org.kercoin.magrit.commands.ReceivePackCommand;

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
public class MagritCommandFactory implements CommandFactory {

	private Collection<AbstractCommand.CommandProvider<?>> commands;
	
	@Inject
	public MagritCommandFactory(CommandsProvider commandsProvider) {
		super();
		this.commands = commandsProvider.get();
	}

	@Override
	public Command createCommand(String command) {
		if (command == null || command.length() == 0) {
			throw new IllegalArgumentException();
		}
		try {
			for (AbstractCommand.CommandProvider<?> cp : commands) {
				if (cp.accept(command)) {
					return cp.get().command(command);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new UnknownCommand(command);
	}

}
