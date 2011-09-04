package org.kercoin.magrit;

import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class Context {
	
	private final Configuration configuration = new Configuration();
	
	private Injector injector;

	public Context() {
	}

	public Configuration configuration() {
		return configuration;
	}
	
	public Injector getInjector() {
		return injector;
	}
	
	public void setInjector(Injector injector) {
		this.injector = injector;
	}

}
