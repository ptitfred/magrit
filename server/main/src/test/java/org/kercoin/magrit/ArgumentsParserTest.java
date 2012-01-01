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


import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static tests.MyAssertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.core.Configuration;
import org.kercoin.magrit.core.Configuration.Authentication;

import tests.ConfigurationAssert;

/**
 * @author ptitfred
 *
 */
public class ArgumentsParserTest {

	Configuration configuration;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		configuration = new Configuration();
	}

	@Test
	public void testConfigure_defaults() throws Exception {
		// when ----------------------------------
		tryConfigure("");
		
		// then ----------------------------------
		checkThat()
			.onPort(2022) //
			.hasHomeDir(System.getProperty("java.io.tmpdir") + "/magrit/repos") //
			.hasWorkDir(System.getProperty("java.io.tmpdir") + "/magrit/builds") //
			.hasPublickeyDir(System.getProperty("java.io.tmpdir") + "/magrit/keys") //
			.hasAuthentication(Authentication.SSH_PUBLIC_KEYS) //
			.isRemoteAllowed(false)
			.isWebAppEnabled(true);
	}

	@Test
	public void testConfigure_port() throws Exception {
		// when ----------------------------------
		tryConfigure("--port 1234");
		
		// then ----------------------------------
		checkThat() //
			.onPort(1234) //
			.hasHomeDir(System.getProperty("java.io.tmpdir") + "/magrit/repos") //
			.hasWorkDir(System.getProperty("java.io.tmpdir") + "/magrit/builds") //
			.hasPublickeyDir(System.getProperty("java.io.tmpdir") + "/magrit/keys");
	}
	
	@Test
	public void testConfigure_auth() throws Exception {
		// when ----------------------------------
		tryConfigure("--authentication none");

		// then ----------------------------------
		checkThat() //
			.hasAuthentication(Authentication.NONE);
	}
	
	@Test
	public void testConfigure_auth_junk() throws Exception {
		// when ----------------------------------
		tryConfigure("--authentication what-ever-unsupported");

		// then ----------------------------------
		checkThat().hasAuthentication(Authentication.NONE);
	}

	@Test
	public void testConfigure_remote() throws Exception {
		// when ----------------------------------
		tryConfigure("--remote");

		// then ----------------------------------
		checkThat() //
			.isRemoteAllowed(true);
	}
	
	@Test
	public void testConfigure_standardLayout() throws Exception {
		// when ----------------------------------
		tryConfigure("--port 5678 --standard /path/to");

		// then ----------------------------------
		checkThat() //
			.onPort(5678) //
			.hasHomeDir("/path/to/bares") //
			.hasWorkDir("/path/to/builds") //
			.hasPublickeyDir("/path/to/keys");
	}
	
	@Test
	public void testConfigure_standardLayout_overriden() throws Exception {
		// given ---------------------------------
		String[] cmds = { //
				// test de prédominance: le standard layout ne s'impose pas aux autres 
				"--port 5678 --work /tmp/magrit-builds --standard /path/to", //
				"--port 5678 --standard /path/to --work /tmp/magrit-builds" //
				};
		for	(String cmd : cmds) {
			// when ----------------------------------
			tryConfigure(cmd);

			// then ----------------------------------
			checkThat() //
				.onPort(5678) //
				.hasHomeDir("/path/to/bares") //
				.hasWorkDir("/tmp/magrit-builds") //
				.hasPublickeyDir("/path/to/keys");

		}
	}
	
	@Test
	public void testConfigure_port_repoDir() throws Exception {
		// when ----------------------------------
		tryConfigure("--port 1234 --bares /path/to/repos");
		
		// then ----------------------------------
		checkThat() //
			.onPort(1234) //
			.hasHomeDir("/path/to/repos") //
			.hasWorkDir(System.getProperty("java.io.tmpdir") + "/magrit/builds") //
			.hasPublickeyDir(System.getProperty("java.io.tmpdir") + "/magrit/keys");
	}

	@Test
	public void testConfigure_port_repoDir_workDir() throws Exception {
		String[] cmdLines = {"-p 1234  -b /path/to/repos -w /path/to/buildspace", "--port=1234  --bares=/path/to/repos --work /path/to/buildspace"};
		for (String cmd : cmdLines) {
			// when ----------------------------------
			tryConfigure(cmd);

			// then ----------------------------------
			checkThat() //
				.onPort(1234) //
				.hasHomeDir("/path/to/repos") //
				.hasWorkDir("/path/to/buildspace") //
				.hasPublickeyDir(System.getProperty("java.io.tmpdir") + "/magrit/keys");
		}
	}


	@Test
	public void testConfigure_port_repoDir_workDir_publicKeys() throws Exception {
		// when ----------------------------------
		tryConfigure("--port 1234 \r\n" +
				"--bares /path/to/repos \n" +
				"--work /path/to/buildspace \r" +
				"--keys /path/to/publickeys");

		// then ----------------------------------
		checkThat()
			.onPort(1234) //
			.hasHomeDir("/path/to/repos") //
			.hasWorkDir("/path/to/buildspace") //
			.hasPublickeyDir("/path/to/publickeys");
	}
	
	@Test(expected=ParseException.class)
	public void testConfigure_port_illegal_nonNumeric() throws Exception {
		tryConfigure("--port xxxxx");
	}

	@Test
	public void testConfigure_port_illegal_nonAllowed() throws Exception {
		int[] values = { -1, 0, 1024};
		for (int port : values) {
			try {
				parse("--port " + port).configure(configuration);
				fail("Should have thrown a ParseException");
			} catch (ParseException e) {}
		}
	}

	@Test
	public void testConfigure_webapp() throws Exception {
		checkThat().isWebAppEnabled(true);
		tryConfigure("--no-webapp");
		checkThat().isWebAppEnabled(false);
	}

	@Test
	public void testConfigure_webapp_port() throws Exception {
		checkThat().onWebAppPort(2080);
		tryConfigure("--http-port 1234");
		checkThat().onWebAppPort(1234);
	}

	void tryConfigure(String commandLine) throws ParseException {
		parse(commandLine).configure(configuration);
	}

	static ArgumentsParser parse(String commandLine) {
		return new ArgumentsParser(split(commandLine));
	}

	static String[] split(String cmdLine) {
		Scanner s = new Scanner(cmdLine);
		s.useDelimiter(Pattern.compile("\\s+"));
		List<String> args = new ArrayList<String>();
		while(s.hasNext()) {
			args.add(s.next());
		}
		return args.toArray(new String[0]);
	}

	ConfigurationAssert checkThat() {
		return assertThat(configuration);
	}

}
