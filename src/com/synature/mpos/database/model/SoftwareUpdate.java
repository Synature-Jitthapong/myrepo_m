package com.synature.mpos.database.model;

public class SoftwareUpdate {
	private boolean isDownloaded;
	private boolean isAlreadyUpdated;
	public boolean isDownloaded() {
		return isDownloaded;
	}
	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}
	public boolean isAlreadyUpdated() {
		return isAlreadyUpdated;
	}
	public void setAlreadyUpdated(boolean isAlreadyUpdated) {
		this.isAlreadyUpdated = isAlreadyUpdated;
	}
}
