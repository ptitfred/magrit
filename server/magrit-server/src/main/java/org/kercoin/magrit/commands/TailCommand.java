package org.kercoin.magrit.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildCallback;
import org.kercoin.magrit.services.BuildQueueService;
import org.kercoin.magrit.services.BuildStatus;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class TailCommand extends AbstractCommand<TailCommand> implements BuildCallback {

	@Singleton
	public static class TailCommandProvider implements CommandProvider<TailCommand>, org.kercoin.magrit.commands.AbstractCommand.EndCallback<TailCommand> {

		private Context ctx;
		
		private BuildQueueService buildQueueService;
		
		@Inject
		public TailCommandProvider(Context ctx, BuildQueueService buildQueueService) {
			super();
			this.ctx = ctx;
			this.buildQueueService = buildQueueService;
		}

		@Override
		public TailCommand get() {
			TailCommand tailCommand = new TailCommand(this.ctx);
			buildQueueService.addCallback(tailCommand);
			tailCommand.addEndCallback(this);
			return tailCommand;
		}

		@Override
		public void onEnd(TailCommand command) {
			buildQueueService.removeCallback(command);
		}

		@Override
		public boolean accept(String command) {
			return "magrit tail".equals(command) ||
				command.startsWith("magrit tail ");
		}
		
	}
	
	@Override
	protected String getName() {
		return "TailCommand";
	}

	@Override
	protected Class<TailCommand> getType() {
		return TailCommand.class;
	}
	
	public TailCommand(Context ctx) {
		super(ctx);
	}

	@Override
	public void run() {
		
	}
	
	@Override
	public TailCommand command(String command) throws IOException {
		return this;
	}

	private PrintStream printOut;
	
	@Override
	public void setOutputStream(OutputStream out) {
		super.setOutputStream(out);
		printOut = new PrintStream(out);
	}
	
	@Override
	public void buildStarted(Repository repo, String sha1) {
		synchronized(out) {
			printOut.println(String.format("%s - Build started on %s @ %s", now(), repo, sha1));
			printOut.flush();
		}
	}

	private String now() {
		return new Date().toString();
	}

	@Override
	public void buildEnded(Repository repo, String sha1, BuildStatus status) {
		synchronized(out) {
			printOut.println(String.format("%s - Build ended %s on %s @ %s", now(), status, repo.getDirectory(), sha1));
			printOut.flush();
		}
	}

}
