package org.kercoin.magrit.services.utils;

import org.kercoin.magrit.utils.UserIdentity;

public interface UserIdentityService {
	UserIdentity find(String login);
}
