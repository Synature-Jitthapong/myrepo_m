package com.syn.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class SessionDetailTable {

	public static final String TABLE_SESSION_ENDDAY_DETAIL = "SessionEnddayDetail";
	public static final String COLUMN_ENDDAY_DATE = "endday_date_time";
	public static final String COLUMN_TOTAL_QTY_RECEIPT = "total_qty_receipt";
	public static final String COLUMN_TOTAL_AMOUNT_RECEIPT = "total_amount_receipt";

	private static final String SQL_CREATE = "CREATE TABLE "
			+ TABLE_SESSION_ENDDAY_DETAIL + " ( "
			+ SessionTable.COLUMN_SESS_DATE + " TEXT, " + COLUMN_ENDDAY_DATE
			+ " TEXT, " + COLUMN_TOTAL_QTY_RECEIPT + " INTEGER, "
			+ COLUMN_TOTAL_AMOUNT_RECEIPT + " REAL, "
			+ BaseColumn.COLUMN_SEND_STATUS + " INTEGER, " + "PRIMARY KEY ("
			+ SessionTable.COLUMN_SESS_DATE + "));";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {

	}

}
