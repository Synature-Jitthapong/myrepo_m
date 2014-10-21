package com.synature.mpos;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.ProductsDao;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.util.Logger;

public class Utils {
	
	/**
	 * Database name
	 */
	public static final String DB_NAME = "mpos.db";
	
	/**
	 * Database version
	 */
	public static int DB_VERSION = 6;
	
	/**
	 * Main url 
	 */
	public static final String REGISTER_URL = "http://www.promise-system.com/promise_registerpos/ws_mpos.asmx";
	
	/**
	 * WebService file name
	 */
	public static final String WS_NAME = "ws_mpos.asmx";

	/**
	 * Menu image dir
	 */
	public static final String IMG_DIR = "mPOSImg";
	
	/**
	 * Log file name
	 */
	public static final String LOG_FILE_NAME = "log_";
	
	/**
	 * Resource dir
	 */
	public static final String RESOURCE_DIR = "mpos";
	
	/**
	 * Backup path
	 */
	public static final String BACKUP_DB_PATH = RESOURCE_DIR + File.separator + "backup";
	
	/**
	 * Log dir
	 */
	public static final String LOG_PATH = RESOURCE_DIR + File.separator + "log";


	/**
	 * Sale dir store partial sale json file
	 */
	public static final String SALE_PATH = RESOURCE_DIR + File.separator + "Sale";
	
	/**
	 * Endday sale dir store endday sale json file
	 */
	public static final String ENDDAY_PATH = RESOURCE_DIR + File.separator + "EnddaySale";
	
	/**
	 * apk file name
	 */
	public static final String UPDATE_FILE_NAME = "mpos.apk";
	
	/**
	 * Image path on server
	 */
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	/**
	 * The minimum date
	 */
	public static final int MINIMUM_YEAR = 1900;
	public static final int MINIMUM_MONTH = 0;
	public static final int MINIMUM_DAY = 1;
	
