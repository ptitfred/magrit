/*
Copyright 2011 Frederic Menou

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

import java.util.concurrent.ExecutorService;

import org.kercoin.magrit.core.utils.GitUtils;

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
