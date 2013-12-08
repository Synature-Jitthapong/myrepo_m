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

	/**
	 * calculate current stock
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
//	protected boolean calculateStock(int productId, long dateFrom, long dateTo){
//		boolean isSuccess = false;
//		if(createStockTmp()){
//			String strSql = "INSERT INTO stock_tmp " +
//					" SELECT p.product_id, " +
//					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
//					" FROM document a " +
//					" LEFT JOIN docdetail b " +
//					" ON a.document_id=b.document_id " +
//					" AND a.shop_id=b.shop_id " +
//					" LEFT JOIN document_type c " +
//					" ON a.document_type_id=c.document_type_id " +
//					" WHERE p.product_id=b.product_id " +
//					" AND a.document_date >=" + dateFrom + 
//					" AND a.document_date <=" + dateTo + 
//					" AND a.document_status=2 " +
//					" AND c.movement_in_stock != 0 " +
//					" GROUP BY b.product_id ) " +
//					" FROM products p " +
//					" WHERE p.activated=1";
//			
//			open();
//			try {
//				mSqlite.execSQL(strSql, null);
//				isSuccess = true;
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			mSqlite.close();
//		}
//		return isSuccess;
//	}
}
