package com.syn.mpos;

import java.net.MalformedURLException;
import java.net.URL;

import com.syn.mpos.database.Computer;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Shop;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class MPOSApplication extends Application {

	private static Context sContext;
	
	private static GlobalProperty sGlobalProp;
	
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

	public static int getComputerId() {
		Computer comp = new Computer(sContext);
		return comp.getComputerProperty().getComputerID();
	}

	public static int getShopId() {
		Shop s = new Shop(sContext);
		return s.getShopProperty().getShopID();
	}

	public static String getDeviceCode() {
		return Secure.getString(sContext.getContentResolver(), Secure.ANDROID_ID);
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
		sGlobalProp = new GlobalProperty(sContext);
	}

	public static GlobalProperty getGlobalProperty(){
		return sGlobalProp;
	}
	
	public static Context getContext() {
		return sContext;
	}
}
