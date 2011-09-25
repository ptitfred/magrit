package org.kercoin.magrit.services.utils;

import org.kercoin.magrit.utils.Pair;

import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleTimeService.class)
public interface TimeService {
	Pair<Long, Integer> now();
	String offsetToString(int minutes);
}
