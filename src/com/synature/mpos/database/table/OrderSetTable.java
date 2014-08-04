package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class OrderSetTable {

	public static final String TABLE_ORDER_SET = "OrderSet";
	public static final String COLUMN_ORDER_SET_ID = "order_set_id";
	public static final String COLUMN_ORDER_SET_QTY = "order_set_qty";
	public static final String COLUMN_ORDER_SET_PRICE = "order_set_price";
	public static final String COLUMN_ORDER_SET_PRICE_DISCOUNT = "order_set_price_discount";

	private static final String SQL_CREATE = 
			" create table " + TABLE_ORDER_SET + "( " 
			+ COLUMN_ORDER_SET_ID + " integer not null," 
			+ OrderDetailTable.COLUMN_ORDER_ID + " integer not null, "
			+ OrderTransactionTable.COLUMN_TRANS_ID + " integer not null, "
			+ ProductComponentTable.COLUMN_PGROUP_ID + " integer not null, " 
			+ ProductTable.COLUMN_PRODUCT_ID + " integer not null, " 
			+ ProductComponentGroupTable.COLUMN_REQ_AMOUNT + " real default 0, " 
			+ COLUMN_ORDER_SET_QTY + " real default 0, " 
			+ COLUMN_ORDER_SET_PRICE + " real default 0, "
			+ COLUMN_ORDER_SET_PRICE_DISCOUNT + " real default 0, "
			+ OrderCommentTable.COLUMN_SELF_ORDER_ID + " integer default 0, "
			+ " primary key (" + COLUMN_ORDER_SET_ID + "));";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
	}
}