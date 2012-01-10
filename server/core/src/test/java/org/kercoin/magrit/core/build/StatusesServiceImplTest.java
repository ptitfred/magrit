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
import static org.mockito.MockitoAnnotations.initMocks;

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
import org.kercoin.magrit.core.build.pipeline.Filters;
import org.kercoin.magrit.core.build.pipeline.Key;
import org.kercoin.magrit.core.build.pipeline.Pipeline;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.GitUtilsTest;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tests.GuiceModulesHolder;

@RunWith(MockitoJUnitRunner.class)
public class StatusesServiceImplTest {

	StatusesServiceImpl service;

	BuildDAO dao;

	@Mock Pipeline pipeline;

	@Mock(answer=Answers.RETURNS_DEEP_STUBS) Key key;
	
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
	
	@Before
	public void setup() {
		initMocks(this);
		dao = GuiceModulesHolder.MAGRIT_MODULE.getInstance(BuildDAO.class);
		service = new StatusesServiceImpl(new GitUtils(), dao, pipeline);
	}
	
	@Test
	public void testGetStatus_withBuilds() {
		assertThat(service.getStatus(test, "4eca6198233ff74ed225744c8f325bfb074c9f29")).containsExactly(Status.OK);
		assertThat(service.getStatus(test, "7dec669f77b48e6420785e523e71e461ba58dc72")).containsExactly(Status.OK, Status.ERROR);
	}

	@Test
	public void testGetStatus_withBuilds_running() {
		givenRunning("7dec669f77b48e6420785e523e71e461ba58dc72");
		assertThat(service.getStatus(test, "7dec669f77b48e6420785e523e71e461ba58dc72")).containsExactly(Status.OK, Status.ERROR, Status.RUNNING);
	}

	@Test
	public void testGetStatus_withBuilds_pending() {
		givenPending("7dec669f77b48e6420785e523e71e461ba58dc72");
		assertThat(service.getStatus(test, "7dec669f77b48e6420785e523e71e461ba58dc72")).containsExactly(Status.OK, Status.ERROR, Status.PENDING);
	}

	private void givenRunning(String sha1) {
		given(pipeline.list(Filters.running())).willReturn(Arrays.asList(key));
		given(task.getTarget()).willReturn(new Pair<Repository, String>(null, sha1));
		given(pipeline.get(key)).willReturn(task);
	}

	private void givenPending(String sha1) {
		given(pipeline.list(Filters.pending())).willReturn(Arrays.asList(key));
		given(task.getTarget()).willReturn(new Pair<Repository, String>(null, sha1));
		given(pipeline.get(key)).willReturn(task);
	}

	@Test
	public void testGetStatus_unknown() {
		assertThat(service.getStatus(test, "1111111111111111111111111111111111111111")).containsExactly(Status.UNKNOWN);
	}

	@Mock BuildTask task;

	@Test
	public void testGetStatus_new_running() {
		givenRunning("ac61d48e3020f61c4d7a19709494f37065c87d34");
		assertThat(service.getStatus(test, "ac61d48e3020f61c4d7a19709494f37065c87d34")).containsExactly(Status.RUNNING);
	}

	@Test
	public void testGetStatus_new_pending() {
		givenPending("ac61d48e3020f61c4d7a19709494f37065c87d34");
		assertThat(service.getStatus(test, "ac61d48e3020f61c4d7a19709494f37065c87d34")).containsExactly(Status.PENDING);
	}

	@Test
	public void testGetStatus_withoutBuild() {
		assertThat(service.getStatus(test, "ac61d48e3020f61c4d7a19709494f37065c87d34")).containsExactly(Status.NEW);
	}

	@Before
	public void check() {
		if (test == null) {
		    Assert.fail("Repository not loaded, can't test");
		}
	}

}
