package com.syn.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class ProductGroupTable{
	
	public static final String TABLE_NAME = "ProductGroup";
	public static final String COLUMN_PRODUCT_GROUP_CODE = "product_group_code";
	public static final String COLUMN_PRODUCT_GROUP_NAME = "product_group_name";
	public static final String COLUMN_PRODUCT_GROUP_TYPE = "product_group_type";
	public static final String COLUMN_IS_COMMENT = "is_comment";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " ( " +
			ProductsTable.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			COLUMN_PRODUCT_GROUP_CODE + " TEXT, " +
			COLUMN_PRODUCT_GROUP_NAME + " TEXT, " +
			COLUMN_PRODUCT_GROUP_TYPE + " INTEGER DEFAULT 0, " +
			COLUMN_IS_COMMENT + " INTEGER DEFAULT 0, " +
			ProductsTable.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			ProductsTable.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ProductsTable.COLUMN_PRODUCT_GROUP_ID + "));";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
		
}
