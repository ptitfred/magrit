package tests;

import org.kercoin.magrit.MagritModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceModulesHolder {
	public static final Injector MAGRIT_MODULE;
	static {
		MAGRIT_MODULE = Guice.createInjector(new MagritModule());
	}
	
	
}
