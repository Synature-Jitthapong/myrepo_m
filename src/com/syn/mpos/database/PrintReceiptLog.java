package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class PrintReceiptLog extends MPOSDatabase{
	
	public static final int PRINT_NOT_SUCCESS = 0;
	public static final int PRINT_SUCCESS = 1;

	public PrintReceiptLog(Context c) {
		super(c);
	}
	
	public List<PrintReceipt> listPrintReceiptLog(){
		List<PrintReceipt> printLst = new ArrayList<PrintReceipt>();
		Cursor cursor = mSqlite.query(PrintReceiptLogTable.TABLE_NAME, 
				new String[]{
					OrderTransactionTable.COLUMN_TRANSACTION_ID,
					ComputerTable.COLUMN_COMPUTER_ID,
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
				print.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
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
	
	public void deletePrintStatus(int printReceiptLogId){
		mSqlite.delete(PrintReceiptLogTable.TABLE_NAME, 
				PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{String.valueOf(printReceiptLogId)}  );
	}
	
	public void updatePrintStatus(int printReceiptLogId, int status){
		ContentValues cv = new ContentValues();
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS, status);
		mSqlite.update(PrintReceiptLogTable.TABLE_NAME, cv, 
				PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{
					String.valueOf(printReceiptLogId)
				}
		);
	}
	
	public void insertLog(int transactionId, int computerId, int staffId) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(StaffTable.COLUMN_STAFF_ID, staffId);
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_TIME, Util.getDateTime().getTimeInMillis());
		cv.put(PrintReceiptLogTable.COLUMN_PRINT_RECEIPT_LOG_STATUS, PRINT_NOT_SUCCESS);
		mSqlite.insertOrThrow(PrintReceiptLogTable.TABLE_NAME, null, cv);
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
