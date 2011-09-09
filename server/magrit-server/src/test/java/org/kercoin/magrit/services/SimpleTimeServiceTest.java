package org.kercoin.magrit.services;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.utils.Pair;


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
