package com.syn.mpos.inventory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Util;
import com.syn.pos.inventory.Document.DocDetail;
import com.syn.pos.inventory.DocumentCreation;
import com.syn.pos.inventory.DocumentDetailCreation;

public class MPOSStockDocument extends Util implements DocumentCreation, DocumentDetailCreation {
	
	public static final int SALE_DOC = 20;
	public static final int VOID_DOC = 21;
	public static final int DAILY_DOC = 24;
	public static final int DAILY_ADD_DOC = 18;
	public static final int DAILY_REDUCE_DOC = 19;
	public static final int DIRECT_RECEIVE_DOC = 39;
	public static final int DOC_STATUS_NEW = 0;
	public static final int DOC_STATUS_SAVE = 1;
	public static final int DOC_STATUS_APPROVE = 2;
	public static final int DOC_STATUS_CANCLE = 99;
	
	private SQLiteHelper mDbHelper;
	
	public MPOSStockDocument(Context context){
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public int getCurrentDocument(int shopId, int documentTypeId){
		int documentId = 0;
		String strSql = "SELECT document_id " +
				" FROM document " +
				" WHERE shop_id=" + shopId +
				" AND document_type_id=" + documentTypeId;
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			documentId = cursor.getInt(0);
		}
		cursor.close();
		mDbHelper.close();
		return documentId;
	}
	
