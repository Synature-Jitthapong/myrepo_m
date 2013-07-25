package com.syn.pos.mobile.mpos.dao;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.mpos.MPOSVar;

public class MPOSTransaction implements IMPOSTransaction, IOrderDetail {
	
	ISqliteHelper dbHelper;
	MPOSVar mposVar;
	
	public MPOSTransaction (Context c, MPOSVar var){
		dbHelper = new MPOSSqliteHelper(c);
		mposVar = var;
	}
	
	@Override
	public long getMaxTransaction(int shopId, int computerId) {
		long transactionId = 0;
		
		String strSql = "SELECT MAX(TransactionID) FROM order_transaction " +
				" WHERE shop_id=" + shopId + " AND computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			transactionId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return ++transactionId;
	}

	@Override
	public void openTransaction(int transactionId, int computerId, int shopId,
			int sessionId, int staffId) {
		Date date = new Date();
		ContentValues cv = new ContentValues();
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("shop_id", shopId);
		cv.put("session_id", sessionId);
		cv.put("open_staff_id", staffId);
		cv.put("open_time", mposVar.dateTimeFormat.format(date));
		cv.put("open_staff_id", staffId);
		cv.put("sale_date", mposVar.dateFormat.format(date));
		cv.put("receipt_year", date.getYear());
		cv.put("receipt_month", date.getMonth());
	}

	@Override
	public void updateTransaction(int transactionId, int computerId,
			int staffId, double transVat, double transExclVat,
			double serviceCharge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxOrderDetail(int transactionId, int computerId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean addOrderDetail(int transactionId, int computerId,
			int productId, double productPrice) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteOrderDetail(int transactionId, int orderDetailId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getMaxReceiptId(int year, int month) {
		long maxReceiptId = 0;
		
		String strSql = "SELECT MAX(receipt_id) FROM order_transaction " +
				" WHERE receipt_year=" + year + 
				" AND receipt_month=" + month;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			maxReceiptId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return ++maxReceiptId;
	}

}
