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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.core.build.Status;
import org.kercoin.magrit.core.build.StatusesService;
import org.kercoin.magrit.core.model.Context;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.sshd.commands.GetStatusCommand;
import org.mockito.Mock;
import org.mockito.Mockito;


public class GetStatusCommandTest {

	GetStatusCommand command;
	private Context ctx;
	
	@Mock StatusesService buildStatusesService;
	private ByteArrayOutputStream out;
	@Mock ExitCallback exitCallback;
	
	@Before
	public void setUp() {
		initMocks(this);

		ctx = Mockito.spy(new Context(new GitUtils()));
		command = new GetStatusCommand(ctx, buildStatusesService);
		out = new ByteArrayOutputStream();
		command.setOutputStream(out);
		command.setExitCallback(exitCallback);
		given(ctx.getInjector()).willThrow(new IllegalAccessError());
	}
	
	@Test
	public void testGetStatusCommand_xargs() throws Exception {
		// given
		given(buildStatusesService.getStatus(any(Repository.class), anyString())).willReturn(
				Arrays.asList(Status.ERROR, Status.INTERRUPTED, Status.OK, Status.RUNNING)
		);
		
		// when
		command.command("magrit status /r1 HEAD").run();
		
		// then
		verify(exitCallback).onExit(0);
		assertThat(out.toString()).isEqualTo("EIOR\n");
	}

	@Test
	public void testGetStatusCommand_stdin_stdout() throws Exception {
		// given
		given(buildStatusesService.getStatus(any(Repository.class), eq("1234567890123456789012345678901234567890"))).willReturn(
				Arrays.asList(Status.ERROR, Status.OK)
		);
		given(buildStatusesService.getStatus(any(Repository.class), eq("abcdef7890123456789012345678901234abcdef"))).willReturn(
				Arrays.asList(Status.ERROR, Status.INTERRUPTED, Status.OK, Status.RUNNING)
		);
		StringBuilder stdin = new StringBuilder();
		stdin.append("abcdef7890123456789012345678901234abcdef").append('\n');
		stdin.append("1234567890123456789012345678901234567890").append('\n');
		stdin.append("--").append('\n');
		command.setInputStream(new ByteArrayInputStream(stdin.toString().getBytes()));
		
		// when
		command.command("magrit status /r1 -").run();
		
		// then
		verify(exitCallback).onExit(0);
		assertThat(out.toString()).isEqualTo("EIOR\n" + "EO\n");
	}
	
}
