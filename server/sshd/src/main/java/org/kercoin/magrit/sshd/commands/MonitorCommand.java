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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.BuildLifeCycleListener;
import org.kercoin.magrit.services.builds.QueueService;
import org.kercoin.magrit.services.builds.Status;
import org.kercoin.magrit.utils.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class MonitorCommand extends AbstractCommand<MonitorCommand> implements BuildLifeCycleListener {

	@Singleton
	public static class MonitorCommandProvider implements CommandProvider<MonitorCommand>, org.kercoin.magrit.sshd.commands.AbstractCommand.EndCallback<MonitorCommand> {

		private Context ctx;
		
		private QueueService buildQueueService;
		
		@Inject
		public MonitorCommandProvider(Context ctx, QueueService buildQueueService) {
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
	
	private final QueueService buildQueueService;
	
	public MonitorCommand(Context ctx, QueueService buildQueueService) {
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
	public void buildEnded(Repository repo, String sha1, Status status) {
		synchronized(out) {
			printOut.println(String.format("%s - Build ended %s on %s @ %s", now(), status, repo.getDirectory(), sha1));
			printOut.flush();
		}
	}

}
