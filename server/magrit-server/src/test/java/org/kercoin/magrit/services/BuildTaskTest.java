package org.kercoin.magrit.services;


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jgit.api.AddNoteCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.utils.CommitterIdentity;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.Pair;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

public class BuildTaskTest {

	private static final String SHA1 = "12345";

	BuildTask buildTask;
	
	@Mock GitUtils gitUtils;
	CommitterIdentity committerIdentity;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Repository remote;
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	Repository buildRepo;

	@Mock ObjectInserter myInserter;
	
	byte[] myLogs;

	ObjectId logObjectId;
	ObjectId blobObjectId;
	
	@Mock TimeService timeService;

	@Mock Git gitWrapper;
	@Mock AddNoteCommand addNoteCommand;

	@Mock RevCommit revCommit;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		committerIdentity = new CommitterIdentity("user@example.org", "Mister Example");
		Pair<Repository, String> target = new Pair<Repository, String>(buildRepo, SHA1);
		buildTask = Mockito.spy(new BuildTask(gitUtils, committerIdentity, timeService, remote, target));
		given(buildTask.wrap(remote)).willReturn(gitWrapper);
		given(gitWrapper.notesAdd()).willReturn(addNoteCommand);
		given(addNoteCommand.setObjectId(Matchers.isA(RevObject.class))).willReturn(addNoteCommand);
		given(addNoteCommand.setMessage(Matchers.anyString())).willReturn(addNoteCommand);
		given(remote.getObjectDatabase().newInserter()).willReturn(myInserter);
		given(gitUtils.getCommit(remote, SHA1)).willReturn(revCommit);
		
		myLogs = "Oh. Yeah.".getBytes("UTF-8");
	
		logObjectId  = ObjectId.fromString("c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3");
		blobObjectId = ObjectId.fromString("d3adc0ded3adc0ded3adc0ded3adc0ded3adc0de");
	}

	@Test
	public void testWriteToRepository_nominal() throws Exception {
		// given
		BuildResult buildResult = new BuildResult(SHA1);
		buildResult.setStartDate(date(2011, Calendar.SEPTEMBER, 7, 1, 18, 35));
		buildResult.setExitCode(3);
		buildResult.setLog(myLogs);
		given(myInserter.insert(eq(Constants.OBJ_BLOB), Matchers.isA(byte[].class)))
			.willReturn(logObjectId)
			.willReturn(blobObjectId);
		given(timeService.now()).willReturn(new Pair<Long, Integer>(123450000000L, 120));
		given(timeService.offsetToString(120)).willReturn("+0200");

		// when
		buildTask.writeToRepository(buildResult);
		
		// then
		ArgumentCaptor<byte[]> blobCaptor = ArgumentCaptor.<byte[]>forClass(byte[].class);
		verify(myInserter, Mockito.times(2)).insert(eq(Constants.OBJ_BLOB), blobCaptor.capture());
		assertThat(new String(blobCaptor.getAllValues().get(0), "UTF-8")).isEqualTo("Oh. Yeah.");
		assertThat(new String(blobCaptor.getAllValues().get(1), "UTF-8")).isEqualTo( //
				"build 12345" + "\n" + //
				"log c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3" + "\n" + //
				"return-code 3" + "\n" + //
				"author \"Mister Example\" <user@example.org>" + "\n" + //
				"when 123450000000 +0200\n" //
				);
		verify(addNoteCommand).setObjectId(revCommit);
		verify(addNoteCommand).setMessage("magrit:built-by " + blobObjectId.name());
		verify(addNoteCommand).call();
		verify(myInserter).flush();
		verify(myInserter).release();
	}
	
	@Test
	public void testWriteToRepository_exception() throws Exception {
		// given
		BuildResult buildResult = new BuildResult(SHA1);
		buildResult.setStartDate(date(2011, Calendar.SEPTEMBER, 7, 1, 18, 35));
		buildResult.setExitCode(3);
		buildResult.setLog(myLogs);
		given(myInserter.insert(eq(Constants.OBJ_BLOB), Matchers.isA(byte[].class)))
			.willThrow(new IOException());
		given(timeService.now()).willReturn(new Pair<Long, Integer>(123450000000L, 120));
		given(timeService.offsetToString(120)).willReturn("+0200");
		
		// when
		buildTask.writeToRepository(buildResult);
		
		// then
		verify(myInserter, Mockito.never()).flush();
		verify(myInserter).release();
	}
	
	@SuppressWarnings("deprecation")
	private Date date(int year, int month, int date, int hrs, int min, int sec) {
		return new Date(year, month, date, hrs, min, sec);
	}

}
