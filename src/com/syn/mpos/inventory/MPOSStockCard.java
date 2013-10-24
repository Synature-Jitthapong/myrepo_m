package com.syn.mpos.inventory;

import java.util.ArrayList;
import java.util.Calendar;
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
	
	public List<StockProduct> listStock(long dateFrom, long dateTo){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();

		if(calculateStockCard(dateFrom, dateTo)){
			String strSql = " SELECT a.init, " +
					" a.receive, " +
					" a.sale, " +
					" a.variance, " +
					" b.product_id, " +
					" b.product_code, " +
					" b.product_name " +
					" FROM stock_card_tmp a " +
					" LEFT JOIN products b " +
					" ON a.id=b.product_id " +
					" WHERE b.activated=1 ";
			
			mDbHelper.open();
			Cursor cursor = mDbHelper.rawQuery(strSql);
			if(cursor.moveToFirst()){
				do{
					StockProduct mat = new StockProduct();
					mat.setProId(cursor.getInt(cursor.getColumnIndex("product_id")));
					mat.setCode(cursor.getString(cursor.getColumnIndex("product_code")));
					mat.setName(cursor.getString(cursor.getColumnIndex("product_name")));
					mat.setInit(cursor.getFloat(cursor.getColumnIndex("init"))); 
					mat.setReceive(cursor.getFloat(cursor.getColumnIndex("receive")));
					mat.setSale(cursor.getFloat(cursor.getColumnIndex("sale")));
					mat.setVariance(cursor.getFloat(cursor.getColumnIndex("variance")));
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
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dateFrom);
		int month = c.get(Calendar.MONTH);
		
		if(createStockCardTmp()){
			String strSql = "INSERT INTO stock_card_tmp " +
					" SELECT p.product_id, " +
					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.product_id " +
					" AND a.document_month=" + month +
					" AND a.document_status=2 " +
					" AND a.document_type_id = 10 " +
					" GROUP BY b.product_id ),  " +
					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.product_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id=39 " +
					" GROUP BY b.product_id ), " +
					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.product_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id IN (20,21) " +
					" GROUP BY b.product_id ), " +
					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.product_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND a.document_type_id IN (18,19) " +
					" GROUP BY b.product_id ) " +
					" FROM products p " +
					" WHERE p.activated=1";
			
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
				" id  INTEGER, " +
				" init REAL DEFAULT 0," +
				" receive REAL DEFAULT 0, " +
				" sale REAL DEFAULT 0, " +
				" variance REAL DEFAULT 0);";
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DROP TABLE IF EXISTS stock_card_tmp");
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
}