	@Override
	public int getMaxDocument(int shopId, int docTypeId) {
		int maxDocId = 0;
		String strSql = "SELECT MAX(document_id) " +
				" FROM document " +
				" WHERE shop_id=" + shopId +
				" AND document_type_id=" + docTypeId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			maxDocId = cursor.getInt(0);
		}
		cursor.close();
		mDbHelper.close();
		return maxDocId + 1;
	}

	@Override
	public int getMaxDocumentNo(int documentId, int shopId, int documentMonth,
			int documentYear, int documentTypeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean updateDocument(int documentId, int shopId, int documentStatus, 
			int staffId, String remark){
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();
		
		String strSql = "UPDATE document " +
				" SET document_status=" + documentStatus + ", " + 
				" update_by=" + staffId + ", " +
				" update_date='" + dateTime.getTimeInMillis() + "', " +
				" remark='" + remark + "' " +
				" WHERE document_id=" + documentId + 
				" AND shop_id=" + shopId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
	
	@Override
	public boolean saveDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_SAVE, staffId, remark);
	}

	@Override
	public boolean approveDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_APPROVE, staffId, remark);
	}

	@Override
	public boolean cancelDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_CANCLE, staffId, remark);
	}

	@Override
	public int getMaxDocumentDetail(int documentId, int shopId) {
		int docDetailId = 0;
		String strSql = "SELECT MAX(docdetail_id " +
				" FROM docdetail " +
				" WHERE document_id=" + documentId + 
				" AND shop_id=" + shopId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			docDetailId = cursor.getInt(0);
		}
		cursor.close();
		mDbHelper.close();
		return docDetailId + 1;
	}

	@Override
	public int addDocumentDetail(int documentId, int shopId, float materialId, 
			float materialQty, float materialPrice, String unitName) {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);
		
		ContentValues cv = new ContentValues();
		cv.put("docdetail_id", docDetailId);
		cv.put("document_id", documentId);
		cv.put("shop_id", shopId);
		cv.put("material_id", materialId);
		cv.put("material_qty", materialQty);
		cv.put("material_price_per_unit", materialPrice);
		cv.put("material_net_price", materialPrice);
		cv.put("material_tax_type", 1);
		cv.put("material_tax_price", 0);
		
		mDbHelper.open();
		if(!mDbHelper.insert("docdetail", cv)) docDetailId = 0; 
		mDbHelper.close();
		return docDetailId;
	}

	@Override
	public int addDocumentDetail(int documentId, int shopId,
			float materialId, float materialQty, float materialPrice,
			int taxType, String unitName) {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);
		float totalPrice = materialPrice * materialQty;
		float tax = taxType == 2 ? calculateVat(totalPrice, 7) : 0;
		float netPrice = totalPrice + tax;
		
		ContentValues cv = new ContentValues();
		cv.put("docdetail_id", docDetailId);
		cv.put("document_id", documentId);
		cv.put("shop_id", shopId);
		cv.put("material_id", materialId);
		cv.put("material_qty", materialQty);
		cv.put("material_price_per_unit", materialPrice);
		cv.put("material_net_price", netPrice);
		cv.put("material_tax_type", taxType);
		cv.put("material_tax_price", tax);
		
		mDbHelper.open();
		if(!mDbHelper.insert("docdetail", cv)) docDetailId = 0;
		mDbHelper.close();
		return docDetailId;
	}

	@Override
	public boolean updateDocumentDetail(int docDetailId, int documentId, int shopId,
			int materialId, float materialQty, float materialPrice,
			String unitName) {
		boolean isSuccess = false;
		float totalPrice = materialPrice * materialQty;
		
		String strSql = "UPDATE docdetail " +
				" SET material_qty=" + materialQty + ", " +
				" material_price_per_unit=" + materialPrice + ", " +
				" material_net_price=" + totalPrice + 
				" WHERE docdetail_id=" + docDetailId + 
				" AND document_id=" + documentId +
				" AND shop_id=" + shopId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean updateDocumentDetail(int docDetailId, int documentId, int shopId,
			int materialId, float materialQty, float materialPrice,
			int taxType, String unitName) {
		boolean isSuccess = false;
		float totalPrice = materialPrice * materialQty;
		float tax = taxType == 2 ? calculateVat(totalPrice, 7) : 0;
		float netPrice = totalPrice + tax;
		
		String strSql = "UPDATE docdetail " +
				" SET material_qty=" + materialQty + ", " +
				" material_price_per_unit=" + materialPrice + ", " +
				" material_net_price=" + netPrice + ", " +
				" material_tax_type=" + taxType + 
				" WHERE docdetail_id=" + docDetailId + 
				" AND document_id=" + documentId +
				" AND shop_id=" + shopId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean deleteDocumentDetail(int docDetailId, int shopId, int documentId) {
		boolean isSuccess = false;
		String strSql = "DELETE FROM docdetail " +
				" WHERE docdetail_id=" + docDetailId + 
				" AND document_id=" + documentId + 
				" AND shop_id=" + shopId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	public DocDetail getDocDetail(int docDetailId, int documentId, int shopId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HashMap<String, String>> listAllDocDetail(int documentId, int shopId) {
		List<HashMap<String, String>> docDetailLst = 
				new ArrayList<HashMap<String,String>>();
		String strSql = "SELECT a.material_id, a.material_c.product_code, " +
				" FROM docdetail a " +
				" LEFT JOIN menu_item b " +
				" ON a.material_id=b.product_id " +
				" LEFT JOIN products c " +
				" ON b.product_id=c.product_id " +
				" WHERE a.document_id=" + documentId + 
				" AND a.shop_id=" + shopId + 
				" AND b.menu_activate=1 " +
				" AND c.activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return docDetailLst;
	}
	
	@Override
	public int createDocument(int shopId, int documentTypeId, int staffId) {
		int documentId = getMaxDocument(shopId, documentTypeId);
		Calendar date = getDate();
		Calendar dateTime = getDateTime();
		
		ContentValues cv = new ContentValues();
		cv.put("document_id", documentId);
		cv.put("shop_id", shopId);
		cv.put("document_type_id", documentTypeId);
		cv.put("document_status", DOC_STATUS_NEW);
		cv.put("document_date", date.getTimeInMillis());
		cv.put("update_by", staffId);
		cv.put("update_date", dateTime.getTimeInMillis());
		
		mDbHelper.open();
		if(!mDbHelper.insert("document", cv)) documentId = 0;
		mDbHelper.close();
		return documentId;
	}

	/**
	 * list stock 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	public List<HashMap<String, String>> listStock(long dateFrom, long dateTo) {
		List<HashMap<String, String>> stockLst = 
				new ArrayList<HashMap<String,String>>();
		
		calculateStock(dateFrom, dateTo);
		
		String strSql = "SELECT a.product_id, a.product_code, b.menu_name_0, " +
				" c.material_qty, c.material_count_qty " +
				" FROM products a " +
				" LEFT JOIN menu_item b " +
				" ON a.product_id=b.product_id " +
				" LEFT OUTER JOIN stock_tmp c " +
				" ON a.product_id=c.material_id " +
				" WHERE a.activate=1 " +
				" AND b.menu_activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				HashMap<String, String> mat = new HashMap<String, String>();
				mat.put("productId", cursor.getString(cursor.getColumnIndex("product_id")));
				mat.put("productCode", cursor.getString(cursor.getColumnIndex("product_code")));
				mat.put("productName", cursor.getString(cursor.getColumnIndex("menu_name_0")));
				mat.put("currQty", cursor.getString(cursor.getColumnIndex("material_qty")));
				mat.put("countQty", cursor.getString(cursor.getColumnIndex("material_count_qty")));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return stockLst;
	}
	
	/**
	 * calculate current stock
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	protected boolean calculateStock(long dateFrom, long dateTo){
		boolean isSuccess = false;
		if(createStockTmp()){
			String strSql = "INSERT INTO stock_tmp " +
					" SELECT b.material_id, " +
					" SUM(b.material_qty * c.movement_in_stock), " +
					" b.material_count_qty " +
					" FROM document a " +
					" LEFT JOIN doc_detail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status_id=2 " +
					" GROUP BY b.material_id";
			
			mDbHelper.open();
			isSuccess = mDbHelper.execSQL(strSql);
			mDbHelper.close();
		}
		return isSuccess;
	}

	/**
	 * create stock temp table
	 * @return
	 */
	protected boolean createStockTmp(){
		boolean isSuccess = false;
		String strSql = " CREATE TABLE stock_tmp ( " +
				" material_id  INTEGER NOT NULL, " +
				" material_qty REAL DEFAULT 0, " +
				" material_count_qty  REAL DEFAULT 0);";
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
}
