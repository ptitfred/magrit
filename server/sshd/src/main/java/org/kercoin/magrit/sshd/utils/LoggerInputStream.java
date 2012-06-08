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
package org.kercoin.magrit.sshd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class LoggerInputStream extends InputStream {
	
	private InputStream delegate;
	private StringWriter logs = new StringWriter();
	
	public LoggerInputStream(InputStream delegate) {
		super();
		this.delegate = delegate;
	}
	public String getData() {
		return logs.getBuffer().toString();
	}
	public int available() throws IOException {
		return delegate.available();
	}
	public void close() throws IOException {
		delegate.close();
	}
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}
	public int hashCode() {
		return delegate.hashCode();
	}
	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}
	public boolean markSupported() {
		return delegate.markSupported();
	}
	public int read() throws IOException {
		int read = delegate.read();
		logs.write(read);
		return read;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int read = delegate.read(b, off, len);
		if (read > 0) {
			logs.write(new String(b), off, read);
		}
		return read;
	}
	public int read(byte[] b) throws IOException {
		int read = delegate.read(b);
		if (read > 0) {
			logs.write(new String(b), 0, read);
		}
		return read;
	}
	public void reset() throws IOException {
		delegate.reset();
		logs = new StringWriter();
	}
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}
	public String toString() {
		return delegate.toString();
	}
	
}