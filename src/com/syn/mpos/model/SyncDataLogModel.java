package com.syn.mpos.model;

public class SyncDataLogModel {
	private String SyncTime;
	private int Status;
	private String Note;
	private String SyncDate;
	private String WebServiceVersion;
	private String WebServiceBuildVersion;
	private String WebServiceBuildDateTime;
	
	public String getWebServiceVersion() {
		return WebServiceVersion;
	}
	public void setWebServiceVersion(String webServiceVersion) {
		WebServiceVersion = webServiceVersion;
	}
	public String getWebServiceBuildVersion() {
		return WebServiceBuildVersion;
	}
	public void setWebServiceBuildVersion(String webServiceBuildVersion) {
		WebServiceBuildVersion = webServiceBuildVersion;
	}
	public String getWebServiceBuildDateTime() {
		return WebServiceBuildDateTime;
	}
	public void setWebServiceBuildDateTime(String webServiceBuildDateTime) {
		WebServiceBuildDateTime = webServiceBuildDateTime;
	}
	public String getSyncDate() {
		return SyncDate;
	}
	public void setSyncDate(String syncDate) {
		SyncDate = syncDate;
	}
	public String getSyncTime() {
		return SyncTime;
	}
	public void setSyncTime(String syncTime) {
		SyncTime = syncTime;
	}
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
	public String getNote() {
		return Note;
	}
	public void setNote(String note) {
		Note = note;
	}
}
