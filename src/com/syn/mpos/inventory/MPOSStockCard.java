package com.syn.mpos.inventory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.mpos.database.MPOSSQLiteHelper;

public class MPOSStockCard {
	private SQLiteHelper mDbHelper;
	
	public MPOSStockCard(Context context){
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public List<StockMaterial> listStock(long dateFrom, long dateTo){
		List<StockMaterial> stockLst = 
				new ArrayList<StockMaterial>();

		if(calculateStockCard(dateFrom, dateTo)){
			String strSql = " SELECT a.material_init, a.material_receive, " +
					" a.material_sale, a.material_variance, b.product_id, " +
					" b.product_code, c.menu_name_0 " +
					" FROM stock_card_tmp a " +
					" LEFT JOIN products b " +
					" ON a.material_id=b.product_id " +
					" LEFT JOIN menu_item c " +
					" ON b.product_id=c.product_id " +
					" WHERE b.activate=1 " +
					" AND c.menu_activate=1";
			
			mDbHelper.open();
			Cursor cursor = mDbHelper.rawQuery(strSql);
			if(cursor.moveToFirst()){
				do{
					StockMaterial mat = new StockMaterial(
							cursor.getInt(cursor.getColumnIndex("product_id")),
							cursor.getString(cursor.getColumnIndex("product_code")),
							cursor.getString(cursor.getColumnIndex("menu_name_0")),
							cursor.getFloat(cursor.getColumnIndex("material_init")), 
							cursor.getFloat(cursor.getColumnIndex("material_receive")),
							cursor.getFloat(cursor.getColumnIndex("material_sale")),
							cursor.getFloat(cursor.getColumnIndex("material_variance")), 0);
					stockLst.add(mat);
				}while(cursor.moveToNext());
			}
			cursor.close();
			mDbHelper.close();
		}
		return stockLst;
	}
	
	/**
	 * calculate current stock
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	protected boolean calculateStockCard(long dateFrom, long dateTo){
		boolean isSuccess = false;
		if(createStockCardTmp()){
			String strSql = "INSERT INTO stock_card_tmp " +
					" SELECT p.product_id, " +
					" (SELECT SUM(b.material_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.material_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id = 10 " +
					" GROUP BY b.material_id ),  " +
					" (SELECT SUM(b.material_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.material_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id=39 " +
					" GROUP BY b.material_id ), " +
					" (SELECT SUM(b.material_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.material_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id IN (20,21) " +
					" GROUP BY b.material_id ), " +
					" (SELECT SUM(b.material_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.material_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id IN (18,19) " +
					" GROUP BY b.material_id ) " +
					" FROM products p " +
					" WHERE p.activate=1";
			
			mDbHelper.open();
			isSuccess = mDbHelper.execSQL(strSql);
			mDbHelper.close();
		}
		return isSuccess;
	}

	/**
	 * create stock card temp table
	 * @return
	 */
	protected boolean createStockCardTmp(){
		boolean isSuccess = false;
		String strSql = " CREATE TABLE stock_card_tmp ( " +
				" material_id  INTEGER NOT NULL, " +
				" material_init REAL DEFAULT 0," +
				" material_receive REAL DEFAULT 0, " +
				" material_sale REAL DEFAULT 0, " +
				" material_variance REAL DEFAULT 0);";
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DROP TABLE IF EXISTS stock_card_tmp");
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
}
