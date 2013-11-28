package com.syn.mpos.database.transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.inventory.StockDocument;
import com.syn.pos.OrderTransaction;
import com.syn.pos.OrderTransaction.OrderDetail;

/**
 * 
 * @author j1tth4
 * 
 */
public class Transaction extends MPOSSQLiteHelper {

	public static final int TRANS_STATUS_NEW = 1;
	public static final int TRANS_STATUS_SUCCESS = 2;
	public static final int TRANS_STATUS_VOID = 8;
	public static final int TRANS_STATUS_HOLD = 9;
		
	public static final String TB_TRANS = "OrderTransaction";
	public static final String COL_TRANS_ID = "TransactionId";
	public static final String COL_RECEIPT_YEAR = "ReceiptYear";
	public static final String COL_RECEIPT_MONTH = "ReceiptMonth";
	public static final String COL_RECEIPT_ID = "ReceiptId";
	public static final String COL_DOC_TYPE = "DocTypeId";
	public static final String COL_OPEN_TIME = "OpenTime";
	public static final String COL_CLOSE_TIME = "CloseTime";
	public static final String COL_OPEN_STAFF = "OpenStaffId";
	public static final String COL_CLOSE_STAFF = "CloseStaffId";
	public static final String COL_STATUS_ID = "TransactionStatusId";
	public static final String COL_PAID_TIME = "PaidTime";
	public static final String COL_PAID_STAFF_ID = "PaidStaffId";
	public static final String COL_SALE_DATE = "SaleDate";
	public static final String COL_TRANS_VAT = "TransactionVat";
	public static final String COL_TRANS_VATABLE = "TransactionVatable";
	public static final String COL_TRANS_EXCLUDE_VAT = "TransactionExcludeVat";
	public static final String COL_TRANS_NOTE = "TransactionNote";
	public static final String COL_VOID_STAFF_ID = "VoidStaffId";
	public static final String COL_VOID_REASON = "VoidReason";
	public static final String COL_VOID_TIME = "VoidTime";
	
	public static final String TB_ORDER = "OrderDetail";
	public static final String TB_ORDER_TMP = "OrderDetailTmp";
	public static final String COL_ORDER_ID = "OrderDetailId";
	public static final String COL_ORDER_QTY = "Qty";
	public static final String COL_TOTAL_RETAIL_PRICE = "TotalRetailPrice";
	public static final String COL_TOTAL_SALE_PRICE = "TotalSalePrice";
	public static final String COL_TOTAL_VAT = "TotalVatAmount";
	public static final String COL_MEMBER_DISCOUNT = "MemberDiscountAmount";
	public static final String COL_PRICE_DISCOUNT = "PriceDiscountAmount";
	
	public Transaction(Context c) {
		super(c);
	}

	@SuppressLint("DefaultLocale")
	public OrderTransaction getTransaction(int transactionId, int computerId) {
		OrderTransaction trans = new OrderTransaction();
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_TRANS_ID + ", " +
				"a." + Computer.COL_COMPUTER_ID + ", " + 
				"a." + COL_PAID_TIME + ", " +
				"a." + COL_RECEIPT_YEAR + ", " +
				"a." + COL_RECEIPT_MONTH + ", " +
				"a." + COL_RECEIPT_ID + ", " + 
				"b." + COL_DOC_TYPE + 
				" FROM " + TB_TRANS + " a " + 
				" LEFT JOIN " + StockDocument.TB_DOCUMENT_TYPE + " b " + 
				" ON a." + COL_DOC_TYPE + "=b." + StockDocument.COL_DOC_TYPE + 
				" WHERE a." + COL_TRANS_ID + "=" + transactionId + 
				" AND a." + Computer.COL_COMPUTER_ID + "=" + computerId + 
				" AND a." + COL_STATUS_ID + "=" + TRANS_STATUS_SUCCESS + 
				" ORDER BY a." + COL_SALE_DATE, null);
		
