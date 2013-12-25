package com.syn.mpos.database.inventory;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Util;

public abstract class StockDocument extends MPOSDatabase {

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
	
	public static final String TB_DOCUMENT_TYPE = "DocumentType";
	public static final String COL_DOC_TYPE = "DocumentTypeId";
	public static final String COL_DOC_TYPE_HEADER = "DocumentTypeHeader";
	public static final String COL_DOC_TYPE_NAME = "DocumentTypeName";
	public static final String COL_MOVE_MENT = "MovementInStock";
	
	public static final String TB_DOCUMENT = "Document";
	public static final String COL_DOC_ID = "DocumentId";
	public static final String COL_REF_DOC_ID = "RefDocId";
	public static final String COL_REF_SHOP_ID = "RefShopId";
	public static final String COL_DOC_NO = "DocumentNo";
	public static final String COL_DOC_DATE = "DocumentDate";
	public static final String COL_DOC_YEAR = "DocumentYear";
	public static final String COL_DOC_MONTH = "DocumentMonth";
	public static final String COL_UPDATE_BY = "UpdateBy";
	public static final String COL_UPDATE_DATE = "UpdateDate";
	public static final String COL_DOC_STATUS = "DocumentStatusId";
	public static final String COL_REMARK = "Remark";
	public static final String COL_IS_SEND_TO_HQ = "IsSendToHq";
	public static final String COL_IS_SEND_TO_HQ_DATE = "IsSendToHqDateTime";
	
	public static final String TB_DOC_DETAIL = "DocDetail";
	public static final String COL_DOC_DETAIL_ID = "DocDetailId";
	public static final String COL_PRODUCT_AMOUNT = "ProductAmount";
	
	public StockDocument(Context c) {
		super(c);
	}

	/**
	 * get working document by document type
	 * 
	 * @param shopId
	 * @param documentTypeId
	 * @return
	 */
	public int getCurrentDocument(int shopId, int documentTypeId) {
		int documentId = 0;
		String strSql = "SELECT " + COL_DOC_ID +
				" FROM " + TB_DOCUMENT +  
				" WHERE " + Shop.COL_SHOP_ID + "=" + shopId + 
				" AND " + COL_DOC_TYPE + "=" + documentTypeId + 
				" AND " + COL_DOC_STATUS + "=1";
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if (cursor.moveToFirst()) {
			documentId = cursor.getInt(0);
		}
		cursor.close();
		close();
		return documentId;
	}

