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
package org.kercoin.magrit.commands;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.BuildLifeCycleListener;
import org.kercoin.magrit.services.builds.QueueService;
import org.kercoin.magrit.services.builds.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class WaitForCommand extends AbstractCommand<WaitForCommand> implements BuildLifeCycleListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static class WaitForCommandProvider implements CommandProvider<WaitForCommand>,
	org.kercoin.magrit.commands.AbstractCommand.EndCallback<WaitForCommand>{
		
		private final Context ctx;
		
		private final QueueService queueService;
		
		@Inject
		public WaitForCommandProvider(Context ctx, QueueService queueService) {
			this.ctx = ctx;
			this.queueService = queueService;
		}
		
		@Override
		public WaitForCommand get() {
			WaitForCommand cmd = new WaitForCommand(ctx, queueService);
			cmd.addEndCallback(this);
			return cmd;
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit wait-for");
		}

		@Override
		public void onEnd(WaitForCommand command) {
			queueService.removeCallback(command);
		}
		
	}
	
	private final QueueService queueService;
	
	public WaitForCommand(Context ctx, QueueService queueService) {
		super(ctx);
		this.queueService = queueService;
	}
	
	private final Set<String> sha1s = new HashSet<String>();
	
	@SuppressWarnings("unused")
	private Repository repo;

	@Override
	public WaitForCommand command(String command) throws Exception {
		queueService.addCallback(this);
		Scanner scanner = new Scanner(command);
		check(scanner.next(), "magrit");
		check(scanner.next(), "wait-for");
		check(command, scanner.hasNext());
		String repo = scanner.next();
		check(command, scanner.hasNext());
		String sha1 = scanner.next();
		checkSha1(sha1);
		sha1s.add(sha1);
		while (scanner.hasNext()) {
			sha1 = scanner.next();
			checkSha1(sha1);
			sha1s.add(sha1);
		}
		this.repo = createRepository(repo);
		return this;
	}
	
	@Override
	public void run() {
		// Nothing to do
		// Connection will be closed on event
		// Could set a timer
	}

	@Override
	protected Class<WaitForCommand> getType() {
		return WaitForCommand.class;
	}
	
	Set<String> getSha1s() {
		return sha1s;
	}
	
	@Override
	public void buildEnded(Repository repo, String sha1, Status status) {
		check(repo, sha1);
	}
	
	@Override
	public void buildScheduled(Repository repo, String sha1) {}
	
	@Override
	public void buildStarted(Repository repo, String sha1) {}
	
	synchronized void check(Repository repo, String sha1) {
		log.info("Checking {}", sha1);
		if (sha1s.contains(sha1)) {
			try {
				log.info("Matching {}, releasing remote.", sha1);
				out.write(sha1.getBytes());
				out.write('\n');
				out.flush();
				sha1s.clear();
				callback.onExit(0);
			} catch (IOException e) {
				callback.onExit(1);
			}
		}
	}

}