		if (cursor.moveToFirst()) {
				String docTypeHeader = cursor.getString(cursor
						.getColumnIndex(StockDocument.COL_DOC_TYPE_HEADER));
				String receiptYear = String.format("%04d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_YEAR)));
				String receiptMonth = String.format("%02d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_MONTH)));
				String receiptId = String.format("%06d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_ID)));
	
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(COL_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(Computer.COL_COMPUTER_ID)));
				trans.setPaidTime(cursor.getLong(cursor
						.getColumnIndex(COL_PAID_TIME)));
				trans.setReceiptNo(docTypeHeader + receiptMonth + receiptYear
						+ receiptId);
		}
		cursor.close();
		close();
		return trans;
	}

	public OrderTransaction getTransaction(long saleDate) {
		OrderTransaction trans = new OrderTransaction();
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_TRANS_ID + ", " +
				"a." + Computer.COL_COMPUTER_ID + ", " + 
				"a." + COL_PAID_TIME + ", " +
				"a." + COL_RECEIPT_YEAR + ", " +
				"a." + COL_RECEIPT_MONTH + ", " +
				"a." + COL_RECEIPT_ID + ", " + 
				"b." + COL_DOC_TYPE + 
				" FROM " + TB_TRANS + " a " + 
				" LEFT JOIN " + StockDocument.TB_DOCUMENT_TYPE + " b " + 
				" ON a." + COL_DOC_TYPE + "=b." + StockDocument.COL_DOC_TYPE + 
				" WHERE a." + COL_SALE_DATE + "='" + saleDate + "' " + 
				" AND a." + COL_STATUS_ID + "=" + TRANS_STATUS_SUCCESS +
				" ORDER BY a." + COL_SALE_DATE, null);
		
		if (cursor.moveToFirst()) {
				String docTypeHeader = cursor.getString(cursor
						.getColumnIndex(StockDocument.COL_DOC_TYPE_HEADER));
				String receiptYear = String.format("%04d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_YEAR)));
				String receiptMonth = String.format("%02d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_MONTH)));
				String receiptId = String.format("%06d",
						cursor.getInt(cursor.getColumnIndex(COL_RECEIPT_ID)));
	
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(COL_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(Computer.COL_COMPUTER_ID)));
				trans.setPaidTime(cursor.getLong(cursor
						.getColumnIndex(COL_PAID_TIME)));
				trans.setReceiptNo(docTypeHeader + receiptMonth + receiptYear
						+ receiptId);
		}
		cursor.close();
		close();
		return trans;
	}

	public float getMemberDiscount(int transactionId, int computerId, boolean tempTable){
		float memberDiscount = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(
				transactionId,
				computerId,
				new String[] { COL_MEMBER_DISCOUNT },
				COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId)},
				tempTable);
		if(cursor.moveToFirst()){
			memberDiscount = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return memberDiscount;
	}
	
