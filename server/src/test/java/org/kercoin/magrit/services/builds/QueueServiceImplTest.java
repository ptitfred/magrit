package org.kercoin.magrit.services.builds;

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
import org.kercoin.magrit.services.utils.TimeService;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.UserIdentity;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tests.GitTestsUtils;

@RunWith(MockitoJUnitRunner.class)
public class QueueServiceImplTest {

	QueueServiceImpl buildQueueServiceImpl;
	
	Context context;
	@Mock GitUtils gitUtils;
	@Mock StatusesService statusService;
	@Mock TimeService timeService;
	
	Repository repo;
	
	UserIdentity committer;

	@Before
	public void createBuildQueueServiceImpl() throws Exception {
		context= new Context();
		repo = GitTestsUtils.open(context, "/r1");
		committer = new UserIdentity("ptitfred@localhost", "ptitfred");
		
		buildQueueServiceImpl = new QueueServiceImpl(context, gitUtils,
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
		List<Status> statuses = Arrays.asList(Status.OK);
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, force);
		
		// then
		assertThat(future).isNotNull();
		
		assertThat(
				enqueue(true,
						Arrays.asList(Status.OK))
				).isNotNull();

	}

	@Test
	public void testEnqueueBuild_onOKs() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(Status.OK))
				).isNull();
	}

	@Test
	public void testEnqueueBuild() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.<Status>asList())
				).isNotNull();
	}

	@Test
	public void testEnqueueBuild_errors() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(Status.ERROR, Status.INTERRUPTED, Status.LOCAL))
				).isNotNull();
	}
	
	@Test
	public void testEnqueueBuild_running() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(Status.ERROR, Status.INTERRUPTED, Status.RUNNING))
				).isNull();
		assertThat(
				enqueue(false,
						Arrays.asList(Status.RUNNING))
				).isNull();
	}

	@Test
	public void testEnqueueBuild_local() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(Status.LOCAL))
				).isNotNull();
	}

	@Test
	public void testEnqueueBuild_unknown() throws Exception {
		assertThat(
				enqueue(false,
						Arrays.asList(Status.UNKNOWN))
				).isNull();
	}

	@Test
	public void testEnqueueBuild_force_running() throws Exception {
		assertThat(
				enqueue(true,
						Arrays.asList(Status.ERROR, Status.INTERRUPTED, Status.RUNNING))
				).isNotNull();
	}

	private Future<BuildResult> enqueue(boolean force,
			List<Status> statuses) throws Exception {
		// given
		String sha1 = "0123401234012340123401234012340123401234";
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, force);
		return future;
	}


}
