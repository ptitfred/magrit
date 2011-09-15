package org.kercoin.magrit.services;

import org.kercoin.magrit.utils.UserIdentity;

public class DummyUserIdentityService implements UserIdentityService {

	@Override
	public UserIdentity find(String login) {
		return new UserIdentity(login + "@localhost", login);
	}

}
