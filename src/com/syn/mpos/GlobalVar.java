package com.syn.mpos;

import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GlobalVar {
	// sqlite db name 
	public static final String DB_NAME = "mpos.db";
	
	// db version
	public static final int DB_VERSION = 1;
	
	// service name
	public static final String WS_NAME = "ws_mpos.asmx";
	
	// image dir
	public static final String IMG_DIR = "mposimg";
	
	// server image path
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	public static String getPrinterIp(Context c){
		SharedPreferences sharedPref = 
				PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}
	
	public static String getImageUrl(Context c){
		return getUrl(c) + "/" + SERVER_IMG_PATH;
	}
	
	public static String getFullUrl(Context c){
		return getUrl(c) + "/" + WS_NAME;
	}
	
	public static String getUrl(Context c){
		SharedPreferences sharedPref = 
				PreferenceManager.getDefaultSharedPreferences(c);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			// not found protocal
			url = "http://" + url;
			e.printStackTrace();
		}
		return url; 
	}
}
