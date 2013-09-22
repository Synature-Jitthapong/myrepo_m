package com.syn.mpos.inventory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

public class MPOSReceiveStock extends MPOSStockDocument{

	public MPOSReceiveStock(Context context) {
		super(context);
	}

	public List<StockMaterial> listStock(int documentId, int shopId){
		List<StockMaterial> stockLst = 
				new ArrayList<StockMaterial>();
		
		String strSql = "SELECT b.docdetail_id, b.material_id, b.material_qty, " +
				" b.material_price_per_unit, b.material_net_price," +
				" b.material_tax_type, b.material_tax_price," +
				" c.product_code, d.menu_name_0 " +
				" FROM document a " +
				" LEFT JOIN docdetail b " +
				" ON a.document_id=b.document_id " +
				" AND a.shop_id=b.shop_id " +
				" LEFT JOIN products c " +
				" ON b.material_id=c.product_id " +
				" LEFT JOIN menu_item d " +
				" ON c.product_id=d.product_id " +
				" WHERE a.document_id=" + documentId +
				" AND a.shop_id=" + shopId +
				" AND c.activate=1 " +
				" AND d.menu_activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				StockMaterial mat = new StockMaterial(
						cursor.getInt(cursor.getColumnIndex("docdetail_id")),
						cursor.getInt(cursor.getColumnIndex("material_id")),
						cursor.getString(cursor.getColumnIndex("product_code")),
						cursor.getString(cursor.getColumnIndex("menu_name_0")),
						cursor.getFloat(cursor.getColumnIndex("material_qty")),
						cursor.getFloat(cursor.getColumnIndex("material_price_per_unit")),
						cursor.getFloat(cursor.getColumnIndex("material_net_price")),
						cursor.getInt(cursor.getColumnIndex("material_tax_type")),
						cursor.getFloat(cursor.getColumnIndex("material_tax_price"))
						);
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return stockLst;
	}
}
