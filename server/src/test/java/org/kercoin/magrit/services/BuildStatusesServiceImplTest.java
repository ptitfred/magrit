package org.kercoin.magrit.services;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.jgit.lib.Repository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.GitUtilsTest;
import org.mockito.runners.MockitoJUnitRunner;

import tests.GuiceModulesHolder;

@RunWith(MockitoJUnitRunner.class)
public class BuildStatusesServiceImplTest {

	BuildStatusesServiceImpl service;

	BuildDAO dao;
	
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
		service = new BuildStatusesServiceImpl(new GitUtils(), dao);
	}
	
	@Test
	public void testGetStatus_withBuilds() {
		check();
		assertThat(service.getStatus(test, "c92788de607fa1375b05c8075814711c145d8dae")).containsExactly(BuildStatus.OK);
		assertThat(service.getStatus(test, "0c33eefaaf70e4ed3d0b65cba1d92ee62f2bd208")).containsExactly(BuildStatus.OK, BuildStatus.ERROR);
	}

	@Test
	public void testGetStatus_unknownCommit() {
		check();
		assertThat(service.getStatus(test, "1111111111111111111111111111111111111111")).containsExactly(BuildStatus.UNKNOWN);
	}

	@Test
	public void testGetStatus_withoutBuild() {
		check();
		assertThat(service.getStatus(test, "ab879396392ba0dd6b45160e5cc94213116fa041")).containsExactly(BuildStatus.NEW);
	}

	private void check() {
		if (test == null) {
		    Assert.fail("Repository not loaded, can't test");
		}
	}
}
