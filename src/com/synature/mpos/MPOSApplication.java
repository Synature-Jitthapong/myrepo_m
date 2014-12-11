package com.synature.mpos;

import com.synature.util.Logger;

import android.app.Application;
import android.content.Context;
import android.os.Build;

public class MPOSApplication extends Application {

	private static Context sContext;
	
	@Override
	public void onCreate() {
		sContext = getApplicationContext();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				StackTraceElement[] arr = e.getStackTrace();
				StringBuilder report = new StringBuilder();
				report.append(e.toString() + "\n\r");
				report.append("--------- Stack trace ---------\n\r");
				for (int i = 0; i < arr.length; i++){
					report.append("    " + arr[i].toString() + "\n\r");
				}
				report.append("-------------------------------\n\r");

				// If the exception was thrown in a background thread inside

				// AsyncTask, then the actual exception can be found with
				// getCause
				report.append("--------- Cause ---------\n\r");
				Throwable cause = e.getCause();
				if (cause != null) {
					report.append(cause.toString() + "\n\r");
					arr = cause.getStackTrace();
					for (int i = 0; i < arr.length; i++){
						report.append("    " + arr[i].toString() + "\n\r");
					}
				}

				/**
				 * 
				 * Getting the Device brand,model and sdk verion details.
				 */
				report.append("--------- Device ---------\n\r");
				report.append("Brand: " + Build.BRAND + "\n\r");
				report.append("Device: " + Build.DEVICE + "\n\r");
				report.append("Model: " + Build.MODEL + "\n\r");
				report.append("Id: " + Build.ID + "\n\r");
				report.append("Product: " + Build.PRODUCT + "\n\r");
				report.append("-------------------------------\n\r");
				report.append("--------- Firmware ---------\n\r");
				report.append("SDK: " + Build.VERSION.SDK + "\n\r");
				report.append("Release: " + Build.VERSION.RELEASE + "\n\r");
				report.append("Incremental: " + Build.VERSION.INCREMENTAL + "\n\r");
				report.append("-------------------------------\n\n\r");
				Logger.appendLog(getApplicationContext(), Utils.ERR_LOG_PATH, Utils.ERR_LOG_FILE_NAME, report.toString());
				System.exit(0);
			}
		});
	}
	
	public static Context getContext(){
		return sContext;
	}
}
