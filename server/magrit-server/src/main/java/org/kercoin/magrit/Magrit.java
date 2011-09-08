package org.kercoin.magrit;

import java.io.File;
import java.io.IOException;

import org.kercoin.magrit.sshd.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class Magrit {

	private Context ctx;
	private Injector guice;

	private Magrit(String[] args) throws IOException {
		bootStrap();
		configure(args);
		launch();
	}
	
	Magrit() {
		bootStrap();
	}

	void bootStrap() {
		guice = Guice.createInjector(new MagritModule());
		
		ctx = guice.getInstance(Context.class);
		ctx.setInjector(guice);
	}
	
	void configure(String[] args) {
		if (args.length >= 1) {
			ctx.configuration().setSshPort(Integer.parseInt(args[0]));
		}

		if (args.length >= 2) {
			ctx.configuration().setRepositoriesHomeDir(new File(args[1]));
		}
		
		if (args.length >= 3) {
			ctx.configuration().setWorkHomeDir(new File(args[2]));
		}
		
		if (args.length >= 4) {
			ctx.configuration().setPublickeysRepositoryDir(new File(args[3]));
		}

	}
	
	Context getCtx() {
		return ctx;
	}

	private void launch() throws IOException {
		guice.getInstance(Server.class).start(ctx.configuration().getSshPort());
	}
	
	public static void main(String[] args) throws IOException {
		new Magrit(args);
	}

}
