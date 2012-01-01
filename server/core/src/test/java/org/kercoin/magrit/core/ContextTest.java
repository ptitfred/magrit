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
package org.kercoin.magrit.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.kercoin.magrit.core.Context;

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
