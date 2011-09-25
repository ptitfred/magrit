package org.kercoin.magrit.services.concurrent;


import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.kercoin.magrit.services.concurrent.RepositoryGuardImpl;

public class RepositoryGuardImplTest {

	RepositoryGuardImpl guard;
	
	@Before
	public void setUp() throws Exception {
		guard = new RepositoryGuardImpl();
	}

	private int computedItems = 0;
	
	@Test
	public void testAcquireRelease_isFair() throws InterruptedException {
		guard.acquire("/R1");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					guard.acquire(" /R1");
					computedItems++;
					guard.release("/R1");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		Thread.sleep(1);
		computedItems++;
		assertThat(computedItems).isEqualTo(1);
		guard.release("/R1 ");
		guard.acquire(" /R1");
		assertThat(computedItems).isEqualTo(2);
		guard.release("/R1");
	}

}
