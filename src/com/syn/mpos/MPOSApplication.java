package com.syn.mpos;

import android.app.Application;

public class MPOSApplication extends Application{
	public static GlobalVar sGlobalVar;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// initial sigleton GlobalVar
		sGlobalVar = GlobalVar.newInstance(getApplicationContext());
	}
}
