package org.kercoin.magrit.commands;

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
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.QueueService;
import org.kercoin.magrit.services.utils.UserIdentityService;
import org.kercoin.magrit.utils.UserIdentity;
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
		sendBuild(repo.resolve(sha1));
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
		if ("--force".equals(remainder)) {
			force = true;
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

	private void sendBuild(ObjectId newId) {
		try {
			if (buildQueueService.enqueueBuild(committer, repo, newId.getName(), force) != null) {
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
