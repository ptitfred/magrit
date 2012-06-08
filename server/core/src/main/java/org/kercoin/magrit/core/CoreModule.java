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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.dao.BuildDAOImpl;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @author ptitfred
 *
 */
public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(BuildDAO.class).to(BuildDAOImpl.class);
		bind(ExecutorService.class).annotatedWith(Names.named("commandRunnerPool")).toInstance(Executors.newCachedThreadPool());
	}

}
