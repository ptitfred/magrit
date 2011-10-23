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
package org.kercoin.magrit;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.kercoin.magrit.commands.AbstractCommand;
import org.kercoin.magrit.commands.CommandsProvider;
import org.kercoin.magrit.commands.ReceivePackCommand;
import org.kercoin.magrit.commands.SyntaxErrorCommand;

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

	private Iterable<AbstractCommand.CommandProvider<?>> commands;
	
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
		} catch (Exception e) {
			return new SyntaxErrorCommand(e);
		}
		return new SyntaxErrorCommand(new IllegalArgumentException("Unknown command " + command));
	}

}
