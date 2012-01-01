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

import java.util.ArrayList;
import java.util.Collection;

import org.kercoin.magrit.core.model.Context;
import org.kercoin.magrit.sshd.commands.AbstractCommand.CommandProvider;
import org.kercoin.magrit.sshd.commands.CatBuildCommand.CatBuildCommandProvider;
import org.kercoin.magrit.sshd.commands.GetStatusCommand.GetStatusCommandProvider;
import org.kercoin.magrit.sshd.commands.MonitorCommand.MonitorCommandProvider;
import org.kercoin.magrit.sshd.commands.PingCommand.PingCommandProvider;
import org.kercoin.magrit.sshd.commands.ReceivePackCommand.ReceivePackCommandProvider;
import org.kercoin.magrit.sshd.commands.SendBuildCommand.SendBuildCommandProvider;
import org.kercoin.magrit.sshd.commands.WaitForCommand.WaitForCommandProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CommandsProvider implements Provider<Iterable<CommandProvider<?>>>{

	private Context ctx;
	
	@Inject
	public CommandsProvider(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public Iterable<CommandProvider<?>> get() {
		Collection<CommandProvider<?>> commands = new ArrayList<CommandProvider<?>>();
		bind(commands, ReceivePackCommandProvider.class);
		bind(commands, GetStatusCommandProvider.class);
		bind(commands, MonitorCommandProvider.class);
		bind(commands, SendBuildCommandProvider.class);
		bind(commands, CatBuildCommandProvider.class);
		bind(commands, PingCommandProvider.class);
		bind(commands, WaitForCommandProvider.class);
		return commands;
	}

	private void bind(Collection<CommandProvider<?>> commands, Class<? extends CommandProvider<?>> commandProviderType) {
		commands.add(loadCP(commandProviderType));
	}
	
	private CommandProvider<?> loadCP(Class<? extends CommandProvider<?>> commandProviderType) {
		return ctx.getInjector().getInstance(commandProviderType);
	}

}
