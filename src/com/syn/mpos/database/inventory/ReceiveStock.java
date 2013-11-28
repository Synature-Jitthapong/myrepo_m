package com.syn.mpos.database.inventory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

public class ReceiveStock extends StockDocument{

	public ReceiveStock(Context context) {
		super(context);
	}

	public List<StockProduct> listStock(int documentId, int shopId){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();
		
		String strSql = "SELECT b.docdetail_id, " +
				" b.product_id, " +
				" b.product_qty, " +
				" b.product_unit_price, " +
				" b.product_net_price," +
				" b.product_tax_type, " +
				" b.product_tax_price," +
				" c.product_code, " +
				" c.product_name " +
				" FROM document a " +
				" LEFT JOIN docdetail b " +
				" ON a.document_id=b.document_id " +
				" AND a.shop_id=b.shop_id " +
				" LEFT JOIN products c " +
				" ON b.product_id=c.product_id " +
				" WHERE a.document_id=" + documentId +
				" AND a.shop_id=" + shopId +
				" AND c.activated=1 ";
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				StockProduct mat = new StockProduct();
				mat.setId(cursor.getInt(cursor.getColumnIndex("docdetail_id")));
				mat.setProId(cursor.getInt(cursor.getColumnIndex("product_id")));
				mat.setCode(cursor.getString(cursor.getColumnIndex("product_code")));
				mat.setName(cursor.getString(cursor.getColumnIndex("product_name")));
				mat.setReceive(cursor.getFloat(cursor.getColumnIndex("product_qty")));
				mat.setUnitPrice(cursor.getFloat(cursor.getColumnIndex("product_unit_price")));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return stockLst;
	}
}
