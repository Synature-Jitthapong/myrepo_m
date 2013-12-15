package com.syn.mpos.database.inventory;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;

import android.content.Context;
import android.database.Cursor;

public class ReceiveStock extends StockDocument{

	public ReceiveStock(Context context) {
		super(context);
	}

	public List<StockProduct> listStock(int documentId, int shopId){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();
		
		String strSql = "SELECT a." + COL_DOC_DETAIL_ID + ", " +
				" a." + Products.COL_PRODUCT_ID + ", " +
				" a." + COL_PRODUCT_AMOUNT + ", " +
				" a." + Products.COL_PRODUCT_PRICE + ", " +
				" b." + Products.COL_PRODUCT_CODE + ", " +
				" b." + Products.COL_PRODUCT_NAME +
				" FROM " + TB_DOC_DETAIL + " a " +
				" LEFT JOIN " + Products.TB_PRODUCT + " b " +
				" ON a." + Products.COL_PRODUCT_ID + "=b." + Products.COL_PRODUCT_ID +
				" WHERE a." + COL_DOC_ID + "=" + documentId +
				" AND a." + Shop.COL_SHOP_ID + "=" + shopId;
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				StockProduct mat = new StockProduct();
				mat.setId(cursor.getInt(cursor.getColumnIndex(StockDocument.COL_DOC_DETAIL_ID)));
				mat.setProId(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)));
				mat.setCode(cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_CODE)));
				mat.setName(cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_NAME)));
				mat.setReceive(cursor.getFloat(cursor.getColumnIndex(StockDocument.COL_PRODUCT_AMOUNT)));
				mat.setUnitPrice(cursor.getFloat(cursor.getColumnIndex(Products.COL_PRODUCT_PRICE)));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return stockLst;
	}
}
