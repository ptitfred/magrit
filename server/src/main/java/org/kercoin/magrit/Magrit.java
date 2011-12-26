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
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.kercoin.magrit.Configuration.Authentication;
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
			ctx.configuration().setSshPort(getNumber(cmdLine.getOptionValue("port"), 1024, "SSH port option"));
		}
		if (cmdLine.hasOption("http-port")) {
			ctx.configuration().setHttpPort(getNumber(cmdLine.getOptionValue("http-port"), 1024, "HTTP port option"));
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
		ctx.configuration().setWebApp(cmdLine.hasOption("webapp"));

	}

	private int getNumber(String value, int min, String label) throws ParseException {
		try {
			int httpPort = Integer.parseInt(value);
			if (httpPort<=min) {
				throw new ParseException(label + " must be >" + min);
			}
			return httpPort;
		} catch (NumberFormatException e) {
			throw new ParseException(label + " should be numeric");
		}
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
		opts.addOption(null, "webapp", false, //
				"enables the monitor web app");
		opts.addOption("h", "http-port", true, //
				"HTTP(s) port to listen to");
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
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private void launch() throws IOException {
		Configuration cfg = ctx.configuration();
		log.info("--------------------------------------------------------------------");
		log.info("Port used  : {}", cfg.getSshPort());
		log.info("Listening  : {}", cfg.isRemoteAllowed() ? "everybody":"localhost");
		log.info("Authent    : {}", cfg.getAuthentication().external());
		if (cfg.getAuthentication() == Authentication.SSH_PUBLIC_KEYS) {
		log.info("  Keys dir : {}", cfg.getPublickeyRepositoryDir());
		}
		log.info("Home dir   : {}", cfg.getRepositoriesHomeDir());
		log.info("Work dir   : {}", cfg.getWorkHomeDir());
		log.info("--------------------------------------------------------------------");
		try {
			tryBind(cfg.getSshPort());
			log.info("Starting...");
			guice.getInstance(Server.class).start(cfg.getSshPort());
			log.info("R E A D Y - {}", new Date());
		} catch (BindException be) {
			log.error("Port {} already bound, aborting...", cfg.getSshPort());
		}
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
