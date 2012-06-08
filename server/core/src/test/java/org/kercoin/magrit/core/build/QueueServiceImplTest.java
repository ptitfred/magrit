/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

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
package org.kercoin.magrit.core.build;

import static org.fest.assertions.Assertions.assertThat;
import static org.kercoin.magrit.core.build.Status.ERROR;
import static org.kercoin.magrit.core.build.Status.NEW;
import static org.kercoin.magrit.core.build.Status.OK;
import static org.kercoin.magrit.core.build.Status.PENDING;
import static org.kercoin.magrit.core.build.Status.RUNNING;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kercoin.magrit.core.Context;
import org.kercoin.magrit.core.build.pipeline.PipelineImpl;
import org.kercoin.magrit.core.dao.BuildDAO;
import org.kercoin.magrit.core.user.UserIdentity;
import org.kercoin.magrit.core.utils.GitUtils;
import org.kercoin.magrit.core.utils.TimeService;
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

	BuildTaskProvider buildTaskProvider;
	@Mock BuildDAO dao;

	@Before
	public void createBuildQueueServiceImpl() throws Exception {
		context = new Context(null, null);
		repo = GitTestsUtils.open(context, "/r1");
		committer = new UserIdentity("ptitfred@localhost", "ptitfred");
		PipelineImpl pipeline = new PipelineImpl(context);
		buildTaskProvider = new BuildTaskProvider(context, new RepositoryGuard(), timeService, dao);
		
		buildQueueServiceImpl = new QueueServiceImpl(
				context,
				statusService,
				pipeline,
				buildTaskProvider
			);
	}

	@Test
	public void testEnqueueBuild_force_onOKs() throws Exception {
		// given
		String sha1 = "0123401234012340123401234012340123401234";
		String cmd = "0123456789012345678901234567890123456789";
		boolean force = true;
		List<Status> statuses = Arrays.asList(Status.OK);
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, cmd, force);
		
		// then
		assertThat(future).isNotNull();
		
		assertThat( enqueue(true, Arrays.asList(Status.OK)) ).isNotNull();

	}

	@Test
	public void testEnqueueBuild_onOKs() throws Exception {
		assertThat(	enqueue(false, Arrays.asList(Status.OK)) ).isNull();
	}

	@Test
	public void testEnqueueBuild() throws Exception {
		assertThat( enqueue(false, Arrays.<Status>asList()) ).isNotNull();
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

	private Future<BuildResult> enqueue(boolean force, List<Status> statuses) throws Exception {
		// given ---------------------------------
		String sha1 = "0123401234012340123401234012340123401234";
		String cmd = "0123456789012345678901234567890123456789";
		given(statusService.getStatus(repo, sha1)).willReturn(statuses);
		
		// when ----------------------------------
		Future<BuildResult> future = buildQueueServiceImpl.enqueueBuild(committer, repo, sha1, cmd, force);
		return future;
	}

	@Test
	public void testShouldBuild() throws Exception {
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(ERROR))).isTrue();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(OK, ERROR))).isTrue();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(NEW))).isTrue();
	}

	@Test
	public void testShouldntBuild() throws Exception {
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(OK))).isFalse();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(ERROR, OK))).isFalse();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(PENDING))).isFalse();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(RUNNING))).isFalse();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(ERROR, PENDING))).isFalse();
		assertThat(buildQueueServiceImpl.shouldBuild(Arrays.asList(ERROR, RUNNING))).isFalse();
	}

}
