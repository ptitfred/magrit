package org.kercoin.magrit;

import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.kercoin.magrit.services.BuildQueueService;
import org.kercoin.magrit.services.BuildQueueServiceImpl;
import org.kercoin.magrit.services.BuildStatusesService;
import org.kercoin.magrit.services.DummyBuildStatusesService;
import org.kercoin.magrit.sshd.GitPublickeyAuthenticator;
import org.kercoin.magrit.services.SimpleTimeService;
import org.kercoin.magrit.services.TimeService;

import com.google.inject.AbstractModule;

public class MagritModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TimeService.class).to(SimpleTimeService.class);
		bind(BuildStatusesService.class).to(DummyBuildStatusesService.class);
		bind(BuildQueueService.class).to(BuildQueueServiceImpl.class);
		bind(PublickeyAuthenticator.class).to(GitPublickeyAuthenticator.class);
		bind(CommandFactory.class).to(MagritCommandFactory.class);
	}

}
