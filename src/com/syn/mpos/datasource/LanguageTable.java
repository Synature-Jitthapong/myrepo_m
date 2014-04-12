package com.syn.mpos.datasource;

import android.database.sqlite.SQLiteDatabase;

public class LanguageTable{
	
	public static final String TABLE_NAME = "Language";
	public static final String COLUMN_LANG_ID = "lang_id";
	public static final String COLUMN_LANG_NAME = "lang_name";
	public static final String COLUMN_LANG_CODE = "lang_code";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " ( " +
			COLUMN_LANG_ID + " INTEGER DEFAULT 1, " +
			COLUMN_LANG_NAME + " TEXT, " +
			COLUMN_LANG_CODE + " TEXT DEFAULT 'en', " +
			"PRIMARY KEY (" + COLUMN_LANG_ID + ") );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
