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
	 * WINTEC POS device path
	 */
	public static final String WINTEC_DEFAULT_DEVICE_PATH = "/dev/ttySAC3";
	public static final String WINTEC_DEFAULT_BAUD_RATE = "BAUD_9600";
	
	/*
	 * Android Device Code
	 */
	public static String sDeviceCode;
	
	/*
	 * Application Context
	 */
	private static Context sContext;
	
	public static String getDeviceCode() {
		return Secure.getString(sContext.getContentResolver(), Secure.ANDROID_ID);
	}

	public static boolean getInternalPrinterSetting(){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(sContext);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRINTER_INTERNAL, false);
	}
	
	public static String getPrinterFont() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(sContext);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_FONT_LIST, "");
	}
	
	public static String getPrinterName() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(sContext);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_LIST, "");
	}
	
	public static String getPrinterIp() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(sContext);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}

	public static String getImageUrl() {
		return getUrl() + "/" + SERVER_IMG_PATH;
	}

	public static String getFullUrl() {
		return getUrl() + "/" + WS_NAME;
	}

	public static String getUrl() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(sContext);
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
		sContext = getApplicationContext();
		
		// show customer display
		showCustomerDisplay();
	}

	public static Context getContext(){
		return sContext;
	}
	
	private void showCustomerDisplay(){
		DspPos dsp = new DspPos(WINTEC_DEFAULT_DEVICE_PATH, 
				ComIO.Baudrate.valueOf(WINTEC_DEFAULT_BAUD_RATE));	    
		
		dsp.DSP_ClearScreen();
		dsp.DSP_Dispay("Welcome to");
		dsp.DSP_MoveCursorDown();
		dsp.DSP_MoveCursorEndLeft();
		dsp.DSP_Dispay("pRoMiSe System");
		dsp.DSP_Close();
	}
}
