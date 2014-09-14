package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SoftwareInfoTable {
	public static final String TABLE_SOFTWARE_INFO = "SoftwareInfo";
	public static final String COLUMN_VERSION = "version";
	public static final String COLUMN_DB_VERSION = "db_version";
	public static final String COLUMN_LAST_UPDATE = "last_update";
	
	private static final String SQL_CREATE = 
			"create table " + TABLE_SOFTWARE_INFO + " ("
			+ COLUMN_VERSION + " text, "
			+ COLUMN_DB_VERSION + " text, "
			+ COLUMN_LAST_UPDATE + " text);";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_SOFTWARE_INFO);
		onCreate(db);
	}
}
