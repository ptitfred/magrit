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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.kercoin.magrit.core.Configuration;
import org.kercoin.magrit.core.Configuration.Authentication;

/**
 * @author ptitfred
 *
 */
class ArgumentsParser {

	private final CommandLine cmdLine;

	ArgumentsParser(String[] args) throws ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = createCmdLineOptions();
		this.cmdLine = parser.parse(options, args, true);
	}

	void configure(Configuration configuration) throws ParseException {
		if (has("standard")) {
			configuration.applyStandardLayout(get("standard"));
		}
		
		if (has("port")) {
			configuration.setSshPort(getPort("port", "SSH"));
		}
		if (has("http-port")) {
			configuration.setHttpPort(getPort("http-port", "HTTP"));
		}
		
		if (has("bares")) {
			configuration.setRepositoriesHomeDir(getFile("bares"));
		}
		
		if (has("work")) {
			configuration.setWorkHomeDir(getFile("work"));
		}
		
		if (has("keys")) {
			configuration.setPublickeysRepositoryDir(getFile("keys"));
		}
		
		if (has("authentication")) {
			Authentication authentication = Authentication.NONE;
			String authValue = get("authentication");
			authentication = Authentication.fromExternalValue(authValue);
			configuration.setAuthentication(authentication);
		}
		
		configuration.setRemoteAllowed(has("remote"));
		configuration.setWebApp(!has("no-webapp"));
	}

	private File getFile(String opt) {
		return new File(cmdLine.getOptionValue(opt));
	}

	private int getPort(String opt, String portName) throws ParseException {
		return getNumber(opt, 1024, portName + " port option");
	}

	private String get(String opt) {
		return cmdLine.getOptionValue(opt);
	}

	private boolean has(String opt) {
		return cmdLine.hasOption(opt);
	}

	private int getNumber(String opt, int min, String label) throws ParseException {
		try {
			int httpPort = Integer.parseInt(get(opt));
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
		opts.addOption(null, "no-webapp", false, //
				"disables the monitor web app");
		opts.addOption("h", "http-port", true, //
				"HTTP(s) port to listen to");
		return opts;
	}

}
