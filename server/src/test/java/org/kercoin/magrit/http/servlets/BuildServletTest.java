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
package org.kercoin.magrit.http.servlets;


import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

/**
 * @author ptitfred
 *
 */
public class BuildServletTest {

	private BuildServlet servlet;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		servlet = new BuildServlet(null);
	}

	@Test
	public void testEncodeJSON() {
		assertThat(servlet.encodeJSON(Collections.<String>emptyList(), Collections.<String>emptyList())).isEqualTo("{\"runnings\":[],\"pendings\":[]}");
		assertThat(servlet.encodeJSON(Arrays.asList("12345"), Collections.<String>emptyList())).isEqualTo("{\"runnings\":[\"12345\"],\"pendings\":[]}");
		assertThat(servlet.encodeJSON(Collections.<String>emptyList(), Arrays.asList("12345", "67890"))).isEqualTo("{\"runnings\":[],\"pendings\":[\"12345\",\"67890\"]}");
	}

}
