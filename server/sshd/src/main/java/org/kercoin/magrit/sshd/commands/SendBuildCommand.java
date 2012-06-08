/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessControlException;
import java.util.Scanner;

import org.apache.sshd.server.Environment;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.QueueService;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.user.UserIdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class SendBuildCommand extends AbstractCommand<SendBuildCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class SendBuildCommandProvider implements CommandProvider<SendBuildCommand> {

		private final Context ctx;
		private final QueueService buildQueueService;
		private final UserIdentityService userService;
		
		@Inject
		public SendBuildCommandProvider(Context ctx, QueueService buildQueueService, UserIdentityService userService) {
			this.ctx = ctx;
			this.buildQueueService = buildQueueService;
			this.userService = userService;
		}
		
		@Override
		public SendBuildCommand get() {
			return new SendBuildCommand(ctx, buildQueueService, userService);
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit send-build ");
		}
		
	}
	
	private final QueueService buildQueueService;
	private final UserIdentityService userService;
	
	private UserIdentity committer;
	
	private Repository repo;
	private String sha1;
	private String command;
	private boolean force;
	private boolean readStdin = false;
	
	public SendBuildCommand(Context ctx, QueueService buildQueueService, UserIdentityService userService) {
		super(ctx);
		this.buildQueueService = buildQueueService;
		this.userService = userService;
	}
	
	private BufferedReader stdin = null;
	
	@Override
	public void destroy() {
		stdin = null;
		super.destroy();
	}
	
	@Override
	public void setInputStream(InputStream in) {
		stdin = new BufferedReader(new InputStreamReader(in));
		super.setInputStream(in);
	}

	@Override
	public void run() {
		try {
			String userId = env.getEnv().get(Environment.ENV_USER);
			this.committer = userService.find(userId);
			if (readStdin) {
				String line = null;
				while((line = stdin.readLine()) != null) {
					if ("--".equals(line)) {
						break;
					}
					handle(line);
				}
			} else {
				handle(this.sha1);
			}
			callback.onExit(0);
		} catch (AccessControlException e) {
			log.error(e.getMessage());
			callback.onExit(2, e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			callback.onExit(1, e.getMessage());
		}
	}

	private void handle(String sha1) throws AmbiguousObjectException, IOException {
		String cmd = command != null ? command : sha1;
		sendBuild(repo.resolve(sha1), repo.resolve(cmd));
	}

	@Override
	public SendBuildCommand command(String command) throws IOException {
		// magrit build send SHA1
		// magrit build send --force SHA1
		Scanner scanner = new Scanner(command);
		scanner.useDelimiter("\\s{1,}");
		check(scanner.next(), "magrit");
		check(scanner.next(), "send-build");
		check(command, scanner.hasNext());
		String remainder = scanner.next();
		boolean force = false;
		while (remainder.startsWith("--")) {
			if ("--force".equals(remainder)) {
				force = true;
			} else if ("--command".equals(remainder)) {
				check(command, scanner.hasNext());
				this.command = scanner.next();
			}
			check(command, scanner.hasNext());
			remainder = scanner.next();
		}
		String repo = remainder;
		check(command, scanner.hasNext());
		String sha1 = scanner.next();
		readStdin = "-".equals(sha1);
		if (!readStdin) {
			checkSha1(sha1);
		}
		
		this.force = force;
		this.sha1 = sha1;
		
		this.repo = createRepository(repo);
		
		return this;
	}

	boolean isForce() {
		return force;
	}
	
	void setForce(boolean force) {
		this.force = force;
	}
	
	String getSha1() {
		return sha1;
	}
	
	void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	
	String getCommand() {
		return this.command;
	}
	
	void setCommand(String command) {
		this.command = command;
	}
	
	Repository getRepo() {
		return repo;
	}
	
	void setRepo(Repository repo) {
		this.repo = repo;
	}

	@Override
	protected Class<SendBuildCommand> getType() {
		return SendBuildCommand.class;
	}

	private void sendBuild(ObjectId newId, ObjectId cmdId) {
		try {
			if (buildQueueService.enqueueBuild(committer, repo, newId.getName(), cmdId.getName(), force) != null) {
				String msg = String.format("Triggering build for commit %s on repository %s by %s.", newId.getName(), repo.getDirectory(), committer);
				log.info(msg);
				this.out.write('1');
			} else {
				String msg = String.format("Asked to build the commit %s on repository %s by %s but was skipped", newId.getName(), repo.getDirectory(), committer);
				log.info(msg);
				this.out.write('0');
			}
			this.out.write('\n');
			this.out.flush();
		} catch (Exception e) {
			log.error("Unable to send build", e);
			e.printStackTrace();
		}
	}

}
