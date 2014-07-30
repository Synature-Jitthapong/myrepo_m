package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class OrderCommentTable{
	
	public static final String TABLE_ORDER_COMMENT = "OrderComment";
	
	public static final String TEMP_ORDER_COMMENT = "OrderCommentTemp";
	
	public static final String COLUMN_ORDER_COMMENT_QTY = "order_comment_qty";
	public static final String COLUMN_ORDER_COMMENT_PRICE = "order_comment_price";
	public static final String COLUMN_ORDER_COMMENT_PRICE_DISCOUNT = "order_comment_price_discount";
	public static final String COLUMN_SELF_ORDER_ID = "self_order_detail_id";
	
	private static final String SQL_CREATE = 
			" create table " + TABLE_ORDER_COMMENT + " ( "
			+ OrderTransactionTable.COLUMN_TRANS_ID + " integer not null, "
			+ OrderDetailTable.COLUMN_ORDER_ID + " integer not null, "
			+ MenuCommentTable.COLUMN_COMMENT_ID + " integer not null, "
			+ COLUMN_SELF_ORDER_ID + " integer default 0, "
			+ COLUMN_ORDER_COMMENT_QTY + " real default 0, "
			+ COLUMN_ORDER_COMMENT_PRICE + " real default 0, "
			+ COLUMN_ORDER_COMMENT_PRICE_DISCOUNT + " real default 0 );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
	}
}
