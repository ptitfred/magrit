package org.kercoin.magrit;

import org.kercoin.magrit.utils.GitUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class Context {
	
	private final Configuration configuration = new Configuration();
	
	private Injector injector;
	
	private final GitUtils gitUtils;

	public Context() {
		gitUtils = null;
	}
	
	@Inject
	public Context(GitUtils gitUtils) {
		this.gitUtils = gitUtils;
	}

	public Configuration configuration() {
		return configuration;
	}
	
	public Injector getInjector() {
		return injector;
	}
	
	public void setInjector(Injector injector) {
		this.injector = injector;
	}
	
	public GitUtils getGitUtils() {
		return gitUtils;
	}

}
