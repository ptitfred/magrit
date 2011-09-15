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