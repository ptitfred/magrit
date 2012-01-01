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
package org.kercoin.magrit.sshd.commands;

import java.io.IOException;

import org.apache.sshd.server.Command;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.kercoin.magrit.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * <p>Wraps a JGit {@link ReceivePack} as a Mina SSHD {@link Command}.</p>
 * @author ptitfred
 * @see ReceivePack
 */
public class ReceivePackCommand extends AbstractCommand<ReceivePackCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Singleton
	public static class ReceivePackCommandProvider implements CommandProvider<ReceivePackCommand> {

		private final Context ctx;
		
		@Inject
		public ReceivePackCommandProvider(Context ctx) {
			this.ctx = ctx;
		}
		
		@Override
		public ReceivePackCommand get() {
			return new ReceivePackCommand(ctx);
		}

		@Override
		public boolean accept(String command) {
			return
				command.startsWith("git-receive-pack ") ||
				command.startsWith("git receive-pack ");
		}
		
	}

	private ReceivePack receivePack;

	public ReceivePackCommand(Context ctx) {
		super(ctx);
	}
	
	public ReceivePackCommand command(String command) throws IOException {
		this.receivePack = new ReceivePack(parse(command));
		this.receivePack.setBiDirectionalPipe(true);
		this.receivePack.setTimeout(5);
		return this;
	}

	Repository parse(String command) throws IOException {
		String parts[] = command.substring(17).split(" ");
		if (parts.length != 1) {
			throw new IllegalArgumentException("Illegal git-receive-pack invokation ; the repository must be supplied");
		}
		
		String repoPath = parts[0].substring(1, parts[0].length()-1);
		
		Repository repo = createRepository(repoPath);
		if (!repo.getDirectory().exists()) {
			repo.create(true);
		}
		return repo;
	}
		
	@Override
	protected Class<ReceivePackCommand> getType() {
		return ReceivePackCommand.class;
	}
	
	@Override
	public void destroy() {
		this.receivePack = null;
		super.destroy();
	}

	@Override
	public void run() {
		try {
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

}
