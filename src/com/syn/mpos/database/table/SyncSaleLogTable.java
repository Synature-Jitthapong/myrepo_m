package com.syn.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SyncSaleLogTable{

	public static final String TABLE_NAME = "SyncSaleLog";
	public static final String COLUMN_SYNC_STATUS = "sync_status";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " ( " +
			SessionTable.COLUMN_SESS_DATE + " TEXT, " + 
			COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0, " + 
			" PRIMARY KEY (" + SessionTable.COLUMN_SESS_DATE + ")" + " );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
