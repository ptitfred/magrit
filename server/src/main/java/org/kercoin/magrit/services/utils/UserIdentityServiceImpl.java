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
package org.kercoin.magrit.services.utils;

import java.io.IOException;
import java.io.StringReader;
import java.security.AccessControlException;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Configuration;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.UserIdentity;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UserIdentityServiceImpl implements UserIdentityService {

	private final GitUtils gitUtils;
	private final Configuration configuration;
	
	private final Properties data;
	
	private Repository datasource;
	
	private ObjectId lastVersion;
	
	@Inject
	public UserIdentityServiceImpl(Context ctx) {
		gitUtils = ctx.getGitUtils();
		configuration = ctx.configuration();
		data = new Properties();
	}
	
	private void open() throws IOException {
		if (this.datasource == null) {
			this.datasource = Git.open(
					configuration.getPublickeyRepositoryDir()
			).getRepository();
		}
	}

	@Override
	public UserIdentity find(String login) throws AccessControlException {
		try {
			open();
			ObjectId v = datasource.resolve("HEAD:users.properties");
			if (v == null) {
				throw new AccessControlException(String.format("User '%s' not found in authentication database.", login));
			}
			if (!v.equals(lastVersion)) {
				data.clear();
				data.load(new StringReader(gitUtils.show(datasource, "HEAD:users.properties")));
			}
			
			String email = data.getProperty(String.format("user.%s.email", login));
			String name = data.getProperty(String.format("user.%s.name", login));
			if (email != null && name != null) {
				return new UserIdentity(email, name);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
		return new UserIdentity(login + "@localhost", login);
	}

}
