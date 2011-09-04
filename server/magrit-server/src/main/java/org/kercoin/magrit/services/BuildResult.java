package org.kercoin.magrit.services;

import java.util.Date;

public class BuildResult {

	private boolean success;

	private byte[] log;

	private Date startDate;
	private Date endDate;

	private int exitCode;

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

}
