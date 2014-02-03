package com.syn.mpos.provider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.OrderTransaction;

/**
 * 
 * @author j1tth4
 * 
 */
public class Transaction extends MPOSDatabase {

	public static final int TRANS_STATUS_NEW = 1;
	public static final int TRANS_STATUS_SUCCESS = 2;
	public static final int TRANS_STATUS_VOID = 8;
	public static final int TRANS_STATUS_HOLD = 9;
		
	public static final String TABLE_TRANSACTION = "OrderTransaction";
	public static final String COLUMN_TRANSACTION_ID = "transaction_id";
	public static final String COLUMN_RECEIPT_YEAR = "receipt_year";
	public static final String COLUMN_RECEIPT_MONTH = "receipt_month";
	public static final String COLUMN_RECEIPT_ID = "receipt_id";
	public static final String COLUMN_RECEIPT_NO = "receipt_no";
	public static final String COLUMN_OPEN_TIME = "open_time";
	public static final String COLUMN_CLOSE_TIME = "close_time";
	public static final String COLUMN_OPEN_STAFF = "open_staff_id";
	public static final String COLUMN_CLOSE_STAFF = "close_staff_id";
	public static final String COLUMN_STATUS_ID = "transaction_status_id";
	public static final String COLUMN_PAID_TIME = "paid_time";
	public static final String COLUMN_PAID_STAFF_ID = "paid_staff_id";
	public static final String COLUMN_SALE_DATE = "sale_date";
	public static final String COLUMN_TRANS_VAT = "transaction_vat";
	public static final String COLUMN_TRANS_VATABLE = "transaction_vatable";
	public static final String COLUMN_TRANS_EXCLUDE_VAT = "transaction_exclude_vat";
	public static final String COLUMN_TRANS_NOTE = "transaction_note";
	public static final String COLUMN_VOID_STAFF_ID = "void_staff_id";
	public static final String COLUMN_VOID_REASON = "void_reason";
	public static final String COLUMN_VOID_TIME = "void_time";
	public static final String COLUMN_OTHER_DISCOUNT = "other_discount";
	public static final String COLUMN_MEMBER_ID = "member_id";
	
	public static final String TABLE_ORDER = "OrderDetail";
	public static final String TABLE_ORDER_TMP = "OrderDetailTmp";
	public static final String COLUMN_ORDER_ID = "order_detail_id";
	public static final String COLUMN_ORDER_QTY = "qty";
	public static final String COLUMN_TOTAL_RETAIL_PRICE = "total_retail_price";
	public static final String COLUMN_TOTAL_SALE_PRICE = "total_sale_price";
	public static final String COLUMN_TOTAL_VAT = "total_vat_amount";
	public static final String COLUMN_TOTAL_VAT_EXCLUDE = "total_vat_amount_exclude";
	public static final String COLUMN_MEMBER_DISCOUNT = "member_discount_amount";
	public static final String COLUMN_PRICE_DISCOUNT = "price_discount_amount";
	public static final String COLUMN_DISCOUNT_TYPE = "discount_type";
	
	public Transaction(SQLiteDatabase db) {
		super(db);
	}

	public Cursor queryTransaction(String[] columns, String selection, 
			String[] selectionArgs, String orderBy){
		Cursor cursor = null;
		cursor = mSqlite.query(TABLE_TRANSACTION, columns, selection, 
				selectionArgs, null, null, orderBy);
		return cursor;
	}
	
