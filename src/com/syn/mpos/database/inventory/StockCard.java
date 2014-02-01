package com.syn.mpos.database.inventory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.mpos.provider.MPOSDatabase;
import com.syn.mpos.provider.Products;

public class StockCard extends MPOSDatabase{
	public static final String TB_STOCK_CARD_TEMP = "StockCardTemp";
	public static final String COL_INIT = "Init";
	public static final String COL_RECEIVE = "Receive";
	public static final String COL_SALE = "Sale";
	public static final String COL_VARIANCE = "Variance";
	
	public StockCard(SQLiteDatabase db){
		super(db);
	}
	
	public List<StockProduct> listStock(long dateFrom, long dateTo){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();

		if(calculateStockCard(dateFrom, dateTo)){
			String strSql = " SELECT a." + COL_INIT + ", " +
					" a." + COL_RECEIVE + ", " +
					" a." + COL_SALE + ", " +
					" a." + COL_VARIANCE + ", " +
					" b." + Products.COLUMN_PRODUCT_ID + ", " +
					" b." + Products.COLUMN_PRODUCT_CODE + ", " +
					" b." + Products.COLUMN_PRODUCT_NAME + ", " +
					" FROM " + TB_STOCK_CARD_TEMP + " a " +
					" LEFT JOIN " + Products.TABLE_PRODUCT + " b " +
					" ON a." + Products.COLUMN_PRODUCT_ID + "=b" + Products.COLUMN_PRODUCT_ID;
			Cursor cursor = mSqlite.rawQuery(strSql, null);
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
		
			try {
				mSqlite.execSQL(strSql);
				isSuccess = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	/**
	 * create stock card temp table
	 * @return
	 */
	protected boolean createStockCardTmp(){
		boolean isSuccess = false;
		try {
			mSqlite.execSQL("DROP TABLE IF EXISTS " + TB_STOCK_CARD_TEMP);
			mSqlite.execSQL(
					"CREATE TABLE " + TB_STOCK_CARD_TEMP + " ( " +
					Products.COLUMN_PRODUCT_ID + " INTEGER, " +
					COL_INIT + " REAL DEFAULT 0, " +
					COL_RECEIVE + " REAL DEFAULT 0, " +
					COL_SALE + " REAL DEFAULT 0, " +
					COL_VARIANCE + " REAL DEFAULT 0);"
					);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
}
