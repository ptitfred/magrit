package org.kercoin.magrit;

import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.kercoin.magrit.services.builds.QueueService;
import org.kercoin.magrit.services.builds.QueueServiceImpl;
import org.kercoin.magrit.services.builds.StatusesService;
import org.kercoin.magrit.services.builds.StatusesServiceImpl;
import org.kercoin.magrit.services.dao.BuildDAO;
import org.kercoin.magrit.services.dao.BuildDAOImpl;
import org.kercoin.magrit.services.utils.DummyUserIdentityService;
import org.kercoin.magrit.services.utils.SimpleTimeService;
import org.kercoin.magrit.services.utils.TimeService;
import org.kercoin.magrit.services.utils.UserIdentityService;
import org.kercoin.magrit.sshd.GitPublickeyAuthenticator;

import com.google.inject.AbstractModule;

public class MagritModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TimeService.class).to(SimpleTimeService.class);
		bind(StatusesService.class).to(StatusesServiceImpl.class);
		bind(QueueService.class).to(QueueServiceImpl.class);
		bind(PublickeyAuthenticator.class).to(GitPublickeyAuthenticator.class);
		bind(CommandFactory.class).to(MagritCommandFactory.class);
		bind(UserIdentityService.class).to(DummyUserIdentityService.class);
		bind(BuildDAO.class).to(BuildDAOImpl.class);
	}

}
