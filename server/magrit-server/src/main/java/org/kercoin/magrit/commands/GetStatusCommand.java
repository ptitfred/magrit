package org.kercoin.magrit.commands;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.services.BuildStatus;
import org.kercoin.magrit.services.BuildStatusesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;


public class GetStatusCommand extends AbstractCommand<GetStatusCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class GetStatusCommandProvider implements CommandProvider<GetStatusCommand> {

		private Context ctx;
		private BuildStatusesService buildStatusesService;

		@Inject
		public GetStatusCommandProvider(Context ctx,
				BuildStatusesService buildStatusesService) {
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
	
	private BuildStatusesService buildStatusesService;
	private Repository repo;
	private String sha1;
	
	public GetStatusCommand(Context ctx, BuildStatusesService buildStatusesService) {
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
	public void run() {
		List<BuildStatus> statuses = buildStatusesService.getStatus(repo, sha1);
		try {
			for(BuildStatus status : statuses) {
				out.write(status.getCode());
			}
			out.write('\n');
			out.flush();
			callback.onExit(0);
		} catch (IOException e) {
			e.printStackTrace();
			callback.onExit(1);
		}
	}

}
