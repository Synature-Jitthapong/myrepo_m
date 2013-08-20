package com.syn.mpos.data;

import android.content.Context;
import com.j1tth4.mobile.sqlite.SqliteDatabase;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSSqliteDatabase extends SqliteDatabase{
	private static final String dbDir = "mpos";
	private static final String dbName = "mpos.db";
	
	public MPOSSqliteDatabase(Context context) {
		super(context, dbDir, dbName);
	}
}
