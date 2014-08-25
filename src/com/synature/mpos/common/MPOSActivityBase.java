package com.synature.mpos.common;

import com.synature.mpos.MyDefaultUncaughExceptionHandler;
import com.synature.mpos.Utils;

import android.app.Activity;
import android.os.Bundle;

public class MPOSActivityBase extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new MyDefaultUncaughExceptionHandler(this, 
				Utils.LOG_PATH, Utils.LOG_FILE_NAME));
	}

}
