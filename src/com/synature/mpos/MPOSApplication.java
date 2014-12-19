package com.synature.mpos;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.synature.util.Logger;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

public class MPOSApplication extends Application {

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
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
				Logger.appendLog(getApplicationContext(), Utils.ERR_LOG_PATH, "", report.toString());
				
				postStackTraceToServer(report.toString());
				System.gc();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
	}
	
	private void postStackTraceToServer(String stackTrace){
		Intent intent = new Intent(getApplicationContext(), RemoteStackTraceService.class);
		intent.putExtra("stackTrace", stackTrace);
		startService(intent);
	}
}
