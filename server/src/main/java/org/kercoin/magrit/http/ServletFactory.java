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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.servlet.ServletMapping;
import org.kercoin.magrit.http.servlets.BuildServlet;
import org.kercoin.magrit.http.servlets.EventsWebSocket;
import org.kercoin.magrit.http.servlets.Home;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ServletFactory {

	private final List<ServletDefinition> servlets = new ArrayList<ServletDefinition>();
	private final List<ServletMapping> mappings = new ArrayList<ServletMapping>();

	@Inject
	public ServletFactory() {
		defineAndBind(Home.class, "home", "/");
		defineAndBind(BuildServlet.class, "build", "/build");
		defineAndBind(EventsWebSocket.class, "events", "/events");
	}

	private void defineAndBind(Class<? extends HttpServlet> type, String servletName, String... paths) {
		define(servletName, type);
		bind(servletName, paths);
	}

	private void define(String servletName, Class<? extends HttpServlet> type) {
		servlets.add(new ServletDefinition(servletName, type));
	}

	private void bind(String servletName, String... paths) {
		ServletMapping mapping = new ServletMapping();
		mapping.setServletName(servletName);
		mapping.setPathSpecs(paths);
		mappings.add(mapping);
	}

	public Collection<ServletDefinition> getServlets() {
		return servlets;
	}

	public Collection<ServletMapping> getServletMappings() {
		return mappings;
	}

}
