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
import org.kercoin.magrit.core.build.Pipeline.Key;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.model.Pair;
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
		check();
		assertThat(service.getStatus(test, "c92788de607fa1375b05c8075814711c145d8dae")).containsExactly(Status.OK);
		assertThat(service.getStatus(test, "0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208")).containsExactly(Status.OK, Status.ERROR);
	}

	@Test
	public void testGetStatus_withBuilds_running() {
		check();
		givenRunning("0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208");
		assertThat(service.getStatus(test, "0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208")).containsExactly(Status.OK, Status.ERROR, Status.RUNNING);
	}

	@Test
	public void testGetStatus_withBuilds_pending() {
		check();
		givenPending("0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208");
		assertThat(service.getStatus(test, "0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208")).containsExactly(Status.OK, Status.ERROR, Status.PENDING);
	}

	private void givenRunning(String sha1) {
		given(pipeline.list(PipelineImpl.running())).willReturn(Arrays.asList(key));
		given(task.getTarget()).willReturn(new Pair<Repository, String>(null, sha1));
		given(pipeline.get(key)).willReturn(task);
	}

	private void givenPending(String sha1) {
		given(pipeline.list(PipelineImpl.pending())).willReturn(Arrays.asList(key));
		given(task.getTarget()).willReturn(new Pair<Repository, String>(null, sha1));
		given(pipeline.get(key)).willReturn(task);
	}

	@Test
	public void testGetStatus_unknown() {
		check();
		assertThat(service.getStatus(test, "1111111111111111111111111111111111111111")).containsExactly(Status.UNKNOWN);
	}

	@Mock BuildTask task;

	@Test
	public void testGetStatus_new_running() {
		check();
		givenRunning("ab879396392ba0dd6b45160e5cc94213116fa041");
		assertThat(service.getStatus(test, "ab879396392ba0dd6b45160e5cc94213116fa041")).containsExactly(Status.RUNNING);
	}

	@Test
	public void testGetStatus_new_pending() {
		check();
		givenPending("ab879396392ba0dd6b45160e5cc94213116fa041");
		assertThat(service.getStatus(test, "ab879396392ba0dd6b45160e5cc94213116fa041")).containsExactly(Status.PENDING);
	}

	@Test
	public void testGetStatus_withoutBuild() {
		check();
		assertThat(service.getStatus(test, "ab879396392ba0dd6b45160e5cc94213116fa041")).containsExactly(Status.NEW);
	}

	private void check() {
		if (test == null) {
		    Assert.fail("Repository not loaded, can't test");
		}
	}
}
