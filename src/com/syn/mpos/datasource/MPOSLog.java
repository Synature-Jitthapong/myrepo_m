package com.syn.mpos.datasource;

import android.content.Context;

import com.j1tth4.mobile.util.Logger;
import com.syn.mpos.MPOSApplication;

public class MPOSLog extends Logger{
	private static final String fileName = "mpos";
	
	public MPOSLog(Context c) {
		super(c, MPOSApplication.LOG_DIR, fileName);
	}

}
