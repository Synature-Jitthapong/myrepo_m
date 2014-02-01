package com.syn.mpos.database.inventory;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.provider.Products;
import com.syn.mpos.provider.Shop;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ReceiveStock extends StockDocument{

	public ReceiveStock(SQLiteDatabase db) {
		super(db);
	}

	public List<StockProduct> listStock(int documentId, int shopId){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();
		
		String strSql = "SELECT a." + COLUMN_DOC_DETAIL_ID + ", " +
				" a." + Products.COLUMN_PRODUCT_ID + ", " +
				" a." + COLUMN_PRODUCT_AMOUNT + ", " +
				" a." + Products.COLUMN_PRODUCT_PRICE + ", " +
				" b." + Products.COLUMN_PRODUCT_CODE + ", " +
				" b." + Products.COLUMN_PRODUCT_NAME +
				" FROM " + TABLE_DOC_DETAIL + " a " +
				" LEFT JOIN " + Products.TABLE_PRODUCT + " b " +
				" ON a." + Products.COLUMN_PRODUCT_ID + "=b." + Products.COLUMN_PRODUCT_ID +
				" WHERE a." + COLUMN_DOC_ID + "=" + documentId +
				" AND a." + Shop.COLUMN_SHOP_ID + "=" + shopId;
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				StockProduct mat = new StockProduct();
				mat.setId(cursor.getInt(cursor.getColumnIndex(StockDocument.COLUMN_DOC_DETAIL_ID)));
				mat.setProId(cursor.getInt(cursor.getColumnIndex(Products.COLUMN_PRODUCT_ID)));
				mat.setCode(cursor.getString(cursor.getColumnIndex(Products.COLUMN_PRODUCT_CODE)));
				mat.setName(cursor.getString(cursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME)));
				mat.setReceive(cursor.getFloat(cursor.getColumnIndex(StockDocument.COLUMN_PRODUCT_AMOUNT)));
				mat.setUnitPrice(cursor.getFloat(cursor.getColumnIndex(Products.COLUMN_PRODUCT_PRICE)));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return stockLst;
	}
}
