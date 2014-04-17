package com.syn.mpos.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class OrderSet extends MPOSDatabase{

	public static final String[] projection = {
		OrderSetTable.COLUMN_ORDER_SET_ID,
		OrderTransactionTable.COLUMN_TRANSACTION_ID,
		OrderDetailTable.COLUMN_ORDER_ID,
		ProductsTable.COLUMN_PRODUCT_ID,
		ProductsTable.COLUMN_PRODUCT_NAME,
		ProductComponentGroupTable.COLUMN_REQ_AMOUNT,
		OrderSetTable.COLUMN_ORDER_SET_QTY
	};
	
	public OrderSet(SQLiteDatabase db) {
		super(db);
	}
	
	public Cursor listOrderSetDetail(int transactionId, int orderDetailId, int pcompGroupId){
		return mSqlite.query(OrderSetTable.TABLE_NAME, 
				projection, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
						+ " AND " + ProductComponentTable.COLUMN_PGROUP_ID + "=?",
						new String[]{String.valueOf(transactionId), 
								String.valueOf(orderDetailId), String.valueOf(pcompGroupId)}, 
								null, null, null);
	}
	
	public Cursor listGroupOrderSet(int transactionId, int orderDetailId){
		return mSqlite.rawQuery(
				" SELECT b." + ProductComponentTable.COLUMN_PGROUP_ID + ", "  
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME + ", "
				+ " FROM " + OrderSetTable.TABLE_NAME + " a "
				+ " LEFT JOIN " + ProductComponentGroupTable.TABLE_NAME + " b "
				+ " ON a." + ProductComponentTable.COLUMN_PGROUP_ID + "=" 
				+ " b." + ProductComponentTable.COLUMN_PGROUP_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(orderDetailId)});
	}
	
	public void addOrderSet(int transactionId, int orderDetailId, int productId, 
			String productName, double reqAmount){
		
		int maxOrderSetId = getMaxOrderSetId(transactionId, orderDetailId);
		
		ContentValues cv = new ContentValues();
		cv.put(OrderSetTable.COLUMN_ORDER_SET_ID, maxOrderSetId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(ProductsTable.COLUMN_PRODUCT_ID, productId);
		cv.put(ProductsTable.COLUMN_PRODUCT_NAME, productName);
		cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, reqAmount);
		
		mSqlite.insertOrThrow(OrderSetTable.TABLE_NAME, ProductsTable.COLUMN_PRODUCT_NAME, cv);
	}
	
	public int getMaxOrderSetId(int transactionId, int orderDetailId){
		int maxOrderSetId = 0;
		
		Cursor cursor = mSqlite.rawQuery(
				"SELECT MAX (" + OrderSetTable.COLUMN_ORDER_SET_ID + ")"
						+ " FROM " + OrderSetTable.TABLE_NAME 
						+ " WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID 
						+ " =? AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
						new String[]{String.valueOf(transactionId), String.valueOf(orderDetailId)});
		if(cursor.moveToFirst()){
			maxOrderSetId = cursor.getInt(0);
		}
		cursor.close();
		return maxOrderSetId + 1;
	}
}
