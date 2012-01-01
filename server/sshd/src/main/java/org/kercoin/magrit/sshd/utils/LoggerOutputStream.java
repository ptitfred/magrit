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
package org.kercoin.magrit.sshd.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.eclipse.jgit.util.io.NullOutputStream;

public class LoggerOutputStream extends java.io.OutputStream {
	private OutputStream delegate;
	private StringWriter logs = new StringWriter();

	public LoggerOutputStream() {
		this(NullOutputStream.INSTANCE);
	}
	
	public LoggerOutputStream(OutputStream delegate) {
		super();
		this.delegate = delegate;
	}

	public void close() throws IOException {
		delegate.close();
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void flush() throws IOException {
		delegate.flush();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public String toString() {
		return delegate.toString();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
		logs.write(new String(b), off, len);
	}

	public void write(byte[] b) throws IOException {
		delegate.write(b);
		logs.write(new String(b));
	}

	public void write(int b) throws IOException {
		delegate.write(b);
		logs.write(new String(new byte[] { (byte) b } ));
	}

	public String getData() {
		return logs.getBuffer().toString();
	}
}