package com.syn.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SessionDetailTable {

	public static final String TABLE_SESSION_ENDDAY_DETAIL = "SessionEnddayDetail";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_SESSION_ENDDAY_DETAIL + " ( " +
			SessionTable.COLUMN_SESS_DATE + " TEXT, " +
			SessionTable.COLUMN_ENDDAY_DATE + " TEXT, " +
			SessionTable.COLUMN_TOTAL_QTY_RECEIPT + " INTEGER, " +
			SessionTable.COLUMN_TOTAL_AMOUNT_RECEIPT + " REAL, " +
			SessionTable.COLUMN_IS_SEND_TO_HQ + " INTEGER, " +
			SessionTable.COLUMN_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + SessionTable.COLUMN_SESS_DATE + "));";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
