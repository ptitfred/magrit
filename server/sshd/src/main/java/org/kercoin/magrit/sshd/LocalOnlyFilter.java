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
package org.kercoin.magrit.sshd;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocalOnlyFilter extends IoFilterAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session)
			throws Exception {
		InetAddress remote = ((InetSocketAddress)session.getRemoteAddress()).getAddress();
		if (!remote.isLoopbackAddress()) {
			// Remote access while it's forbidden
			log.warn("FORBIDDEN - Attempt to connect from {}", remote.getHostAddress());
			session.close(true);
		} else {
			super.sessionOpened(nextFilter, session);
		}
	}

}
