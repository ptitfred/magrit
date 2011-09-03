package org.kercoin.magrit.git;

import java.io.IOException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.services.BuildStatusesService;


public class GetStatusCommand extends AbstractCommand {

	private BuildStatusesService buildStatusesService;
	private Repository repo;
	private String sha1;
	
	public GetStatusCommand(Context ctx, String command, BuildStatusesService buildStatusesService) throws IOException {
		super(ctx);
		this.buildStatusesService = buildStatusesService;
		parse(command);
	}
	
	// magrit status /path/to/repo <sha1>
	void parse(String command) throws IOException {
		if (!command.startsWith("magrit status")) {
			throw new IllegalArgumentException("");
		}
		String[] parts = command.substring(15).split(" ");
		
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
