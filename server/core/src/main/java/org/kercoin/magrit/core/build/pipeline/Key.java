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


public interface Key extends Comparable<Key> {
	/**
	 * An uniq id, doesn't have any sense for client of the pipeline.
	 * @return
	 */
	int uniqId();
	/**
	 * Tells if the key is valid or not. A non valid task avoids the use of <code>null</code> magic value.
	 * @return
	 */
	boolean isValid();
}