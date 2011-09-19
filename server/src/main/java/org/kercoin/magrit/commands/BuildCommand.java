package org.kercoin.magrit.commands;

import java.io.IOException;
import java.util.Scanner;

import org.apache.sshd.server.Environment;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildQueueService;
import org.kercoin.magrit.services.UserIdentityService;
import org.kercoin.magrit.utils.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class BuildCommand extends AbstractCommand<BuildCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class BuildCommandProvider implements CommandProvider<BuildCommand> {

		private final Context ctx;
		private final BuildQueueService buildQueueService;
		private final UserIdentityService userService;
		
		@Inject
		public BuildCommandProvider(Context ctx, BuildQueueService buildQueueService, UserIdentityService userService) {
			this.ctx = ctx;
			this.buildQueueService = buildQueueService;
			this.userService = userService;
		}
		
		@Override
		public BuildCommand get() {
			return new BuildCommand(ctx, buildQueueService, userService);
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit build ");
		}
		
	}
	
	private final BuildQueueService buildQueueService;
	private final UserIdentityService userService;
	
	private UserIdentity committer;
	
	private Repository repo;
	private String sha1;
	private boolean force;
	
	public BuildCommand(Context ctx, BuildQueueService buildQueueService, UserIdentityService userService) {
		super(ctx);
		this.buildQueueService = buildQueueService;
		this.userService = userService;
	}

	@Override
	public void run() {
		try {
			String userId = env.getEnv().get(Environment.ENV_USER);
			this.committer = userService.find(userId);
			sendBuild(repo.resolve(sha1));
			callback.onExit(0);
		} catch (Exception e) {
			e.printStackTrace();
			callback.onExit(1, e.getMessage());
		}
	}

	@Override
	public BuildCommand command(String command) throws IOException {
		// magrit build send SHA1
		// magrit build send --force SHA1
		Scanner scanner = new Scanner(command);
		scanner.useDelimiter("\\s{1,}");
		check(scanner.next(), "magrit");
		check(scanner.next(), "build");
		check(command, scanner.hasNext());
		check(scanner.next(), "send");
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
		checkSha1(sha1);
		
		this.force = force;
		this.sha1 = sha1;
		
		this.repo = createRepository(repo);
		
		return this;
	}

	private void checkSha1(String sha1) {
		if (!gitUtils.isSha1(sha1)) {
			throw new IllegalArgumentException("Syntax: magrit build send <SHA1>");
		}
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

	private void check(String command, boolean hasNext) {
		if (!hasNext) {
			throw new IllegalArgumentException(String.format("Too many argument for command %s to be executed", command));
		}
	}

	private void check(String tested, String ref) throws IllegalArgumentException {
		if (!ref.equals(tested)) {
			throw new IllegalArgumentException(String.format("Expected %s but was %s", ref, tested));
		}
	}

	@Override
	protected Class<BuildCommand> getType() {
		return BuildCommand.class;
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
			this.out.flush();
		} catch (Exception e) {
			log.error("Unable to send build", e);
			e.printStackTrace();
		}
	}

}
