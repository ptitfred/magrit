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
