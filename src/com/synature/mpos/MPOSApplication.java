package com.synature.mpos;

import android.app.Application;
import android.content.Intent;

public class MPOSApplication extends Application {

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				handleUncaughtException(thread, e);
			}
		});
		Utils.switchLanguage(getApplicationContext(),
				Utils.getLangCode(getApplicationContext()));
	}

	public void handleUncaughtException(Thread thread, Throwable e) {
		e.printStackTrace(); 
		Intent intent = new Intent(this, ErrorLogMailingService.class);
		startService(intent);
	}
}
