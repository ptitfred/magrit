package org.kercoin.magrit.services.utils;

import org.kercoin.magrit.utils.UserIdentity;

import com.google.inject.ImplementedBy;

@ImplementedBy(DummyUserIdentityService.class)
public interface UserIdentityService {
	UserIdentity find(String login);
}
