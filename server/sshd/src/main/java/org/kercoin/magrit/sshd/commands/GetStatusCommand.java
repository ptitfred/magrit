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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.Status;
import org.kercoin.magrit.core.build.StatusesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;


public class GetStatusCommand extends AbstractCommand<GetStatusCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class GetStatusCommandProvider implements CommandProvider<GetStatusCommand> {

		private Context ctx;
		private StatusesService buildStatusesService;

		@Inject
		public GetStatusCommandProvider(Context ctx,
				StatusesService buildStatusesService) {
			super();
			this.ctx = ctx;
			this.buildStatusesService = buildStatusesService;
		}

		@Override
		public GetStatusCommand get() {
			return new GetStatusCommand(this.ctx, this.buildStatusesService);
		}

		@Override
		public boolean accept(String command) {
			return command.startsWith("magrit status ");
		}
		
	}
	
	private StatusesService buildStatusesService;
	private Repository repo;
	private String sha1;
	private BufferedReader stdin;
	
	public GetStatusCommand(Context ctx, StatusesService buildStatusesService) {
		super(ctx);
		this.buildStatusesService = buildStatusesService;
	}
	
	@Override
	public GetStatusCommand command(String command) throws IOException {
		parse(command);
		return this;
	}
	
	// magrit status /path/to/repo <sha1>
	void parse(String command) throws IOException {
		if (!command.startsWith("magrit status")) {
			throw new IllegalArgumentException("");
		}
		String[] parts = command.substring("magrit status".length() + 1).split(" ");
		
		if (parts.length != 2) {
			throw new IllegalArgumentException();
		}
		
		this.repo = createRepository(parts[0]);
		this.sha1 = parts[1];
	}

	@Override
	protected String getName() {
		return "GetStatus";
	}
	
	@Override
	protected Class<GetStatusCommand> getType() {
		return GetStatusCommand.class;
	}
	
	@Override
	public void setInputStream(InputStream in) {
		stdin = new BufferedReader(new InputStreamReader(in));
		super.setInputStream(in);
	}
	
	@Override
	public void run() {
		try {
			if ("-".equals(sha1)) {
				String line = null;
				while ((line = stdin.readLine()) != null) {
					if ("--".equals(line)) {
						break;
					}
					handle(line);
				}
			} else {
				handle(sha1);
			}
			callback.onExit(0);
		} catch (IOException e) {
			e.printStackTrace();
			callback.onExit(1);
		}
	}

	private void handle(String sha1) throws IOException {
		List<Status> statuses = buildStatusesService.getStatus(repo, sha1);
		for(Status status : statuses) {
			out.write(status.getCode());
		}
		out.write('\n');
		out.flush();		
	}

}
