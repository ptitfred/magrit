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

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.servlet.ServletMapping;
import org.kercoin.magrit.http.servlets.BuildServlet;
import org.kercoin.magrit.http.servlets.Home;

import com.google.inject.Singleton;

@Singleton
public class ServletFactory {

	public Collection<ServletDefinition> getServlets() {
		return Arrays.asList(
				define("build", BuildServlet.class),
				define("home", Home.class)
			);
	}

	private ServletDefinition define(String servletName, Class<? extends HttpServlet> type) {
		return new ServletDefinition(servletName, type);
	}

	public Collection<ServletMapping> getServletMappings() {
		return Arrays.asList(
				bind("build", "/build"),
				bind("home", "/")
			);
	}

	private ServletMapping bind(String servletName, String... paths) {
		ServletMapping mapping = new ServletMapping();
		mapping.setServletName(servletName);
		mapping.setPathSpecs(paths);
		return mapping;
	}

}
