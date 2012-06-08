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
package org.kercoin.magrit.core;

import java.io.File;

public class Configuration {

	private static final String DEFAULT_BASE_DIR = System.getProperty("java.io.tmpdir") + "/magrit";

	private int sshPort = 2022;
	
	private File repositoriesHomeDir = new File(DEFAULT_BASE_DIR, "repos");
	
	private File publickeysRepositoryDir = new File(DEFAULT_BASE_DIR, "keys");
	
	private File workHomeDir = new File(DEFAULT_BASE_DIR, "builds");

	private Authentication authentication = Authentication.SSH_PUBLIC_KEYS;

	private boolean remoteAllowed;
	
	public static enum Authentication {
		SSH_PUBLIC_KEYS { public String external() { return "ssh-public-keys"; } },
		NONE { public String external() { return "none"; } };
		
		public abstract String external();

		/**
		 * @param authValue
		 * @return
		 */
		public static Authentication fromExternalValue(String authValue) {
			for (Authentication auth : Authentication.values()) {
				if (auth.external().equals(authValue)) {
					return auth;
				}
			}
			return Authentication.NONE;
		}
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	
	public Authentication getAuthentication() {
		return authentication;
	}
	
	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}
	
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

	public File getPublickeyRepositoryDir() {
		return publickeysRepositoryDir;
	}
	
	public void setPublickeysRepositoryDir(File publickeysRepositoryDir) {
		this.publickeysRepositoryDir = publickeysRepositoryDir;
	}

	public void applyStandardLayout(String dir) {
		repositoriesHomeDir = new File(dir, "bares");
		workHomeDir = new File(dir, "builds");
		publickeysRepositoryDir = new File(dir, "keys");
	}

	public boolean isRemoteAllowed() {
		return remoteAllowed;
	}

	public void setRemoteAllowed(boolean remoteAllowed) {
		this.remoteAllowed = remoteAllowed;
	}

	public int getSlots() {
		return Runtime.getRuntime().availableProcessors();
	}
	
}
