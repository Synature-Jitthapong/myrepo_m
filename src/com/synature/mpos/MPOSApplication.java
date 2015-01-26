package com.synature.mpos;

import java.io.File;

import android.app.Application;

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
		Utils.switchLanguage(getApplicationContext(), Utils.getLangCode(getApplicationContext()));
	}
}
