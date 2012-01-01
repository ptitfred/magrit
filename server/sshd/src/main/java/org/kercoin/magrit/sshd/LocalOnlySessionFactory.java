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
package org.kercoin.magrit.sshd;

import org.apache.mina.core.session.IoSession;
import org.apache.sshd.common.session.AbstractSession;
import org.apache.sshd.server.session.SessionFactory;

class LocalOnlySessionFactory extends SessionFactory {

	@Override
	protected AbstractSession createSession(IoSession ioSession)
			throws Exception {
		ioSession.getFilterChain().addFirst("localOnly", new LocalOnlyFilter());
		return super.createSession(ioSession);
	}
}