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
package org.kercoin.magrit.core.build;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.File;
import java.util.Arrays;

import junit.framework.Assert;

import org.eclipse.jgit.lib.Repository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.pipeline.Filter;
import org.kercoin.magrit.core.build.pipeline.Filters;
import org.kercoin.magrit.core.build.pipeline.Key;
import org.kercoin.magrit.core.build.pipeline.Pipeline;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.GitUtilsTest;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import tests.GuiceModulesHolder;

@RunWith(MockitoJUnitRunner.class)
public class StatusesServiceImplTest {

	StatusesServiceImpl service;

	BuildDAO dao;

	@Mock Pipeline pipeline;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS) Key key;
	
	@Mock BuildTask task;
	@Spy GitUtils gitUtils = new GitUtils();

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

	private static final String C1 = "4eca6198233ff74ed225744c8f325bfb074c9f29";
	private static final String C2 = "7dec669f77b48e6420785e523e71e461ba58dc72";
	private static final String C3 = "ac61d48e3020f61c4d7a19709494f37065c87d34";
	private static final String T1 = "3d7c9869ac09462450ba3256dcba99692abf144f";
	private static final String T2 = "eff372311c487bc09a6d94c7b82aa34263f405fc";

	/**
	 * <pre>
	 * C1 --<-- C2
	 *   \        \
	 *    ----<--- C3
	 *
	 * C1 has tree T1.
	 * C3 is a trivial commit merge from C2 into C1
	 *   => C3 and C2 share the same tree T2.
	 * </pre>
	 */
	@Before
	public void fakeDb() {
		givenCommitHasTree(C1, T1);
		givenCommitHasTree(C2, T2);
		givenCommitHasTree(C3, T2);
	}

	@Before
	public void setup() {
		dao = GuiceModulesHolder.MAGRIT_MODULE.getInstance(BuildDAO.class);
		service = new StatusesServiceImpl(gitUtils, dao, pipeline);
	}

	@Before
	public void check() {
		if (test == null) {
		    Assert.fail("Repository not loaded, can't test");
		}
	}

	@Test
	public void testGetStatus_withBuilds() {
		assertThat(service.getStatus(test, C1)).containsExactly(Status.OK);
		assertThat(service.getStatus(test, C2)).containsExactly(Status.OK, Status.ERROR);
	}

	@Test
	public void testGetStatus_withBuilds_running() {
		givenRunning(C2);
		assertThat(service.getStatus(test, C2)).containsExactly(Status.OK, Status.ERROR, Status.RUNNING);
	}

	@Test
	public void testGetStatus_withBuilds_pending() {
		givenPending(C2);
		assertThat(service.getStatus(test, C2)).containsExactly(Status.OK, Status.ERROR, Status.PENDING);
	}

	@Test
	public void testGetStatus_unknown() {
		assertThat(service.getStatus(test, "1111111111111111111111111111111111111111")).containsExactly(Status.UNKNOWN);
	}

	@Test
	public void testGetStatus_new_running() {
		givenRunning(C3);
		givenContainsCommit(test, C3);
		assertThat(service.getStatus(test, C3)).containsExactly(Status.RUNNING);
	}

	@Test
	public void testGetStatus_new_pending() {
		givenPending(C2);
		givenContainsCommit(test, C3);
		assertThat(service.getStatus(test, C3)).containsExactly(Status.PENDING);
		assertThat(service.getStatus(test, C2)).containsExactly(Status.OK, Status.ERROR, Status.PENDING);
	}

	@Test
	public void testGetStatus_withoutBuild() {
		assertThat(service.getStatus(test, "ac61d48e3020f61c4d7a19709494f37065c87d34")).containsExactly(Status.NEW);
	}

	private void givenRunning(String sha1) {
		givenInPipeline(Filters.running(), sha1);
	}

	private void givenPending(String sha1) {
		givenInPipeline(Filters.pending(), sha1);
	}

	private void givenInPipeline(Filter filter, String sha1) {
		given(pipeline.list(filter)).willReturn(Arrays.asList(key));
		given(task.getTarget()).willReturn(new Pair<Repository, String>(null, sha1));
		given(pipeline.get(key)).willReturn(task);
	}

	private void givenContainsCommit(Repository repository, String sha1Commit) {
		Mockito.doReturn(true).when(gitUtils).containsCommit(repository, sha1Commit);
	}

	private void givenCommitHasTree(String commitSha1, String treeSha1) {
		Mockito.doReturn(treeSha1).when(gitUtils).getTree(any(Repository.class), eq(commitSha1));
	}

}
