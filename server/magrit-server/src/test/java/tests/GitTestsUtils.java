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
import org.eclipse.jgit.util.FileUtils;
import org.kercoin.magrit.utils.GitUtils;

/**
 * A very different place from {@link GitUtils} for git utilities ; to be used by unit tests
 * @author ptitfred
 *
 */
public final class GitTestsUtils {

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
	
}
