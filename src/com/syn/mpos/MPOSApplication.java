package com.syn.mpos;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.GlobalProperty;
import com.syn.mpos.provider.MPOSSQLiteHelper;
import com.syn.mpos.provider.Products;
import com.syn.mpos.provider.Shop;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class MPOSApplication extends Application {

	private static Context sContext;
	
	private static MPOSSQLiteHelper sSqliteHelper;
	
	private static GlobalProperty sGlobalProp;
	
	private static Products sProduct;
	
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
		Computer comp = new Computer(getWriteDatabase());
		return comp.getComputerProperty().getComputerID();
	}

	public static int getShopId() {
		Shop s = new Shop(getWriteDatabase());
		return s.getShopProperty().getShopID();
	}

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
		sSqliteHelper = new MPOSSQLiteHelper(sContext);
		sGlobalProp = new GlobalProperty(getWriteDatabase());
		sProduct = new Products(getWriteDatabase());
	}

	public static String fixesDigitLength(int length, double value){
		BigDecimal b = new BigDecimal(value);
		return b.setScale(length, BigDecimal.ROUND_HALF_UP).toString();
	}
	
	public static SQLiteDatabase getReadDatabase(){
		return sSqliteHelper.getReadableDatabase();
	}
	
	public static SQLiteDatabase getWriteDatabase(){
		return sSqliteHelper.getWritableDatabase();
	}
	
	public static Products getProduct(){
		return sProduct;
	}
	
	public static GlobalProperty getGlobalProperty(){
		return sGlobalProp;
	}
	
	public static Context getContext() {
		return sContext;
	}
}
