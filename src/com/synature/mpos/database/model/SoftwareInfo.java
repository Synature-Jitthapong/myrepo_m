package com.synature.mpos.database.model;

public class SoftwareInfo {
	private int id;
	private String version;
	private String dbVersion;
	private String expDate;
	private String lockDate;
	private boolean isDownloaded;
	private boolean isAlreadyUpdate;
	public String getDbVersion() {
		return dbVersion;
	}
	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public boolean isAlreadyUpdate() {
		return isAlreadyUpdate;
	}
	public void setAlreadyUpdate(boolean isAlreadyUpdate) {
		this.isAlreadyUpdate = isAlreadyUpdate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isDownloaded() {
		return isDownloaded;
	}
	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}
	public String getExpDate() {
		return expDate;
	}
	public void setExpDate(String expDate) {
		this.expDate = expDate;
	}
	public String getLockDate() {
		return lockDate;
	}
	public void setLockDate(String lockDate) {
		this.lockDate = lockDate;
	}
}
