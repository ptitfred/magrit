package org.kercoin.magrit.services.utils;

import java.security.AccessControlException;

import org.kercoin.magrit.utils.UserIdentity;

import com.google.inject.ImplementedBy;

@ImplementedBy(UserIdentityServiceImpl.class)
public interface UserIdentityService {
	UserIdentity find(String login) throws AccessControlException;
}
