package org.kercoin.magrit.sshd;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.kercoin.magrit.MagritModule;
import org.kercoin.magrit.git.Context;
import org.kercoin.magrit.git.GitCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Server {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private SshServer sshd;
	private int port;

	public Server(int port, CommandFactory factory) {
		this.port = port;
		sshd = SshServer.setUpDefaultServer();
		
		sshd.setPort(port);
		sshd.setUserAuthFactories(createUserAuth());
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("key.pem"));
        } else {
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("key.ser"));
        }
		sshd.setCommandFactory(factory);
        
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

	private List<NamedFactory<UserAuth>> createUserAuth() {
		List<NamedFactory<UserAuth>> list = new ArrayList<NamedFactory<UserAuth>>();
		list.add(new UserAuthNone.Factory());
		return list;
	}

	public void start() throws IOException {
		log.info("Port used: {}", port);
		sshd.start();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MagritModule());
		
		int port = 2022;
		Context ctx = injector.getInstance(Context.class);
		
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		
		if (args.length >=2) {
			ctx.setRepositoriesHomeDir(new File(args[1]));
		} else {
			ctx.setRepositoriesHomeDir(new File("/tmp/magrit-tests"));
		}
		
	    GitCommandFactory factory = injector.getInstance(GitCommandFactory.class);
	    new Server(port, factory).start();
	}
	
}
