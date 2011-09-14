package tests;

import org.kercoin.magrit.Configuration;

public class MyAssertions extends org.fest.assertions.Assertions {
	
	public static ConfigurationAssert assertThat(Configuration cfg) {
		return new ConfigurationAssert(ConfigurationAssert.class, cfg);
	}
}
