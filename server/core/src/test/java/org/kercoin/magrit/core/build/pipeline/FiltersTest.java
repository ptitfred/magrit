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
package org.kercoin.magrit.core.build.pipeline;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;



/**
 * @author ptitfred
 *
 */
public class FiltersTest {

	@Test
	public void filters_byDates() throws InterruptedException {
		Date d0 = now();
		Date ref1 = now();
		Date d1 = now();
		Date ref2 = now();
		Date d2 = now();

		Filter between = Filters.between(ref1, ref2);
		assertThat(between.matches(true, d0)).isFalse();
		assertThat(between.matches(true, d1)).isTrue();
		assertThat(between.matches(true, d2)).isFalse();

		Filter since = Filters.since(ref1);
		assertThat(since.matches(true, d0)).isFalse();
		assertThat(since.matches(true, d1)).isTrue();
		assertThat(since.matches(true, d2)).isTrue();

		Filter until = Filters.until(ref1);
		assertThat(until.matches(true, d0)).isTrue();
		assertThat(until.matches(true, d1)).isFalse();
		assertThat(until.matches(true, d2)).isFalse();
	}

	Date now() {
		try {
			return new Date();
		} finally {
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		}
	}
	
}
