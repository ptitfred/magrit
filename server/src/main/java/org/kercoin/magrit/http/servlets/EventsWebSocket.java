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
import org.kercoin.magrit.services.builds.BuildLifeCycleListener;
import org.kercoin.magrit.services.builds.QueueService;
import org.kercoin.magrit.services.builds.Status;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author ptitfred
 *
 */
@Singleton
public class EventsWebSocket extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	private final QueueService queue;

	@Inject
	public EventsWebSocket(QueueService queue) {
		this.queue = queue;
	}
	
	@Override
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new QueueListenerWebSocket();
	}

	class QueueListenerWebSocket implements WebSocket, BuildLifeCycleListener {

		private Outbound out;

		@Override
		public void onConnect(Outbound outbound) {
			this.out = outbound;
			EventsWebSocket.this.queue.addCallback(this);
		}

		@Override
		public void onMessage(byte frame, String data) {
		}

		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
		}

		@Override
		public void onDisconnect() {
			this.out = null;
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
				out.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
