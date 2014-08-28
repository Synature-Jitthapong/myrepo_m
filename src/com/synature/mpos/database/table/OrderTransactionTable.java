package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class OrderTransactionTable extends BaseColumn {
	public static final String TABLE_ORDER_TRANS = "OrderTransaction";
	public static final String COLUMN_TRANS_ID = "transaction_id";
	public static final String COLUMN_RECEIPT_YEAR = "receipt_year";
	public static final String COLUMN_RECEIPT_MONTH = "receipt_month";
	public static final String COLUMN_RECEIPT_ID = "receipt_id";
	public static final String COLUMN_RECEIPT_NO = "receipt_no";
	public static final String COLUMN_STATUS_ID = "transaction_status_id";
	public static final String COLUMN_PAID_TIME = "paid_time";
	public static final String COLUMN_PAID_STAFF_ID = "paid_staff_id";
	public static final String COLUMN_SALE_DATE = "sale_date";
	public static final String COLUMN_TRANS_VAT = "transaction_vat";
	public static final String COLUMN_TRANS_VATABLE = "transaction_vatable";
	public static final String COLUMN_TRANS_EXCLUDE_VAT = "transaction_exclude_vat";
	public static final String COLUMN_TRANS_NOTE = "transaction_note";
	public static final String COLUMN_VOID_STAFF_ID = "void_staff_id";
	public static final String COLUMN_VOID_REASON = "void_reason";
	public static final String COLUMN_VOID_TIME = "void_time";
	public static final String COLUMN_OTHER_DISCOUNT = "other_discount";
	public static final String COLUMN_MEMBER_ID = "member_id";
	public static final String COLUMN_DOC_TYPE_ID = "document_type_id";

	private static final String SQL_CREATE = 
			" create table " + TABLE_ORDER_TRANS + " ( " 
			+ COLUMN_UUID + " text not null, " 
			+ COLUMN_TRANS_ID + " integer not null, "
			+ ComputerTable.COLUMN_COMPUTER_ID + " integer not null, "
			+ ShopTable.COLUMN_SHOP_ID + " integer not null, " 
			+ PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID + " integer default 0, "
			+ COLUMN_OPEN_TIME + " text not null, " 
			+ COLUMN_OPEN_STAFF + " integer not null, "
			+ COLUMN_PAID_TIME + " text, " 
			+ COLUMN_PAID_STAFF_ID + " integer, " 
			+ COLUMN_CLOSE_TIME + " text, "
			+ COLUMN_CLOSE_STAFF + " integer, " 
			+ COLUMN_STATUS_ID + " integer default 1, " 
			+ COLUMN_DOC_TYPE_ID + " integer default 8, " 
			+ COLUMN_RECEIPT_YEAR + " integer not null, "
			+ COLUMN_RECEIPT_MONTH + " integer not null, " 
			+ COLUMN_RECEIPT_ID + " integer, " 
			+ COLUMN_RECEIPT_NO + " text, "
			+ COLUMN_SALE_DATE + " text not null, " 
			+ SessionTable.COLUMN_SESS_ID + " integer not null, " 
			+ COLUMN_VOID_STAFF_ID + " integer, "
			+ COLUMN_VOID_REASON + " text, " 
			+ COLUMN_VOID_TIME + " text, "
			+ COLUMN_MEMBER_ID + " integer, " 
			+ COLUMN_TRANS_VAT + " real default 0, " 
			+ COLUMN_TRANS_EXCLUDE_VAT + " real default 0, " 
			+ COLUMN_TRANS_VATABLE + " real default 0, " 
			+ COLUMN_TRANS_NOTE + " text, "
			+ COLUMN_OTHER_DISCOUNT + " real default 0, "
			+ COLUMN_SEND_STATUS + " integer default 0, "
			+ ProductTable.COLUMN_SALE_MODE + " integer default 1, "
			+ ProductTable.COLUMN_VAT_RATE + " real default 0, "
			+ " primary key (" + COLUMN_TRANS_ID + ") ); ";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
	}
}
