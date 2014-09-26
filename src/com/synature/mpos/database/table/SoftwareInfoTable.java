package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SoftwareInfoTable {
	public static final String TABLE_SOFTWARE_INFO = "SoftwareInfo";
	public static final String COLUMN_EXP_DATE = "exp_date";
	public static final String COLUMN_LOCK_DATE = "lock_date";
	
	private static final String SQL_CREATE = 
			"create table " + TABLE_SOFTWARE_INFO + " ("
			+ COLUMN_EXP_DATE + " text, "
			+ COLUMN_LOCK_DATE + " text);";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_SOFTWARE_INFO);
		onCreate(db);
	}
}
