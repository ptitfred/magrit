/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

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

import java.util.Date;

/**
 * @author ptitfred
 *
 */
public final class Filters {
	private Filters() {}
	
	private static final Filter ANY = new Filter() {
		@Override
		public boolean matches(boolean isRunning, Date submissionDate) {
			return true;
		}
	};
	private static final Filter PENDING = new Filter() {
		public boolean matches(boolean r, Date d) {
			return !r;
		}
	};
	private static final Filter RUNNING = new Filter() {
		public boolean matches(boolean r, Date d) {
			return r;
		}
	};

	public static Filter any() {
		return Filters.ANY;
	}

	public static Filter pending() {
		return Filters.PENDING;
	}

	public static Filter running() {
		return Filters.RUNNING;
	}

	public static Filter since(final Date from) {
		return between(from, null);
	}

	public static Filter until(final Date to) {
		return between(null, to);
	}

	public static Filter between(final Date from, final Date to) {
		if (from == null && to == null) {
			throw new IllegalArgumentException("Both dates can't be null.");
		}
		if (from != null && to != null && from.after(to)) {
			throw new IllegalArgumentException("from must be before to");
		}
		return new Filter() {
			@Override
			public boolean matches(boolean isRunning, Date submissionDate) {
				return (from == null || submissionDate.after(from))
						&& (to == null || submissionDate.before(to));
			}
		};
	}

}
