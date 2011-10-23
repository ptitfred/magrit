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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.sshd.server.Environment;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.LoggerInputStream;
import org.kercoin.magrit.utils.LoggerOutputStream;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCommandTest {

	AbstractCommand<?> cmd;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Context ctx;

	@Mock ExecutorService commandRunnerPool;

	@Mock Environment env;

	static class FakeCommand extends AbstractCommand<FakeCommand> {
		public FakeCommand(Context ctx) { super(ctx); }
		public void run() {}
		public FakeCommand command(String command) throws Exception { return this; }
		protected Class<FakeCommand> getType() { return FakeCommand.class; }
	}

	@Before
	public void setUp() throws Exception {
		given(ctx.getCommandRunnerPool()).willReturn(commandRunnerPool);
		cmd = new FakeCommand(new Context(new GitUtils(), commandRunnerPool));
	}

	@Test
	public void testSetInputStream() throws IOException {
		// given ---------------------------------
		cmd.logStreams = true;

		// when ----------------------------------
		cmd.setInputStream(new ByteArrayInputStream(new byte[]{6, '*', 9, '=', 42}));

		// then ----------------------------------
		assertThat(cmd.getInputStream()).isInstanceOf(LoggerInputStream.class);
		cmd.getInputStream().read(new byte[4]);
		assertThat(cmd.getInputStream().read()).isEqualTo(42);
	}

	@Test
	public void testSetOutputStream() {
		// given ---------------------------------
		cmd.logStreams = true;

		// when ----------------------------------
		cmd.setOutputStream(NullOutputStream.INSTANCE);

		// then ----------------------------------
		assertThat(cmd.getOutputStream()).isInstanceOf(LoggerOutputStream.class);
	}

	@Test
	public void testSetErrorStream() {
		// given ---------------------------------
		cmd.logStreams = true;

		// when ----------------------------------
		cmd.setErrorStream(NullOutputStream.INSTANCE);

		// then ----------------------------------
		assertThat(cmd.getErrorStream()).isInstanceOf(LoggerOutputStream.class);
	}

	@Test
	public void testCheckSha1_valid() {
		cmd.checkSha1("012340abcdef2340123401234012340123401234");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCheckSha1_invalid() {
		cmd.checkSha1("01234012340gh340123401234012340123401234");
	}

	@Test
	public void testCreateRepository_default() throws IOException {
		// given ---------------------------------
		given(ctx.configuration().getRepositoriesHomeDir()).willReturn(new File("/tmp"));

		// when ----------------------------------
		new FakeCommand(ctx).createRepository("/r1");

		// then ----------------------------------
		ArgumentCaptor<File> where = ArgumentCaptor.forClass(File.class);
		verify(ctx.getGitUtils()).createRepository(where.capture());
		assertThat(where.getValue().getAbsolutePath()).isEqualTo("/tmp/r1");
	}

	@Test
	public void testCreateRepository_nominal() throws IOException {
		// given ---------------------------------
		given(ctx.configuration().getRepositoriesHomeDir()).willReturn(new File("/tmp"));

		// when ----------------------------------
		new FakeCommand(ctx).createRepository("r1");

		// then ----------------------------------
		ArgumentCaptor<File> where = ArgumentCaptor.forClass(File.class);
		verify(ctx.getGitUtils()).createRepository(where.capture());
		assertThat(where.getValue().getAbsolutePath()).isEqualTo("/tmp/r1");
	}

	@Test
	public void testCreateRepository_absurd() throws IOException {
		// given ---------------------------------
		given(ctx.configuration().getRepositoriesHomeDir()).willReturn(new File("/tmp"));

		// when ----------------------------------
		new FakeCommand(ctx).createRepository("///r1");

		// then ----------------------------------
		ArgumentCaptor<File> where = ArgumentCaptor.forClass(File.class);
		verify(ctx.getGitUtils()).createRepository(where.capture());
		assertThat(where.getValue().getAbsolutePath()).isEqualTo("/tmp/r1");
	}

	@Test
	public void testStart() throws Exception {
		// given ---------------------------------

		// when ----------------------------------
		cmd.start(env);

		// then ----------------------------------
		verify(commandRunnerPool).execute(cmd);
	}
}
