package org.kercoin.magrit.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.kercoin.magrit.services.BuildStatus;
import org.kercoin.magrit.services.BuildStatusesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


public class GetStatusCommand extends AbstractCommand<GetStatusCommand> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class GetStatusCommandProvider implements Provider<GetStatusCommand> {

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
	public void run() {
		// TODO read status in the follow-up database
		try {
			RevCommit commit = getCommit(repo, sha1);
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
