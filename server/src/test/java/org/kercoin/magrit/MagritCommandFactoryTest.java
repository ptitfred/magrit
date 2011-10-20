package org.kercoin.magrit;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kercoin.magrit.commands.CatBuildCommand;
import org.kercoin.magrit.commands.CommandsProvider;
import org.kercoin.magrit.commands.GetStatusCommand;
import org.kercoin.magrit.commands.MonitorCommand;
import org.kercoin.magrit.commands.PingCommand;
import org.kercoin.magrit.commands.ReceivePackCommand;
import org.kercoin.magrit.commands.SendBuildCommand;

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

	private void assertCommand(String commandLine, Class<?> type) {
		assertThat(magritCommandFactory.createCommand(commandLine)).isInstanceOf(type);
	}

}
