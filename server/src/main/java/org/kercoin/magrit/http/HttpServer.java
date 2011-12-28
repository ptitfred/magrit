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

import java.net.URL;
import java.util.Collection;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
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

	private final Context ctx;
	private final Server httpd;

	@Inject
	public HttpServer(final Context ctx, ServletFactory factory) {
		this.ctx = ctx;
		httpd = new Server(ctx.configuration().getHttpPort());
		httpd.setHandler(getHandler(factory));
	}

	private Handler getHandler(ServletFactory factory) {
		HandlerList global = new HandlerList();
		global.addHandler(getResourceHandler());
		global.addHandler(getServletHandler(factory));
		global.addHandler(new DefaultHandler());
		return global;
	}

	private Handler getResourceHandler() {
		ResourceHandler handler = new ResourceHandler();
		handler.setResourceBase(getPath());
		handler.setWelcomeFiles(new String[] { "index.html" });
		handler.setDirectoriesListed(true);
		return handler;
	}

	private String getPath() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource("web");
		if (resource != null) {
			return resource.toString();
		}
		return null;
	}

	private ServletHandler getServletHandler(ServletFactory factory) {
		ServletHandler handler = new ServletHandler();
		handler.setServlets(asHolders(factory.getServlets()));
		handler.setServletMappings(factory.getServletMappings().toArray(new ServletMapping[0]));
		return handler;
	}

	private ServletHolder[] asHolders(Collection<ServletDefinition> servlets) {
		ServletHolder[] holders = new ServletHolder[servlets.size()];
		int i=0;
		for (ServletDefinition servlet : servlets) {
			holders[i++] = asHolder(servlet);
		}
		return holders;
	}

	private ServletHolder asHolder(ServletDefinition servlet) {
		ServletHolder holder = new GuiceServletHolder(ctx.getInjector());
		holder.setName(servlet.getName());
		holder.setClassName(servlet.getType().getName());
		return holder;
	}

	public void start() throws Exception {
		httpd.start();
	}
}
