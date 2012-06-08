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
package tests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.util.FileUtils;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.utils.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very different place from {@link GitUtils} for git utilities ; to be used by unit tests
 * @author ptitfred
 *
 */
public final class GitTestsUtils {

	protected static final Logger log = LoggerFactory.getLogger(GitTestsUtils.class);
	
	public static Repository inflate(File archive, File where) throws Exception {
		assert archive.exists();
		assert archive.isFile();
		assert archive.canRead();
		assert where.exists();
		assert where.isDirectory();
		assert where.canWrite();
		
		if (where.list().length > 0) {
			FileUtils.delete(where, FileUtils.RECURSIVE);
			if (!where.exists()) {
				where.mkdir();
			} else if (where.list().length > 0) {
				throw new Exception("Couldn't clean up !");
			}
		}
		
		final int BUFFER = 2048;
		try {
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(archive)));
	
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				File output = new File(where, entry.getName());
				output.getParentFile().mkdirs();
				if (!entry.isDirectory()) {

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
			}
	
			zin.close();
	
		} catch (IOException e) {
			log.error("Error while inflating repository from " + archive.getPath(), e);
		}
		try {
			return Git.open(where).getRepository();
		} catch (IOException e) {
			log.error("Error while opening repository " + where.getPath(), e);
		}
		return null;
	}

	public static Repository open(Context context, String repoPath) throws IOException {
		RepositoryBuilder builder = new RepositoryBuilder();
		builder.setGitDir(new File(context.configuration().getRepositoriesHomeDir(), repoPath));
		return builder.build();
	}
	
}
