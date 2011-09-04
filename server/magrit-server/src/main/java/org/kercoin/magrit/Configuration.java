package org.kercoin.magrit;

import java.io.File;

public class Configuration {

	private File repositoriesHomeDir;
	
	private File workHomeDir = new File(System.getProperty("java.io.tmpdir"), "magrit");

	public File getRepositoriesHomeDir() {
		return repositoriesHomeDir;
	}

	public void setRepositoriesHomeDir(File repositoriesHomeDir) {
		this.repositoriesHomeDir = repositoriesHomeDir;
	}
	
	public File getWorkHomeDir() {
		return workHomeDir;
	}
	
	public void setWorkHomeDir(File workHomeDir) {
		this.workHomeDir = workHomeDir;
	}
	
}
