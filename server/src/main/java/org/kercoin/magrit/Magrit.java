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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.kercoin.magrit.Configuration.Authentication;
import org.kercoin.magrit.Service.ConfigurationLogger;
import org.kercoin.magrit.sshd.Server;
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
		guice = Guice.createInjector(new MagritModule());
		
		ctx = guice.getInstance(Context.class);
		ctx.setInjector(guice);
	}
	
	void configure(String[] args) throws ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = createCmdLineOptions();
		CommandLine cmdLine = parser.parse(options, args, true);

		if (cmdLine.hasOption("standard")) {
			ctx.configuration().applyStandardLayout(cmdLine.getOptionValue("standard"));
		}
		
		if (cmdLine.hasOption("port")) {
			try {
				int sshdPort = Integer.parseInt(cmdLine.getOptionValue("port"));
				if (sshdPort<=1024) {
					throw new ParseException("SSH port must be >1024");
				}
				ctx.configuration().setSshPort(sshdPort);
			} catch (NumberFormatException e) {
				throw new ParseException("SSH port option should be numeric");
			}
		}
		
		if (cmdLine.hasOption("bares")) {
			ctx.configuration().setRepositoriesHomeDir(new File(cmdLine.getOptionValue("bares")));
		}
		
		if (cmdLine.hasOption("work")) {
			ctx.configuration().setWorkHomeDir(new File(cmdLine.getOptionValue("work")));
		}
		
		if (cmdLine.hasOption("keys")) {
			ctx.configuration().setPublickeysRepositoryDir(new File(cmdLine.getOptionValue("keys")));
		}
		
		if (cmdLine.hasOption("authentication")) {
			Authentication authentication = Authentication.NONE;
			String authValue = cmdLine.getOptionValue("authentication");
			for (Authentication auth : Authentication.values()) {
				if (auth.external().equals(authValue)) {
					authentication = auth;
					break;
				}
			}
			ctx.configuration().setAuthentication(authentication);
		}
		
		ctx.configuration().setRemoteAllowed(cmdLine.hasOption("remote"));

	}
	
	private Options createCmdLineOptions() {
		Options opts = new Options();
		opts.addOption("p", "port", true, //
				"SSH port to listen to");
		opts.addOption("b", "bares", true, //
				"directory where to create bare repositories");
		opts.addOption("w", "work", true, //
				"directory where to create work directories (for builds)");
		opts.addOption("k", "keys", true, //
				"non-bare Git repository containing SSH public keys for authentication." +
				"Useless if and only if authentication=ssh-public-keys");
		opts.addOption("s", "standard", true, //
				"directory where to apply the standard " + //
				"layout for bare repositories, " + //
				"work directories and public keys, " + //
				"all put in the supplied directory");
		opts.addOption("a", "authentication", true, //
				"authentication provider : ssh-public-keys or none");
		opts.addOption("r", "remote", false, //
				"allows the Magrit instance to be accessed by non-local client");
		return opts;
	}

	Context getCtx() {
		return ctx;
	}
	
	void tryBind(int port) throws IOException {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
		} finally {
			if (ss!= null && ss.isBound()) {
				ss.close();
			}
		}
	}
	
	void tryBindOrFail(int port) {
		try {
			tryBind(port);
		} catch (IOException e) {
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
		return services.toArray(new Service[0]);
	}

	private Service getService(Class<? extends Service> type) {
		return guice.getInstance(type);
	}

	private void logConfig(Service[] services) {
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

	private final ConfigurationLogger configurationLogger = new LogConfigurationLogger();

	final class LogConfigurationLogger implements ConfigurationLogger {

		@Override
		public void logKey(String key, Object value) {
			log.info(format(key, value));
		}

		private String format(String key, Object value) {
			return String.format(" %-16s : %s", key, value);
		}

		@Override
		public void logSubKey(String subKey, Object value) {
			log.info(format("  " + subKey, value));
		}
	}
}
