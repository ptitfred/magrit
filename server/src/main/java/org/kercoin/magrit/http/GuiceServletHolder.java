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
package org.kercoin.magrit.http;

import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author ptitfred
 *
 */
class GuiceServletHolder extends ServletHolder {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Injector injector;

	GuiceServletHolder(Injector injector) {
		this.injector = injector;
	}

	@Override
	public synchronized Object newInstance() throws InstantiationException,
			IllegalAccessException {
		Class<?> servletClass = getServletClass();
		if (servletClass != null) {
			return injector.getInstance(servletClass);
		}
		throw new InstantiationException("Couldn't find class " + getClassName());
	}

	private Class<?> getServletClass() {
		try {
			return getClassLoader().loadClass(getClassName());
		} catch (ClassNotFoundException e) {
			log.error("Couldn't load Servlet class " + getClassName());
		}
		return null;
	}

	private ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}
