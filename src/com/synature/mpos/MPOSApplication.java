package com.synature.mpos;

import android.app.Application;

public class MPOSApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.switchLanguage(getApplicationContext(), Utils.getLangCode(getApplicationContext()));
	}
}
