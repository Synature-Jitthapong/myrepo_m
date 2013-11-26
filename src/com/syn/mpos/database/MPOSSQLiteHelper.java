package com.syn.mpos.database;

import com.j1tth4.mobile.sqlite.SqliteExternalDatabase;
import android.content.Context;

/**
 * 
 * @author j1tth4
 *
 */
public abstract class MPOSSQLiteHelper extends SqliteExternalDatabase{
	public static final String DB_DIR = "MPOSDB";
	public static final String DB_NAME = "mpos.db";
	
	// column uuid
	public static final String COL_UUID = "UUID";
	
	public static final String COMMA_SEP = ",";
			
	public MPOSSQLiteHelper(Context context) {
		super(context, DB_DIR, DB_NAME);
	}
	
	public void open() {
		openDataBase();
	}
	
	public void close() {
		closeDataBase();
	}
}
