package tests;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.fest.assertions.GenericAssert;
import org.kercoin.magrit.Configuration;
import org.kercoin.magrit.Configuration.Authentication;

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

	private static String cleanPath(String absolutePath) {
		return new File(absolutePath).getPath();
	}

}
