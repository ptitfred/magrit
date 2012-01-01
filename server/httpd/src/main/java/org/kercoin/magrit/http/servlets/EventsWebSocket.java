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
package org.kercoin.magrit.http.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.build.BuildLifeCycleListener;
import org.kercoin.magrit.core.build.QueueService;
import org.kercoin.magrit.core.build.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author ptitfred
 *
 */
@Singleton
public class EventsWebSocket extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final QueueService queue;

	@Inject
	public EventsWebSocket(QueueService queue) {
		this.queue = queue;
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		log.info("Accepting websocket connection from " + request.getRemoteHost() + ":" + request.getRemotePort() + " with protocol " + protocol);
		return new QueueListenerWebSocket();
	}

	class QueueListenerWebSocket implements WebSocket.OnTextMessage, BuildLifeCycleListener {

		private Connection connection;

		@Override
		public void onOpen(Connection connection) {
			log.info("WS opening");
			this.connection = connection;
			EventsWebSocket.this.queue.addCallback(this);
		}

		@Override
		public void onMessage(String data) {
		}

		@Override
		public void onClose(int closeCode, String message) {
			log.info("WS closing");
			this.connection = null;
			EventsWebSocket.this.queue.removeCallback(this);
		}

		@Override
		public void buildScheduled(Repository repo, String sha1) {
			sendMsg("SCHEDULED " + sha1);
		}

		@Override
		public void buildStarted(Repository repo, String sha1) {
			sendMsg("START " + sha1);
		}

		@Override
		public void buildEnded(Repository repo, String sha1, Status status) {
			sendMsg("END " + sha1 + " " + status.getCode());
		}
		
		private void sendMsg(String message) {
			try {
				connection.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
