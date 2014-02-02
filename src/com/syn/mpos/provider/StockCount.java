package com.syn.mpos.provider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StockCount extends StockDocument {

	public StockCount(SQLiteDatabase db) {
		super(db);
	}
	
	/**
	 * constructor for create summary stock
	 * @param context
	 * @param dateFrom
	 * @param dateTo
	 */
	public StockCount(SQLiteDatabase db, long dateFrom, long dateTo){
		super(db);
	}

	public Cursor listStock(long dateFrom, long dateTo){
		//double currStock = getCurrentStock(dateFrom, dateTo);
		return mSqlite.rawQuery(
				"SELECT SUM(", 
				new String[]{});
	}
	
	public double getCurrentStock(int productId, long dateFrom, long dateTo){
		double currStock = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT (a." + StockDocument.COLUMN_PRODUCT_AMOUNT + "*" + 
				"b." + StockDocument.COLUMN_MOVE_MENT + ") " +
				"FROM " + StockDocument.TABLE_DOC_DETAIL + " a " +
				"LEFT JOIN " + StockDocument.TABLE_DOCUMENT_TYPE + " b " +
				"ON a.DocumentId=b.DocumentId " +
				"AND a.ShopID=b.ShopID " +
				"LEFT JOIN DocumentType c " +
				"ON a.DocumentTypeId=c.DocumentTypeId " +
				"WHERE c." + Products.COLUMN_PRODUCT_ID + 
				"AND c.MovementInStock != 0 " +
				"AND a.DocumentDate BETWEEN ? AND ? " +
				"GROUP BY c.DocumentTypeId", 
				new String[]{String.valueOf(dateFrom),  String.valueOf(dateTo)});
		
		if(cursor.moveToFirst()){
			currStock = cursor.getFloat(0);
		}
		cursor.close();
		return currStock;
	}
}
