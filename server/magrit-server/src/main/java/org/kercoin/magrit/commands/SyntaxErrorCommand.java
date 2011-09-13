package org.kercoin.magrit.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class SyntaxErrorCommand implements Command {

	private OutputStream err;
	private ExitCallback callback;
	private Exception exception;

	public SyntaxErrorCommand(Exception exception) {
		this.exception = exception;
	}
	
	public void setInputStream(InputStream in) {}
	public void setOutputStream(OutputStream out) {}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		PrintStream printErr = new PrintStream(err);
		printErr.println(exception.getMessage());
		printErr.flush();
		callback.onExit(1, exception.getMessage());
	}

	public void destroy() {}

}
