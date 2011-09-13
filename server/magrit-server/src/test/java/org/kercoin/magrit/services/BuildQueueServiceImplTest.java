package org.kercoin.magrit.services;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.UserIdentity;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tests.GitTestsUtils;

@RunWith(MockitoJUnitRunner.class)
public class BuildQueueServiceImplTest {

	BuildQueueServiceImpl buildQueueServiceImpl;
	
	Context context;
	@Mock GitUtils gitUtils;
	@Mock BuildStatusesService statusService;
	@Mock TimeService timeService;
	
	Repository repo;
	
	UserIdentity committer;

	@Before
	public void createBuildQueueServiceImpl() throws Exception {
		context= new Context();
		repo = GitTestsUtils.open(context, "/r1");
		committer = new UserIdentity("ptitfred@localhost", "ptitfred");
		
		buildQueueServiceImpl = new BuildQueueServiceImpl(context, gitUtils,
				timeService, statusService);
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEnqueueBuild_force_onOKs() throws Exception {
		// given
		String sha1 = "0123401234012340123401234012340123401234";
		boolean force = true;
		List<BuildStatus> statuses = Arrays.asList(BuildStatus.OK);
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, force);
		
		// then
		assertThat(future).isNotNull();
		
		assertThat(
				enqueue(true,
						Arrays.asList(BuildStatus.OK))
				).isNotNull();

	}

	@Test
	public void testEnqueueBuild_onOKs() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.OK))
				).isNull();
	}

	@Test
	public void testEnqueueBuild() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.<BuildStatus>asList())
				).isNotNull();
	}

	@Test
	public void testEnqueueBuild_errors() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.ERROR, BuildStatus.INTERRUPTED, BuildStatus.LOCAL))
				).isNotNull();
	}
	
	@Test
	public void testEnqueueBuild_running() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.ERROR, BuildStatus.INTERRUPTED, BuildStatus.RUNNING))
				).isNull();
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.RUNNING))
				).isNull();
	}

	@Test
	public void testEnqueueBuild_local() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.LOCAL))
				).isNull();
	}

	@Test
	public void testEnqueueBuild_unknown() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(BuildStatus.UNKNOWN))
				).isNotNull();
	}

	@Test
	public void testEnqueueBuild_force_running() throws Exception {
		assertThat(
				enqueue(true,
						Arrays.asList(BuildStatus.ERROR, BuildStatus.INTERRUPTED, BuildStatus.RUNNING))
				).isNotNull();
	}

	private Future<BuildResult> enqueue(boolean force,
			List<BuildStatus> statuses) throws Exception {
		// given
		String sha1 = "0123401234012340123401234012340123401234";
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, force);
		return future;
	}


}
