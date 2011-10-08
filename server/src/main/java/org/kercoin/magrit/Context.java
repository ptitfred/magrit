package org.kercoin.magrit;

import java.util.concurrent.ExecutorService;

import org.kercoin.magrit.utils.GitUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class Context {
	
	private final Configuration configuration = new Configuration();
	
	private Injector injector;
	
	private final GitUtils gitUtils;
	
	private final ExecutorService commandRunnerPool;

	public Context() {
		gitUtils = null;
		commandRunnerPool = null;
	}
	
	public Context(GitUtils gitUtils) {
		this(gitUtils, null);
	}
	
	@Inject
	public Context(GitUtils gitUtils,
			@Named("commandRunnerPool") ExecutorService commandRunnerPool) {
		this.gitUtils = gitUtils;
		this.commandRunnerPool = commandRunnerPool;
	}

	public Configuration configuration() {
		return configuration;
	}
	
	public Injector getInjector() {
		return injector;
	}
	
	public ExecutorService getCommandRunnerPool() {
		return commandRunnerPool;
	}
	
	public void setInjector(Injector injector) {
		this.injector = injector;
	}
	
	public GitUtils getGitUtils() {
		return gitUtils;
	}

}
