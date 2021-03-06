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

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Callable;


public interface Task<E> extends Callable<E> {
	Key getKey();
	void setKey(Key k);
	Date getSubmitDate();
	void setSubmitDate(Date d);
	CriticalResource getUnderlyingResource();
	InputStream openStdout();
}