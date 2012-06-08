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
package org.kercoin.magrit.sshd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.auth.UserAuthPublicKey;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.kercoin.magrit.core.Configuration;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.Configuration.Authentication;
import org.kercoin.magrit.core.services.Service;
import org.kercoin.magrit.core.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Server implements Service.UseTCP {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private SshServer sshd;

	private final int port;

	@Inject
	public Server(final Context ctx, CommandFactory factory) {
		port = ctx.configuration().getSshPort();
		sshd = SshServer.setUpDefaultServer();
		
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("key.pem"));
        } else {
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("key.ser"));
        }
        
        PublickeyAuthenticator auth = null;
        if (ctx.configuration().getAuthentication() == Configuration.Authentication.SSH_PUBLIC_KEYS) {
        	auth = ctx.getInjector().getInstance(PublickeyAuthenticator.class);
        }
        setupUserAuth(auth);
        
		sshd.setCommandFactory(factory);

		if (!ctx.configuration().isRemoteAllowed()) {
			sshd.setSessionFactory(new LocalOnlySessionFactory());
		}
		
        sshd.setForwardingFilter(new ForwardingFilter() {
            public boolean canForwardAgent(ServerSession session) {
                return false;
            }

            public boolean canForwardX11(ServerSession session) {
                return false;
            }

            public boolean canListen(InetSocketAddress address, ServerSession session) {
                return false;
            }

            public boolean canConnect(InetSocketAddress address, ServerSession session) {
                return false;
            }
        });
        
	}

	private void setupUserAuth(PublickeyAuthenticator auth) {
		List<NamedFactory<UserAuth>> list = new ArrayList<NamedFactory<UserAuth>>();
		if (auth != null) {
			list.add(new UserAuthPublicKey.Factory());
			sshd.setPublickeyAuthenticator(auth);
		} else {
			list.add(new UserAuthNone.Factory());
		}
		sshd.setUserAuthFactories(list);
	}

	@Override
	public void start() throws ServiceException {
		sshd.setPort(port);
		try {
			sshd.start();
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public String getName() {
		return "SSH Service";
	}

	@Override
	public int getTCPPort() {
		return port;
	}

	@Override
	public void logConfig(ConfigurationLogger log, Configuration cfg) {
		log.logKey("SSHd", cfg.getSshPort());
		log.logKey("Listening", cfg.isRemoteAllowed() ? "everybody" : "localhost");
		log.logKey("Authent", cfg.getAuthentication().external());
		if (cfg.getAuthentication() == Authentication.SSH_PUBLIC_KEYS) {
			log.logSubKey("Keys dir", cfg.getPublickeyRepositoryDir());
		}
		log.logKey("Home dir", cfg.getRepositoriesHomeDir());
		log.logKey("Work dir", cfg.getWorkHomeDir());
	}
}
