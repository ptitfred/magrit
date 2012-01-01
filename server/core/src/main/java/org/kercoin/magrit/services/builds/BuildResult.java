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
package org.kercoin.magrit.services.builds;

import java.util.Date;

public class BuildResult {

	private final String sha1;
	
	private boolean success;

	private byte[] log;

	private Date startDate;
	private Date endDate;

	private int exitCode;
	
	public BuildResult(String sha1) {
		this.sha1 = sha1;
		success = false;
		exitCode = -1;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setLog(byte[] log) {
		this.log = log;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public long getDuration() {
		if (!endDate.after(startDate)) {
			throw new IllegalArgumentException("End date is before start date > incoherent!");
		}
		return endDate.getTime() - startDate.getTime();
	}

	public boolean isSuccess() {
		return success;
	}

	public byte[] getLog() {
		return log;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public int getExitCode() {
		return exitCode;
	}

	public String getCommitSha1() {
		return sha1;
	}

}
