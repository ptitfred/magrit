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
package org.kercoin.magrit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Installer {

	static enum Profile {
		QUICKTEST, INSTALL, UNKNOWN
	}

	private static final int BUFFER_SIZE = 2048;
	
	private Installer() {
		
	}
	
	private final File TEMP = new File(System.getProperty("java.io.tmpdir"));
	private final String FILE_SEP = System.getProperty("file.separator");
	
    private final Scanner in = new Scanner(System.in);
	
	private Profile profile = Profile.UNKNOWN;
	
	private String getVersion() {
		String file = "META-INF/maven/org.kercoin.magrit/magrit-installer/pom.properties";
		InputStream is = null;
		try {
			is = getClass().getClassLoader().getResourceAsStream(file);
			if (is == null) {
				return "illegal";
			}
			Properties props = new Properties();
			props.load(is);
			return props.getProperty("version");
		} catch (IOException e) {
			e.printStackTrace();
			return "unknown";
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) {}
			}
		}
		
	}
	
	private void go() {
		try {
			try {
				greetings();
			} catch (InterruptedException e) {}
			grabConfiguration();
			install();
		} catch(ExitException ee) {
			p(ee.getMessage());
		}
	}

	private void greetings() throws InterruptedException {
		banner("Welcome to the MAGRIT INSTALLER", "Version: " + getVersion());

		p("This will install Magrit in your system for a clean developer experience.");
		nl();
		
		p("Install Magrit will:");
		p(" - register some shell scripts in PATH");
		p(" - install an executable as a system service");
		p(" - configure this service");
		nl();
		
		p("HOWEVER");
		p("If you just want to give a try to Magrit, you can quick test it :-)");
		p("This will install Magrit and all working/temporary files in " + TEMP.getAbsolutePath() + FILE_SEP + "magrit-quick.");

		Thread.sleep(1000);
		nl();

		p("So...");
		Thread.sleep(1000);
		nl();
	}

	private void grabConfiguration() {
		int i = ask("Do you want to 1) TRY or to 2) INSTALL ? (1/2)", 1, 2);
		if (i == 1) {
			profile = Profile.QUICKTEST;
		} else if (i == 2) {
			profile = Profile.INSTALL;
		}
		nl();
	}
	
	private void install() {
		switch(profile) {
		case INSTALL:
			banner("INSTALL");
			throw new ExitException("Not yet available :-(");
		case QUICKTEST:
			banner("QUICKTEST setup");
			quicktestSetup();
			break;
		}
	}
	
	private void write(InputStream inputStream, File destDir, String name)
			throws FileNotFoundException, IOException {
		OutputStream outputStream = new FileOutputStream(new File(destDir, name));
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
				outputStream.write(buffer, 0, read);
			}
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	private void quicktestSetup() {
		try {
			final File magritDir = new File(TEMP, "magrit-quick");
			p("Setup sandbox: " + magritDir.getAbsolutePath());

			// mkdir -p /tmp/magrit-quick/{bares,builds,keys}
			String[] dirs = {"bares", "builds", "keys"};
			for (String path : dirs) {
				File dir = new File(magritDir, path);
				dir.mkdirs();
			}
			
			Properties p = new Properties();
			InputStream packageResource = getClass().getClassLoader().getResourceAsStream("META-INF/packs/content.properties");
			if (packageResource == null) throw new ExitException("Unable to read package content, aborting... :-/");
			p.load(packageResource);

			p("Installing server...");
			InputStream magritResource = getClass().getClassLoader().getResourceAsStream("META-INF/packs/" + p.getProperty("server.archive"));
			if (magritResource == null) throw new ExitException("The installer archive seems corrupted or incomplete, aborting... :-(");
			write(magritResource, magritDir, p.getProperty("server.archive"));
			
			p("Installing CLI...");
			InputStream scriptsResource = getClass().getClassLoader().getResourceAsStream("META-INF/packs/" + p.getProperty("shell-scripts.archive"));
			if (scriptsResource == null) throw new ExitException("The installer archive seems corrupted or incomplete, aborting... :-(");
			write(scriptsResource, magritDir, p.getProperty("shell-scripts.archive"));
			
			// unzip /tmp/magrit-quick/scripts.zip -d bin
			File scriptsDirectory = new File(magritDir, "scripts");
			inflate(new File(magritDir, p.getProperty("shell-scripts.archive")), scriptsDirectory);
			
			{ // Utility script to setup PATH
				PrintWriter pw = open(new File(magritDir, "setup.sh"));
				pw.println("chmod +x " + scriptsDirectory.getAbsolutePath() + "/magrit*");
				pw.println("export PATH=\"" + scriptsDirectory.getAbsolutePath() + System.getProperty("path.separator") + "$PATH\"");
				pw.println(". " + new File(scriptsDirectory.getAbsolutePath(), "/completion/magrit").getAbsolutePath() );
				pw.println();
				pw.close();
			}

			{ // Startup script for the server
				PrintWriter pw2 = open(new File(magritDir, "start.sh"));
				pw2.println("java -jar " + magritDir.getAbsolutePath() + "/" + p.getProperty("server.archive") + " -a none -s " + magritDir.getAbsolutePath());
				pw2.close();
			}
			
			p(Color.GREEN, "Quick install completed");
			nl();
			
			p("In order to use Magrit, please execute");
			p(Color.YELLOW, "  .  " + magritDir.getAbsolutePath() + "/setup.sh" );
			nl();
			p("Start the server: ");
			boolean isScreen = System.getenv("TERM").startsWith("screen");
			String startServerCmd = "sh " + magritDir.getAbsolutePath() + "/start.sh";
			if (isScreen) {
				p(Color.YELLOW, "  screen -t magrit-server " + startServerCmd);
			} else {
				p(Color.YELLOW, "  " + startServerCmd );
			}
			
		} catch (Exception e) {
			throw new ExitException(e.getMessage(), e);
		}
	}
	
	private static PrintWriter open(File setupScript) throws IOException {
		if (!setupScript.createNewFile()) {
			throw new ExitException("The finalization script couldn't have been created, aborting... :'(");
		}
		return new PrintWriter(new FileWriter(setupScript));
	}

	public static void inflate(File archive, File where) {
		if (where.exists() && !where.isDirectory()) {
			throw new ExitException("Can't unzip file here because this isn't a directory: " + where.getAbsolutePath());
		}
		if (!where.exists()) {
			if (!where.mkdir()) {
				throw new ExitException("Couldn't create directory " + where.getAbsolutePath());
			}
		}
		if (where.list().length > 0) {
			throw new ExitException("Can't unzip file here: " + where.getAbsolutePath());
		}
		
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
							BUFFER_SIZE);

					int count;
					byte data[] = new byte[BUFFER_SIZE];
					while ((count = zin.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
					}

					dest.flush();
					dest.close();
				}
			}
	
			zin.close();
		} catch (IOException e) {
			throw new ExitException(e.getMessage(), e);
		}
	}
	
	private int ask(String question, int... validValues) {
		int i=Integer.MIN_VALUE;
		int tries=0;
		p(question);
		while (tries++<3) {
			try {
				System.out.print("> ");
				i = in.nextInt();
				if (in(i, validValues)) return i;
			} catch (InputMismatchException e) {
				p(Color.RED, "Incorrect answer.");
				in.next();
			}
		}
		throw new ExitException("I give up!");
	}
	
	@SuppressWarnings("serial")
	static class ExitException extends RuntimeException {
		ExitException(String message) {super(message);}
		ExitException(String message, Throwable cause) {super(message, cause);}
	}
	
	private boolean in(int actual, int... validValues) {
		if (validValues==null || validValues.length ==0) return false;
		for (int valid : validValues) {
			if (actual == valid) return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		new Installer().go();
	}

	// ---------------------------------------------------------

	private static void banner(String... texts) {
		banner(Color.NONE, texts);
	}
	
	private static void banner(Color color, String... texts) {
		p(color, "+-" + HEADER + "-+");
		for (String text : texts) {
			String padded = text.substring(0, Math.min(text.length(), WIDTH));
			p(color, "| " + padded + FILLER.substring(padded.length()) + " |");
		}
		p(color, "+-" + HEADER + "-+");
	}

	private static enum Color {
		NONE("0"), GREEN("92"), RED("91"), YELLOW("33");

		private final String code;
		private Color(String code) {
			this.code = code;
		}
		
		public String code() {
			return code;
		}
	}
	
	private static void p(String text) {
		p(Color.NONE, text);
	}
	private static void p(Color color, String text) {
		if (color == Color.NONE) {
			System.out.println(text);
		} else {
			System.out.print("\033["+color.code()+"m");
			System.out.print(text);
			System.out.println("\033["+Color.NONE.code()+"m");
		}
	}
	
	private static void nl() {
		System.out.println();
	}
	
	private final static int WIDTH = 40;
	
	private final static String HEADER = mkstr(WIDTH, '-');
	private final static String FILLER = mkstr(WIDTH, ' ');
	
	private static String mkstr(int width, char c) {
		char[] bheader = new char[width];
		Arrays.fill(bheader, c);
		String header = new String(bheader);
		return header;
	}

}
