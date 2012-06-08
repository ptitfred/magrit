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
package org.kercoin.magrit;

import org.kercoin.magrit.core.services.Service.ConfigurationLogger;
import org.slf4j.Logger;

final class LogConfigurationLogger implements ConfigurationLogger {

	private final Logger log;
	
	LogConfigurationLogger(Logger log) {
		this.log = log;
	}
	
	@Override
	public void logKey(String key, Object value) {
		log.info(format(key, value));
	}

	private String format(String key, Object value) {
		return String.format(" %-16s : %s", key, value);
	}

	@Override
	public void logSubKey(String subKey, Object value) {
		log.info(format("  " + subKey, value));
	}
}