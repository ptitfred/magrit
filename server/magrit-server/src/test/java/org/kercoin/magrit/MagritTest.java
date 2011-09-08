package org.kercoin.magrit;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class MagritTest {

	@Test
	public void testConfigure_defaults() throws IOException {
		// given
		Magrit magrit = new Magrit();
		// when
		magrit.configure(new String[] {});
		// then
		Configuration cfg = magrit.getCtx().configuration();
		assertThat(cfg.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/repos");
		assertThat(cfg.getWorkHomeDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/builds");
		assertThat(cfg.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/keys");
		assertThat(cfg.getSshPort()).isEqualTo(2022);
	}

	@Test
	public void testConfigure_port() throws IOException {
		// given
		Magrit magrit = new Magrit();
		// when
		magrit.configure(new String[] {"1234"});
		// then
		Configuration cfg = magrit.getCtx().configuration();
		assertThat(cfg.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/repos");
		assertThat(cfg.getWorkHomeDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/builds");
		assertThat(cfg.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/keys");
		assertThat(cfg.getSshPort()).isEqualTo(1234);
	}

	@Test
	public void testConfigure_port_repoDir() throws IOException {
		// given
		Magrit magrit = new Magrit();
		// when
		magrit.configure(new String[] {"1234", "/path/to/repos"});
		// then
		Configuration cfg = magrit.getCtx().configuration();
		assertThat(cfg.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo("/path/to/repos");
		assertThat(cfg.getWorkHomeDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/builds");
		assertThat(cfg.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/keys");
		assertThat(cfg.getSshPort()).isEqualTo(1234);
	}

	@Test
	public void testConfigure_port_repoDir_workDir() throws IOException {
		// given
		Magrit magrit = new Magrit();
		// when
		magrit.configure(new String[] {"1234", "/path/to/repos", "/path/to/buildspace"});
		// then
		Configuration cfg = magrit.getCtx().configuration();
		assertThat(cfg.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo("/path/to/repos");
		assertThat(cfg.getWorkHomeDir().getAbsolutePath()).isEqualTo("/path/to/buildspace");
		assertThat(cfg.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo(System.getProperty("java.io.tmpdir") + "/magrit/keys");
		assertThat(cfg.getSshPort()).isEqualTo(1234);
	}

	@Test
	public void testConfigure_port_repoDir_workDir_publicKeys() throws IOException {
		// given
		Magrit magrit = new Magrit();
		// when
		magrit.configure(new String[] {"1234", "/path/to/repos", "/path/to/buildspace", "/path/to/publickeys"});
		// then
		Configuration cfg = magrit.getCtx().configuration();
		assertThat(cfg.getRepositoriesHomeDir().getAbsolutePath()).isEqualTo("/path/to/repos");
		assertThat(cfg.getWorkHomeDir().getAbsolutePath()).isEqualTo("/path/to/buildspace");
		assertThat(cfg.getPublickeyRepositoryDir().getAbsolutePath()).isEqualTo("/path/to/publickeys");
		assertThat(cfg.getSshPort()).isEqualTo(1234);
	}

}
