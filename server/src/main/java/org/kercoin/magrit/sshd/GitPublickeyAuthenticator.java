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
package org.kercoin.magrit.sshd;

import java.io.IOException;
import java.security.PublicKey;

import org.apache.sshd.common.Digest;
import org.apache.sshd.common.digest.SHA1;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.util.Arrays;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.AuthorizedKeysDecoder;
import org.kercoin.magrit.utils.GitUtils;

import com.google.inject.Inject;

public class GitPublickeyAuthenticator implements PublickeyAuthenticator {

	private final Context context;
	private final GitUtils gitUtils;
	
	private Repository datasource;
	
	@Inject
	public GitPublickeyAuthenticator(Context context, GitUtils gitUtils) {
		this.context = context;
		this.gitUtils = gitUtils;
	}
	
	private void open() throws IOException {
		if (this.datasource == null) {
			this.datasource = Git.open(
					context.configuration().getPublickeyRepositoryDir()
			).getRepository();
		}
	}
	
	@Override
	public boolean authenticate(String username, PublicKey authKey,
			ServerSession session) {
		try {
			open();
			PublicKey targetKey = readKeyFromRepository(username);
			return targetKey != null && areEqual(targetKey, authKey);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	boolean areEqual(PublicKey ref, PublicKey candidate) {
		byte[] raw = sha1(candidate.getEncoded());
		byte[] target = sha1(ref.getEncoded());
		return Arrays.areEqual(target, raw);
	}
	
	byte[] sha1(byte[] data) {
		try {
			Digest digest = new SHA1.Factory().create();
			digest.init();
			digest.update(data, 0, data.length);
			return digest.digest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[20];
	}
	
	private PublicKey readKeyFromRepository(String username) throws AmbiguousObjectException, Exception {
		String revstr = String.format("HEAD:keys/%s.pub", username);
		String encoded = gitUtils.show(datasource, revstr);
		if (encoded == null)
			return null;

		return new AuthorizedKeysDecoder().decodePublicKey(encoded);
	}

}
