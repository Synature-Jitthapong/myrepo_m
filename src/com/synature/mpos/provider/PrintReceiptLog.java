package com.synature.mpos.provider;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.provider.Staffs.StaffTable;
import com.synature.mpos.provider.Transaction.OrderTransactionTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PrintReceiptLog extends MPOSDatabase{
	
	public static final int PRINT_NOT_SUCCESS = 0;
	public static final int PRINT_SUCCESS = 1;

	public PrintReceiptLog(Context context) {
		super(context);
	}
	
	/**
	 * @return List<PrintReceipt>
	 */
	public List<PrintReceipt> listPrintReceiptLog(){
		List<PrintReceipt> printLst = new ArrayList<PrintReceipt>();
		Cursor cursor = getReadableDatabase().query(PrintReceiptLogTable.TABLE_PRINT_LOG, 
				new String[]{
					OrderTransactionTable.COLUMN_TRANSACTION_ID,
					StaffTable.COLUMN_STAFF_ID,
					PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID,
					PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_TIME,
					PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS	
				}, PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS + "=?", 
				new String[]{
					String.valueOf(PRINT_NOT_SUCCESS)
				}, null, null, PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_TIME);
		if(cursor.moveToFirst()){
			do{
				PrintReceipt print = new PrintReceipt();
				print.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				print.setStaffId(cursor.getInt(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_ID)));
				print.setPriceReceiptLogId(cursor.getInt(cursor.getColumnIndex(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID)));
				print.setPrintReceiptLogTime(cursor.getString(cursor.getColumnIndex(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_TIME)));
				print.setPrintReceiptLogStatus(cursor.getInt(cursor.getColumnIndex(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS)));
				printLst.add(print);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return printLst;
	}
	
	/**
	 * @param printReceiptLogId
	 */
	public void deletePrintStatus(int printReceiptLogId){
		getWritableDatabase().delete(PrintReceiptLogTable.TABLE_PRINT_LOG, 
				PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{String.valueOf(printReceiptLogId)}  );
	}
	
	/**
	 * @param printReceiptLogId
	 * @param status
	 */
	public void updatePrintStatus(int printReceiptLogId, int status){
		ContentValues cv = new ContentValues();
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS, status);
		getWritableDatabase().update(PrintReceiptLogTable.TABLE_PRINT_LOG, cv, 
				PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{
					String.valueOf(printReceiptLogId)
				}
		);
	}
	
	/**
	 * @param transactionId
	 * @param staffId
	 * @throws SQLException
	 */
	public void insertLog(int transactionId, int staffId) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(StaffTable.COLUMN_STAFF_ID, staffId);
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_TIME, Util.getCalendar().getTimeInMillis());
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS, PRINT_NOT_SUCCESS);
		getWritableDatabase().insertOrThrow(PrintReceiptLogTable.TABLE_PRINT_LOG, null, cv);
	}

	public static class PrintReceiptLogTable{
		
		public static final String TABLE_PRINT_LOG = "PrintReceiptLog";
		public static final String COLUMN_PRINT_RECEIPT_LOG_ID = "print_receipt_log_id";
		public static final String COLUMN_PRINT_RECEIPT_LOG_TIME = "print_receipt_log_time";
		public static final String COLUMN_PRINT_RECEIPT_LOG_STATUS = "print_receipt_log_status";
		
		private static final String SQL_CREATE = 
				"CREATE TABLE " + TABLE_PRINT_LOG + "( " +
				COLUMN_PRINT_RECEIPT_LOG_ID + " INTEGER, " + 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER, " +
				StaffTable.COLUMN_STAFF_ID + " INTEGER, " +
				COLUMN_PRINT_RECEIPT_LOG_TIME + " TEXT, " +
				COLUMN_PRINT_RECEIPT_LOG_STATUS + " INTEGER DEFAULT 0, " +
				"PRIMARY KEY (" + COLUMN_PRINT_RECEIPT_LOG_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
	
	public static class PrintReceipt{
		private int priceReceiptLogId;
		private int transactionId;
		private int computerId;
		private int staffId;
		private String printReceiptLogTime;
		private int printReceiptLogStatus;
		
		public int getStaffId() {
			return staffId;
		}
		public void setStaffId(int staffId) {
			this.staffId = staffId;
		}
		public String getPrintReceiptLogTime() {
			return printReceiptLogTime;
		}
		public void setPrintReceiptLogTime(String printReceiptLogTime) {
			this.printReceiptLogTime = printReceiptLogTime;
		}
		public int getPrintReceiptLogStatus() {
			return printReceiptLogStatus;
		}
		public void setPrintReceiptLogStatus(int printReceiptLogStatus) {
			this.printReceiptLogStatus = printReceiptLogStatus;
		}
		public int getPriceReceiptLogId() {
			return priceReceiptLogId;
		}
		public void setPriceReceiptLogId(int priceReceiptLogId) {
			this.priceReceiptLogId = priceReceiptLogId;
		}
		public int getTransactionId() {
			return transactionId;
		}
		public void setTransactionId(int transactionId) {
			this.transactionId = transactionId;
		}
		public int getComputerId() {
			return computerId;
		}
		public void setComputerId(int computerId) {
			this.computerId = computerId;
		}
	}
}
