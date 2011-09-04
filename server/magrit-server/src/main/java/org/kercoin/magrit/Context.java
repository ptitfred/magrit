package org.kercoin.magrit;

import java.io.File;

import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class Context {
	
	private File repositoriesHomeDir;
	
	private Injector injector;

	public Context() {
	}

	public File getRepositoriesHomeDir() {
		return repositoriesHomeDir;
	}

	public void setRepositoriesHomeDir(File repositoriesHomeDir) {
		this.repositoriesHomeDir = repositoriesHomeDir;
	}
	
	public Injector getInjector() {
		return injector;
	}
	
	public void setInjector(Injector injector) {
		this.injector = injector;
	}

}
