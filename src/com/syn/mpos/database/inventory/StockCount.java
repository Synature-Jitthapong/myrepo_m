package com.syn.mpos.database.inventory;

import com.syn.mpos.database.Products;
import android.content.Context;
import android.database.Cursor;

public class StockCount extends StockDocument {

	public StockCount(Context c) {
		super(c);
	}
	
	/**
	 * constructor for create summary stock
	 * @param context
	 * @param dateFrom
	 * @param dateTo
	 */
	public StockCount(Context context, long dateFrom, long dateTo){
		super(context);
	}

	public Cursor listStock(long dateFrom, long dateTo){
		//float currStock = getCurrentStock(dateFrom, dateTo);
		return getDatabase().rawQuery(
				"SELECT SUM(", 
				new String[]{});
	}
	
	public float getCurrentStock(int productId, long dateFrom, long dateTo){
		float currStock = 0.0f;

		Cursor cursor = getDatabase().rawQuery(
				"SELECT (a." + StockDocument.COL_PRODUCT_AMOUNT + "*" + 
				"b." + StockDocument.COL_MOVE_MENT + ") " +
				"FROM " + StockDocument.TB_DOC_DETAIL + " a " +
				"LEFT JOIN " + StockDocument.TB_DOCUMENT_TYPE + " b " +
				"ON a.DocumentId=b.DocumentId " +
				"AND a.ShopID=b.ShopID " +
				"LEFT JOIN DocumentType c " +
				"ON a.DocumentTypeId=c.DocumentTypeId " +
				"WHERE c." + Products.COL_PRODUCT_ID + 
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
