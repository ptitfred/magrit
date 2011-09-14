package org.kercoin.magrit.commands;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildStatus;
import org.kercoin.magrit.services.BuildStatusesService;
import org.kercoin.magrit.utils.GitUtils;
import org.mockito.Mock;
import org.mockito.Mockito;


public class GetStatusCommandTest {

	GetStatusCommand command;
	private Context ctx;
	
	@Mock BuildStatusesService buildStatusesService;
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
	public void testGetStatusCommandProvider() throws Exception {
		// given
		given(buildStatusesService.getStatus(any(Repository.class), anyString())).willReturn(
				Arrays.asList(BuildStatus.ERROR, BuildStatus.INTERRUPTED, BuildStatus.OK, BuildStatus.RUNNING)
		);
		
		// when
		command.command("magrit status /r1 HEAD").run();
		
		// then
		verify(exitCallback).onExit(0);
		assertThat(out.toString()).isEqualTo("EIOR\n");
	}

}
