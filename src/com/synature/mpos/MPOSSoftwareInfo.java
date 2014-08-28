package com.synature.mpos;

public class MPOSSoftwareInfo {
	private String szRegisterServiceUrl;
	private String szExpireDate;
	private String szRegisterMessage;
	private String szSoftwareVersion;
	private String szSoftwareDownloadUrl;
	public String getSzRegisterServiceUrl() {
		return szRegisterServiceUrl;
	}
	public void setSzRegisterServiceUrl(String szRegisterServiceUrl) {
		this.szRegisterServiceUrl = szRegisterServiceUrl;
	}
	public String getSzExpireDate() {
		return szExpireDate;
	}
	public void setSzExpireDate(String szExpireDate) {
		this.szExpireDate = szExpireDate;
	}
	public String getSzRegisterMessage() {
		return szRegisterMessage;
	}
	public void setSzRegisterMessage(String szRegisterMessage) {
		this.szRegisterMessage = szRegisterMessage;
	}
	public String getSzSoftwareVersion() {
		return szSoftwareVersion;
	}
	public void setSzSoftwareVersion(String szSoftwareVersion) {
		this.szSoftwareVersion = szSoftwareVersion;
	}
	public String getSzSoftwareDownloadUrl() {
		return szSoftwareDownloadUrl;
	}
	public void setSzSoftwareDownloadUrl(String szSoftwareDownloadUrl) {
		this.szSoftwareDownloadUrl = szSoftwareDownloadUrl;
	}
}
