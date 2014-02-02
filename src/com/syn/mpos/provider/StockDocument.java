package com.syn.mpos.provider;

import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
	
	public static final String TABLE_DOCUMENT_TYPE = "DocumentType";
	public static final String COLUMN_DOC_TYPE = "document_type_id";
	public static final String COLUMN_DOC_TYPE_HEADER = "document_type_header";
	public static final String COLUMN_DOC_TYPE_NAME = "document_type_name";
	public static final String COLUMN_MOVE_MENT = "movement_in_stock";
	
	public static final String TABLE_DOCUMENT = "Document";
	public static final String COLUMN_DOC_ID = "document_id";
	public static final String COLUMN_REF_DOC_ID = "ref_doc_id";
	public static final String COLUMN_REF_SHOP_ID = "ref_shop_id";
	public static final String COLUMN_DOC_NO = "document_no";
	public static final String COLUMN_DOC_DATE = "document_date";
	public static final String COLUMN_DOC_YEAR = "document_year";
	public static final String COLUMN_DOC_MONTH = "document_month";
	public static final String COLUMN_UPDATE_BY = "update_by";
	public static final String COLUMN_UPDATE_DATE = "update_date";
	public static final String COLUMN_DOC_STATUS = "document_status_id";
	public static final String COLUMN_REMARK = "remark";
	public static final String COLUMN_IS_SEND_TO_HQ = "is_send_to_hq";
	public static final String COLUMN_IS_SEND_TO_HQ_DATE = "is_send_to_hq_date_time";
	
	public static final String TABLE_DOC_DETAIL = "DocDetail";
	public static final String COLUMN_DOC_DETAIL_ID = "doc_detail_id";
	public static final String COLUMN_PRODUCT_AMOUNT = "product_amount";
	
	public StockDocument(SQLiteDatabase db) {
		super(db);
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
		String strSql = "SELECT " + COLUMN_DOC_ID +
				" FROM " + TABLE_DOCUMENT +  
				" WHERE " + Shop.COLUMN_SHOP_ID + "=" + shopId + 
				" AND " + COLUMN_DOC_TYPE + "=" + documentTypeId + 
				" AND " + COLUMN_DOC_STATUS + "=1";
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if (cursor.moveToFirst()) {
			documentId = cursor.getInt(0);
		}
		cursor.close();
		return documentId;
	}

	public int getMaxDocument(int shopId) {
		int maxDocId = 0;
		Cursor cursor = mSqlite.rawQuery("SELECT MAX(" + COLUMN_DOC_ID + ") " + 
				" FROM " + TABLE_DOCUMENT + 
				" WHERE " + Shop.COLUMN_SHOP_ID + "=" + shopId, null);
		if (cursor.moveToFirst()) {
			maxDocId = cursor.getInt(0);
		}
		cursor.close();
		return maxDocId + 1;
	}

	public int getMaxDocumentNo(int documentId, int shopId, int documentMonth,
			int documentYear, int documentTypeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getDocumentHeader(int docType){
		String header = "";
		Cursor cursor = mSqlite.rawQuery("SELECT "
				+ COLUMN_DOC_TYPE_HEADER
				+ " FROM " + TABLE_DOCUMENT_TYPE
				+ " WHERE " + COLUMN_DOC_TYPE + "=?", 
				new String[]{String.valueOf(docType)});
		if(cursor.moveToFirst()){
			header = cursor.getString(0);
		}
		return header;
	}
	
	public int getMaxDocumentDetail(int documentId, int shopId) {
		int docDetailId = 0;
		Cursor cursor = mSqlite.rawQuery("SELECT MAX(" + COLUMN_DOC_DETAIL_ID + ") " + 
				" FROM " + TABLE_DOC_DETAIL + 
				" WHERE " + COLUMN_DOC_ID + "=" + documentId + 
				" AND " + Shop.COLUMN_SHOP_ID + "=" + shopId, null);
		if (cursor.moveToFirst()) {
			docDetailId = cursor.getInt(0);
		}
		cursor.close();
		return docDetailId + 1;
	}

	public void clearDocument() throws SQLException{
		mSqlite.execSQL("DELETE FROM " + TABLE_DOCUMENT + " WHERE " + COLUMN_DOC_STATUS + "=0");
	}

	public int createDocument(int shopId, int documentTypeId, int staffId) throws SQLException{
		int documentId = getMaxDocument(shopId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DOC_ID, documentId);
		cv.put(Shop.COLUMN_SHOP_ID, shopId);
		cv.put(COLUMN_DOC_TYPE, documentTypeId);
		cv.put(COLUMN_DOC_STATUS, DOC_STATUS_NEW);
		cv.put(COLUMN_DOC_DATE, date.getTimeInMillis());
		cv.put(COLUMN_UPDATE_BY, staffId);
		cv.put(COLUMN_UPDATE_DATE, dateTime.getTimeInMillis());

		try {
			mSqlite.insertOrThrow(TABLE_DOCUMENT, null, cv);
		} catch (Exception e) {
			documentId = 0;
			e.printStackTrace();
		}
		return documentId;
	}

	public int createDocument(int shopId, int refDocumentId, int refShopId,
			int documentTypeId, int staffId) throws SQLException{
		int documentId = getMaxDocument(shopId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DOC_ID, documentId);
		cv.put(Shop.COLUMN_SHOP_ID, shopId);
		cv.put(COLUMN_REF_DOC_ID, refDocumentId);
		cv.put(COLUMN_REF_SHOP_ID, refShopId);
		cv.put(COLUMN_DOC_TYPE, documentTypeId);
		cv.put(COLUMN_DOC_STATUS, DOC_STATUS_NEW);
		cv.put(COLUMN_DOC_DATE, date.getTimeInMillis());
		cv.put(COLUMN_UPDATE_BY, staffId);
		cv.put(COLUMN_UPDATE_DATE, dateTime.getTimeInMillis());

		try {
			mSqlite.insertOrThrow(TABLE_DOCUMENT, null, cv);
		} catch (Exception e) {
			documentId = 0;
			e.printStackTrace();
		}
		return documentId;
	}

	private boolean updateDocument(int documentId, int shopId,
			int documentStatus, int staffId, String remark) throws SQLException{
		boolean isSuccess = false;
		Calendar dateTime = Util.getDateTime();
		String strSql = "UPDATE " + TABLE_DOCUMENT +  
				" SET " + COLUMN_DOC_STATUS + "=" + documentStatus + ", " + 
				COLUMN_UPDATE_BY + "=" + staffId + ", " + 
				COLUMN_UPDATE_DATE + "='" + dateTime.getTimeInMillis() + "', " + 
				COLUMN_REMARK + "='" + remark + "' " + 
				" WHERE " + COLUMN_DOC_ID + "=" + documentId + 
				" AND " + Shop.COLUMN_SHOP_ID + "=" + shopId;

		try {
			mSqlite.execSQL(strSql);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			double productQty, double productPrice, 
			String unitName, String remark) throws SQLException {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DOC_DETAIL_ID, docDetailId);
		cv.put(COLUMN_DOC_ID, documentId);
		cv.put(Shop.COLUMN_SHOP_ID, shopId);
		cv.put(Products.COLUMN_PRODUCT_ID, productId);
		cv.put(COLUMN_PRODUCT_AMOUNT, productQty);
		cv.put(Products.COLUMN_PRODUCT_PRICE, productPrice);
		cv.put(COLUMN_REMARK, remark);

		try {
			mSqlite.insertOrThrow(TABLE_DOC_DETAIL, null, cv);
		} catch (Exception e) {
			docDetailId = 0;
			e.printStackTrace();
		}
		return docDetailId;
	}
	
	public int addDocumentDetail(int documentId, int shopId, int productId,
			double productQty, double productPrice, String unitName) throws SQLException {
		int docDetailId = getMaxDocumentDetail(documentId, shopId);
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DOC_DETAIL_ID, docDetailId);
		cv.put(COLUMN_DOC_ID, documentId);
		cv.put(Shop.COLUMN_SHOP_ID, shopId);
		cv.put(Products.COLUMN_PRODUCT_ID, productId);
		cv.put(COLUMN_PRODUCT_AMOUNT, productQty);
		cv.put(Products.COLUMN_PRODUCT_PRICE, productPrice);
		cv.put(Products.COLUMN_PRODUCT_UNIT_NAME, unitName);

		try {
			mSqlite.insertOrThrow(TABLE_DOC_DETAIL, null, cv);
		} catch (Exception e) {
			docDetailId = 0;
			e.printStackTrace();
		}
		return docDetailId;
	}

	public boolean updateDocumentDetail(int docDetailId, int documentId,
			int shopId, int productId, double productQty, double productPrice,
			String unitName) throws SQLException {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" UPDATE " + TABLE_DOC_DETAIL + 
					" SET " + 
					COLUMN_PRODUCT_AMOUNT + "=" + productQty + ", " + 
					Products.COLUMN_PRODUCT_PRICE + "=" + productPrice + ", " + 
					Products.COLUMN_PRODUCT_UNIT_NAME + "='" + unitName + "' " +
					" WHERE " + COLUMN_DOC_DETAIL_ID + "=" + docDetailId + 
					" AND " + COLUMN_DOC_ID + "=" + documentId + 
					" AND " + Shop.COLUMN_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean deleteDocumentDetail(int docDetailId, int documentId,
			int shopId) {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TABLE_DOC_DETAIL + 
					" WHERE " + COLUMN_DOC_DETAIL_ID + "=" + docDetailId + 
					" AND " + COLUMN_DOC_ID + "=" + documentId + 
					" AND " + Shop.COLUMN_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean deleteDocumentDetail(int documentId, int shopId) {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TABLE_DOC_DETAIL + 
					" WHERE " + COLUMN_DOC_ID + "=" + documentId + 
					" AND " + Shop.COLUMN_SHOP_ID + "=" + shopId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
}
