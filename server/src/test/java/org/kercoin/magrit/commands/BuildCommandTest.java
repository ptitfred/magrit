package org.kercoin.magrit.commands;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildQueueService;
import org.kercoin.magrit.services.UserIdentityService;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.UserIdentity;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildCommandTest {

	BuildCommand buildCommand;

	Context ctx;
	GitUtils gitUtils;
	@Mock BuildQueueService buildQueueService;
	@Mock UserIdentityService userService;
	@Mock Environment env;

	@Mock Repository repo;

	@Mock ExitCallback exitCallback;
	
	@SuppressWarnings("serial")
	@Before
	public void createBuildCommand() throws Exception {
		gitUtils = new GitUtils();
		ctx = new Context(gitUtils);
		buildCommand = new BuildCommand(ctx, buildQueueService, userService);
		given(env.getEnv()).willReturn(new HashMap<String, String>() {{put(Environment.ENV_USER, "ptitfred");}});
		buildCommand.env = env;
		buildCommand.callback = exitCallback;
	}

	@Test
	public void testCommandString() throws IOException {
		// when
		buildCommand.command("magrit build send /r1 0123401234012340123401234012340123401234");

		// then
		assertThat(buildCommand.isForce()).isFalse();
		assertThat(buildCommand.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(buildCommand.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCommandString_onlySha1() throws IOException {
		// when
		buildCommand.command("magrit build send /r1 HEAD");

		// then
		assertThat(buildCommand.isForce()).isFalse();
		assertThat(buildCommand.getSha1()).isEqualTo("HEAD");
		assertThat(buildCommand.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test
	public void testCommandString_force() throws IOException {
		// when
		buildCommand.command("magrit build send --force /r1 0123401234012340123401234012340123401234");

		// then
		assertThat(buildCommand.isForce()).isTrue();
		assertThat(buildCommand.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(buildCommand.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test
	public void testRun() throws Exception {
		// given
		UserIdentity expectedUserIdentity = new UserIdentity("ptitfred@localhost", "ptitfred");
		given(userService.find("ptitfred")).willReturn(expectedUserIdentity);
		buildCommand.setForce(false);
		buildCommand.setSha1("HEAD");
		buildCommand.setRepo(repo);
		ObjectId what = ObjectId.zeroId();
		given(repo.resolve("HEAD")).willReturn(what);
		
		// when
		buildCommand.run();
		
		// then
		verify(userService).find("ptitfred");
		verify(buildQueueService).enqueueBuild(expectedUserIdentity, repo, "0000000000000000000000000000000000000000", false);
	}

}
