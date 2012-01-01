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
package org.kercoin.magrit.core.build;


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jgit.api.AddNoteCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ShowNoteCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.model.Context;
import org.kercoin.magrit.core.model.Pair;
import org.kercoin.magrit.core.model.UserIdentity;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.TimeService;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildTaskTest {

	private static final String SHA1 = "12345";
	private static final String SHA2 = "67890";

	BuildTask buildTask;
	
	@Mock GitUtils gitUtils;

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
	@Mock ShowNoteCommand showNoteCommand;

	@Mock RevCommit revCommit;
	
	@Mock(answer=Answers.RETURNS_DEEP_STUBS) Note previousNote;
	@Mock ObjectId previousNoteId;

	@Mock RepositoryGuard guard;
	
	@Before
	public void setUp() throws Exception {
		Context context = new Context(gitUtils);
		UserIdentity committerIdentity = new UserIdentity("user@example.org", "Mister Example");
		Pair<Repository, String> target = new Pair<Repository, String>(buildRepo, SHA1);
		buildTask = Mockito.spy(new BuildTask(context, guard, committerIdentity, timeService, remote, target, SHA2));
		given(buildTask.wrap(remote)).willReturn(gitWrapper);
		given(gitWrapper.notesAdd()).willReturn(addNoteCommand);
		given(gitWrapper.notesShow()).willReturn(showNoteCommand);
		given(addNoteCommand.setObjectId(Matchers.isA(RevObject.class))).willReturn(addNoteCommand);
		given(addNoteCommand.setMessage(Matchers.anyString())).willReturn(addNoteCommand);
		given(showNoteCommand.setObjectId(isA(RevObject.class))).willReturn(showNoteCommand);
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
	public void testWriteToRepository_overwrite() throws Exception {
		// given ---------------------------------
		BuildResult buildResult = new BuildResult(SHA1);
		buildResult.setStartDate(date(2011, Calendar.SEPTEMBER, 7, 1, 18, 35));
		buildResult.setExitCode(3);
		buildResult.setLog(myLogs);
		given(myInserter.insert(eq(Constants.OBJ_BLOB), Matchers.isA(byte[].class)))
			.willReturn(logObjectId)
			.willReturn(blobObjectId);
		given(timeService.now()).willReturn(new Pair<Long, Integer>(123450000000L, 120));
		given(timeService.offsetToString(120)).willReturn("+0200");
		
		given(showNoteCommand.call()).willReturn(previousNote);
		ObjectId previousNoteId = ObjectId.fromString("9876543210987654321098765432109876543210");
		given(previousNote.getData()).willReturn(previousNoteId);
		given(gitUtils.show(remote, "9876543210987654321098765432109876543210")).willReturn("magrit:built-by previous");

		// when ----------------------------------
		buildTask.writeToRepository(buildResult);
		
		// then ----------------------------------
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
		verify(addNoteCommand).setMessage("magrit:built-by " + blobObjectId.name() + "\n\n" + "magrit:built-by previous");
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