	public OrderTransaction getTransaction(int transactionId, int computerId) {
		OrderTransaction trans = new OrderTransaction();
		Cursor cursor = queryTransaction(
				new String[]{
						COLUMN_TRANSACTION_ID, 
						Computer.COLUMN_COMPUTER_ID,
						COLUMN_STATUS_ID,
						COLUMN_PAID_TIME, 
						COLUMN_RECEIPT_NO
				},
				COLUMN_TRANSACTION_ID + "=? AND "
				+ Computer.COLUMN_COMPUTER_ID + "=? AND "
				+ COLUMN_STATUS_ID + "=?",
				new String[]{String.valueOf(transactionId), String.valueOf(computerId),
						String.valueOf(TRANS_STATUS_SUCCESS)}, COLUMN_SALE_DATE);
		if(cursor != null){
			if (cursor.moveToFirst()) {
					trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID)));
					trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COLUMN_COMPUTER_ID)));
					trans.setTransactionStatusId(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS_ID)));
					trans.setPaidTime(cursor.getString(cursor.getColumnIndex(COLUMN_PAID_TIME)));
					trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(COLUMN_RECEIPT_NO)));
			}
			cursor.close();
		}
		return trans;
	}

	public OrderTransaction getTransaction(long saleDate) {
		OrderTransaction trans = new OrderTransaction();
		Cursor cursor = queryTransaction(
				new String[]{
						COLUMN_TRANSACTION_ID, 
						Computer.COLUMN_COMPUTER_ID,
						COLUMN_PAID_TIME, 
						COLUMN_RECEIPT_NO
				},
				COLUMN_SALE_DATE + "=? AND " 
				+ COLUMN_STATUS_ID + "=?",
				new String[]{
						String.valueOf(saleDate),
						String.valueOf(TRANS_STATUS_SUCCESS), 
				}, COLUMN_SALE_DATE);
		
		if(cursor != null){
			if (cursor.moveToFirst()) {
					trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID)));
					trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COLUMN_COMPUTER_ID)));
					trans.setPaidTime(cursor.getString(cursor.getColumnIndex(COLUMN_PAID_TIME)));
					trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(COLUMN_RECEIPT_NO)));
			}
			cursor.close();
		}
		return trans;
	}

	public double getMemberDiscount(int transactionId, int computerId, boolean tempTable){
		double memberDiscount = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_MEMBER_DISCOUNT + ")" +
				" FROM " + (tempTable == true ? TABLE_ORDER_TMP : TABLE_ORDER) +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? AND " +
				Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{String.valueOf(transactionId), 
					String.valueOf(computerId)});
		if(cursor.moveToFirst()){
			memberDiscount = cursor.getFloat(0);
		}
		cursor.close();
		return memberDiscount;
	}
	
	public double getOtherDiscount(int transactionId, int computerId){
		double otherDiscount = 0.0f;
		Cursor cursor = mSqlite.query(TABLE_TRANSACTION, new String[]{COLUMN_OTHER_DISCOUNT}, 
				COLUMN_TRANSACTION_ID + "=? AND " + 
				Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(transactionId), 
				String.valueOf(computerId)}, null, null, null);
		if(cursor.moveToFirst()){
			otherDiscount = cursor.getFloat(0);
		}
		cursor.close();
		return otherDiscount;
	}
	
	public String getReceiptNo(int transactionId, int computerId){
		String receiptNo = "";
		Cursor cursor = mSqlite.query(TABLE_TRANSACTION, 
				new String[]{
					COLUMN_RECEIPT_NO
				},
				COLUMN_TRANSACTION_ID + "=? AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			receiptNo = cursor.getString(0);
		}
		cursor.close();
		return receiptNo;
	}
	
	public double getTransactionVatExclude(int transactionId, int computerId){
		double transVatExclude = 0.0f;
		Cursor cursor = mSqlite.query(TABLE_TRANSACTION, 
				new String[]{
					COLUMN_TRANS_EXCLUDE_VAT
				},
				COLUMN_TRANSACTION_ID + "=? AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			transVatExclude = cursor.getFloat(0);
		}
		cursor.close();
		return transVatExclude;
	}
	
	public double getTransactionVatable(int transactionId, int computerId){
		double transVatable = 0.0f;
		Cursor cursor = mSqlite.query(TABLE_TRANSACTION, 
				new String[]{
					COLUMN_TRANS_VATABLE
				},
				COLUMN_TRANSACTION_ID + "=? AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			transVatable = cursor.getFloat(0);
		}
		cursor.close();
		return transVatable;
	}
	
	public double getTransactionVat(int transactionId, int computerId){
		double transVat = 0.0f;
		Cursor cursor = mSqlite.query(TABLE_TRANSACTION, 
				new String[]{
					COLUMN_TRANS_VAT
				},
				COLUMN_TRANSACTION_ID + "=? AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			transVat = cursor.getFloat(0);
		}
		cursor.close();
		return transVat;
	}
	
	public double getDiscountPriceDiscount(int transactionId, int computerId){
		double priceDiscount = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_PRICE_DISCOUNT + ")" +
				" FROM " + TABLE_ORDER_TMP +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			priceDiscount = cursor.getFloat(0);
		}
		cursor.close();
		return priceDiscount;
	}
	
	public double getPriceDiscount(int transactionId, int computerId){
		double priceDiscount = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_PRICE_DISCOUNT + ")" +
				" FROM " + TABLE_ORDER +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			priceDiscount = cursor.getFloat(0);
		}
		cursor.close();
		return priceDiscount;
	}
	
	public double getDiscountTotalVatExclude(int transactionId, int computerId){
		double totalVat = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_VAT_EXCLUDE + ")" +
				" FROM " + TABLE_ORDER_TMP +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ", 
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		return totalVat;
	}
	
	public double getTotalVatExclude(int transactionId, int computerId){
		double totalVat = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_VAT_EXCLUDE + ")" +
				" FROM " + TABLE_ORDER +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ", 
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		return totalVat;
	}
	
	public double getDiscountTotalVat(int transactionId, int computerId){
		double totalVat = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_VAT + ")" +
				" FROM " + TABLE_ORDER_TMP +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		return totalVat;
	}
	
	public double getTotalVat(int transactionId, int computerId){
		double totalVat = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_VAT + ")" +
				" FROM " + TABLE_ORDER +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalVat = cursor.getFloat(0);
		}
		cursor.close();
		return totalVat;
	}
	
	public double getDiscountTotalSalePrice(int transactionId, int computerId){
		double totalSalePrice = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_SALE_PRICE + ")" +
				" FROM " + TABLE_ORDER_TMP +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalSalePrice = cursor.getFloat(0);
		}
		cursor.close();
		return totalSalePrice;
	}
	
	public double getTotalSalePrice(int transactionId, int computerId){
		double totalSalePrice = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_SALE_PRICE + ")" +
				" FROM " + TABLE_ORDER +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalSalePrice = cursor.getFloat(0);
		}
		cursor.close();
		return totalSalePrice;
	}
	
	public double getDisocuntTotalRetailPrice(int transactionId, int computerId){
		double totalRetailPrice = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_RETAIL_PRICE + ")" +
				" FROM " + TABLE_ORDER_TMP +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalRetailPrice = cursor.getFloat(0);
		}
		cursor.close();
		return totalRetailPrice;
	}
	
	public double getTotalRetailPrice(int transactionId, int computerId){
		double totalRetailPrice = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_TOTAL_RETAIL_PRICE + ")" +
				" FROM " + TABLE_ORDER +
				" WHERE " + COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=? ",
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalRetailPrice = cursor.getFloat(0);
		}
		cursor.close();
		return totalRetailPrice;
	}

	public int getTotalQty(int transactionId, int computerId){
		int totalQty = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_ORDER_QTY + ") " +
				" FROM " + TABLE_ORDER + 
				" WHERE " + COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?",
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalQty = cursor.getInt(0);
		}
		cursor.close();
		return totalQty;
	}
	
	public OrderTransaction.OrderDetail getOrder(int transactionId,
			int computerId, int orderDetailId) {
		OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COLUMN_ORDER_ID + ", " + 
				" a." + Products.COLUMN_PRODUCT_ID + ", " +
				" a." + COLUMN_ORDER_QTY + ", " +
				" a." + Products.COLUMN_PRODUCT_PRICE + ", " + 
				" a." + COLUMN_TOTAL_RETAIL_PRICE + ", " +
				" a." + COLUMN_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COLUMN_VAT_TYPE + ", " +
				" a." + COLUMN_MEMBER_DISCOUNT + ", " +
				" a." + COLUMN_PRICE_DISCOUNT + ", " + 
				" a." + COLUMN_DISCOUNT_TYPE + ", " +
				" b." + Products.COLUMN_PRODUCT_NAME +
				" FROM " + TABLE_ORDER + " a " +
				" LEFT JOIN " + Products.TABLE_PRODUCT + " b " +
				" ON a." + Products.COLUMN_PRODUCT_ID + "=" +
				" b." + Products.COLUMN_PRODUCT_ID + 
				" WHERE a." + COLUMN_TRANSACTION_ID + "=?" + 
				" AND a." + Computer.COLUMN_COMPUTER_ID + "=?" + 
				" AND a." + COLUMN_ORDER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId),
						String.valueOf(computerId),
						String.valueOf(orderDetailId)
				});
		
		if (cursor.moveToFirst()) {
			orderDetail = toOrderDetail(cursor);
		}
		cursor.close();
		return orderDetail;
	}

	public List<OrderTransaction.OrderDetail> listAllOrderTmp(int transactionId,
			int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<OrderTransaction.OrderDetail>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COLUMN_ORDER_ID + ", " +
				" a." + Products.COLUMN_PRODUCT_ID + ", " +
				" a." + COLUMN_ORDER_QTY + ", " +
				" a." + Products.COLUMN_PRODUCT_PRICE + ", " +
				" a." + COLUMN_TOTAL_RETAIL_PRICE + ", " +
				" a." + COLUMN_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COLUMN_VAT_TYPE + ", " +
				" a." + COLUMN_MEMBER_DISCOUNT + ", " +
				" a." + COLUMN_PRICE_DISCOUNT + ", " +
				" a." + COLUMN_DISCOUNT_TYPE + ", " +
				" b." + Products.COLUMN_PRODUCT_NAME + 
				" FROM " + TABLE_ORDER_TMP + " a " +
				" LEFT JOIN " + Products.TABLE_PRODUCT + " b " +
				" ON a." + Products.COLUMN_PRODUCT_ID + "=" +
				" b." + Products.COLUMN_PRODUCT_ID +
				" WHERE a." + COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId),
						String.valueOf(computerId)
				});
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	public List<OrderTransaction.OrderDetail> listAllOrder(int transactionId,
			int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<OrderTransaction.OrderDetail>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COLUMN_ORDER_ID + ", " +
				" a." + Products.COLUMN_PRODUCT_ID + ", " +
				" a." + COLUMN_ORDER_QTY + ", " +
				" a." + Products.COLUMN_PRODUCT_PRICE + ", " +
				" a." + COLUMN_TOTAL_RETAIL_PRICE + ", " +
				" a." + COLUMN_TOTAL_SALE_PRICE + ", " +
				" a." + Products.COLUMN_VAT_TYPE + ", " +
				" a." + COLUMN_MEMBER_DISCOUNT + ", " +
				" a." + COLUMN_PRICE_DISCOUNT + ", " +
				" a." + COLUMN_DISCOUNT_TYPE + ", " +
				" b." + Products.COLUMN_PRODUCT_NAME + 
				" FROM " + TABLE_ORDER + " a" +
				" LEFT JOIN " + Products.TABLE_PRODUCT + " b" +
				" ON a." + Products.COLUMN_PRODUCT_ID + "=" +
				" b." + Products.COLUMN_PRODUCT_ID +
				" WHERE a." + COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)
				});
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	private OrderTransaction.OrderDetail toOrderDetail(Cursor cursor){
		OrderTransaction.OrderDetail orderDetail = 
				new OrderTransaction.OrderDetail();
		
		orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_ID)));
		orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex(Products.COLUMN_PRODUCT_ID)));
		orderDetail.setProductName(cursor.getString(cursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME)));
		orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex(COLUMN_ORDER_QTY)));
		orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex(Products.COLUMN_PRODUCT_PRICE)));
		orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex(COLUMN_TOTAL_RETAIL_PRICE)));
		orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex(COLUMN_TOTAL_SALE_PRICE)));
		orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex(Products.COLUMN_VAT_TYPE)));
		orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex(COLUMN_MEMBER_DISCOUNT)));
		orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE_DISCOUNT)));
		orderDetail.setDiscountType(cursor.getInt(cursor.getColumnIndex(COLUMN_DISCOUNT_TYPE)));
		
		return orderDetail;
	}

	public static String formatReceiptNo(String header, int year, int month, int id){
		String receiptYear = 
				String.format(Locale.getDefault(), "%04d", year);
		String receiptMonth = 
				String.format(Locale.getDefault(), "%02d", month);
		String receiptId = 
				String.format(Locale.getDefault(), "%06d", id);
		return header + receiptMonth + receiptYear + "/" + receiptId;
	}
	
	public List<OrderTransaction> listTransaction(long saleDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COLUMN_TRANSACTION_ID + ", " +
				" a." + Computer.COLUMN_COMPUTER_ID + ", " + 
				" a." + COLUMN_PAID_TIME + ", " +
				" a." + COLUMN_TRANS_NOTE + ", " +
				" a." + COLUMN_RECEIPT_YEAR + ", " +
				" a." + COLUMN_RECEIPT_MONTH + ", " +
				" a." + COLUMN_RECEIPT_ID + ", " +
				" a." + COLUMN_RECEIPT_NO + ", " +
				" b." + Staff.COLUMN_STAFF_CODE + ", " +
				" b." + Staff.COLUMN_STAFF_NAME + 
				" FROM " + TABLE_TRANSACTION + " a " + 
				" LEFT JOIN " + Staff.TABLE_STAFF + " b " + 
				" ON a." + COLUMN_OPEN_STAFF + "=b." + Staff.COLUMN_STAFF_ID +
				" WHERE a." + COLUMN_SALE_DATE + "=?" + 
				" AND a." + COLUMN_STATUS_ID + "=?", 
				new String[]{
						String.valueOf(saleDate), 
						String.valueOf(TRANS_STATUS_SUCCESS)
				});
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COLUMN_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(COLUMN_TRANS_NOTE)));
				trans.setPaidTime(cursor.getString(cursor.getColumnIndex(COLUMN_PAID_TIME)));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COLUMN_STAFF_CODE))
						+ ":" + cursor.getString(cursor.getColumnIndex(Staff.COLUMN_STAFF_NAME)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(COLUMN_RECEIPT_NO)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	public List<OrderTransaction> listHoldOrder(int computerId) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + COLUMN_TRANSACTION_ID + ", " +
				" a." + Computer.COLUMN_COMPUTER_ID + ", " + 
				" a." + COLUMN_OPEN_TIME + ", " +
				" a." + COLUMN_TRANS_NOTE + ", " +
				" b." + Staff.COLUMN_STAFF_CODE + ", " +
				" b." + Staff.COLUMN_STAFF_NAME + 
				" FROM " + TABLE_TRANSACTION + " a " +
				" LEFT JOIN " + Staff.TABLE_STAFF + " b " + 
				" ON a." + COLUMN_OPEN_STAFF + "=" +
				" b." + Staff.COLUMN_STAFF_ID +
				" WHERE a." + Computer.COLUMN_COMPUTER_ID + "=?" + 
				" AND a." + COLUMN_SALE_DATE + "=?" + 
				" AND a." + COLUMN_STATUS_ID + "=?", 
				new String[]{
					String.valueOf(computerId),
					String.valueOf(Util.getDate().getTimeInMillis()),
					String.valueOf(TRANS_STATUS_HOLD)
				});
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COLUMN_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(COLUMN_TRANS_NOTE)));
				trans.setOpenTime(cursor.getString(cursor.getColumnIndex(COLUMN_OPEN_TIME)));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COLUMN_STAFF_CODE))
						+ ":" + cursor.getString(cursor.getColumnIndex(Staff.COLUMN_STAFF_NAME)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	public int getMaxTransaction(int computerId) {
		int transactionId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COLUMN_TRANSACTION_ID + ") " +
				" FROM " + TABLE_TRANSACTION +
				" WHERE " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
						String.valueOf(computerId)
				});
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return transactionId + 1;
	}

	public int getMaxReceiptId(int computerId, int year, int month) {
		int maxReceiptId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COLUMN_RECEIPT_ID + ") " +
				" FROM " + TABLE_TRANSACTION + 
				" WHERE " + Computer.COLUMN_COMPUTER_ID + "=?" +	 
				" AND " + COLUMN_RECEIPT_YEAR + "=?" + 
				" AND " + COLUMN_RECEIPT_MONTH + "=?",
				new String[]{
						String.valueOf(computerId),
						String.valueOf(year),
						String.valueOf(month)
				});
		if (cursor.moveToFirst()) {
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return maxReceiptId + 1;
	}

	public int getCurrTransaction(int computerId) {
		int transactionId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT " + COLUMN_TRANSACTION_ID + 
				" FROM " + TABLE_TRANSACTION + 
				" WHERE " + Computer.COLUMN_COMPUTER_ID + "=?" + 
				" AND " + COLUMN_STATUS_ID + "=?" + 
				" AND " + COLUMN_SALE_DATE + "=?", 
				new String[]{
						String.valueOf(computerId),
						String.valueOf(TRANS_STATUS_NEW),
						String.valueOf(Util.getDate().getTimeInMillis())						
				});
		if (cursor.moveToFirst()) {
			if (cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return transactionId;
	}

	public int openTransaction(int computerId, int shopId, int sessionId,
			int staffId) throws SQLException {
		int transactionId = getMaxTransaction(computerId);
		Calendar dateTime = Util.getDateTime();
		Calendar date = Util.getDate();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_UUID, getUUID());
		cv.put(COLUMN_TRANSACTION_ID, transactionId);
		cv.put(Computer.COLUMN_COMPUTER_ID, computerId);
		cv.put(Shop.COLUMN_SHOP_ID, shopId);
		cv.put(Session.COLUMN_SESS_ID, sessionId);
		cv.put(COLUMN_OPEN_STAFF, staffId);
		cv.put(StockDocument.COLUMN_DOC_TYPE, 8);
		cv.put(COLUMN_OPEN_TIME, dateTime.getTimeInMillis());
		cv.put(COLUMN_SALE_DATE, date.getTimeInMillis());
		cv.put(COLUMN_RECEIPT_YEAR, dateTime.get(Calendar.YEAR));
		cv.put(COLUMN_RECEIPT_MONTH, dateTime.get(Calendar.MONTH) + 1);
		
		long rowId = mSqlite.insertOrThrow(TABLE_TRANSACTION, null, cv);
		if(rowId == -1)
			transactionId = 0;
		
		return transactionId;
	}

	public boolean successTransaction(int transactionId, int computerId,
			int staffId){
		boolean isSuccess = false;
		Calendar calendar = Util.getDateTime();
		int receiptId = getMaxReceiptId(computerId,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
		SaleStock stock = new SaleStock(mSqlite);
		String receiptHeader = stock.getDocumentHeader(8); 
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_STATUS_ID, TRANS_STATUS_SUCCESS);
		cv.put(COLUMN_RECEIPT_ID, receiptId);
		cv.put(COLUMN_CLOSE_TIME, calendar.getTimeInMillis());
		cv.put(COLUMN_PAID_TIME, calendar.getTimeInMillis());
		cv.put(COLUMN_PAID_STAFF_ID, staffId);
		cv.put(COLUMN_CLOSE_STAFF, staffId);
		cv.put(COLUMN_RECEIPT_NO, formatReceiptNo(receiptHeader, 
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, receiptId));
		
		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, COLUMN_TRANSACTION_ID + "=?"
				+ " AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(computerId)
				});
		
		if(affectedRow > 0) 
			isSuccess = true;
		return isSuccess;
	}
	
	public boolean prepareTransaction(int transactionId, 
			int computerId) throws SQLException {
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_STATUS_ID, TRANS_STATUS_NEW);
		cv.put(COLUMN_TRANS_NOTE, "");

		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, COLUMN_TRANSACTION_ID + "=? AND " + 
				Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(computerId)
				});
		
		if(affectedRow > 0)
			isSuccess = true;

		return isSuccess;
	}

	public boolean deleteTransaction(int transactionId, 
			int computerId) throws SQLException {
		boolean isSuccess = false;
		
		int affectedRow = mSqlite.delete(TABLE_TRANSACTION, COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				});
		
		if(affectedRow >= 0)
			isSuccess = true;
		
		return isSuccess;
	}

	public int countHoldOrder(int computerId) {
		int total = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT COUNT(" + COLUMN_TRANSACTION_ID + ") " + 
				" FROM " + TABLE_TRANSACTION + 
				" WHERE " + COLUMN_STATUS_ID + "=?" + 
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?" + 
				" AND " + COLUMN_SALE_DATE + "=?", 
				new String[]{
					String.valueOf(TRANS_STATUS_HOLD),
					String.valueOf(computerId),
					String.valueOf(Util.getDate().getTimeInMillis())
				});
		if (cursor.moveToFirst()) {
			total = cursor.getInt(0);
		}
		cursor.close();
		return total;
	}

	public boolean holdTransaction(int transactionId, int computerId,
			String note) throws SQLException {
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_STATUS_ID, TRANS_STATUS_HOLD);
		cv.put(COLUMN_TRANS_NOTE, note);
		
		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?",
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				});
		
		if(affectedRow > 0)
			isSuccess = true;

		return isSuccess;
	}
	
	public void updateTransactionSendStatus(String saleDate){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEND_STATUS, SyncSaleLog.SYNC_SUCCESS);
		mSqlite.update(TABLE_TRANSACTION, cv, 
				COLUMN_SALE_DATE + "=?" +
						" AND " + COLUMN_STATUS_ID + " IN(?,?) ", 
				new String[]{
					saleDate,
					String.valueOf(TRANS_STATUS_SUCCESS),
					String.valueOf(TRANS_STATUS_VOID)
				});
	}
	
	public boolean updateTransactionVat(int transactionId, int computerId, double totalSalePrice) {
		boolean isSuccess = false;
		double totalVat = getTotalVat(transactionId, computerId);
		double totalVatExclude = getTotalVatExclude(transactionId, computerId);
		double totalVatable = totalSalePrice + totalVatExclude;
		
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRANS_VAT, totalVat + totalVatExclude);
		cv.put(COLUMN_TRANS_VATABLE, totalVatable);
		cv.put(COLUMN_TRANS_EXCLUDE_VAT, totalVatExclude);
		
		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, 
				COLUMN_TRANSACTION_ID + "=? AND " + 
				Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(computerId)
				});
		
		if(affectedRow > 0)
			isSuccess = true;	
		
		return isSuccess;
	}

	public boolean cancelDiscount(int transactionId, int computerId) throws SQLException{
		return deleteOrderDetailTmp(transactionId, computerId);
	}
	
	public boolean confirmDiscount(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		
		if (deleteOrderDetail(transactionId, computerId)) {
			try {
				mSqlite.execSQL(
						" INSERT INTO " + TABLE_ORDER + 
						" SELECT * FROM " + TABLE_ORDER_TMP +
						" WHERE " + COLUMN_TRANSACTION_ID + "=" + transactionId + 
						" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId);
				isSuccess = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	public boolean otherDiscount(int transactionId, int computerId, double discount){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_OTHER_DISCOUNT, discount);
		
		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, 
				COLUMN_TRANSACTION_ID + "=? " +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
			    new String[]{
					String.valueOf(transactionId), 
					String.valueOf(computerId)
				});
		
		if(affectedRow > 0)
			isSuccess = true;
		
		return isSuccess;
	}
	
	public boolean discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, int vatType, double vatRate, 
			double salePrice, double discount, int discountType) {
		boolean isSuccess = false;
		double vat = Util.calculateVat(salePrice, vatRate);
		try {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PRICE_DISCOUNT, discount);
			cv.put(COLUMN_TOTAL_SALE_PRICE, salePrice);
			if(vatType == Products.VAT_TYPE_INCLUDED)
				cv.put(COLUMN_TOTAL_VAT, vat);
			else if(vatType == Products.VAT_TYPE_EXCLUDE)
				cv.put(COLUMN_TOTAL_VAT_EXCLUDE, vat);
			else
			cv.put(COLUMN_DISCOUNT_TYPE, discountType);
	
			mSqlite.update(TABLE_ORDER_TMP, cv, 
					COLUMN_ORDER_ID + "=? " +
					" AND " + COLUMN_TRANSACTION_ID + "=?" +
					" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
					new String[]{
						String.valueOf(orderDetailId), 
						String.valueOf(transactionId), 
						String.valueOf(computerId)
					});
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		int affectedRow = mSqlite.delete(TABLE_ORDER, 
				COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				});
		
		if(affectedRow >= 0)
			isSuccess = true;
		
		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId,
			int orderDetailId) throws SQLException{
		boolean isSuccess = false;
		int affectedRow = mSqlite.delete(TABLE_ORDER, 
				COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?" +
				" AND " + COLUMN_ORDER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId),
					String.valueOf(orderDetailId)
				});
		
		if(affectedRow > 0)
			isSuccess = true;
		
		return isSuccess;
	}

	public boolean copyOrderToTmp(int transactionId, int computerId) {
		boolean isSuccess = false;
		if (deleteOrderDetailTmp(transactionId, computerId)) {
			try {
				mSqlite.execSQL(
						" INSERT INTO " + TABLE_ORDER_TMP + 
						" SELECT * FROM " + TABLE_ORDER + 
						" WHERE " + COLUMN_TRANSACTION_ID + "=" + transactionId + 
						" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId);
				isSuccess = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

	private boolean deleteOrderDetailTmp(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		int affectedRow = mSqlite.delete(TABLE_ORDER_TMP, 
				COLUMN_TRANSACTION_ID + "=?" +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				});
		
		if(affectedRow >= 0)
			isSuccess = true;
		
		return isSuccess;
	}

	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, double vatRate, double orderQty, 
			double pricePerUnit) {
		boolean isSuccess = false;
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Util.calculateVat(totalRetailPrice, vatRate);
		
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ORDER_QTY, orderQty);
		cv.put(Products.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		if(vatType == Products.VAT_TYPE_INCLUDED)
			cv.put(COLUMN_TOTAL_VAT, vat);
		else if(vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(COLUMN_TOTAL_VAT_EXCLUDE, vat);
		cv.put(COLUMN_PRICE_DISCOUNT, 0);

		int affectRow = mSqlite.update(TABLE_ORDER, cv, 
				COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + COLUMN_ORDER_ID + "=? "
				+ " AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(computerId)
				});
		
		if(affectRow > 0)
			isSuccess = true;
		
		return isSuccess;
	}

	public int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, double vatRate, double orderQty,
			double pricePerUnit) throws SQLException {
		
		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Util.calculateVat(totalRetailPrice, vatRate);
		
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ORDER_ID, orderDetailId);
		cv.put(COLUMN_TRANSACTION_ID, transactionId);
		cv.put(Computer.COLUMN_COMPUTER_ID, computerId);
		cv.put(Products.COLUMN_PRODUCT_ID, productId);
		cv.put(COLUMN_ORDER_QTY, orderQty);
		cv.put(Products.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(Products.COLUMN_VAT_TYPE, vatType);
		if(vatType == Products.VAT_TYPE_INCLUDED)
			cv.put(COLUMN_TOTAL_VAT, vat);
		else if(vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(COLUMN_TOTAL_VAT_EXCLUDE, vat);

		long rowId = mSqlite.insertOrThrow(TABLE_ORDER, null, cv);
		if(rowId == -1)
			orderDetailId = 0;
		
		return orderDetailId;
	}

	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COLUMN_ORDER_ID + ") " + 
				" FROM " + TABLE_ORDER +  
				" WHERE " + COLUMN_TRANSACTION_ID + "=?" + 
				" AND " + Computer.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId),
						String.valueOf(computerId)
				});
		
		if (cursor.moveToFirst()) {
			orderDetailId = cursor.getInt(0);
		}
		cursor.close();
		
		return orderDetailId + 1;
	}
	
	public boolean voidTransaction(int transactionId, int computerId,
			int staffId, String reason) throws SQLException {
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_STATUS_ID, TRANS_STATUS_VOID);
		cv.put(COLUMN_VOID_STAFF_ID, staffId);
		cv.put(COLUMN_VOID_REASON, reason);
		cv.put(COLUMN_VOID_TIME, Util.getDateTime().getTimeInMillis());
		
		int affectedRow = mSqlite.update(TABLE_TRANSACTION, cv, COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + Computer.COLUMN_COMPUTER_ID + "=? ", 
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(computerId)
				});
		if(affectedRow > 0)
			isSuccess = true;
		
		return isSuccess;
	}
	
	public int getTotalReceipt(String sessionDate){
		int totalReceipt = 0;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT COUNT (" + Transaction.COLUMN_TRANSACTION_ID + ") "
						+ " FROM " + Transaction.TABLE_TRANSACTION 
						+ " WHERE " + Transaction.COLUMN_SALE_DATE + "=? "
						+ " AND " + Transaction.COLUMN_STATUS_ID + " IN (?,?)", 
				new String[]{
						sessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS), 
						String.valueOf(Transaction.TRANS_STATUS_VOID)
				});
		if(cursor.moveToFirst()){
			totalReceipt = cursor.getInt(0);
		}
		cursor.close();
		return totalReceipt;
	}
	
	public double getTotalReceiptAmount(String sessionDate){
		double totalReceiptAmount = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT SUM (" + Transaction.COLUMN_TRANS_VATABLE + ") "
						+ " FROM " + Transaction.TABLE_TRANSACTION 
						+ " WHERE " + Transaction.COLUMN_SALE_DATE + "=? "
						+ " AND " + Transaction.COLUMN_STATUS_ID + "=?", 
				new String[]{
						sessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		if(cursor.moveToFirst()){
			totalReceiptAmount = cursor.getFloat(0);
		}
		cursor.close();
		return totalReceiptAmount;
	}
}
