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
import org.apache.sshd.server.auth.UserAuthPublicKey;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.kercoin.magrit.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Server {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private SshServer sshd;

	@Inject
	public Server(final Context ctx, CommandFactory factory, PublickeyAuthenticator auth) {
		sshd = SshServer.setUpDefaultServer();
		
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("key.pem"));
        } else {
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("key.ser"));
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
//		list.add(new UserAuthNone.Factory());
		list.add(new UserAuthPublicKey.Factory());
		sshd.setUserAuthFactories(list);
		sshd.setPublickeyAuthenticator(auth);
	}

	public void start(int port) throws IOException {
		log.info("Port used: {}", port);
		sshd.setPort(port);
		sshd.start();
	}

}
