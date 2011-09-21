package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jgit.util.FileUtils;

public class FilesUtils {

	public static String tail(File file, int lines) {
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
	
	public static String readFile(File file) throws FileNotFoundException {
		return readStream(new FileInputStream(file));
	}
	
	public static String readStream(InputStream in) {
		StringBuffer sb = new StringBuffer();
		String buffer = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			while ((buffer = br.readLine())!=null) {
				sb.append(buffer).append('\n');
			}
		} catch (IOException e) {
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {}
			}
		}
		return sb.toString();
	}

	public static void recursiveDelete(File path) {
		try {
			FileUtils.delete(path, FileUtils.RECURSIVE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
