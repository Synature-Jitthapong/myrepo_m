package com.synature.mpos.database.model;

public class SoftwareInfo {
	private String version;
	private String dbVersion;
	private String lastUpdate;
	private boolean isAlreadyUpdate;
	public String getDbVersion() {
		return dbVersion;
	}
	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
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
}
