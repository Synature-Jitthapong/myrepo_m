package com.syn.pos.mobile.mpos.dao;

import android.content.Context;

import com.j1tth4.mobile.core.sqlite.SqliteDatabase;

public class MPOSSqliteDatabase extends SqliteDatabase{
	private static final String dbDir = "mpos";
	private static final String dbName = "mpos.db";
	
	public MPOSSqliteDatabase(Context context) {
		super(context, dbDir, dbName);
	}

}
