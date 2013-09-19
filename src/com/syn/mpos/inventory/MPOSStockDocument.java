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
	
	protected SQLiteHelper mDbHelper;
	
	public MPOSStockDocument(Context context){
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public int getCurrentDocument(int shopId, int documentTypeId){
		int documentId = 0;
		String strSql = "SELECT document_id " +
				" FROM document " +
				" WHERE shop_id=" + shopId +
				" AND document_type_id=" + documentTypeId + 
				" AND document_status=1";
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
	public int getMaxDocument(int shopId) {
		int maxDocId = 0;
		String strSql = "SELECT MAX(document_id) " +
				" FROM document " +
				" WHERE shop_id=" + shopId;
		
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
		String strSql = "SELECT MAX(docdetail_id) " +
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
			float materialQty, float materialCountQty, float materialPrice, String unitName) {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);
		
		ContentValues cv = new ContentValues();
		cv.put("docdetail_id", docDetailId);
		cv.put("document_id", documentId);
		cv.put("shop_id", shopId);
		cv.put("material_id", materialId);
		cv.put("material_qty", materialQty);
		cv.put("material_count_qty", materialCountQty);
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
			float materialId, float materialQty, float materialCountQty,
			float materialPrice, int taxType, String unitName) {
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
		cv.put("material_count_qty", materialCountQty);
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
	public boolean deleteDocumentDetail(int docDetailId, int documentId, int shopId) {
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
	
	@Override
	public boolean deleteDocumentDetail(int documentId, int shopId) {
		boolean isSuccess = false;
		String strSql = "DELETE FROM docdetail " +
				" WHERE document_id=" + documentId + 
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
	
	public void clearDocument(){
		mDbHelper.open();
		mDbHelper.execSQL("DELETE FROM document WHERE document_status=0");
		mDbHelper.close();
	}

	@Override
	public int createDocument(int shopId, int documentTypeId, int staffId) {
		int documentId = getMaxDocument(shopId);
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
	
	@Override
	public int createDocument(int shopId, int refDocumentId, int refShopId,
			int documentTypeId, int staffId) {
		int documentId = getMaxDocument(shopId);
		Calendar date = getDate();
		Calendar dateTime = getDateTime();
		
		ContentValues cv = new ContentValues();
		cv.put("document_id", documentId);
		cv.put("shop_id", shopId);
		cv.put("ref_document_id", refDocumentId);
		cv.put("ref_shop_id", refShopId);
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

}
