package com.syn.mpos;

import java.net.MalformedURLException;
import java.net.URL;

import com.syn.mpos.database.Computer;
import com.syn.mpos.database.Shop;
import com.syn.pos.ShopData;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class MPOSApplication extends Application {
	
	// service name
	public static final String WS_NAME = "ws_mpos.asmx";

	// image dir
	public static final String IMG_DIR = "mPOSImg";
	
	// log file name
	public static final String LOG_FILE_NAME = "mpos_";
	
	// log dir
	public static final String LOG_DIR = "mPOSLog";

	// server image path
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	// device code
	public static String sDeviceCode;
	
	// application context
	private static Context sContext;
	
	// shop objecj
	private static Shop sShop;
	
	// computer object
	private static Computer sComputer;
	
	public static String getDeviceCode() {
		return Secure.getString(sContext.getContentResolver(), Secure.ANDROID_ID);
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
	}

	public static ShopData.ComputerProperty getComputer(){
		return sComputer.getComputerProperty();
	}
	
	public static ShopData.ShopProperty getShop(){
		return sShop.getShopProperty();
	}
	
	public static Context getContext(){
		return sContext;
	}
}
