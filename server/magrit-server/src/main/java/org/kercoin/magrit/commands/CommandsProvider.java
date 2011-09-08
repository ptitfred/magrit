package org.kercoin.magrit.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.kercoin.magrit.Context;
import org.kercoin.magrit.commands.AbstractCommand.CommandProvider;
import org.kercoin.magrit.commands.GetStatusCommand.GetStatusCommandProvider;
import org.kercoin.magrit.commands.MonitorCommand.MonitorCommandProvider;
import org.kercoin.magrit.commands.ReceivePackCommand.ReceivePackCommandProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CommandsProvider implements Provider<Collection<CommandProvider<?>>>{

	private Context ctx;
	
	@Inject
	public CommandsProvider(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public Collection<CommandProvider<?>> get() {
		Collection<CommandProvider<?>> commands = new ArrayList<CommandProvider<?>>();
		bind(commands, ReceivePackCommandProvider.class);
		bind(commands, GetStatusCommandProvider.class);
		bind(commands, MonitorCommandProvider.class);
		return commands;
	}

	private void bind(Collection<CommandProvider<?>> commands, Class<? extends CommandProvider<?>> commandProviderType) {
		commands.add(loadCP(commandProviderType));
	}
	
	private CommandProvider<?> loadCP(Class<? extends CommandProvider<?>> commandProviderType) {
		return ctx.getInjector().getInstance(commandProviderType);
	}

}
