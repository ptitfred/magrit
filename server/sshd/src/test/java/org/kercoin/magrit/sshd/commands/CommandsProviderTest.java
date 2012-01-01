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
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.CoreModule;
import org.kercoin.magrit.sshd.SshdModule;
import org.kercoin.magrit.sshd.commands.AbstractCommand.CommandProvider;
import org.kercoin.magrit.sshd.commands.CatBuildCommand.CatBuildCommandProvider;
import org.kercoin.magrit.sshd.commands.GetStatusCommand.GetStatusCommandProvider;
import org.kercoin.magrit.sshd.commands.MonitorCommand.MonitorCommandProvider;
import org.kercoin.magrit.sshd.commands.PingCommand.PingCommandProvider;
import org.kercoin.magrit.sshd.commands.ReceivePackCommand.ReceivePackCommandProvider;
import org.kercoin.magrit.sshd.commands.SendBuildCommand.SendBuildCommandProvider;
import org.kercoin.magrit.sshd.commands.WaitForCommand.WaitForCommandProvider;
import org.mockito.Answers;
import org.mockito.Mock;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CommandsProviderTest {

	private CommandsProvider commandsProvider;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS) Context ctx;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		Injector sshdModule = Guice.createInjector(new CoreModule(), new SshdModule());
		given(ctx.getInjector()).willReturn(sshdModule);
		this.commandsProvider = new CommandsProvider(ctx);
	}

	@Test
	public void testGet() {
		// when
		Iterable<CommandProvider<?>> iterable = commandsProvider.get();
		
		@SuppressWarnings("rawtypes")
		Set<Class<? extends CommandProvider>> types = new HashSet<Class<? extends CommandProvider>>();
		for (CommandProvider<?> cp : iterable) {
			assertThat(cp).isNotNull();
			types.add(cp.getClass());
		}
		
		// then
		assertThat(types)
			.hasSize(7)
			.contains(
					SendBuildCommandProvider.class,
					CatBuildCommandProvider.class,
					ReceivePackCommandProvider.class,
					GetStatusCommandProvider.class,
					MonitorCommandProvider.class,
					PingCommandProvider.class,
					WaitForCommandProvider.class
					);
	}

}
