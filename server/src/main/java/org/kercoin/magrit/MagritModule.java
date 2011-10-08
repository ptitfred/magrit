package org.kercoin.magrit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.kercoin.magrit.sshd.GitPublickeyAuthenticator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class MagritModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PublickeyAuthenticator.class).to(GitPublickeyAuthenticator.class);
		bind(CommandFactory.class).to(MagritCommandFactory.class);
		
		bind(ExecutorService.class).annotatedWith(Names.named("commandRunnerPool")).toInstance(Executors.newCachedThreadPool());
	}

}