	/**
	 * Enable/Disable log
	 */
	public static final boolean sIsEnableLog = true;

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param lastSessCal
	 * @return true if success endday
	 */
	public static boolean endingMultipleDay(Context context, int shopId, 
			int computerId, int staffId, Calendar lastSessCal){
		int diffDay = getDiffDay(lastSessCal);
		try {
			SessionDao sess = new SessionDao(context);
			// if have some previous session does not end
			String prevSessDate = String.valueOf(lastSessCal.getTimeInMillis());
			if(!sess.checkEndday(prevSessDate)){
				TransactionDao trans = new TransactionDao(context);
				int prevSessId = sess.getLastSessionId();
				sess.addSessionEnddayDetail(prevSessDate, 
						trans.getTotalReceipt(prevSessId, prevSessDate), 
						trans.getTotalReceiptAmount(prevSessDate));
				sess.closeSession(prevSessId, staffId, 0, true);
			}
			Calendar sessCal = (Calendar) lastSessCal.clone();
			for(int i = 1; i < diffDay; i++){
				sessCal.add(Calendar.DAY_OF_MONTH, 1);
				int sessId = sess.openSession(String.valueOf(sessCal.getTimeInMillis()), shopId, computerId, staffId, 0);
				sess.addSessionEnddayDetail(String.valueOf(sessCal.getTimeInMillis()), 0, 0);
				sess.closeSession(sessId, staffId, 0, true);
			}
			try {
				GlobalPropertyDao format = new GlobalPropertyDao(context);
				Logger.appendLog(context, LOG_PATH,
						LOG_FILE_NAME,
						"Success ending multiple day : " 
						+ " from : " + format.dateFormat(lastSessCal.getTime())
						+ " to : " + format.dateFormat(Calendar.getInstance().getTime()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.appendLog(context, LOG_PATH,
					LOG_FILE_NAME,
					"Error ending multiple day : " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * @param lastSessCal
	 * @return get day number
	 */
	public static int getDiffDay(Calendar lastSessCal){
		Calendar currCalendar = Calendar.getInstance();
		int diffDay = 0;
		if(lastSessCal.get(Calendar.YEAR) == currCalendar.get(Calendar.YEAR)){
			diffDay = currCalendar.get(Calendar.DAY_OF_YEAR) - lastSessCal.get(Calendar.DAY_OF_YEAR);
		}else if(lastSessCal.get(Calendar.YEAR) < currCalendar.get(Calendar.YEAR)){
			diffDay = (lastSessCal.getActualMaximum(Calendar.DAY_OF_YEAR) 
					- lastSessCal.get(Calendar.DAY_OF_YEAR)) + currCalendar.get(Calendar.DAY_OF_YEAR);
		}	
		return diffDay;
	}
	
	public static Calendar getCalendar(){
		return Calendar.getInstance(Locale.getDefault());
	}
	
	public static Calendar getMinimum(){
		return new GregorianCalendar(MINIMUM_YEAR, MINIMUM_MONTH, MINIMUM_DAY);
	}
	
	public static Calendar getDate(int year, int month, int day){
		return new GregorianCalendar(year, month, day);
	}
	
	public static Calendar getDate(){
		Calendar c = getCalendar();
		return new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), 
				c.get(Calendar.DAY_OF_MONTH));
	}
	
	public static Calendar convertStringToCalendar(String dateTime){
		Calendar calendar = getCalendar();
		if(dateTime == null || dateTime.isEmpty()){
			dateTime = String.valueOf(getMinimum().getTimeInMillis());
		}
		calendar.setTimeInMillis(Long.parseLong(dateTime));
		return calendar;
	}
	
	public static double calculateVatAmount(double totalPrice, double vatRate, int vatType){
		if(vatType == ProductsDao.VAT_TYPE_INCLUDED)
			return totalPrice * vatRate / (100 + vatRate);
		else if(vatType == ProductsDao.VAT_TYPE_EXCLUDE)
			return totalPrice * vatRate / 100;
		else
			return 0;
	}
	
	public static void makeToask(Context c, String msg){
		Toast toast = Toast.makeText(c, 
				msg, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * @param scale
	 * @param value
	 * @return string fixes digit
	 */
	public static String fixesDigitLength(GlobalPropertyDao format, int scale, double value){
		return format.currencyFormat(value, "#,##0.0000");
	}
	
	/**
	 * @param roundType
	 * @param price
	 * @return rounding value
	 */
	public static double roundingPrice(int roundType, double price){
		double result = price;
		long iPart;		// integer part
		double fPart;	// fractional part
		iPart = (long) price;
		fPart = price - iPart;
		switch(roundType){
		case 1:
		case 7:
			if(fPart > 0){
				iPart += 1;
				fPart = 0;
			}
			break;
		case 2:
		case 8:
			if(fPart < 0.5){
				fPart = 0;
			}else if (fPart == 0.5){
				fPart = 0.5;
			}else{
				iPart += 1;
				fPart = 0;
			}
			break;
		case 3:
		case 9:
			if(fPart > 0 && fPart < 0.25){
				fPart = 0.25;
			}else if(fPart > 0.25 && fPart <= 0.5){
				fPart = 0.5;
			}else if(fPart > 0.5){
				iPart += 1;
				fPart = 0;
			}
			break;
		case 4:
			fPart = 0;
			break;
		case 5:
			if(fPart < 0.5){
				fPart = 0;
			}else if (fPart >= 0.5){
				fPart = 0.5;
			}
			break;
		case 6:
			if(fPart < 0.25){
				fPart = 0;
			}else if(fPart >= 0.25 && fPart < 0.5){
				fPart = 0.25;
			}else if(fPart >= 0.5){
				fPart = 0.5;
			}	
			break;
		}
		result = iPart + fPart;
		return result;
	}

	public static double stringToDouble(String text) throws ParseException{
		double value = 0.0d;
		NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
		Number num = format.parse(text);
		value = num.doubleValue();
		return value;
	}

	public static void logServerResponse(Context context, String msg){
		Logger.appendLog(context, LOG_PATH,
				LOG_FILE_NAME,
				" Server Response : " + msg);
	}
	
	/**
	 * Get software version
	 * @param context
	 * @return version name
	 */
	public static String getSoftWareVersion(Context context){
		String ver = "";
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ver = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ver;
	}
	
	/**
	 * @param context
	 * @return android device id
	 */
	public static String getDeviceCode(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
	/**
	 * @param context
	 * @return drawer baud rate
	 */
	public static String getWintecDrwBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DRW_BAUD_RATE, "BAUD_38400");
	}
	
	/**
	 * @param context
	 * @return drawer dev path
	 */
	public static String getWintecDrwDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DRW_DEV_PATH, "/dev/ttySAC1");
	}
	
	/**
	 * @param context
	 * @return magnetic reader baud rate
	 */
	public static String getWintecMsrBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_MSR_BAUD_RATE, "BAUD_9600");
	}
	
	/**
	 * @param context
	 * @return magnetic reader dev path
	 */
	public static String getWintecMsrDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_MSR_DEV_PATH, "/dev/ttySAC3");
	}
	
	/**
	 * @param context
	 * @return true if enable internal printer
	 */
	public static boolean isInternalPrinterSetting(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRINTER_INTERNAL, true);
	}
	
	/**
	 * @param context
	 * @return printer baud rate
	 */
	public static String getWintecPrinterBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_BAUD_RATE, "BAUD_38400");
	}
	
	/**
	 * @param context
	 * @return printer dev path
	 */
	public static String getWintecPrinterDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_DEV_PATH, "/dev/ttySAC1");
	}
	
	/**
	 * @param context
	 * @return epson model name
	 */
	public static String getEPSONModelName(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_LIST, "");
	}
	
	/**
	 * @param context
	 * @return epson printer ip
	 */
	public static String getPrinterIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}
	
	/**
	 * @param context
	 * @return true if enable wintec customer display
	 */
	public static boolean isEnableWintecCustomerDisplay(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_DSP, true);
	}
	
	/**
	 * @param context
	 * @return dsp baud rate
	 */
	public static String getWintecDspBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_BAUD_RATE, "BAUD_9600");
	}
	
	/**
	 * @param context
	 * @return text line 2
	 */
	public static String getWintecDspTextLine2(Context context){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_TEXT_LINE2, "mPOS");
	}
	
	/**
	 * @param context
	 * @return text line 1
	 */
	public static String getWintecDspTextLine1(Context context){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_TEXT_LINE1, "Welcome to");
	}
	
	/**
	 * @param context
	 * @return dsp dev path
	 */
	public static String getWintecDspPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_DEV_PATH, "/dev/ttySAC3");
	}
	
	/**
	 * @param context
	 * @return second display ip
	 */
	public static String getSecondDisplayIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_SECOND_DISPLAY_IP, "");
	}
	
	/**
	 * @param context
	 * @return second display port
	 */
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
	
	/**
	 * @param context
	 * @return true if enabled
	 */
	public static boolean isEnableBackupDatabase(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_BACKUP_DB, true);
	}
	
	/**
	 * @param context
	 * @return true if enable second display
	 */
	public static boolean isEnableSecondDisplay(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_SECOND_DISPLAY, false);
	}

	/**
	 * @param context
	 * @return menu image url
	 */
	public static String getImageUrl(Context context) {
		return getUrl(context) + "/" + SERVER_IMG_PATH;
	}

	/**
	 * @param context
	 * @return full webservice url
	 */
	public static String getFullUrl(Context context) {
		return getUrl(context) + "/" + WS_NAME;
	}

	/**
	 * @param context
	 * @return connection time out millisecond
	 */
	public static int getConnectionTimeOut(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String strTimeOut = sharedPref.getString(SettingsActivity.KEY_PREF_CONN_TIME_OUT_LIST, "30");
		int timeOut = Integer.parseInt(strTimeOut);
		return timeOut * 1000;
	}
	
	public static String getUrl(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			// not found protocal
			url = "http://" + url;
			//e.printStackTrace();
		}
		return url;
	}
	
	/**
	 * @param context
	 * @return language code en_US, th_TH
	 */
	public static String getLangCode(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_LANGUAGE_LIST, "en_US");
	}
	
	/**
	 * @param context
	 * @return true if enable show menu image
	 */
	public static boolean isShowMenuImage(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_SHOW_MENU_IMG, true);
	}
	
	/**
	 * Switch language
	 * @param context
	 * @param langCode
	 */
	public static void switchLanguage(Context context, String langCode){
		Locale locale = new Locale(langCode);  
		Locale.setDefault(locale); 
		Configuration config = new Configuration(); 
		config.locale = locale; 
		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
	}
	
	public static Locale getLocale(Context context){
		return new Locale(getLangCode(context));
	}
	
	public static void shutdown(){
		Process chperm;
		try {
			chperm = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(chperm.getOutputStream());

			os.writeBytes("shutdown\n");
			os.flush();

			chperm.waitFor();

		} catch (IOException e) {
			Log.d("Shutdown", e.getMessage());
		} catch (InterruptedException e) {
			Log.d("Shutdown", e.getMessage());
		}
	}
	
	public static void backupDatabase(Context context){
		String backupDbName = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + "_" + DB_NAME;
		File sd = Environment.getExternalStorageDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		File dbPath = context.getDatabasePath(DB_NAME);
		File sdPath = new File(sd, BACKUP_DB_PATH);
		if(!sdPath.exists())
			sdPath.mkdirs();
		deleteBackupDatabase(context);
		try {
			source = new FileInputStream(dbPath).getChannel();
			destination = new FileOutputStream(sdPath + File.separator + backupDbName).getChannel();
			destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
			makeToask(context, context.getString(R.string.backup_db_success));
		} catch (IOException e) {
			e.printStackTrace();
			makeToask(context, e.getLocalizedMessage());
		}
	}
	
	public static void deleteBackupDatabase(Context context){
		File sd = Environment.getExternalStorageDirectory();
		File sdPath = new File(sd, BACKUP_DB_PATH);
		File files[] = sdPath.listFiles();
		if(files != null){
			Calendar current = Calendar.getInstance();
			Calendar lastMod = Calendar.getInstance();
			for(File file : files){
				lastMod.setTimeInMillis(file.lastModified());
				if(getDiffDay(lastMod) > current.getActualMaximum(Calendar.DAY_OF_MONTH)){
					file.delete();
				}
			}
		}
	}
	
	public static LinearLayout.LayoutParams getLinHorParams(float weight){
		return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
	}
}
