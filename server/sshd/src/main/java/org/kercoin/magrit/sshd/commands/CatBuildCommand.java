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
package org.kercoin.magrit.sshd.commands;

import java.io.PrintStream;
import java.util.Scanner;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.BuildResult;
import org.kercoin.magrit.core.dao.BuildDAO;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class CatBuildCommand extends AbstractCommand<CatBuildCommand> {

	@Singleton
	public static class CatBuildCommandProvider implements CommandProvider<CatBuildCommand> {

		private final Context ctx;
		private final BuildDAO dao;

		@Inject
		public CatBuildCommandProvider(Context ctx,
				BuildDAO dao) {
			super();
			this.ctx = ctx;
			this.dao = dao;
		}

		@Override
		public CatBuildCommand get() {
			return new CatBuildCommand(ctx, dao);
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit cat-build ");
		}
		
	}
	
	private final BuildDAO dao;
	
	private String sha1;
	private Repository repository;

	public CatBuildCommand(Context ctx, BuildDAO dao) {
		super(ctx);
		this.dao = dao;
	}

	@Override
	protected Class<CatBuildCommand> getType() { return CatBuildCommand.class; }

	String getSha1() {
		return sha1;
	}
	
	Repository getRepository() {
		return repository;
	}
	
	@Override
	public CatBuildCommand command(String command) throws Exception {
		// magrit cat-build SHA1
		Scanner s = new Scanner(command);
		s.useDelimiter("\\s{1,}");
		check(s.next(), "magrit");
		check(s.next(), "cat-build");
		check(command, s.hasNext());
		this.repository = createRepository(s.next());
		check(command, s.hasNext());
		checkSha1(sha1 = s.next());
		
		return this;
	}
	
	@Override
	public void run() {
		PrintStream pOut =null;
		try {
			BuildResult last = dao.getLast(repository, sha1);
			pOut = new PrintStream(out);
			if (last != null) {
				out.write(last.getLog());
			} else {
				pOut.println("No log found for this commit.");
			}
			out.flush();
			callback.onExit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			callback.onExit(1, e.getMessage());
		} finally {
			if (pOut != null) {
				pOut.close();
			}
		}
	}

	void setSha1(String sha1) {
		this.sha1 = sha1;
		
	}

	void setRepository(Repository repo) {
		this.repository = repo;
	}

}
