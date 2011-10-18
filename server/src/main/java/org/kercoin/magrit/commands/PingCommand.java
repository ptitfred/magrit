package org.kercoin.magrit.commands;

import org.kercoin.magrit.Context;

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