	public int getMaxDocument(int shopId) {
		int maxDocId = 0;
		
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT MAX(" + COL_DOC_ID + ") " + 
				" FROM " + TB_DOCUMENT + 
				" WHERE " + Shop.COL_SHOP_ID + "=" + shopId, null);
		if (cursor.moveToFirst()) {
			maxDocId = cursor.getInt(0);
		}
		cursor.close();
		close();
		return maxDocId + 1;
	}

	public int getMaxDocumentNo(int documentId, int shopId, int documentMonth,
			int documentYear, int documentTypeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getDocumentHeader(int docType){
		String header = "";
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT "
				+ COL_DOC_TYPE_HEADER
				+ " FROM " + TB_DOCUMENT_TYPE
				+ " WHERE " + COL_DOC_TYPE + "=?", 
				new String[]{String.valueOf(docType)});
		if(cursor.moveToFirst()){
			header = cursor.getString(0);
		}
		close();
		return header;
	}
	
	public int getMaxDocumentDetail(int documentId, int shopId) {
		int docDetailId = 0;
	
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT MAX(" + COL_DOC_DETAIL_ID + ") " + 
				" FROM " + TB_DOC_DETAIL + 
				" WHERE " + COL_DOC_ID + "=" + documentId + 
				" AND " + Shop.COL_SHOP_ID + "=" + shopId, null);
		if (cursor.moveToFirst()) {
			docDetailId = cursor.getInt(0);
		}
		cursor.close();
		close();
		return docDetailId + 1;
	}

	public void clearDocument() throws SQLException{
		open();
		mSqlite.execSQL("DELETE FROM " + TB_DOCUMENT + " WHERE " + COL_DOC_STATUS + "=0");
		close();
	}

	public int createDocument(int shopId, int documentTypeId, int staffId) throws SQLException{
		int documentId = getMaxDocument(shopId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_DOC_ID, documentId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(COL_DOC_TYPE, documentTypeId);
		cv.put(COL_DOC_STATUS, DOC_STATUS_NEW);
		cv.put(COL_DOC_DATE, date.getTimeInMillis());
		cv.put(COL_UPDATE_BY, staffId);
		cv.put(COL_UPDATE_DATE, dateTime.getTimeInMillis());

		open();
		try {
			mSqlite.insertOrThrow(TB_DOCUMENT, null, cv);
		} catch (Exception e) {
			documentId = 0;
			e.printStackTrace();
		}
		close();
		return documentId;
	}

	public int createDocument(int shopId, int refDocumentId, int refShopId,
			int documentTypeId, int staffId) throws SQLException{
		int documentId = getMaxDocument(shopId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_DOC_ID, documentId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(COL_REF_DOC_ID, refDocumentId);
		cv.put(COL_REF_SHOP_ID, refShopId);
		cv.put(COL_DOC_TYPE, documentTypeId);
		cv.put(COL_DOC_STATUS, DOC_STATUS_NEW);
		cv.put(COL_DOC_DATE, date.getTimeInMillis());
		cv.put(COL_UPDATE_BY, staffId);
		cv.put(COL_UPDATE_DATE, dateTime.getTimeInMillis());

		open();
		try {
			mSqlite.insertOrThrow(TB_DOCUMENT, null, cv);
		} catch (Exception e) {
			documentId = 0;
			e.printStackTrace();
		}
		close();
		return documentId;
	}

	private boolean updateDocument(int documentId, int shopId,
			int documentStatus, int staffId, String remark) throws SQLException{
		boolean isSuccess = false;
		Calendar dateTime = Util.getDateTime();

		String strSql = "UPDATE " + TB_DOCUMENT +  
				" SET " + COL_DOC_STATUS + "=" + documentStatus + ", " + 
				COL_UPDATE_BY + "=" + staffId + ", " + 
				COL_UPDATE_DATE + "='" + dateTime.getTimeInMillis() + "', " + 
				COL_REMARK + "='" + remark + "' " + 
				" WHERE " + COL_DOC_ID + "=" + documentId + 
				" AND " + Shop.COL_SHOP_ID + "=" + shopId;

		open();
		try {
			mSqlite.execSQL(strSql);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean saveDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_SAVE, staffId,
				remark);
	}

	public boolean approveDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_APPROVE, staffId,
				remark);
	}

	public boolean cancelDocument(int documentId, int shopId, int staffId,
			String remark) {
		return updateDocument(documentId, shopId, DOC_STATUS_CANCLE, staffId,
				remark);
	}

	public int addDocumentDetail(int documentId, int shopId, int productId,
			float productQty, float productPrice, 
			String unitName, String remark) throws SQLException {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);

		ContentValues cv = new ContentValues();
		cv.put(COL_DOC_DETAIL_ID, docDetailId);
		cv.put(COL_DOC_ID, documentId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(Products.COL_PRODUCT_ID, productId);
		cv.put(COL_PRODUCT_AMOUNT, productQty);
		cv.put(Products.COL_PRODUCT_PRICE, productPrice);
		cv.put(COL_REMARK, remark);

		open();
		try {
			mSqlite.insertOrThrow(TB_DOC_DETAIL, null, cv);
		} catch (Exception e) {
			docDetailId = 0;
			e.printStackTrace();
		}
		close();
		return docDetailId;
	}
	
	public int addDocumentDetail(int documentId, int shopId, int productId,
			float productQty, float productPrice, String unitName) throws SQLException {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);

		ContentValues cv = new ContentValues();
		cv.put(COL_DOC_DETAIL_ID, docDetailId);
		cv.put(COL_DOC_ID, documentId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(Products.COL_PRODUCT_ID, productId);
		cv.put(COL_PRODUCT_AMOUNT, productQty);
		cv.put(Products.COL_PRODUCT_PRICE, productPrice);
		cv.put(Products.COL_PRODUCT_UNIT_NAME, unitName);

		open();
		try {
			mSqlite.insertOrThrow(TB_DOC_DETAIL, null, cv);
		} catch (Exception e) {
			docDetailId = 0;
			e.printStackTrace();
		}
		close();
		return docDetailId;
	}

	public boolean updateDocumentDetail(int docDetailId, int documentId,
			int shopId, int productId, float productQty, float productPrice,
			String unitName) throws SQLException {
		boolean isSuccess = false;

		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_DOC_DETAIL + 
					" SET " + 
					COL_PRODUCT_AMOUNT + "=" + productQty + ", " + 
					Products.COL_PRODUCT_PRICE + "=" + productPrice + ", " + 
					Products.COL_PRODUCT_UNIT_NAME + "='" + unitName + "' " +
					" WHERE " + COL_DOC_DETAIL_ID + "=" + docDetailId + 
					" AND " + COL_DOC_ID + "=" + documentId + 
					" AND " + Shop.COL_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean deleteDocumentDetail(int docDetailId, int documentId,
			int shopId) {
		boolean isSuccess = false;

		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_DOC_DETAIL + 
					" WHERE " + COL_DOC_DETAIL_ID + "=" + docDetailId + 
					" AND " + COL_DOC_ID + "=" + documentId + 
					" AND " + Shop.COL_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean deleteDocumentDetail(int documentId, int shopId) {
		boolean isSuccess = false;

		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_DOC_DETAIL + 
					" WHERE " + COL_DOC_ID + "=" + documentId + 
					" AND " + Shop.COL_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}
}
