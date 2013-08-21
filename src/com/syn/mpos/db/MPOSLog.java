package com.syn.mpos.db;

import android.content.Context;

import com.j1tth4.mobile.util.Logger;

public class MPOSLog extends Logger{
	private static final String logDir = "mpos";
	private static final String fileName = "mpos";
	
	public MPOSLog(Context c) {
		super(c, logDir, fileName);
	}

}
