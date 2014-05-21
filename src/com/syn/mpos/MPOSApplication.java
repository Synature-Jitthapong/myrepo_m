package com.syn.mpos;

import java.net.MalformedURLException;
import java.net.URL;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.DspPos;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class MPOSApplication extends Application {
	
	/*
	 * WebService file name
	 */
	public static final String WS_NAME = "ws_mpos.asmx";

	/*
	 * Menu image dir
	 */
	public static final String IMG_DIR = "mPOSImg";
	
	/*
	 * Log file name
	 */
	public static final String LOG_FILE_NAME = "mpos_";
	
	/*
	 * Log dir
	 */
	public static final String LOG_DIR = "mPOSLog";

	/*
	 * Image path on server
	 */
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	/*
	 * Android Device Code
	 */
	public static String sDeviceCode;
	
	public static String getDeviceCode(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public static boolean getInternalPrinterSetting(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRINTER_INTERNAL, false);
	}
	
	public static String getPrinterFont(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_FONT_LIST, "");
	}
	
	public static String getPrinterName(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_LIST, "");
	}
	
	public static String getPrinterIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}

	public static String getImageUrl(Context context) {
		return getUrl(context) + "/" + SERVER_IMG_PATH;
	}

	public static String getFullUrl(Context context) {
		return getUrl(context) + "/" + WS_NAME;
	}

	public static String getUrl(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL,
				"");
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			// not found protocal
			url = "http://" + url;
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// show customer display
		showCustomerDisplay();
	}
	
	private void showCustomerDisplay(){
		WintecCustomerDisplay wd = new WintecCustomerDisplay();
		wd.displayWelcome();
	}
}
