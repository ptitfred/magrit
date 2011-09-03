package org.kercoin.magrit.git;

import java.io.File;

import com.google.inject.Singleton;

@Singleton
public class Context {
	
	private File repositoriesHomeDir;

	public Context() {
	}

	public File getRepositoriesHomeDir() {
		return repositoriesHomeDir;
	}

	public void setRepositoriesHomeDir(File repositoriesHomeDir) {
		this.repositoriesHomeDir = repositoriesHomeDir;
	}

}
