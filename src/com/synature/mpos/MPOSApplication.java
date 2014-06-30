package com.synature.mpos;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
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
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRINTER_INTERNAL, true);
	}
	
	public static String getEPSONPrinterFont(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_FONT_LIST, "");
	}
	
	public static String getEPSONModelName(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_LIST, "");
	}
	
	public static String getPrinterIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}
	
	public static String getSecondDisplayIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_SECOND_DISPLAY_IP, "");
	}
	
	public static int getSecondDisplayPort(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int port = Integer.parseInt(context.getString(R.string.default_second_display_port));
		try {
			String prefPort = sharedPref.getString(SettingsActivity.KEY_PREF_SECOND_DISPLAY_PORT, "");
			if(!prefPort.equals("")){
				port = Integer.parseInt(prefPort);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return port;
	}
	
	public static boolean isEnableSecondDisplay(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_SECOND_DISPLAY, false);
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
			//e.printStackTrace();
		}
		return url;
	}
	
	public static boolean isShowMenuImage(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_SHOW_MENU_IMG, true);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Start load master data service
//		Intent intent = new Intent(getApplicationContext(), MasterDataService.class);
//		startService(intent);
		
		
		// show customer display
		showCustomerDisplay();
	}
	
	private void showCustomerDisplay(){
		WintecCustomerDisplay wd = new WintecCustomerDisplay(getApplicationContext());
		wd.displayWelcome();
	}
}
