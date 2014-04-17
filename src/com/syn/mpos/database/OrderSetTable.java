package com.syn.mpos.database;

import android.database.sqlite.SQLiteDatabase;

public class OrderSetTable {
	
	public static final String TABLE_NAME = "OrderSet";
	public static final String COLUMN_ORDER_SET_ID = "order_set_id";
	public static final String COLUMN_ORDER_SET_QTY = "order_set_qty";
	
	private static final String SQL_CREATE = 
			"CREATE TABLE " + TABLE_NAME + "( "
			+ COLUMN_ORDER_SET_ID + " INTEGER NOT NULL,"
			+ OrderDetailTable.COLUMN_ORDER_ID + " INTEGER NOT NULL, "
			+ OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER NOT NULL, "
			+ ProductComponentTable.COLUMN_PGROUP_ID + " INTEGER NOT NULL, "
			+ ProductsTable.COLUMN_PRODUCT_ID + " INTEGER NOT NULL, " 
			+ ProductsTable.COLUMN_PRODUCT_NAME + " TEXT, "
			+ ProductComponentGroupTable.COLUMN_REQ_AMOUNT + " REAL NOT NULL DEFAULT 0, "
			+ COLUMN_ORDER_SET_QTY + " REAL NOT NULL DEFAULT 0, "
			+ "PRIMARY KEY (" + COLUMN_ORDER_SET_ID + ", " + OrderDetailTable.COLUMN_ORDER_ID + ", "
			+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db){
		db.execSQL(SQL_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}
}
