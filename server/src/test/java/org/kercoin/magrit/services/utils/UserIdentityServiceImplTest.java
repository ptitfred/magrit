package org.kercoin.magrit.services.utils;


import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.security.AccessControlException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.GitUtilsTest;
import org.kercoin.magrit.utils.UserIdentity;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserIdentityServiceImplTest {

	static Repository repo;
	
	static Repository empty;
	
	@BeforeClass
	public static void loadRepo() {
		{ // repo
			File where = new File("target/tmp/repos/test3");
			where.mkdirs();

			try {
				repo = tests.GitTestsUtils.inflate(new File(GitUtilsTest.class.getClassLoader().getResource("archives/test3.zip").toURI()), where);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		{ // empty
			File where = new File("target/tmp/repos/empty");
			where.mkdirs();

			try {
				empty = Git.init().setDirectory(where).setBare(false).call().getRepository();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public static void unloadRepo() {
		closeRepo(repo, empty);
	}

	private static void closeRepo(Repository... repos) {
		for (Repository repo : repos) {
			if (repo != null) {
				repo.close();
				tests.FilesUtils.recursiveDelete(repo.getDirectory().getParentFile());
				repo = null;
			}
		}
	}
	
	UserIdentityService service;
	UserIdentityService serviceNoDB;
	
	@Before
	public void setUp() throws Exception {
		service = new UserIdentityServiceImpl(createCtx(repo));
		serviceNoDB = new UserIdentityServiceImpl(createCtx(empty));
	}
	
	private Context createCtx(Repository dbRepo) {
		Context ctx = new Context(new GitUtils());
		ctx.configuration().setPublickeysRepositoryDir(dbRepo.getDirectory().getParentFile());
		return ctx;
	}

	@Test
	public void testFind_known() {
		// when ----------------------------------
		UserIdentity user = service.find("ptitfred");
		
		// then ----------------------------------
		assertThat(user.getEmail()).isEqualTo("frederic.doe@example.com");
		assertThat(user.getName()).isEqualTo("John Doe");
	}

	@Test
	public void testFind_unknown() {
		// when ----------------------------------
		UserIdentity user = service.find("john.carmack");
		
		// then ----------------------------------
		assertThat(user.getEmail()).isEqualTo("john.carmack@localhost");
		assertThat(user.getName()).isEqualTo("john.carmack");
	}

	@Test(expected=AccessControlException.class)
	public void testFind_missingDb() {
		// when ----------------------------------
		serviceNoDB.find("john.carmack");
	}

}
