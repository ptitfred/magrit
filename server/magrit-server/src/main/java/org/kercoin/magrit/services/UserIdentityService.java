package org.kercoin.magrit.services;

import org.kercoin.magrit.utils.UserIdentity;

public interface UserIdentityService {
	UserIdentity find(String login);
}
