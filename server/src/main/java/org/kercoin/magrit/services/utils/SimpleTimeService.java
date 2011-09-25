package org.kercoin.magrit.services.utils;

import java.util.Calendar;
import java.util.TimeZone;

import org.kercoin.magrit.utils.Pair;

public class SimpleTimeService implements TimeService {

	@Override
	public Pair<Long, Integer> now() {
		return now(Calendar.getInstance());
	}
	
	Pair<Long, Integer> now(Calendar when) {
		TimeZone tz = when.getTimeZone();
		int offsetInMillis = tz.getOffset(when.getTimeInMillis());
		int offsetInMinutes = offsetInMillis / (1000 * 60);
		return new Pair<Long, Integer>(when.getTimeInMillis(), offsetInMinutes);
	}
	
	
	public String offsetToString(int minutes) {
		int offsetInHours = Math.abs(minutes) / 60;
		int remainderInMinutes = Math.abs(minutes) % 60;
		String offsetEncoded = Integer.toString(offsetInHours * 100 + remainderInMinutes);
		StringBuilder sb = new StringBuilder();
		sb.append(minutes >= 0 ? '+' : '-');
		sb.append("0000".substring(offsetEncoded.length()));
		sb.append(offsetEncoded);
		return sb.toString();
	}

}
