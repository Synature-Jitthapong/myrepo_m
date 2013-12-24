package com.syn.mpos.database.inventory;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.inventory.Document;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

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

	public float getCurrentStock(long dateFrom, long dateTo){
		float currStock = 0.0f;
		
		
		Cursor cursor = mSqlite.rawQuery(
				"SELECT (b.ProductAmount * c.MovementInStock) " +
				"FROM Document a " +
				"LEFT JOIN DocDetail b " +
				"ON a.DocumentId=b.DocumentId " +
				"AND a.ShopID=b.ShopID " +
				"LEFT JOIN DocumentType c " +
				"ON a.DocumentTypeId=c.DocumentTypeId " +
				"WHERE c.MovementInStock != 0 " +
				"AND a.DocumentDate BETWEEN ? AND ? " +
				"GROUP BY c.DocumentTypeId", 
				new String[]{String.valueOf(dateFrom),  String.valueOf(dateTo)});
		
		
		
		return currStock;
	}
}
