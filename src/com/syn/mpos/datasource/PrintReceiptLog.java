package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.datasource.Computer.ComputerEntry;
import com.syn.mpos.datasource.Staff.StaffEntry;
import com.syn.mpos.datasource.Transaction.TransactionEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PrintReceiptLog extends MPOSDatabase{
	public static final int PRINT_NOT_SUCCESS = 0;
	public static final int PRINT_SUCCESS = 1;

	public PrintReceiptLog(SQLiteDatabase db) {
		super(db);
	}
	
	public List<PrintReceipt> listPrintReceiptLog(){
		List<PrintReceipt> printLst = new ArrayList<PrintReceipt>();
		Cursor cursor = mSqlite.query(PrintLogEntry.TABLE_PRINT_RECEIPT_LOG, 
				new String[]{
					TransactionEntry.COLUMN_TRANSACTION_ID,
					ComputerEntry.COLUMN_COMPUTER_ID,
					StaffEntry.COLUMN_STAFF_ID,
					PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID,
					PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME,
					PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS	
				}, PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS + "=?", 
				new String[]{
					String.valueOf(PRINT_NOT_SUCCESS)
				}, null, null, PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME);
		if(cursor.moveToFirst()){
			do{
				PrintReceipt print = new PrintReceipt();
				print.setTransactionId(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_ID)));
				print.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerEntry.COLUMN_COMPUTER_ID)));
				print.setStaffId(cursor.getInt(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_ID)));
				print.setPriceReceiptLogId(cursor.getInt(cursor.getColumnIndex(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID)));
				print.setPrintReceiptLogTime(cursor.getString(cursor.getColumnIndex(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME)));
				print.setPrintReceiptLogStatus(cursor.getInt(cursor.getColumnIndex(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS)));
				printLst.add(print);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return printLst;
	}
	
	public void deletePrintStatus(int printReceiptLogId){
		mSqlite.delete(PrintLogEntry.TABLE_PRINT_RECEIPT_LOG, 
				PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{String.valueOf(printReceiptLogId)}  );
	}
	
	public void updatePrintStatus(int printReceiptLogId, int status){
		ContentValues cv = new ContentValues();
		cv.put(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS, status);
		mSqlite.update(PrintLogEntry.TABLE_PRINT_RECEIPT_LOG, cv, 
				PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + "=?", 
				new String[]{
					String.valueOf(printReceiptLogId)
				}
		);
	}
	
	public void insertLog(int transactionId, int computerId, int staffId) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(TransactionEntry.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerEntry.COLUMN_COMPUTER_ID, computerId);
		cv.put(StaffEntry.COLUMN_STAFF_ID, staffId);
		cv.put(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME, Util.getDateTime().getTimeInMillis());
		cv.put(PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS, PRINT_NOT_SUCCESS);
		mSqlite.insertOrThrow(PrintLogEntry.TABLE_PRINT_RECEIPT_LOG, null, cv);
	}
	
	public static abstract class PrintLogEntry{
		public static final String TABLE_PRINT_RECEIPT_LOG = "PrintReceiptLog";
		public static final String COLUMN_PRINT_RECEIPT_LOG_ID = "print_receipt_log_id";
		public static final String COLUMN_PRINT_RECEIPT_LOG_TIME = "print_receipt_log_time";
		public static final String COLUMN_PRINT_RECEIPT_LOG_STATUS = "print_receipt_log_status";
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
