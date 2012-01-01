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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.CoreModule;
import org.kercoin.magrit.core.services.Service;
import org.kercoin.magrit.core.services.Service.ConfigurationLogger;
import org.kercoin.magrit.core.services.ServiceException;
import org.kercoin.magrit.http.HttpServer;
import org.kercoin.magrit.sshd.Server;
import org.kercoin.magrit.sshd.SshdModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class Magrit {

	private Context ctx;
	private Injector guice;

	Magrit() {
		bootStrap();
	}

	void bootStrap() {
		guice = Guice.createInjector(new CoreModule(), new SshdModule());
		
		ctx = guice.getInstance(Context.class);
		ctx.setInjector(guice);
	}
	
	void configure(String[] args) throws ParseException {
		new ArgumentsParser(args).configure(ctx.configuration());
	}

	boolean tryBind(int port) {
		boolean success = false;
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
		} catch (Exception e) {
			log.error("Unable to bind the TCP port " + port, e);
		} finally {
			try {
				if (ss!= null && ss.isBound()) {
					success = true;
					ss.close();
				}
			} catch (IOException e) {
				log.error("Unmanageable error while closing test socket", e);
			}
		}
		return success;
	}

	private void tryBindOrFail(int port) {
		if (!tryBind(port)) {
			log.error("Port {} already bound, aborting...", port);
			System.exit(1);
		}
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private void launch() throws Exception {
		Service[] services = getServices();
		logConfig(services);
		checkTCPServices(services);
		startServices(services);
		log.info("R E A D Y - {}", new Date());
	}

	private Service[] getServices() {
		List<Service> services = new ArrayList<Service>();
		services.add(getService(Server.class));
		if (ctx.configuration().hasWebApp()) {
			services.add(getService(HttpServer.class));
		}
		return services.toArray(new Service[0]);
	}

	private Service getService(Class<? extends Service> type) {
		return guice.getInstance(type);
	}

	private void logConfig(Service[] services) {
		final ConfigurationLogger configurationLogger = new LogConfigurationLogger(log);
		log.info("--------------------------------------------------------------------");
		boolean first = true;
		for (Service svc : services) {
			if (first) {
				first = false;
			} else {
				log.info("---");
			}
			log.info(svc.getName() + " configuration");
			svc.logConfig(configurationLogger, ctx.configuration());
		}
		log.info("--------------------------------------------------------------------");
	}

	private void checkTCPServices(Service[] services) {
		for (Service svc : services) {
			if (svc instanceof Service.UseTCP) {
				tryBindOrFail(((Service.UseTCP) svc).getTCPPort());
			}
		}
	}

	private void startServices(Service[] services) throws ServiceException {
		for (Service svc : services) {
			start(svc);
		}
	}

	private void start(Service service) throws ServiceException {
		log.info("Starting " + service.getName());
		service.start();
	}
	
	public static void main(String[] args) throws Exception {
		Magrit m = new Magrit();
		try {
			m.configure(args);
			m.launch();
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
}
