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
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketByteListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

/**
 * @author ptitfred
 *
 */
public class EventsListener {

	protected static final Logger log = LoggerFactory.getLogger(EventsListener.class);

	public static void main(String[] args) throws Exception {
		log.info("WebSocket test client");
		new EventsListener(getUrl(args)).start();
		System.exit(0);
	}
	
	private static String getUrl(String[] args) {
		if (args.length > 0) {
			return args[0];
		}
		return "ws://localhost:2080/events";
	}
	
	private final String url;
	private final WebSocketUpgradeHandler handler;

	private class Listener implements WebSocketByteListener {
		public void onOpen(WebSocket websocket) {
			log.info("WebSocket open");
		}
		
		public void onClose(WebSocket websocket) {
			log.info("WebSocket closing...");
			synchronized (EventsListener.this) {
				EventsListener.this.notifyAll();
			}
			log.info("WebSocket closed");
		}
		
		public void onError(Throwable t) {
			log.error("WebSocket received an error", t);
		}
		
		public void onMessage(byte[] message) {
			log.info("message: " + new String(message));
		}
		
		public void onFragment(byte[] fragment, boolean last) {
			log.info("fragment: " + (last ? "(last)" : "") + new String(fragment));
		}
	}
	
	private EventsListener(String url) {
		log.info("On URL: " + url);
		this.url = url;
		this.handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(new Listener()).build();
	}
	
	private void start() throws InterruptedException, ExecutionException, IOException {
		AsyncHttpClient c = new AsyncHttpClient();
		log.info("Starting");
		@SuppressWarnings("unused")
		WebSocket socket = c.prepareGet(url).execute(handler).get();
		synchronized (this) {
			wait();
			log.info("We're done");
		}
	}

}
