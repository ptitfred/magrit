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
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildResult;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.sshd.commands.CatBuildCommand;
import org.kercoin.magrit.sshd.utils.LoggerOutputStream;
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
		ctx = new Context(gitUtils);
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

	@Test
	public void testRun_notFound() throws Exception {
		// given ---------------------------------
		buildCommand.setSha1("0000000000000000000000000000000000000000");
		buildCommand.setRepository(repo);
		LoggerOutputStream out = new LoggerOutputStream();
		buildCommand.setOutputStream(out);
		
		given(dao.getLast(isA(Repository.class), anyString()))
			.willReturn(null);
		
		// when ----------------------------------
		buildCommand.run();

		// then ----------------------------------
		verify(dao).getLast(repo, "0000000000000000000000000000000000000000");
		assertThat(out.getData()).isEqualTo("No log found for this commit.\n");
	}

}
