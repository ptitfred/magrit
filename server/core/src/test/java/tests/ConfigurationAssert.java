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
package tests;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.fest.assertions.GenericAssert;
import org.kercoin.magrit.core.Configuration;
import org.kercoin.magrit.core.Configuration.Authentication;

public class ConfigurationAssert extends GenericAssert<ConfigurationAssert, Configuration> {

	ConfigurationAssert(Class<ConfigurationAssert> selfType,
			Configuration actual) {
		super(selfType, actual);
	}
	
	public ConfigurationAssert onPort(int expected) {
		assertThat(actual.getSshPort()).isEqualTo(expected);
		return this;
	}

	public ConfigurationAssert hasHomeDir(String absolutePath) {
		assertThat(actual.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo(cleanPath(absolutePath));
		return this;
	}
	
	public ConfigurationAssert hasWorkDir(String absolutePath) {
		assertThat(actual.getWorkHomeDir().getAbsolutePath()).isEqualTo(cleanPath(absolutePath));
		return this;
	}

	public ConfigurationAssert hasPublickeyDir(String absolutePath) {
		assertThat(actual.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo(cleanPath(absolutePath));
		return this;
	}

	public ConfigurationAssert hasAuthentication(Authentication expected) {
		assertThat(actual.getAuthentication()).isEqualTo(expected);
		return this;
	}
	
	public ConfigurationAssert isRemoteAllowed(boolean expected) {
		assertThat(actual.isRemoteAllowed()).isEqualTo(expected);
		return this;
	}

	public ConfigurationAssert isWebAppEnabled(boolean expected) {
		assertThat(actual.hasWebApp()).isEqualTo(expected);
		return this;
	}

	public ConfigurationAssert onWebAppPort(int expected) {
		assertThat(actual.getHttpPort()).isEqualTo(expected);
		return this;
	}

	private static String cleanPath(String absolutePath) {
		return new File(absolutePath).getPath();
	}

}
