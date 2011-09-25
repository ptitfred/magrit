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
