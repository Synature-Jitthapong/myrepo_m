package com.syn.mpos.model;

public class AppConfigModel {
	private String serverIP;
	private String webServiceUrl;
	private int displayImageMenu;
	
	public int getDisplayImageMenu() {
		return displayImageMenu;
	}
	public void setDisplayImageMenu(int displayImageMenu) {
		this.displayImageMenu = displayImageMenu;
	}
	public String getServerIP() {
		return serverIP;
	}
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	public String getWebServiceUrl() {
		return webServiceUrl;
	}
	public void setWebServiceUrl(String wsUrl) {
		this.webServiceUrl = wsUrl;
	}
}
