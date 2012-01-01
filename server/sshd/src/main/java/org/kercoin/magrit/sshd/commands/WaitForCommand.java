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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildLifeCycleListener;
import org.kercoin.magrit.core.build.QueueService;
import org.kercoin.magrit.core.build.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class WaitForCommand extends AbstractCommand<WaitForCommand> implements BuildLifeCycleListener {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static class WaitForCommandProvider implements CommandProvider<WaitForCommand>,
	org.kercoin.magrit.sshd.commands.AbstractCommand.EndCallback<WaitForCommand>{
		
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
			return command.startsWith("magrit wait-for ");
		}

		@Override
		public void onEnd(WaitForCommand command) {
			queueService.removeCallback(command);
			command.close();
		}
		
	}
	
	private final QueueService queueService;
	
	public WaitForCommand(Context ctx, QueueService queueService) {
		super(ctx);
		this.queueService = queueService;
	}
	
	public void close() {
		synchronized (timeoutLock) {
			closed = true;
			timeoutLock.notifyAll();
		}
	}

	private final Object timeoutLock = new Object();

	private boolean closed = false;

	private Set<Event> eventMask = EnumSet.of(Event.END);
	private final Set<String> sha1s = new HashSet<String>();
	
	@SuppressWarnings("unused")
	private Repository repo;

	private int timeout = -1;

	public static enum Event {
		START('S'), END('E'), SCHEDULED('P');
		private final char code;

		private Event(char code) {
			this.code = code;
		}

		char getCode() {
			return code;
		}

		static Event resolveFromChar(char c) {
			for (Event candidate : Event.values()) {
				if (candidate.code == c) { return candidate; }
			}
			return null;
		}
	}

	@Override
	public WaitForCommand command(String command) throws Exception {
		queueService.addCallback(this);
		Scanner scanner = new Scanner(command);
		check(scanner.next(), "magrit");
		check(scanner.next(), "wait-for");
		check(command, scanner.hasNext());
		String buffer = consumeOptions(scanner);
		String repoValue = buffer;
		check(command, scanner.hasNext());
		String sha1 = scanner.next();
		checkSha1(sha1);
		sha1s.add(sha1);
		while (scanner.hasNext()) {
			sha1 = scanner.next();
			checkSha1(sha1);
			sha1s.add(sha1);
		}
		this.repo = createRepository(repoValue);
		return this;
	}
	
	private String consumeOptions(Scanner scanner) {
		String buffer = null;
		do {
			buffer = scanner.next();
			if (buffer.startsWith("--timeout=")) {
				String timeoutValue = buffer.substring("--timeout=".length());
				this.timeout = Integer.parseInt(timeoutValue);
				buffer = scanner.next();
			} else if (buffer.startsWith("--event-mask=")) {
				String evtMask = buffer.substring("--event-mask=".length());
				eventMask = EnumSet.noneOf(Event.class);
				for (char c : evtMask.toCharArray()) {
					Event evt = Event.resolveFromChar(c);
					if (evt != null) {
						eventMask.add(evt);
					}
				}
			}
		} while (buffer.startsWith("--"));
		return buffer;
	}

	@Override
	public void run() {
		if (timeout >0) {
			synchronized(timeoutLock) {
				if (!closed) {
					try {
						timeoutLock.wait(timeout);
						exit("timeout", 2);
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
		}
		// else {
		// -- Nothing to do
		// -- Connection will be closed on event
		// }
	}

	@Override
	protected Class<WaitForCommand> getType() {
		return WaitForCommand.class;
	}
	
	Set<String> getSha1s() {
		return sha1s;
	}
	
	int getTimeout() {
		return timeout;
	}

	Set<Event> getEventMask() {
		return eventMask;
	}

	@Override
	public void buildEnded(Repository repo, String sha1, Status status) {
		check(repo, sha1, Event.END);
	}
	
	@Override
	public void buildScheduled(Repository repo, String sha1) {
		check(repo, sha1, Event.SCHEDULED);
	}
	
	@Override
	public void buildStarted(Repository repo, String sha1) {
		check(repo, sha1, Event.START);
	}
	
	synchronized void check(Repository repo, String sha1, Event evt) {
		log.info("Checking {}", sha1);
		if (eventMask.contains(evt) && sha1s.contains(sha1)) {
			log.info("Matching {}, releasing remote.", sha1);
			exit(sha1, 0);
		}
	}

	private void exit(String msg, int exitCode) {
		if (closed) {
			return;
		}
		try {
			out.write(msg.getBytes());
			out.write('\n');
			out.flush();
			sha1s.clear();
			callback.onExit(exitCode);
		} catch (IOException e) {
			callback.onExit(1);
		}
	}

}
