package com.syn.pos.mobile.mpos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.ShopData;

public class MPOSTransaction implements IMPOSTransaction, IOrderDetail {
	
	ISqliteHelper dbHelper;
	ShopData shopData;
	
	public MPOSTransaction (Context c, ShopData sd){
		dbHelper = new MPOSSqliteHelper(c);
		shopData = sd;
	}
	
	@Override
	public long getMaxTransaction(int shopId, int computerId) {
		long transactionId = 0;
		
		String strSql = "SELECT MAX(TransactionID) FROM order_transaction " +
				" WHERE shop_id=" + shopId + " AND computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			transactionId = cursor.getInt(0);
		}
		cursor.close();
		dbHelper.close();
		
		return ++transactionId;
	}

	@Override
	public void openTransaction(int transactionId, int computerId, int shopId,
			int sessionId, int staffId) {
		ContentValues cv = new ContentValues();
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("shop_id", shopId);
		cv.put("session_id", sessionId);
		cv.put("open_staff_id", staffId);
		//cv.put("open_time");
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

}
