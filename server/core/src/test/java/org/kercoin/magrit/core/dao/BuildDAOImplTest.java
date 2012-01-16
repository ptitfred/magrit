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
package org.kercoin.magrit.core.dao;


import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.BuildResult;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.GitUtilsTest;
import org.kercoin.magrit.core.utils.TimeService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildDAOImplTest {

	static Repository test;
	
	@BeforeClass
	public static void setUpClass() {
		File where = new File("target/tmp/repos/test2");
		where.mkdirs();

		try {
			test = tests.GitTestsUtils.inflate(new File(GitUtilsTest.class.getClassLoader().getResource("archives/test2.zip").toURI()), where);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDownClass() {
		if (test != null) {
			test.close();
			tests.FilesUtils.recursiveDelete(test.getDirectory().getParentFile());
			test = null;
		}
	}

	private static final String SHA1 = "12345";

	BuildDAOImpl dao;
	
	@Mock Note note;

	@Mock Repository repo;

	@Mock GitUtils gitUtils;
	@Mock TimeService time;
	
	UserIdentity misterExample = new UserIdentity("user@example.org", "Mister Example");
	
	@Before
	public void setUp() throws Exception {
		dao = new BuildDAOImpl(gitUtils, time);
	}

	@Before
	public void check() {
		if (test == null) {
		    Assert.fail("Repository not loaded, can't test");
		}
	}

	@Test
	public void testGetLast() throws Exception {
		// given ---------------------------------
		dao = new BuildDAOImpl(new GitUtils(), time);

		// when ----------------------------------
		BuildResult result = dao.getLast(test, "7dec669f77b48e6420785e523e71e461ba58dc72");

		// then ----------------------------------
		assertThat(result).isNotNull();
		assertThat(result.getExitCode()).isEqualTo(1);
	}
	
	@Test
	public void testGetAll() throws Exception {
		// given ---------------------------------
		dao = new BuildDAOImpl(new GitUtils(), time);

		// when ----------------------------------
		List<BuildResult> results = dao.getAll(test, "7dec669f77b48e6420785e523e71e461ba58dc72");

		// then ----------------------------------
		assertThat(results).hasSize(2);
		assertThat(results.get(0).getExitCode()).isEqualTo(0);
		assertThat(results.get(1).getExitCode()).isEqualTo(1);
	}
	
	@Test
	public void testParseNoteListing() throws Exception {
		// given ---------------------------------
		String data = "magrit:built-by 1234000000000000000000000000000000000000\n\nmagrit:built-by 5678000000000000000000000000000000000000\n";
		
		// when ----------------------------------
		List<String> notes = dao.parseNoteListing(data);

		// then ----------------------------------
		assertThat(notes)
			.isNotNull()
			.hasSize(2)
			.containsSequence("1234000000000000000000000000000000000000", "5678000000000000000000000000000000000000");
	}
	
	@Test
	public void testParseNote() throws Exception {
		// given ---------------------------------
		String data =
				"build 47b999bbdad3f33878f51b5a21cb71fda557324c\n" +
				"log e957a8b6b4997cc3321a7e18dd330ca7cbe8e679\n" +
				"return-code 38\n" +
				"author \"ptitfred\" <ptitfred@localhost>\n" +
				"when 1316391912464 +0200\n";
		String log= "row 1\nrow 2\n";
		given(gitUtils.showBytes(eq(repo), eq("e957a8b6b4997cc3321a7e18dd330ca7cbe8e679"))).willReturn(log.getBytes());
		
		// when ----------------------------------
		BuildResult buildResult = dao.parseNote(repo, "47b999bbdad3f33878f51b5a21cb71fda557324c", data);

		// then ----------------------------------
		assertThat(buildResult).isNotNull();
		assertThat(buildResult.getCommitSha1()).isEqualTo("47b999bbdad3f33878f51b5a21cb71fda557324c");
		assertThat(buildResult.getExitCode()).isEqualTo(38);
		assertThat(buildResult.getLog()).isEqualTo(log.getBytes());
	}

	@Test
	public void testSerializeBuildResult_nominal() throws Exception {
		// given
		final BuildResult buildResult = new BuildResult(SHA1);
		buildResult.setStartDate(date(2011, Calendar.SEPTEMBER, 7, 1, 18, 35));
		buildResult.setExitCode(3);
		buildResult.setLog("Oh. Yeah.".getBytes("UTF-8"));
		final Pair<Long, Integer> when = new Pair<Long, Integer>(123450000000L, 120);
		final String who = misterExample.toString();
		final ObjectId logs = ObjectId.fromString("c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3");
		given(time.offsetToString(120)).willReturn("+0200");

		// when
		String text = dao.serializeResult(buildResult, who, when, logs);

		// then
		assertThat(text).isEqualTo( //
				"build 12345" + "\n" + //
				"log c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3" + "\n" + //
				"return-code 3" + "\n" + //
				"author \"Mister Example\" <user@example.org>" + "\n" + //
				"when 123450000000 +0200\n" //
				);
	}

	@Test
	public void testSerializeNote() throws Exception {
		String text = dao.serializeBuildNote(ObjectId.fromString("c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3"));
		assertThat(text).isEqualTo("magrit:built-by c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3c4f3b4b3");
	}

	@Test
	public void testConcatenateNote() {
		assertThat(dao.concatenateNotes("blabla", "bloblo")).isEqualTo("blabla\n\nbloblo");
	}

	@SuppressWarnings("deprecation")
	private Date date(int year, int month, int date, int hrs, int min, int sec) {
		return new Date(year, month, date, hrs, min, sec);
	}

}
