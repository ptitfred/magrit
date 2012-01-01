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

import static org.fest.assertions.Assertions.assertThat;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.core.model.Pair;
import org.kercoin.magrit.core.utils.SimpleTimeService;


public class SimpleTimeServiceTest {

	private static final long SECOND = 1000L;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;
	private static final long DAY = 24 * HOUR;
	private static final long WEEK = 7 * DAY;
	
	SimpleTimeService timeService;
	
	@Before
	public void setUp() {
		timeService = new SimpleTimeService();
	}

	@Test
	public void testNow() {
		Calendar when = Calendar.getInstance(TimeZone.getTimeZone("CET"));
		// 1970.01.10 (yyyy.mm.dd)
		when.setTimeInMillis(1* WEEK + 3* DAY);
		Pair<Long,Integer> time = timeService.now(when);
		assertThat(time).isNotNull();
		assertThat(time.getU()).isEqualTo(60);
		assertThat(time.getT()).isEqualTo(10* DAY);
	}
	
	@Test
	public void testOffsetToString() {
		assertThat(timeService.offsetToString(120)).isEqualTo("+0200");
		assertThat(timeService.offsetToString(-120)).isEqualTo("-0200");
		assertThat(timeService.offsetToString(90)).isEqualTo("+0130");
		assertThat(timeService.offsetToString(0)).isEqualTo("+0000");
	}

}
