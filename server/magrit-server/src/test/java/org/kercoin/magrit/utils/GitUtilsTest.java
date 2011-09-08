package org.kercoin.magrit.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GitUtilsTest {

	GitUtils gitUtils;
	
	static Repository inflate(File archive, File where) {
		assert archive.exists();
		assert archive.isFile();
		assert archive.canRead();
		assert where.exists();
		assert where.isDirectory();
		assert where.canWrite();
		assert where.list().length == 0;
		
		final int BUFFER = 2048;
		try {
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(archive)));

			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				File output = new File(where, entry.getName());
				output.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(
						output.getAbsolutePath()
						);
				BufferedOutputStream dest = new BufferedOutputStream(fos,
						BUFFER);

				int count;
				byte data[] = new byte[BUFFER];
				while ((count = zin.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
					
				}

				dest.flush();
				dest.close();
			}

			zin.close();

		} catch (IOException e) {
		}
		try {
			return Git.open(where).getRepository();
		} catch (IOException e) {
		}
		return null;
	}
	
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
			test = inflate(new File(GitUtilsTest.class.getClassLoader().getResource("archives/test1.zip").toURI()), where);
		} catch (URISyntaxException e) {
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
	
	@Test(expected=NullPointerException.class)
	public void testGetCommit_notFound() throws Exception {
		try {
		gitUtils.getCommit(test, "46568aa0ac7a12c9e4fa9194b9e2a65d4a132a2c^");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.fail("Should have thrown a NullPointerException");
	}

	@Test
	public void testShow() throws Exception {
		assertThat(gitUtils.show(test, "HEAD:README")).isEqualTo("Readme\n");
		assertThat(gitUtils.show(test, "HEAD^:README")).isEmpty();
	}
	
	@Test
	public void testAddRemote() throws Exception {
		gitUtils.addRemote(clone, "copy", test);
		Git.wrap(clone).fetch().setRemote("copy").call();
		assertThat(tail(new File(clone.getWorkTree(), ".git/config"), 3)).isEqualTo(
				"[remote \"copy\"]\n" +
			    "\tfetch = +refs/heads/*:refs/remotes/copy/*\n" +
			    "\turl = /home/ptitfred/git/magrit/server/magrit-server/target/tmp/repos/test\n"
			    );
	}
	
	static String tail(File file, int lines) {
		String[] linesBuffer = new String[lines];
		int pos = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String buffer;
			while ((buffer = br.readLine())!=null) {
				linesBuffer[pos++] = buffer;
				if (pos == lines) {
					pos=0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		StringBuffer accu = new StringBuffer();
		for (int i=pos; i<pos+lines; i++) {
			accu.append(linesBuffer[i%lines]).append('\n');
		}
		return accu.toString();
	}

}
