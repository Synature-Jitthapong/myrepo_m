package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SoftwareUpdateTable {
	public static final String TABLE_SOFTWARE_UPDATE = "SoftwareUpdate";
	public static final String COLUMN_VERSION = "version";
	public static final String COLUMN_IS_DOWNLOADED = "is_downloaded";
	public static final String COLUMN_IS_ALREADY_UPDATED = "is_already_updated";
	
	private static final String SQL_CREATE = 
			"create table " + TABLE_SOFTWARE_UPDATE + " ("
			+ COLUMN_VERSION + " text, "
			+ COLUMN_IS_DOWNLOADED + " integer default 0, "
			+ COLUMN_IS_ALREADY_UPDATED + " integer default 0);";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < 5){
			db.execSQL("drop table if exists " + TABLE_SOFTWARE_UPDATE);
			onCreate(db);
		}
	}
}
