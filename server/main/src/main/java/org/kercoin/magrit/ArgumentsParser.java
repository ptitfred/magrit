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

	private final String[] args;

	ArgumentsParser(String[] args) {
		this.args = args;
	}

	void configure(Configuration configuration) throws ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = createCmdLineOptions();
		CommandLine cmdLine = parser.parse(options, args, true);

		if (cmdLine.hasOption("standard")) {
			configuration.applyStandardLayout(cmdLine.getOptionValue("standard"));
		}
		
		if (cmdLine.hasOption("port")) {
			try {
				int sshdPort = Integer.parseInt(cmdLine.getOptionValue("port"));
				if (sshdPort<=1024) {
					throw new ParseException("SSH port must be >1024");
				}
				configuration.setSshPort(sshdPort);
			} catch (NumberFormatException e) {
				throw new ParseException("SSH port option should be numeric");
			}
		}
		
		if (cmdLine.hasOption("bares")) {
			configuration.setRepositoriesHomeDir(new File(cmdLine.getOptionValue("bares")));
		}
		
		if (cmdLine.hasOption("work")) {
			configuration.setWorkHomeDir(new File(cmdLine.getOptionValue("work")));
		}
		
		if (cmdLine.hasOption("keys")) {
			configuration.setPublickeysRepositoryDir(new File(cmdLine.getOptionValue("keys")));
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
			configuration.setAuthentication(authentication);
		}
		
		configuration.setRemoteAllowed(cmdLine.hasOption("remote"));
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

}
