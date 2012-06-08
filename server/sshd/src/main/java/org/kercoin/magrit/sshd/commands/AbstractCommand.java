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
package org.kercoin.magrit.sshd.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.sshd.utils.LoggerInputStream;
import org.kercoin.magrit.sshd.utils.LoggerOutputStream;

import com.google.inject.Provider;

public abstract class AbstractCommand<C extends AbstractCommand<C>> implements Command, Runnable {

	public static interface CommandProvider<C extends AbstractCommand<C>> extends Provider<C> {
		boolean accept(String command);
	}
	
	protected InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	protected ExitCallback callback;
	protected final Context ctx;
	protected final GitUtils gitUtils;
	protected Environment env;

	public AbstractCommand(Context ctx) {
		this.ctx = ctx;
		this.gitUtils = ctx.getGitUtils();
	}
	
	public abstract C command(String command) throws Exception;
	
	protected Repository createRepository(String repoPath) throws IOException {
		return gitUtils.createRepository(
				new File(	ctx.configuration().getRepositoriesHomeDir(),
							repoPath
						 )
			);
	}
	
	boolean logStreams = false;
	
	@Override
	public void setInputStream(InputStream in) {
		if (logStreams && !(in instanceof LoggerInputStream)) {
			this.in = new LoggerInputStream(in);
		} else {
			this.in = in;
		}
	}
	
	protected InputStream getInputStream() {
		return this.in;
	}
	
	@Override
	public void setOutputStream(OutputStream out) {
		if (logStreams && !(out instanceof LoggerOutputStream)) {
			this.out = new LoggerOutputStream(out);
		} else {
			this.out = out;
		}
	}

	protected OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		if (logStreams && !(err instanceof LoggerOutputStream)) {
			this.err = new LoggerOutputStream(err);
		} else {
			this.err = err;
		}
	}
	
	protected OutputStream getErrorStream() {
		return err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		this.env = env;
		ctx.getCommandRunnerPool().execute(this);
	}
	
	protected String getName() {
		return getType().getSimpleName();
	}
	
	public static interface EndCallback<C> {
		void onEnd(C command);
	}
	
	private Set<EndCallback<C>> callbacks = new HashSet<EndCallback<C>>();
	
	public void addEndCallback(EndCallback<C> callback) {
		callbacks.add(callback);
	}

	@Override
	public void destroy() {
		this.out = null;
		this.in = null;
		this.err = null;
		this.callback = null;
		if (this.callbacks != null && this.callbacks.size()>0) {
			for(EndCallback<C> endCallback : this.callbacks) {
				endCallback.onEnd(getType().cast(this));
			}
		}
		this.callbacks = null;
	}
	
	protected abstract Class<C> getType();

	protected void checkSha1(String sha1) {
		if (!gitUtils.isSha1(sha1)) {
			throw new IllegalArgumentException(String.format("%s isn't a valid 40 bytes SHA1", sha1));
		}
	}

	protected void check(String command, boolean hasNext) {
		if (!hasNext) {
			throw new IllegalArgumentException(String.format("Too few arguments for command %s to be executed", command));
		}
	}

	protected void check(String tested, String ref)
			throws IllegalArgumentException {
				if (!ref.equals(tested)) {
					throw new IllegalArgumentException(String.format("Expected %s but was %s", ref, tested));
				}
			}

}
