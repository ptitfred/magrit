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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ptitfred
 *
 */
class Notifier {

	private Set<Listener> listeners = new HashSet<Listener>();
	private ExecutorService edt;

	Notifier() {
		edt = Executors.newSingleThreadExecutor();
	}

	void justStarted(Key k) {
		fire(EventType.STARTED, k);
	}
	void justEnded(Key k) {
		fire(EventType.ENDED, k);
	}
	void justSubmitted(Key k) {
		fire(EventType.SUBMITTED, k);
	}

	void fire(EventType t, Key k) {
		edt.execute(new Event(t, k));
	}

	enum EventType {
		STARTED, ENDED, SUBMITTED
	}

	class Event implements Runnable {

		private EventType type;
		private Key key;

		public Event(EventType type, Key key) {
			super();
			this.type = type;
			this.key = key;
		}

		@Override
		public void run() {
			synchronized (listeners) {
				for (Listener l : listeners) {
					switch (type) {
					case STARTED:
						l.onStart(key);
						break;
					case ENDED:
						l.onDone(key);
						break;
					case SUBMITTED:
						l.onSubmit(key);
						break;
					default:
						throw new IllegalStateException("Unknown EventType");
					}
				}
			}
		}

	}

	void addListener(Listener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return edt.awaitTermination(timeout, unit);
	}
}
