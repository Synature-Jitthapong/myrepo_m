package com.synature.mpos.common;

import com.synature.mpos.MyDefaultUncaughExceptionHandler;
import com.synature.mpos.Utils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MPOSFragmentActivityBase extends FragmentActivity{

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
