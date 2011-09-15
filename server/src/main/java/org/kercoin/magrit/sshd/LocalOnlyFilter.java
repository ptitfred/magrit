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
