package com.synature.mpos.database;

import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.database.table.PayTypeTable;
import com.synature.mpos.database.table.PaymentDetailTable;
import com.synature.mpos.database.table.PaymentDetailWasteTable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDetailWasteDao{

	private SQLiteDatabase mDatabase;
	
	public PaymentDetailWasteDao(SQLiteDatabase db) {
		mDatabase = db;
	}

	protected void addPaymentDetailWaste(int transactionId, int computerId, 
			int payTypeId, double payAmount, String remark) throws SQLException{
		int maxId = getMaxPaymentDetailWasteId();
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailTable.COLUMN_PAY_ID, maxId);
		cv.put(OrderTransTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payTypeId);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, payAmount);
		cv.put(PaymentDetailTable.COLUMN_REMARK, remark);
		mDatabase.insertOrThrow(PaymentDetailWasteTable.TEMP_PAYMENT_DETAIL_WASTE, 
				PaymentDetailTable.COLUMN_REMARK, cv);
	}

	protected void deletePaymentDetailWaste(int transactionId){
		delete(OrderTransTable.COLUMN_TRANS_ID + "=?", 
				new String[]{
					String.valueOf(transactionId)
				});
	}

	protected void deletePaymentDetailWaste(int transactionId, int paymentId){
		delete(OrderTransTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + PaymentDetailTable.COLUMN_PAY_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(paymentId)
				});
	}
	
	private void delete(String whereClause, String[] whereArgs){
		mDatabase.delete(
				PaymentDetailWasteTable.TEMP_PAYMENT_DETAIL_WASTE,
				whereClause,
				whereArgs);	
	}
	
	protected void moveToRealTable(int transactionId) throws SQLException{
		String where = OrderTransTable.COLUMN_TRANS_ID + "=" + transactionId;
		mDatabase.execSQL("insert into " + PaymentDetailWasteTable.TABLE_PAYMENT_DETAIL_WASTE
				+ " select * from " + PaymentDetailWasteTable.TEMP_PAYMENT_DETAIL_WASTE
				+ " where " + where);
		mDatabase.execSQL("delete from " + PaymentDetailTable.TEMP_PAYMENT_DETAIL
				+ " where " + where);
	}
	
	private int getMaxPaymentDetailWasteId(){
		int maxId = 0;
		Cursor cursor = mDatabase.rawQuery(
				"select max(" + PaymentDetailTable.COLUMN_PAY_ID + ")"
				+ " from " + PaymentDetailWasteTable.TABLE_PAYMENT_DETAIL_WASTE, null);
		if(cursor.moveToFirst()){
			maxId = cursor.getInt(0);
		}
		return maxId + 1;
	}
}
