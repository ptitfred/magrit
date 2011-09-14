package org.kercoin.magrit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.kercoin.magrit.sshd.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class Magrit {

	private Context ctx;
	private Injector guice;

	Magrit() {
		bootStrap();
	}

	void bootStrap() {
		guice = Guice.createInjector(new MagritModule());
		
		ctx = guice.getInstance(Context.class);
		ctx.setInjector(guice);
	}
	
	void configure(String[] args) throws ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = createCmdLineOptions();
		CommandLine cmdLine = parser.parse(options, args, true);

		if (cmdLine.hasOption("standard")) {
			ctx.configuration().applyStandardLayout(cmdLine.getOptionValue("standard"));
		}
		
		if (cmdLine.hasOption("port")) {
			try {
				int sshdPort = Integer.parseInt(cmdLine.getOptionValue("port"));
				if (sshdPort<=1024) {
					throw new ParseException("SSH port must be >1024");
				}
				ctx.configuration().setSshPort(sshdPort);
			} catch (NumberFormatException e) {
				throw new ParseException("SSH port option should be numeric");
			}
		}
		
		if (cmdLine.hasOption("bares")) {
			ctx.configuration().setRepositoriesHomeDir(new File(cmdLine.getOptionValue("bares")));
		}
		
		if (cmdLine.hasOption("work")) {
			ctx.configuration().setWorkHomeDir(new File(cmdLine.getOptionValue("work")));
		}
		
		if (cmdLine.hasOption("keys")) {
			ctx.configuration().setPublickeysRepositoryDir(new File(cmdLine.getOptionValue("keys")));
		}

	}
	
	private Options createCmdLineOptions() {
		Options opts = new Options();
		opts.addOption("p", "port", true, //
				"SSH port to listen to");
		opts.addOption("b", "bares", true, //
				"directory where to create bare repositories");
		opts.addOption("w", "work", true, //
				"directory where to create work directories (for builds)");
		opts.addOption("k", "keys", true, //
				"non-bare Git repository containing SSH public keys for authentication");
		opts.addOption("s", "standard", true, //
				"directory where to apply the standard " + //
				"layout for bare repositories, " + //
				"work directories and public keys, " + //
				"all put in the supplied directory");
		return opts;
	}

	Context getCtx() {
		return ctx;
	}

	private void launch() throws IOException {
		guice.getInstance(Server.class).start(ctx.configuration().getSshPort());
	}
	
	public static void main(String[] args) throws Exception {
		Magrit m = new Magrit();
		try {
			m.configure(args);
			m.launch();
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

}
