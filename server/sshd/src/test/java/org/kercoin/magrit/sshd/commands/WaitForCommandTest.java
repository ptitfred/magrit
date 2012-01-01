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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;

import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.QueueService;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.sshd.commands.WaitForCommand;
import org.kercoin.magrit.sshd.commands.WaitForCommand.Event;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WaitForCommandTest {

	private static final char NL = '\n';

	WaitForCommand cmd;
	
	Context ctx;
	@Mock GitUtils gitUtils;
	
	@Mock QueueService queueService;

	@Mock ExitCallback callback;

	private ByteArrayOutputStream output;
	
	@Before
	public void setUp() throws Exception {
		ctx = new Context(gitUtils);
		cmd = new WaitForCommand(ctx, queueService);
		cmd.setExitCallback(callback);
		output = Mockito.spy(new ByteArrayOutputStream());
		cmd.setOutputStream(output);
		given(gitUtils.isSha1(anyString())).willReturn(true);
	}

	@Test(timeout=100)
	public void nominal() throws Exception {
		// given ---------------------------------
		String c1 = "1234567890123456789012345678901234567890";
		String c2 = "abcdefabcdabcdefabcdabcdefabcdabcdefabcd";

		// when ----------------------------------
		cmd.command("magrit wait-for /r1 " + c1 + " " + c2);

		// then ----------------------------------
		verify(queueService).addCallback(cmd);
		assertThat(cmd.getTimeout()).isEqualTo(-1);
		assertThat(cmd.getSha1s()).containsOnly(c1, c2);
	}

	@Test(timeout=100)
	public void withTimeout() throws Exception {
		// given ---------------------------------
		String c1 = "1234567890123456789012345678901234567890";
		String c2 = "abcdefabcdabcdefabcdabcdefabcdabcdefabcd";

		// when ----------------------------------
		cmd.command("magrit wait-for --timeout=1000 /r1 " + c1 + " " + c2);

		// then ----------------------------------
		verify(queueService).addCallback(cmd);
		assertThat(cmd.getTimeout()).isEqualTo(1000);
		assertThat(cmd.getSha1s()).containsOnly(c1, c2);
		assertThat(cmd.getEventMask()).containsOnly(Event.END);
	}

	@Test(timeout=100)
	public void withOptions() throws Exception {
		// given ---------------------------------
		String commit = "1234567890123456789012345678901234567890";
		
		// when ----------------------------------
		cmd.command("magrit wait-for --event-mask=SeESPEP /r1 " + commit);

		// then ----------------------------------
		verify(queueService).addCallback(cmd);
		assertThat(cmd.getEventMask()).containsOnly(Event.START, Event.END, Event.SCHEDULED);
		assertThat(cmd.getSha1s()).containsOnly(commit);
	}

	@Test(timeout=100)
	public void timeout() throws Exception {
		// given ---------------------------------
		String commit = "1234567890123456789012345678901234567890";

		// when ----------------------------------
		cmd.command("magrit wait-for --timeout=10 /1 " + commit);
		cmd.run();

		// then ----------------------------------
		assertThat(output.toString()).isEqualTo("timeout" + NL);
		verify(output).flush();
		verify(callback).onExit(2);
	}

	@Test
	public void check_valid() throws Exception {
		// given ---------------------------------
		String c1 = "1234567890123456789012345678901234567890";
		String c2 = "abcdefabcdabcdefabcdabcdefabcdabcdefabcd";
		cmd.command("magrit wait-for /r1 " + c1 + " " + c2);

		// when ----------------------------------
		cmd.check((Repository) null, c2, Event.START);
		cmd.check((Repository) null, c1, Event.SCHEDULED);
		cmd.check((Repository) null, c2, Event.END);

		// then ----------------------------------
		verify(callback).onExit(0);
		assertThat(output.toString()).isEqualTo(c2 + NL);
		verify(output).flush();
	}

}
