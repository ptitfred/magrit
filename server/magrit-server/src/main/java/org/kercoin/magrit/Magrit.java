package org.kercoin.magrit;

import java.io.File;
import java.io.IOException;

import org.kercoin.magrit.sshd.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class Magrit {
	private Magrit() {}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MagritModule());
		
		int port = 2022;
		Context ctx = injector.getInstance(Context.class);
		ctx.setInjector(injector);
		
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		
		if (args.length >=2) {
			ctx.configuration().setRepositoriesHomeDir(new File(args[1]));
		} else {
			ctx.configuration().setRepositoriesHomeDir(new File("/tmp/magrit-tests"));
		}
		
	    MagritCommandFactory factory = injector.getInstance(MagritCommandFactory.class);
	    new Server(port, factory).start();
	}

}
