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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.concurrent.Future;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildResult;
import org.kercoin.magrit.core.build.QueueService;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.user.UserIdentityService;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.sshd.commands.SendBuildCommand;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SendBuildCommandTest {

	SendBuildCommand command;

	Context ctx;
	GitUtils gitUtils;
	@Mock QueueService buildQueueService;
	@Mock UserIdentityService userService;
	@Mock Environment env;

	@Mock Repository repo;

	@Mock ExitCallback exitCallback;

	@Mock Future<BuildResult> futureResult;

	private OutputStream out;
	
	@SuppressWarnings("serial")
	@Before
	public void createBuildCommand() throws Exception {
		gitUtils = new GitUtils();
		ctx = new Context(gitUtils);
		command = new SendBuildCommand(ctx, buildQueueService, userService);
		given(env.getEnv()).willReturn(new HashMap<String, String>() {{put(Environment.ENV_USER, "ptitfred");}});
		command.env = env;
		command.callback = exitCallback;
		out = new ByteArrayOutputStream();
		command.setOutputStream(out);
	}

	@Test
	public void send() throws IOException {
		// when
		command.command("magrit send-build /r1 0123401234012340123401234012340123401234");

		// then
		assertThat(command.isForce()).isFalse();
		assertThat(command.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(command.getCommand()).isNull();
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void send_onlySha1() throws IOException {
		// when
		command.command("magrit send-build /r1 HEAD");

		// then
		assertThat(command.isForce()).isFalse();
		assertThat(command.getSha1()).isEqualTo("HEAD");
		assertThat(command.getCommand()).isEqualTo("HEAD");
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test
	public void send_force() throws IOException {
		// when
		command.command("magrit send-build --force /r1 0123401234012340123401234012340123401234");

		// then
		assertThat(command.isForce()).isTrue();
		assertThat(command.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(command.getCommand()).isNull();
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}
	
	@Test
	public void send_custom_build() throws Exception {
		// given ---------------------------------

		// when ----------------------------------
		command.command("magrit send-build --command 0123456789012345678901234567890123456789 /r1 0123401234012340123401234012340123401234");

		// then ----------------------------------
		assertThat(command.isForce()).isFalse();
		assertThat(command.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(command.getCommand()).isEqualTo("0123456789012345678901234567890123456789");
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}

	@Test
	public void send_forced_custom_build() throws Exception {
		// given ---------------------------------

		// when ----------------------------------
		command.command("magrit send-build --force --command 0123456789012345678901234567890123456789 /r1 0123401234012340123401234012340123401234");

		// then ----------------------------------
		assertThat(command.isForce()).isTrue();
		assertThat(command.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(command.getCommand()).isEqualTo("0123456789012345678901234567890123456789");
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}

	@Test
	public void send_custom_build_forced() throws Exception {
		// given ---------------------------------

		// when ----------------------------------
		command.command("magrit send-build --command 0123456789012345678901234567890123456789 --force /r1 0123401234012340123401234012340123401234");

		// then ----------------------------------
		assertThat(command.isForce()).isTrue();
		assertThat(command.getSha1()).isEqualTo("0123401234012340123401234012340123401234");
		assertThat(command.getCommand()).isEqualTo("0123456789012345678901234567890123456789");
		assertThat(command.getRepo().getDirectory().getAbsolutePath()).isEqualTo(ctx.configuration().getRepositoriesHomeDir().getAbsolutePath() + "/r1");
	}

	@Test
	public void run() throws Exception {
		// given
		UserIdentity expectedUserIdentity = new UserIdentity("ptitfred@localhost", "ptitfred");
		given(userService.find("ptitfred")).willReturn(expectedUserIdentity);
		command.setForce(false);
		command.setSha1("HEAD");
		command.setCommand("HEAD^");
		command.setRepo(repo);
		ObjectId what = ObjectId.zeroId();
		given(repo.resolve("HEAD")).willReturn(what);
		ObjectId whatElse = ObjectId.fromString("0123456789012345678901234567890123456789");
		given(repo.resolve("HEAD^")).willReturn(whatElse);
		
		// when
		command.run();
		
		// then
		verify(userService).find("ptitfred");
		verify(buildQueueService).enqueueBuild(expectedUserIdentity, repo,
				"0000000000000000000000000000000000000000", "0123456789012345678901234567890123456789", false);
	}
	
	@Test
	public void send_stream() throws Exception {
		// given ---------------------------------
		UserIdentity expectedUserIdentity = new UserIdentity("ptitfred@localhost", "ptitfred");
		given(userService.find("ptitfred")).willReturn(expectedUserIdentity);
		StringBuilder stdin = new StringBuilder();
		stdin.append("abcdef7890123456789012345678901234abcdef").append('\n');
		stdin.append("1234567890123456789012345678901234567890").append('\n');
		stdin.append("--").append('\n');
		command.setInputStream(new ByteArrayInputStream(stdin.toString().getBytes()));
		given(this.buildQueueService.enqueueBuild(
				isA(UserIdentity.class),
				isA(Repository.class),
				eq("abcdef7890123456789012345678901234abcdef"),
				eq("abcdef7890123456789012345678901234abcdef"),
				eq(false)
			)).willReturn(futureResult);
		given(this.buildQueueService.enqueueBuild(
				isA(UserIdentity.class),
				isA(Repository.class),
				eq("1234567890123456789012345678901234567890"),
				eq("1234567890123456789012345678901234567890"),
				eq(false)
			)).willReturn(null);

		// when ----------------------------------
		command.command("magrit send-build /r1 -").run();

		// then ----------------------------------
		assertThat(out.toString()).isEqualTo("1\n0\n");
	}
	
	@Test
	public void send_stream_custom_build() throws Exception {
		// given ---------------------------------
		UserIdentity expectedUserIdentity = new UserIdentity("ptitfred@localhost", "ptitfred");
		given(userService.find("ptitfred")).willReturn(expectedUserIdentity);
		StringBuilder stdin = new StringBuilder();
		stdin.append("abcdef7890123456789012345678901234abcdef").append('\n');
		stdin.append("1234567890123456789012345678901234567890").append('\n');
		stdin.append("--").append('\n');
		command.setInputStream(new ByteArrayInputStream(stdin.toString().getBytes()));
		given(this.buildQueueService.enqueueBuild(
				isA(UserIdentity.class),
				isA(Repository.class),
				eq("abcdef7890123456789012345678901234abcdef"),
				eq("0123456789012345678901234567890123456789"),
				eq(false)
			)).willReturn(futureResult);
		given(this.buildQueueService.enqueueBuild(
				isA(UserIdentity.class),
				isA(Repository.class),
				eq("1234567890123456789012345678901234567890"),
				eq("0123456789012345678901234567890123456789"),
				eq(false)
			)).willReturn(null);

		// when ----------------------------------
		command.command("magrit send-build --command 0123456789012345678901234567890123456789 /r1 -").run();

		// then ----------------------------------
		assertThat(out.toString()).isEqualTo("1\n0\n");
	}
	
	@Test
	public void unknownUser() throws Exception {
		// given ---------------------------------
		given(userService.find("ptitfred")).willThrow(new AccessControlException("User unknown"));

		// when ----------------------------------
		command.command("magrit send-build /r1 1234512345123451234512345123451234512345").run();

		// then ----------------------------------
		verify(exitCallback).onExit(2, "User unknown");
		assertThat(out.toString()).isEqualTo("");
	}

}