	public float getPriceDiscount(int transactionId, int computerId, boolean tempTable){
		float priceDiscount = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(
				transactionId,
				computerId,
				new String[] { COL_PRICE_DISCOUNT },
				COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId)},
				tempTable);
		if(cursor.moveToFirst()){
			priceDiscount = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return priceDiscount;
	}
	
	public float getTotalVatExclude(int transactionId, int computerId, boolean tempTable){
		float totalVat = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(
				transactionId,
				computerId,
				new String[] { COL_TOTAL_VAT },
				COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=? AND "
						+ Products.COL_VAT_TYPE + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId), String.valueOf(2) },
				tempTable);
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return totalVat;
	}
	
	public float getTotalVatIncluded(int transactionId, int computerId, boolean tempTable){
		float totalVat = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(
				transactionId,
				computerId,
				new String[] { COL_TOTAL_VAT },
				COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=? AND "
						+ Products.COL_VAT_TYPE + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId), String.valueOf(1) },
				tempTable);
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return totalVat;
	}
	
	public float getTotalSalePrice(int transactionId, int computerId, boolean tempTable){
		float totalSalePrice = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(transactionId, computerId, 
				new String[]{COL_TOTAL_SALE_PRICE}, COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(computerId)}, tempTable);
		if(cursor.moveToFirst()){
			totalSalePrice = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return totalSalePrice;
	}
	
	public float getTotalRetailPrice(int transactionId, int computerId, boolean tempTable){
		float totalRetailPrice = 0.0f;
		
		open();
		Cursor cursor = getOrderColumn(transactionId, computerId, 
				new String[]{COL_TOTAL_RETAIL_PRICE}, 
				COL_TRANS_ID + "=? AND " + Computer.COL_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(computerId)}, tempTable);
		if(cursor.moveToFirst()){
			totalRetailPrice = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return totalRetailPrice;
	}
	
	public Cursor getOrderColumn(int transactionId, int computerId, 
			String[] colsToSelect, String selection, String[] selectArgs, boolean tempTable){
		return mSqlite.query(tempTable == true ? TB_ORDER_TMP : TB_ORDER, colsToSelect, 
				selection, selectArgs, null, null, null);
	}

	public OrderTransaction.OrderDetail getOrder(int transactionId,
			int computerId, int orderDetailId) {
		OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();
	
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_ORDER_ID + ", " + 
				" a." + Products.COL_PRODUCT_ID + ", " +
				" a." + COL_ORDER_QTY + ", " +
				" a." + Products.COL_PRODUCT_PRICE + ", " + 
				" a." + COL_TOTAL_RETAIL_PRICE + ", " +
				" a." + COL_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COL_VAT_TYPE + ", " +
				" a." + COL_MEMBER_DISCOUNT + ", " +
				" a." + COL_PRICE_DISCOUNT + ", " + 
				" b." + Products.COL_PRODUCT_NAME +
				" FROM " + TB_ORDER + " a " +
				" LEFT JOIN " + Products.TB_PRODUCT + " b " +
				" ON a." + Products.COL_PRODUCT_ID + "=" +
				" b." + Products.COL_PRODUCT_ID + 
				" WHERE a." + COL_TRANS_ID + "=" + transactionId + 
				" AND a." + Computer.COL_COMPUTER_ID + "=" + computerId + 
				" AND a." + COL_ORDER_ID + "=" + orderDetailId, null);
		
		if (cursor.moveToFirst()) {
			orderDetail = toOrderDetail(cursor);
		}
		cursor.close();
		close();
		return orderDetail;
	}

	public List<OrderTransaction.OrderDetail> listAllOrderTmp(int transactionId,
			int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<OrderTransaction.OrderDetail>();
	
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_ORDER_ID + ", " +
				" a." + Products.COL_PRODUCT_ID + ", " +
				" a." + COL_ORDER_QTY + ", " +
				" a." + Products.COL_PRODUCT_PRICE + ", " +
				" a." + COL_TOTAL_RETAIL_PRICE + ", " +
				" a." + COL_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COL_VAT_TYPE + ", " +
				" a." + COL_MEMBER_DISCOUNT + ", " +
				" a." + COL_PRICE_DISCOUNT + ", " +
				" b." + Products.COL_PRODUCT_NAME + 
				" FROM " + TB_ORDER_TMP + " a" +
				" LEFT JOIN " + Products.TB_PRODUCT + " b" +
				" ON a." + Products.COL_PRODUCT_ID + "=" +
				" b." + Products.COL_PRODUCT_ID +
				" WHERE a." + COL_TRANS_ID + "=" + transactionId +
				" AND a." + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return orderDetailLst;
	}

	public List<OrderTransaction.OrderDetail> listAllOrder(int transactionId,
			int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<OrderTransaction.OrderDetail>();
	
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_ORDER_ID + ", " +
				" a." + Products.COL_PRODUCT_ID + ", " +
				" a." + COL_ORDER_QTY + ", " +
				" a." + Products.COL_PRODUCT_PRICE + ", " +
				" a." + COL_TOTAL_RETAIL_PRICE + ", " +
				" a." + COL_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COL_VAT_TYPE + ", " +
				" a." + COL_MEMBER_DISCOUNT + ", " +
				" a." + COL_PRICE_DISCOUNT + ", " +
				" b." + Products.COL_PRODUCT_NAME + 
				" FROM " + TB_ORDER + " a" +
				" LEFT JOIN " + Products.TB_PRODUCT + " b" +
				" ON a." + Products.COL_PRODUCT_ID + "=" +
				" b." + Products.COL_PRODUCT_ID +
				" WHERE a." + COL_TRANS_ID + "=" + transactionId +
				" AND a." + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return orderDetailLst;
	}

	private OrderTransaction.OrderDetail toOrderDetail(Cursor cursor){
		OrderTransaction.OrderDetail orderDetail = 
				new OrderTransaction.OrderDetail();
		
		orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex(COL_ORDER_ID)));
		orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)));
		orderDetail.setProductName(cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_NAME)));
		orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex(COL_ORDER_QTY)));
		orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex(Products.COL_PRODUCT_PRICE)));
		orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex(COL_TOTAL_RETAIL_PRICE)));
		orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex(COL_TOTAL_SALE_PRICE)));
		orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex(Products.COL_VAT_TYPE)));
		orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex(COL_MEMBER_DISCOUNT)));
		orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex(COL_PRICE_DISCOUNT)));
		
		return orderDetail;
	}

	public List<OrderTransaction> listTransaction(long saleDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
	
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_TRANS_ID + ", " +
				" a." + Computer.COL_COMPUTER_ID + ", " + 
				" a." + COL_OPEN_TIME + ", " +
				" a." + COL_TRANS_NOTE + ", " +
				" b." + Staff.COL_STAFF_CODE + ", " +
				" b." + Staff.COL_STAFF_NAME + 
				" FROM " + TB_TRANS + " a " +
				" LEFT JOIN " + Staff.TB_STAFF + " b " + 
				" ON a." + COL_OPEN_STAFF + "=" +
				" b." + Staff.COL_STAFF_ID +
				" WHERE a." + COL_SALE_DATE + "='" + saleDate + "' " + 
				" AND a." + COL_STATUS_ID + "=" + TRANS_STATUS_SUCCESS, null);
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COL_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(COL_TRANS_NOTE)));
				trans.setOpenTime(cursor.getLong(cursor.getColumnIndex(COL_OPEN_TIME)));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_CODE))
						+ ":" + cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_NAME)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return transLst;
	}
	
	public List<OrderTransaction> listHoldOrder(int computerId) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Calendar c = Util.getDate();
	
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COL_TRANS_ID + ", " +
				" a." + Computer.COL_COMPUTER_ID + ", " + 
				" a." + COL_OPEN_TIME + ", " +
				" a." + COL_TRANS_NOTE + ", " +
				" b." + Staff.COL_STAFF_CODE + ", " +
				" b." + Staff.COL_STAFF_NAME + 
				" FROM " + TB_TRANS + " a " +
				" LEFT JOIN " + Staff.TB_STAFF + " b " + 
				" ON a." + COL_OPEN_STAFF + "=" +
				" b." + Staff.COL_STAFF_ID +
				" WHERE a." + Computer.COL_COMPUTER_ID + "=" + computerId + 
				" AND a." + COL_SALE_DATE + "='" + c.getTimeInMillis() + "' " + 
				" AND a." + COL_STATUS_ID + "=" + TRANS_STATUS_HOLD, null);
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COL_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(COL_TRANS_NOTE)));
				trans.setOpenTime(cursor.getLong(cursor.getColumnIndex(COL_OPEN_TIME)));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_CODE))
						+ ":" + cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_NAME)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return transLst;
	}

	public int getMaxTransaction(int computerId) {
		int transactionId = 0;
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COL_TRANS_ID + ") " +
				" FROM " + TB_TRANS +
				" WHERE " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return transactionId + 1;
	}

	public int getMaxReceiptId(int computerId, int year, int month) {
		int maxReceiptId = 0;
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COL_RECEIPT_ID + ") " +
				" FROM " + TB_TRANS + 
				" WHERE " + Computer.COL_COMPUTER_ID + "=" + computerId + 
				" AND " + COL_RECEIPT_YEAR + "=" + year + 
				" AND " + COL_RECEIPT_MONTH + "=" + month +
				" AND " + COL_STATUS_ID + "=" + TRANS_STATUS_SUCCESS, null);
		if (cursor.moveToFirst()) {
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return maxReceiptId + 1;
	}

	public int getCurrTransaction(int computerId) {
		int transactionId = 0;
		Calendar c = Util.getDate();
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT " + COL_TRANS_ID + 
				" FROM " + TB_TRANS + 
				" WHERE " + Computer.COL_COMPUTER_ID + "= " + computerId + 
				" AND " + COL_STATUS_ID + "=" + TRANS_STATUS_NEW + 
				" AND " + COL_SALE_DATE + "='" + c.getTimeInMillis() + "' ", null);
		if (cursor.moveToFirst()) {
			if (cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return transactionId;
	}

	public int openTransaction(int computerId, int shopId, int sessionId,
			int staffId) throws SQLException {
		int transactionId = getMaxTransaction(computerId);
		Calendar dateTime = Util.getDateTime();
		Calendar date = Util.getDate();

		ContentValues cv = new ContentValues();
		cv.put(COL_TRANS_ID, transactionId);
		cv.put(Computer.COL_COMPUTER_ID, computerId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(Session.COL_SESS_ID, sessionId);
		cv.put(COL_OPEN_STAFF, staffId);
		cv.put(COL_DOC_TYPE, 8);
		cv.put(COL_OPEN_TIME, dateTime.getTimeInMillis());
		cv.put(COL_SALE_DATE, date.getTimeInMillis());
		cv.put(COL_RECEIPT_YEAR, dateTime.get(Calendar.YEAR));
		cv.put(COL_RECEIPT_MONTH, dateTime.get(Calendar.MONTH));
		
		open();
		try {
			mSqlite.insertOrThrow(TB_TRANS, null, cv);
		} catch (Exception e) {
			e.printStackTrace();
			transactionId = 0;
		}
		close();
		return transactionId;
	}

	public boolean successTransaction(int transactionId, int computerId,
			int staffId) throws SQLException{
		boolean isSuccess = false;
		Calendar calendar = Util.getDateTime();
		int receiptId = getMaxReceiptId(computerId,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
		
		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_TRANS + 
					" SET " + 
					COL_STATUS_ID + "=" + TRANS_STATUS_SUCCESS + ", " + 
					COL_RECEIPT_ID + "=" + receiptId + ", " + 
					COL_CLOSE_TIME + "='" + calendar.getTimeInMillis() + "', " + 
					COL_PAID_TIME + "='" + calendar.getTimeInMillis() + "', " + 
					COL_PAID_STAFF_ID + "=" + staffId + 
					" WHERE " + COL_TRANS_ID + "="+ transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean prepareTransaction(int transactionId, 
			int computerId) throws SQLException {
		boolean isSuccess = false;
		
		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_TRANS + 
					" SET " + 
					COL_STATUS_ID + "=" + TRANS_STATUS_NEW + ", " +
					COL_TRANS_NOTE + "='' " + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean deleteTransaction(int transactionId, 
			int computerId) throws SQLException {
		boolean isSuccess = false;
		
		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_TRANS + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public int countHoldOrder(int computerId) {
		int total = 0;
		Calendar c = Util.getDate();
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT COUNT(" + COL_TRANS_ID + ") " + 
				" FROM " + TB_TRANS + 
				" WHERE " + COL_STATUS_ID + "=" + TRANS_STATUS_HOLD + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId + 
				" AND " + COL_SALE_DATE + "='" + c.getTimeInMillis() + "'", null);
		if (cursor.moveToFirst()) {
			total = cursor.getInt(0);
		}
		cursor.close();
		close();
		return total;
	}

	public boolean holdTransaction(int transactionId, int computerId,
			String note) throws SQLException {
		boolean isSuccess = false;
		
		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_TRANS + 
					" SET " +
					COL_STATUS_ID + "=" + TRANS_STATUS_HOLD + ", " + 
					COL_TRANS_NOTE + "='" + note + "' " + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	/***********/
	public boolean updateTransactionVat(int transactionId, int computerId,
			float totalSalePrice, float vatExclude) {
		boolean isSuccess = false;
		float vat = Util.calculateVat(totalSalePrice, 7.0f);
		
		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_TRANS + 
					" SET " + 
					COL_TRANS_VAT + "=" + vat + ", " + 
					COL_TRANS_VATABLE + "=" + (totalSalePrice + vatExclude) + ", " + 
					COL_TRANS_EXCLUDE_VAT + "=" + vatExclude + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean cancelDiscount(int transactionId, int computerId) throws SQLException{
		return deleteOrderDetailTmp(transactionId, computerId);
	}
	
	public boolean confirmDiscount(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		
		if (deleteOrderDetail(transactionId, computerId)) {
			open();
			try {
				mSqlite.execSQL(
						" INSERT INTO " + TB_ORDER + 
						" SELECT * FROM " + TB_ORDER_TMP +
						" WHERE " + COL_TRANS_ID + "=" + transactionId + 
						" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
				isSuccess = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			close();
		}
		return isSuccess;
	}

	public boolean discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, float vatRate, float salePrice, float discount) {
		boolean isSuccess = false;

		float vat = Util.calculateVat(salePrice, vatRate);

		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_ORDER_TMP + 
					" SET " + 
					COL_PRICE_DISCOUNT + "=" + discount + ", " + 
					COL_TOTAL_SALE_PRICE + "=" + salePrice + ", " + 
					COL_TOTAL_VAT + "=" + vat + ", " + 
					" WHERE " + COL_ORDER_ID + "=" + orderDetailId + 
					" AND " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "="+ computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;

		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_ORDER + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId,
			int orderDetailId) throws SQLException{
		boolean isSuccess = false;

		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_ORDER + 
					" WHERE " + COL_ORDER_ID + "=" + orderDetailId + 
					" AND " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean copyOrderToTmp(int transactionId, int computerId) {
		boolean isSuccess = false;
	
		if (deleteOrderDetailTmp(transactionId, computerId)) {
			open();
			try {
				mSqlite.execSQL(
						" INSERT INTO " + TB_ORDER_TMP + 
						" SELECT * FROM " + TB_ORDER + 
						" WHERE " + COL_TRANS_ID + "=" + transactionId + 
						" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
				isSuccess = false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			close();
		}
		return isSuccess;
	}

	private boolean deleteOrderDetailTmp(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
	
		open();
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_ORDER_TMP + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, float vatRate, float orderQty, float pricePerUnit) {
		boolean isSuccess = false;
		float totalRetailPrice = pricePerUnit * orderQty;
		float vat = Util.calculateVat(totalRetailPrice, vatRate);

		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_ORDER + 
					" SET " + 
					COL_ORDER_QTY + "=" + orderQty + ", " + 
					Products.COL_PRODUCT_PRICE + "=" + pricePerUnit + ", " + 
					COL_TOTAL_RETAIL_PRICE + "=" + totalRetailPrice + ", " + 
					COL_TOTAL_SALE_PRICE + "=" + totalRetailPrice + ", " + 
					COL_TOTAL_VAT + "=" + vat + ", " + 
					COL_PRICE_DISCOUNT + "=0 " + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + COL_ORDER_ID + "=" + orderDetailId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}

	public int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, float vatRate, float orderQty,
			float pricePerUnit) {
		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
		float totalRetailPrice = pricePerUnit * orderQty;
		float vat = Util.calculateVat(totalRetailPrice, vatRate);
	
		ContentValues cv = new ContentValues();
		cv.put(COL_ORDER_ID, orderDetailId);
		cv.put(COL_TRANS_ID, transactionId);
		cv.put(Computer.COL_COMPUTER_ID, computerId);
		cv.put(Products.COL_PRODUCT_ID, productId);
		cv.put(COL_ORDER_QTY, orderQty);
		cv.put(Products.COL_PRODUCT_PRICE, pricePerUnit);
		cv.put(COL_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(COL_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(Products.COL_VAT_TYPE, vatType);
		cv.put(COL_TOTAL_VAT, vat);
	
		open();
		try {
			mSqlite.insertOrThrow(TB_ORDER, null, cv);
		} catch (SQLException e) {
			e.printStackTrace();
			orderDetailId = 0;
		}
		close();
		return orderDetailId;
	}

	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;

		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COL_ORDER_ID + ") " + 
				" FROM " + TB_ORDER +  
				" WHERE " + COL_TRANS_ID + "=" + transactionId + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			orderDetailId = cursor.getInt(0);
		}
		cursor.close();
		close();
		return orderDetailId + 1;
	}
	
	public boolean voidTransaction(int transactionId, int computerId,
			int staffId, String reason) throws SQLException {
		boolean isSuccess = false;
		Calendar dateTime = Util.getDateTime();

		open();
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_TRANS + 
					" SET " + COL_STATUS_ID + "=" + TRANS_STATUS_VOID + ", " + 
					COL_VOID_STAFF_ID + "=" + staffId + ", " + 
					COL_VOID_REASON + "='" + reason + "', " + 
					COL_VOID_TIME + "='" + dateTime.getTimeInMillis() + "' " + 
					" WHERE " + COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		return isSuccess;
	}
}
