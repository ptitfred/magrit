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
