package com.syn.mpos.datasource;

import android.database.sqlite.SQLiteDatabase;

public class HeaderFooterReceiptTable{
	
	public static final String TABLE_NAME = "HeaderFooterReceipt";
	public static final String COLUMN_TEXT_IN_LINE = "text_inline";
	public static final String COLUMN_LINE_TYPE = "line_type";
	public static final String COLUMN_LINE_ORDER = "line_order";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " ( " +
			COLUMN_TEXT_IN_LINE + " TEXT, " +
			COLUMN_LINE_TYPE + " INTEGER, " +
			COLUMN_LINE_ORDER + " INTEGER );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
