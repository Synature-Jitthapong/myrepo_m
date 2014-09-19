package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SoftwareInfoTable {
	public static final String TABLE_SOFTWARE_INFO = "SoftwareInfo";
	public static final String COLUMN_SOFTWARE_INFO_ID = "software_info_id";
	public static final String COLUMN_VERSION = "version";
	public static final String COLUMN_DB_VERSION = "db_version";
	public static final String COLUMN_IS_DOWNLOADED = "is_downloaded";
	public static final String COLUMN_IS_ALREADY_UPDATE = "is_already_update";
	public static final String COLUMN_LAST_UPDATE = "last_update";
	
	private static final String SQL_CREATE = 
			"create table " + TABLE_SOFTWARE_INFO + " ("
			+ COLUMN_SOFTWARE_INFO_ID + " integer, "
			+ COLUMN_VERSION + " text, "
			+ COLUMN_DB_VERSION + " text, "
			+ COLUMN_LAST_UPDATE + " text, "
			+ COLUMN_IS_DOWNLOADED + " integer default 0, "
			+ COLUMN_IS_ALREADY_UPDATE + " integer default 0, "
			+ " primary key(" + COLUMN_SOFTWARE_INFO_ID + ");";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_SOFTWARE_INFO);
		onCreate(db);
	}
}
