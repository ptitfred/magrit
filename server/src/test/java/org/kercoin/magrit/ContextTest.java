package org.kercoin.magrit;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import tests.GuiceModulesHolder;

public class ContextTest {

	@Test
	public void testInjection() throws Exception {
		// when ----------------------------------
		Context context = GuiceModulesHolder.MAGRIT_MODULE.getInstance(Context.class);
		// then ----------------------------------
		assertThat(context.getCommandRunnerPool()).isNotNull();
	}

}
