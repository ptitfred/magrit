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
package org.kercoin.magrit.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tests.FilesUtils;

public class GitUtilsTest {

	GitUtils gitUtils;
	
	static Repository test;
	static Repository clone;
	
	@BeforeClass
	public static void setUpClass() {
		File where = new File("target/tmp/repos/test");
		where.mkdirs();
		
		File clonePath = new File("target/tmp/repos/clone");
		clonePath.mkdirs();
		Git.init().setDirectory(clonePath).call();
		
		try {
			test = tests.GitTestsUtils.inflate(new File(GitUtilsTest.class.getClassLoader().getResource("archives/test1.zip").toURI()), where);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		gitUtils = new GitUtils();
		clone = Git.open(new File("target/tmp/repos/clone")).getRepository();
	}

	@Test
	public void testGetCommit() throws Exception {
		assertThat(gitUtils.getCommit(test, "HEAD").getFullMessage()).isEqualTo("C3\n");
		assertThat(gitUtils.getCommit(test, "HEAD^").getFullMessage()).isEqualTo("C2\n");
		assertThat(gitUtils.getCommit(test, "46568aa0ac7a12c9e4fa9194b9e2a65d4a132a2c").getFullMessage()).isEqualTo("C1\n");
	}
	
	@Test
	public void testGetCommit_notFound() throws Exception {
		assertThat(gitUtils.getCommit(test, "46568aa0ac7a12c9e4fa9194b9e2a65d4a132a2c^")).isNull();
	}

	@Test
	public void testShow() throws Exception {
		assertThat(gitUtils.show(test, "HEAD:README")).isEqualTo("Readme\n");
		assertThat(gitUtils.show(test, "HEAD^:README")).isEmpty();
		assertThat(gitUtils.show(test, "HEAD:unknown")).isNull();
	}
	
	@Test
	public void testAddRemote() throws Exception {
		gitUtils.addRemote(clone, "copy", test);
		Git.wrap(clone).fetch().setRemote("copy").call();
		assertThat(FilesUtils.tail(new File(clone.getWorkTree(), ".git/config"), 3)).isEqualTo(
				"[remote \"copy\"]\n" +
			    "\tfetch = +refs/heads/*:refs/remotes/copy/*\n" +
			    "\turl = "+test.getDirectory().getParentFile().getAbsolutePath()+"\n"
			    );
	}
	
}
