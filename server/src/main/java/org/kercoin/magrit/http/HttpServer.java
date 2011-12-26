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

import java.util.Collection;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.kercoin.magrit.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HttpServer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private Server httpd;

	@Inject
	public HttpServer(final Context ctx, ServletFactory factory) {
		httpd = new Server(ctx.configuration().getHttpPort());
		httpd.setHandler(getHandler(factory));
	}

	private Handler getHandler(ServletFactory factory) {
		ServletHandler servletHandler = new ServletHandler();
		servletHandler.setServlets(asHolders(servletHandler, factory.getServlets()));
		servletHandler.setServletMappings(factory.getServletMappings().toArray(new ServletMapping[0]));
		return servletHandler;
	}

	private ServletHolder[] asHolders(ServletHandler handler, Collection<ServletDefinition> servlets) {
		ServletHolder[] holders = new ServletHolder[servlets.size()];
		int i=0;
		for (ServletDefinition servlet : servlets) {
			holders[i++] = asHolder(handler, servlet);
		}
		return holders;
	}

	private ServletHolder asHolder(ServletHandler handler, ServletDefinition servlet) {
		ServletHolder holder = handler.newServletHolder();
		holder.setName(servlet.getName());
		holder.setClassName(servlet.getType().getName());
		return holder;
	}

	public void start() throws Exception {
		httpd.start();
	}
}
