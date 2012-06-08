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
package org.kercoin.magrit.sshd.auth;

import java.io.IOException;
import java.security.PublicKey;

import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.util.Arrays;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.utils.GitUtils;

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
		return Arrays.areEqual(ref.getEncoded(), candidate.getEncoded());
	}
		
	private PublicKey readKeyFromRepository(String username) throws AmbiguousObjectException, Exception {
		String revstr = String.format("HEAD:keys/%s.pub", username);
		String encoded = gitUtils.show(datasource, revstr);
		if (encoded == null)
			return null;

		return new AuthorizedKeysDecoder().decodePublicKey(encoded);
	}

}
