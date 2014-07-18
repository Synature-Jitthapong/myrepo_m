package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class PromotionProductDiscountTable {
	
	public static final String TABLE_PROMOTION_PRODUCT_DISCOUNT = "promotion_product_discount";
	public static final String COLUMN_PRICE_GROUP_ID = "price_group_id";
	public static final String COLUMN_DISCOUNT_AMOUNT = "discount_amount";
	public static final String COLUMN_DISCOUNT_PERCENT = "discount_percent";
	public static final String COLUMN_AMOUNT_OR_PERCENT = "amount_or_percent";
	
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_PROMOTION_PRODUCT_DISCOUNT + "("
			+ COLUMN_PRICE_GROUP_ID + " INTEGER, "
			+ ProductTable.COLUMN_PRODUCT_ID + " INTEGER, "
			+ ProductTable.COLUMN_SALE_MODE + " INTEGER, "
			+ COLUMN_DISCOUNT_AMOUNT + " REAL, "
			+ COLUMN_DISCOUNT_PERCENT + " REAL, "
			+ COLUMN_AMOUNT_OR_PERCENT + " INTEGER DEFAULT 0"
			+ ");";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROMOTION_PRODUCT_DISCOUNT);
		onCreate(db);
	}
}
