package org.kercoin.magrit.services.utils;

import org.kercoin.magrit.utils.Pair;

public interface TimeService {
	Pair<Long, Integer> now();
	String offsetToString(int minutes);
}
