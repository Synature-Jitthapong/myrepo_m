package com.syn.mpos.database;

import android.database.sqlite.SQLiteDatabase;

public class ProductDeptTable{
	
	public static final String TABLE_NAME = "ProductDept";
	public static final String COLUMN_PRODUCT_DEPT_CODE = "product_dept_code";
	public static final String COLUMN_PRODUCT_DEPT_NAME = "product_dept_name";
	
	private static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " ( " +
			ProductsTable.COLUMN_PRODUCT_DEPT_ID + " INTEGER, " +
			ProductsTable.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			COLUMN_PRODUCT_DEPT_CODE + " TEXT, " +
			COLUMN_PRODUCT_DEPT_NAME + " TEXT, " +
			ProductsTable.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			ProductsTable.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "));";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
