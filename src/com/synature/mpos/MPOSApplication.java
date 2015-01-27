package com.synature.mpos;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.synature.util.Logger;

import android.app.Application;
import android.os.Build;

public class MPOSApplication extends Application {

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
	 * WebService point name (ABC Point)
	 */
	public static final String WS_POINT_NAME = "ws_abcpoint.asmx";
	
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
	 * Error log path
	 */
	public static final String ERR_LOG_PATH = RESOURCE_DIR + File.separator + "error";
	
	/**
	 * Sale dir store partial sale json file
	 */
	public static final String SALE_PATH = RESOURCE_DIR + File.separator + "Sale";
	
	/**
	 * Endday sale dir store endday sale json file
	 */
	public static final String ENDDAY_PATH = RESOURCE_DIR + File.separator + "EnddaySale";

	/**
	 * Update path stored apk for update.
	 */
	public static final String UPDATE_PATH = RESOURCE_DIR + File.separator + "Update";
	
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
	
	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
		Utils.switchLanguage(getApplicationContext(), Utils.getLangCode(getApplicationContext()));
	}
	
	private class MyUncaughtExceptionHandler implements UncaughtExceptionHandler{

		private UncaughtExceptionHandler mDefaultUEH;
		
		public MyUncaughtExceptionHandler() {
			mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		}
		
		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			StackTraceElement[] arr = e.getStackTrace();
			StringBuilder report = new StringBuilder();
			report.append(new SimpleDateFormat("HH:mm:ss").format(
					Calendar.getInstance().getTime()) + "\n");
			report.append(e.toString() + "\n");
			report.append("--------- Stack trace ---------\n");
			for (int i = 0; i < arr.length; i++){
				report.append("    " + arr[i].toString() + "\n");
			}
			Throwable cause = e.getCause();
			if (cause != null) {
				report.append("--------- Cause ---------\n");
				report.append(cause.toString() + "\n");
				arr = cause.getStackTrace();
				for (int i = 0; i < arr.length; i++){
					report.append("    " + arr[i].toString() + "\n");
				}
			}
			report.append("--------- Device ---------\n");
			report.append("Brand: " + Build.BRAND + "\n");
			report.append("Device: " + Build.DEVICE + "\n");
			report.append("Model: " + Build.MODEL + "\n");
			report.append("Id: " + Build.ID + "\n\r");
			report.append("Product: " + Build.PRODUCT + "\n");
			report.append("--------- Firmware ---------\n");
			report.append("SDK: " + Build.VERSION.SDK + "\n");
			report.append("Release: " + Build.VERSION.RELEASE + "\n");
			report.append("Incremental: " + Build.VERSION.INCREMENTAL + "\n");
			report.append("-------------------------------\n");
			Logger.appendLog(getApplicationContext(), ERR_LOG_PATH, "", report.toString());
			
			mDefaultUEH.uncaughtException(thread, e);
		}
		
	}
}
