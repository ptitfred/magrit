package org.kercoin.magrit.utils;

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