package com.syn.mpos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.syn.mpos.database.Computer;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.transaction.Transaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class GlobalVar {
	private static GlobalVar instance = null;

	// sqlite db name 
	public static final String DB_NAME = "mpos.db";
	
	// db version
	public static final int DB_VERSION = 2;
	
	// service name
	public static final String WS_NAME = "ws_mpos.asmx";
	
	// image dir
	public static final String IMG_DIR = "mposimg";
	
	// server image path
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	// preference
	private SharedPreferences mSharedPref; 
	
	// global property
	private GlobalProperty mGlobalProperty;
	
	// product
	private Products mProduct;
	
	// shop data property
	private Shop mShop;
	
	// computer data property
	private Computer mComputer;
	
	// transaction 
	private Transaction mTransaction;
	
	private GlobalVar(Context c){
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(c);
		mGlobalProperty = new GlobalProperty(c);
		mProduct = new Products(c);
		mShop = new Shop(c);
		mComputer = new Computer(c);
		mTransaction = new Transaction(c);
	}
	
	public static synchronized GlobalVar newInstance(Context c){
		if(instance == null){
			instance = new GlobalVar(c);
		}
		return instance;
	}
	
	public String getPrinterPort(){
		return mSharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_PORT, "");
	}
	
	public String getPrinterIp(){
		return mSharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}
	
	public String getImageUrl(){
		return mSharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "") + "/" + SERVER_IMG_PATH;
	}
	
	public String getFullUrl(){
		return mSharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "") + "/" + WS_NAME;
	}
	
	public String getHost(){
		Uri uri = Uri.parse(mSharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, ""));
		return uri.getHost();
	}
	
	public Transaction getTransaction(){
		return mTransaction;
	}
	
	public Computer getComputer(){
		return mComputer;
	}
	
	public Shop getShop(){
		return mShop;
	}
	
	public Products getProduct(){
		return mProduct;
	}
	
	public GlobalProperty getGlobalProperty(){
		return mGlobalProperty;
	}
	
	public SharedPreferences getSharedPreference(){
		return mSharedPref;
	}
	
	public String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return dateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM-dd", Locale.getDefault());
		
		if(!mGlobalProperty.getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(mGlobalProperty.getGlobalProperty().getDateFormat());
			
		return dateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return dateTimeFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		
		if(!mGlobalProperty.getGlobalProperty().getDateFormat().equals("") && 
				!mGlobalProperty.getGlobalProperty().getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(mGlobalProperty.getGlobalProperty().getDateFormat() + " " +
					mGlobalProperty.getGlobalProperty().getTimeFormat());
			
		return dateTimeFormat.format(d);
	}
	
	public String timeFormat(Date d){
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		
		if(!mGlobalProperty.getGlobalProperty().getTimeFormat().equals(""))
			timeFormat.applyPattern(mGlobalProperty.getGlobalProperty().getTimeFormat());
			
		return timeFormat.format(d);
	}
	
	public String qtyFormat(float qty, String pattern){
		DecimalFormat qtyFormat = new DecimalFormat(pattern);
		return qtyFormat.format(qty);
	}
	
	public String qtyFormat(float qty){
		DecimalFormat qtyFormat = new DecimalFormat("#,##0.####");
		
		if(!mGlobalProperty.getGlobalProperty().getQtyFormat().equals(""))
			qtyFormat.applyPattern(mGlobalProperty.getGlobalProperty().getQtyFormat());
		
		return qtyFormat.format(qty);
	}
	
	public String currencyFormat(float currency, String pattern){
		DecimalFormat currencyFormat = new DecimalFormat(pattern);
		return currencyFormat.format(currency);
	}
	
	public String currencyFormat(float currency){
		DecimalFormat currencyFormat = new DecimalFormat("#,##0.####");	
		
		if(!mGlobalProperty.getGlobalProperty().getCurrencyFormat().equals(""))
			currencyFormat.applyPattern(mGlobalProperty.getGlobalProperty().getCurrencyFormat());
			
		return currencyFormat.format(currency);
	}
}
