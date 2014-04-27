package com.syn.mpos.database.table;

import com.syn.mpos.database.StaffTable;

import android.database.sqlite.SQLiteDatabase;

public class PrintReceiptLogTable{
	
	public static final String TABLE_NAME = "PrintReceiptLog";
	public static final String COLUMN_PRINT_RECEIPT_LOG_ID = "print_receipt_log_id";
	public static final String COLUMN_PRINT_RECEIPT_LOG_TIME = "print_receipt_log_time";
	public static final String COLUMN_PRINT_RECEIPT_LOG_STATUS = "print_receipt_log_status";
	
	private static final String SQL_CREATE = 
			"CREATE TABLE " + TABLE_NAME + "( " +
			COLUMN_PRINT_RECEIPT_LOG_ID + " INTEGER, " + 
			OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, " +
			StaffTable.COLUMN_STAFF_ID + " INTEGER, " +
			COLUMN_PRINT_RECEIPT_LOG_TIME + " TEXT, " +
			COLUMN_PRINT_RECEIPT_LOG_STATUS + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + COLUMN_PRINT_RECEIPT_LOG_ID + " AUTOINCREMENT ) );";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
