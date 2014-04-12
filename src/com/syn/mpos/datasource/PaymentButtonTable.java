package com.syn.mpos.datasource;

import android.database.sqlite.SQLiteDatabase;

public class PaymentButtonTable{

	public static final String TABLE_NAME = "PaymentAmountButton";
	public static final String COLUMN_PAYMENT_AMOUNT_ID = "payment_amount_id";
	public static final String COLUMN_PAYMENT_AMOUNT = "payment_amount";
	public static final String COLUMN_ORDERING = "ordering";

	private static final String SQL_CREATE = 
			"CREATE TABLE " + TABLE_NAME + "( " +
			COLUMN_PAYMENT_AMOUNT_ID + " INTEGER, " +
			COLUMN_PAYMENT_AMOUNT + " REAL DEFAULT 0, " +
			COLUMN_ORDERING + " INTEGER DEFAULT 0 );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
