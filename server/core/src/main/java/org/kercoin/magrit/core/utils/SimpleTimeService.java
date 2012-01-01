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
package org.kercoin.magrit.core.utils;

import java.util.Calendar;
import java.util.TimeZone;

import org.kercoin.magrit.core.Pair;

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
