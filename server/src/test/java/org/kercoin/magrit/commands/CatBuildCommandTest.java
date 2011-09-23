package org.kercoin.magrit.commands;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.builds.BuildResult;
import org.kercoin.magrit.services.dao.BuildDAO;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.LoggerOutputStream;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatBuildCommandTest {

	CatBuildCommand buildCommand;

	Context ctx;
	GitUtils gitUtils;
	
	@Mock Environment env;
	@Mock ExitCallback exitCallback;
	
	@Mock Repository repo;

	@Mock BuildDAO dao;
	@Mock BuildResult build;

	@SuppressWarnings("serial")
	@Before
	public void createBuildCommand() throws Exception {
		gitUtils = new GitUtils();
		ctx = new Context(gitUtils, null);
		buildCommand = new CatBuildCommand(ctx, dao);
		given(env.getEnv()).willReturn(
				new HashMap<String, String>() {{put(Environment.ENV_USER, "ptitfred");}}
			);
		buildCommand.env = env;
		buildCommand.callback = exitCallback;
	}
	
	@Test
	public void testCommand() throws Exception {
		// given ---------------------------------

		// when ----------------------------------
		Object o = buildCommand.command("magrit cat-build /r1 1234512345123451234512345123451234512345");

		// then ----------------------------------
		assertThat(o).isNotNull().isEqualTo(buildCommand);
		assertThat(buildCommand.getSha1()).isEqualTo("1234512345123451234512345123451234512345");
		assertThat(buildCommand.getRepository().getDirectory().getAbsolutePath())
			.isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test
	public void testRun() throws Exception {
		// given ---------------------------------
		buildCommand.setSha1("0000000000000000000000000000000000000000");
		buildCommand.setRepository(repo);
		LoggerOutputStream out = new LoggerOutputStream();
		buildCommand.setOutputStream(out);
		
		String logMessage = "build 12345\nexit-code: 1";
		given(build.getLog()).willReturn(logMessage.getBytes());
		given(dao.getLast(isA(Repository.class), anyString()))
			.willReturn(build);
		
		// when ----------------------------------
		buildCommand.run();

		// then ----------------------------------
		verify(dao).getLast(repo, "0000000000000000000000000000000000000000");
		assertThat(out.getData()).isEqualTo(logMessage);
	}

}
