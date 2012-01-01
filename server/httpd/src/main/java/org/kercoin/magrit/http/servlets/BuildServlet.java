/*
Copyright 2011 Frederic Menou

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
package org.kercoin.magrit.http.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.lib.Repository;
import org.json.simple.JSONValue;
import org.kercoin.magrit.core.Pair;
import org.kercoin.magrit.core.build.QueueService;

import com.google.inject.Inject;

/**
 * @author ptitfred
 *
 */
public class BuildServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final QueueService buildQueueService;

	@Inject
	public BuildServlet(QueueService buildQueueService) {
		this.buildQueueService = buildQueueService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		enableCORS(resp);
		List<String> currentTasks = mapSHA1s(buildQueueService.getCurrentTasks());
		List<String> scheduledTasks = mapSHA1s(buildQueueService.getScheduledTasks());
		resp.getWriter().println(encodeJSON(currentTasks, scheduledTasks));
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @param resp
	 */
	private void enableCORS(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
	}

	private List<String> mapSHA1s(Collection<Pair<Repository, String>> tasks) {
		List<String> sha1s = new ArrayList<String>();
		for (Pair<Repository, String> task : tasks) {
			sha1s.add(task.getU());
		}
		return sha1s;
	}

	String encodeJSON(List<String> runnings, List<String> pendings) {
		return JSONValue.toJSONString(wrap(runnings, pendings));
	}

	private Map<String, Object> wrap(List<String> runnings,
			List<String> pendings) {
		Map<String, Object> content = new LinkedHashMap<String, Object>();
		content.put("runnings", runnings);
		content.put("pendings", pendings);
		return content;
	}
}
