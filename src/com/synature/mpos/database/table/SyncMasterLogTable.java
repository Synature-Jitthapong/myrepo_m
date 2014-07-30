package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SyncMasterLogTable {

	public static final String TABLE_SYNC_MASTER = "SyncMasterLog";
	public static final String COLUMN_SYNC_TYPE = "sync_type";
	public static final String COLUMN_SYNC_STATUS = "sync_status";
	public static final String COLUMN_SYNC_DATE = "sync_date";
	
	private static final String SQL_CREATE =
			" create table " + TABLE_SYNC_MASTER + " ( " 
			+ COLUMN_SYNC_TYPE + " integer not null, " 
			+ COLUMN_SYNC_STATUS + " integer default 0, "
			+ COLUMN_SYNC_DATE + " text not null, "
			+ " primary key (" + COLUMN_SYNC_TYPE + "));";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_SYNC_MASTER);
		onCreate(db);
	}
	
}