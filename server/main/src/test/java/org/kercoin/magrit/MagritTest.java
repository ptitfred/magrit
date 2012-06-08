/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

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

import static org.junit.Assert.fail;

import java.net.BindException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;

public class MagritTest {

	Magrit magrit;
	
	@Before
	public void setup() {
		magrit = new Magrit();
	}
	
	@Test(expected=BindException.class)
	public void testTryBind() throws Exception {
		int port = 22222;
		ServerSocket ss = new ServerSocket(port);
		try {
			magrit.tryBind(port);
		} finally {
			ss.close();
		}
	}
	
	@Test
	public void testTryBind_releasePortAfterTry() throws Exception {
		int port = 22222;
		magrit.tryBind(port);
		try {
			magrit.tryBind(port);
		} catch (BindException e) {
			fail("Magrit.tryBind(int) didn't release the TCP port.");
		}
	}
	
	
}
