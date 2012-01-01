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
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.commands.CatBuildCommand;
import org.kercoin.magrit.commands.CommandsProvider;
import org.kercoin.magrit.commands.GetStatusCommand;
import org.kercoin.magrit.commands.MagritCommandFactory;
import org.kercoin.magrit.commands.MonitorCommand;
import org.kercoin.magrit.commands.PingCommand;
import org.kercoin.magrit.commands.ReceivePackCommand;
import org.kercoin.magrit.commands.SendBuildCommand;
import org.kercoin.magrit.commands.WaitForCommand;

import tests.GuiceModulesHolder;

public class MagritCommandFactoryTest {

	MagritCommandFactory magritCommandFactory;

	static Context ctx;

	@BeforeClass
	public static void setUpClass() {
		ctx = new Context();
		ctx.setInjector(GuiceModulesHolder.MAGRIT_MODULE);
	}

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		this.magritCommandFactory = new MagritCommandFactory(new CommandsProvider(ctx));
	}

	@Test
	public void testCreateCommand_nominal_ReceivePackCommand() {
		assertCommand("git receive-pack /r1", 	ReceivePackCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_SendBuildCommand() {
		assertCommand("magrit send-build /r1 1234512345123451234512345123451234512345", SendBuildCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_CatBuildCommand() {
		assertCommand("magrit cat-build /r1 1234512345123451234512345123451234512345", CatBuildCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_MonitorCommand() {
		assertCommand("magrit monitor", 		MonitorCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_GetStatusCommand() {
		assertCommand("magrit status /r1 sha1", GetStatusCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_PingCommand() {
		assertCommand("magrit ping", PingCommand.class);
	}

	@Test
	public void testCreateCommand_nominal_WaitForCommand() {
		assertCommand("magrit wait-for /r1 1234512345123451234512345123451234512345", WaitForCommand.class);
	}

	private void assertCommand(String commandLine, Class<?> type) {
		assertThat(magritCommandFactory.createCommand(commandLine)).isInstanceOf(type);
	}

}
