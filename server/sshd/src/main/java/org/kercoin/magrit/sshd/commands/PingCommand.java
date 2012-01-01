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

import org.kercoin.magrit.core.Context;

import com.google.inject.Inject;

public class PingCommand extends AbstractCommand<PingCommand> {

	public static class PingCommandProvider implements CommandProvider<PingCommand> {

		private Context ctx;

		@Inject
		public PingCommandProvider(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		public PingCommand get() {
			return new PingCommand(ctx);
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit ping");
		}
		
		
	}
	
	public PingCommand(Context ctx) {
		super(ctx);
	}

	@Override
	public void run() {
		callback.onExit(0);
	}

	@Override
	public PingCommand command(String command) throws Exception {
		return this;
	}

	@Override
	protected Class<PingCommand> getType() {
		return PingCommand.class;
	}

}
