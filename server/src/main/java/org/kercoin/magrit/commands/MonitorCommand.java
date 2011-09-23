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
import org.kercoin.magrit.utils.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class MonitorCommand extends AbstractCommand<MonitorCommand> implements BuildCallback {

	@Singleton
	public static class MonitorCommandProvider implements CommandProvider<MonitorCommand>, org.kercoin.magrit.commands.AbstractCommand.EndCallback<MonitorCommand> {

		private Context ctx;
		
		private BuildQueueService buildQueueService;
		
		@Inject
		public MonitorCommandProvider(Context ctx, BuildQueueService buildQueueService) {
			super();
			this.ctx = ctx;
			this.buildQueueService = buildQueueService;
		}

		@Override
		public MonitorCommand get() {
			MonitorCommand command = new MonitorCommand(this.ctx, buildQueueService);
			buildQueueService.addCallback(command);
			command.addEndCallback(this);
			return command;
		}

		@Override
		public void onEnd(MonitorCommand command) {
			buildQueueService.removeCallback(command);
		}

		@Override
		public boolean accept(String command) {
			return "magrit monitor".equals(command);
		}
		
	}
	
	@Override
	protected String getName() {
		return "MonitorCommand";
	}

	@Override
	protected Class<MonitorCommand> getType() {
		return MonitorCommand.class;
	}
	
	private final BuildQueueService buildQueueService;
	
	public MonitorCommand(Context ctx, BuildQueueService buildQueueService) {
		super(ctx);
		this.buildQueueService = buildQueueService;
	}

	@Override
	public void run() {
		printOut.println("-- In Progress builds --");
		// TODO read previous statuses and log them
		if (buildQueueService.getCurrentTasks().size() > 0) {
			for (Pair<Repository, String> build : buildQueueService.getCurrentTasks()) {
				printOut.println(String.format("  %s %s", build.getT().getDirectory(), build.getU()));
			}
		} else {
			printOut.println("  none");
		}
		printOut.println("-- Live updates :");
		printOut.flush();
	}
	
	@Override
	public MonitorCommand command(String command) throws IOException {
		return this;
	}

	private PrintStream printOut;
	
	@Override
	public void setOutputStream(OutputStream out) {
		super.setOutputStream(out);
		printOut = new PrintStream(out);
	}
	
	private String now() {
		return new Date().toString();
	}
	
	@Override
	public void buildScheduled(Repository repo, String sha1) {
		synchronized(out) {
			printOut.println(String.format("%s - Build scheduled on %s @ %s", now(), repo, sha1));
			printOut.flush();
		}
	}
	
	@Override
	public void buildStarted(Repository repo, String sha1) {
		synchronized(out) {
			printOut.println(String.format("%s - Build started on %s @ %s", now(), repo, sha1));
			printOut.flush();
		}
	}

	@Override
	public void buildEnded(Repository repo, String sha1, BuildStatus status) {
		synchronized(out) {
			printOut.println(String.format("%s - Build ended %s on %s @ %s", now(), status, repo.getDirectory(), sha1));
			printOut.flush();
		}
	}

}