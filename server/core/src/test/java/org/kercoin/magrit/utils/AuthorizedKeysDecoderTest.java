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
package org.kercoin.magrit.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;

import tests.FilesUtils;

public class AuthorizedKeysDecoderTest {

	AuthorizedKeysDecoder decoder;
	
	@Before
	public void setUp() throws Exception {
		decoder = new AuthorizedKeysDecoder();
	}

	@Test
	public void testDecodePublicKey_dsa() throws Exception {
		// given
		String encoded = FilesUtils.readStream(getClass().getClassLoader()
				.getResourceAsStream("keys/dsa@localhost.pub"));
		
		// when
		PublicKey pk = decoder.decodePublicKey(encoded);
		
		// then
		assertThat(pk).isNotNull();
		assertThat(pk.getAlgorithm()).isEqualTo("DSA");
		assertThat(pk.getFormat()).isEqualTo("X.509");
		assertThat(pk.getEncoded()).hasSize(443);
	}

	@Test
	public void testDecodePublicKey_rsa() throws Exception {
		// given
		String encoded = FilesUtils.readStream(getClass().getClassLoader()
				.getResourceAsStream("keys/rsa@localhost.pub"));
		
		// when
		PublicKey pk = decoder.decodePublicKey(encoded);
		
		// then
		assertThat(pk).isNotNull();
		assertThat(pk.getAlgorithm()).isEqualTo("RSA");
		assertThat(pk.getFormat()).isEqualTo("X.509");
		assertThat(pk.getEncoded()).hasSize(292);
	}

}
