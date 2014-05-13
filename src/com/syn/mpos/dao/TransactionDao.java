package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.mpos.dao.ComputerDao.ComputerTable;
import com.syn.mpos.dao.MPOSOrderTransaction.OrderSet;
import com.syn.mpos.dao.ProductsDao.ProductComponentGroupTable;
import com.syn.mpos.dao.ProductsDao.ProductComponentTable;
import com.syn.mpos.dao.ProductsDao.ProductsTable;
import com.syn.mpos.dao.SessionDao.SessionTable;
import com.syn.mpos.dao.ShopDao.ShopTable;
import com.syn.mpos.dao.StaffDao.StaffTable;
import com.syn.mpos.dao.StockDocument.DocumentTypeTable;

/**
 * 
 * @author j1tth4
 * 
 */
public class TransactionDao extends MPOSDatabase {

	/*
	 * New transaction status
	 */
	public static final int TRANS_STATUS_NEW = 1;

	/*
	 * Success transaction status
	 */
	public static final int TRANS_STATUS_SUCCESS = 2;

	/*
	 * void transaction status
	 */
	public static final int TRANS_STATUS_VOID = 8;

	/*
	 * hold transaction status
	 */
	public static final int TRANS_STATUS_HOLD = 9;

	public TransactionDao(Context context) {
		super(context);
	}

	/**
	 * @param transactionId
	 * @param computerId
	 * @return MPOSOrderTransaction
	 */
	public MPOSOrderTransaction getTransaction(int transactionId) {
		MPOSOrderTransaction trans = new MPOSOrderTransaction();
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				new String[] { OrderTransactionTable.COLUMN_TRANSACTION_ID,
						ComputerTable.COLUMN_COMPUTER_ID,
						OrderTransactionTable.COLUMN_TRANS_VATABLE,
						OrderTransactionTable.COLUMN_TRANS_VAT,
						OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT,
						OrderTransactionTable.COLUMN_STATUS_ID,
						OrderTransactionTable.COLUMN_PAID_TIME,
						OrderTransactionTable.COLUMN_RECEIPT_NO,
						OrderTransactionTable.COLUMN_OPEN_STAFF },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) }, null, null,
				OrderTransactionTable.COLUMN_SALE_DATE);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setTransactionVatable(cursor.getDouble(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)));
				trans.setTransactionVat(cursor.getDouble(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT)));
				trans.setTransactionVatExclude(cursor.getDouble(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionStatusId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
				trans.setPaidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				trans.setOpenStaffId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
			}
			cursor.close();
		}
		return trans;
	}

	/**
	 * @param saleDate
	 * @return MPOSOrderTransaction
	 */
	// public MPOSOrderTransaction getTransaction(long saleDate) {
	// MPOSOrderTransaction trans = new MPOSOrderTransaction();
	// Cursor cursor = getReadableDatabase().query(
	// OrderTransactionTable.TABLE_ORDER_TRANS,
	// new String[] { OrderTransactionTable.COLUMN_TRANSACTION_ID,
	// ComputerTable.COLUMN_COMPUTER_ID,
	// OrderTransactionTable.COLUMN_TRANS_VATABLE,
	// OrderTransactionTable.COLUMN_TRANS_VAT,
	// OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT,
	// OrderTransactionTable.COLUMN_PAID_TIME,
	// OrderTransactionTable.COLUMN_RECEIPT_NO,
	// OrderTransactionTable.COLUMN_OPEN_STAFF },
	// OrderTransactionTable.COLUMN_SALE_DATE + "=? AND "
	// + OrderTransactionTable.COLUMN_STATUS_ID + "=?",
	// new String[] { String.valueOf(saleDate),
	// String.valueOf(TRANS_STATUS_SUCCESS), }, null, null,
	// OrderTransactionTable.COLUMN_SALE_DATE);
	//
	// if (cursor != null) {
	// if (cursor.moveToFirst()) {
	// trans.setTransactionId(cursor.getInt(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
	// trans.setTransactionVatable(cursor.getDouble(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)));
	// trans.setTransactionVat(cursor.getDouble(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT)));
	// trans.setTransactionVatExclude(cursor.getDouble(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT)));
	// trans.setComputerId(cursor.getInt(cursor
	// .getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
	// trans.setPaidTime(cursor.getString(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
	// trans.setReceiptNo(cursor.getString(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
	// trans.setOpenStaffId(cursor.getInt(cursor
	// .getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
	// }
	// cursor.close();
	// }
	// return trans;
	// }

	/**
	 * Get summary order for discount
	 * 
	 * @param transactionId
	 * @return
	 */
	public MPOSOrderTransaction.MPOSOrderDetail getSummaryOrderForDiscount(
			int transactionId) {
		MPOSOrderTransaction.MPOSOrderDetail orderDetail = new MPOSOrderTransaction.MPOSOrderDetail();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + " SUM (" + OrderDetailTable.COLUMN_ORDER_QTY
						+ ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ", " + " SUM ("
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS "
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_VAT + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
						+ " FROM " + OrderDetailTable.TABLE_ORDER_TMP
						+ " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			orderDetail.setQty(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			orderDetail.setPriceDiscount(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			orderDetail
					.setTotalRetailPrice(cursor.getDouble(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			orderDetail.setTotalSalePrice(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			orderDetail.setVat(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
			orderDetail
					.setVatExclude(cursor.getDouble(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE)));
		}
		cursor.close();
		return orderDetail;
	}

	/**
	 * Get summary order
	 * 
	 * @param transactionId
	 * @return
	 */
	public MPOSOrderTransaction.MPOSOrderDetail getSummaryOrder(
			int transactionId) {
		MPOSOrderTransaction.MPOSOrderDetail orderDetail = new MPOSOrderTransaction.MPOSOrderDetail();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + " SUM (" + OrderDetailTable.COLUMN_ORDER_QTY
						+ ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ", " + " SUM ("
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS "
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_VAT + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
						+ " FROM " + OrderDetailTable.TABLE_ORDER + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			orderDetail.setQty(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			orderDetail.setPriceDiscount(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			orderDetail
					.setTotalRetailPrice(cursor.getDouble(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			orderDetail.setTotalSalePrice(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			orderDetail.setVat(cursor.getDouble(cursor
					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
			orderDetail
					.setVatExclude(cursor.getDouble(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE)));
		}
		cursor.close();
		return orderDetail;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return MPOSOrderTransaction.MPOSOrderDetail
	 */
	public MPOSOrderTransaction.MPOSOrderDetail getOrder(int transactionId,
			int orderDetailId) {
		MPOSOrderTransaction.MPOSOrderDetail orderDetail = new MPOSOrderTransaction.MPOSOrderDetail();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + " a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " a." + ProductsTable.COLUMN_VAT_TYPE + ", " + " a."
						+ OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE + ", "
						+ " b." + ProductsTable.COLUMN_PRODUCT_NAME + " FROM "
						+ OrderDetailTable.TABLE_ORDER + " a " + " LEFT JOIN "
						+ ProductsTable.TABLE_PRODUCTS + " b " + " ON a."
						+ ProductsTable.COLUMN_PRODUCT_ID + "=" + " b."
						+ ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
						+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });

		if (cursor.moveToFirst()) {
			orderDetail = toOrderDetail(cursor);
		}
		cursor.close();
		return orderDetail;
	}

	/**
	 * @param transactionId
	 * @return List<MPOSOrderTransaction.MPOSOrderDetail>
	 */
	public List<MPOSOrderTransaction.MPOSOrderDetail> listAllOrderForDiscount(
			int transactionId) {
		List<MPOSOrderTransaction.MPOSOrderDetail> orderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + " a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " a." + ProductsTable.COLUMN_VAT_TYPE + ", " + " a."
						+ OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE + ", "
						+ " b." + ProductsTable.COLUMN_PRODUCT_NAME + " FROM "
						+ OrderDetailTable.TABLE_ORDER_TMP + " a "
						+ " LEFT JOIN " + ProductsTable.TABLE_PRODUCTS + " b "
						+ " ON a." + ProductsTable.COLUMN_PRODUCT_ID + "="
						+ " b." + ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	/**
	 * @param transactionId
	 * @return List<MPOSOrderTransaction.MPOSOrderDetail>
	 */
	public List<MPOSOrderTransaction.MPOSOrderDetail> listAllOrderGroupByProduct(
			int transactionId) {
		List<MPOSOrderTransaction.MPOSOrderDetail> orderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + " a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_ID + ", "
						+ " SUM(a." + OrderDetailTable.COLUMN_ORDER_QTY
						+ ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " SUM(a." + ProductsTable.COLUMN_PRODUCT_PRICE
						+ ") AS " + ProductsTable.COLUMN_PRODUCT_PRICE + ", "
						+ " SUM(a."
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS "
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " a." + ProductsTable.COLUMN_VAT_TYPE + ", "
						+ " SUM(a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT
						+ ") AS " + OrderDetailTable.COLUMN_MEMBER_DISCOUNT
						+ ", " + " SUM(a."
						+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS "
						+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " + " a."
						+ OrderDetailTable.COLUMN_DISCOUNT_TYPE + ", " + " b."
						+ ProductsTable.COLUMN_PRODUCT_NAME + " FROM "
						+ OrderDetailTable.TABLE_ORDER + " a" + " LEFT JOIN "
						+ ProductsTable.TABLE_PRODUCTS + " b" + " ON a."
						+ ProductsTable.COLUMN_PRODUCT_ID + "=" + " b."
						+ ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
						+ " GROUP BY a." + ProductsTable.COLUMN_PRODUCT_ID
						+ ", a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ " Order BY a." + OrderDetailTable.COLUMN_ORDER_ID,
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	/**
	 * @param transactionId
	 * @return List<MPOSOrderTransaction.MPOSOrderDetail>
	 */
	public List<MPOSOrderTransaction.MPOSOrderDetail> listAllOrder(
			int transactionId) {
		List<MPOSOrderTransaction.MPOSOrderDetail> orderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + " a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " a." + ProductsTable.COLUMN_PRODUCT_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " a." + ProductsTable.COLUMN_VAT_TYPE + ", " + " a."
						+ OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE + ", "
						+ " b." + ProductsTable.COLUMN_PRODUCT_NAME + " FROM "
						+ OrderDetailTable.TABLE_ORDER + " a" + " LEFT JOIN "
						+ ProductsTable.TABLE_PRODUCTS + " b" + " ON a."
						+ ProductsTable.COLUMN_PRODUCT_ID + "=" + " b."
						+ ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	private MPOSOrderTransaction.MPOSOrderDetail toOrderDetail(Cursor cursor) {
		MPOSOrderTransaction.MPOSOrderDetail orderDetail = new MPOSOrderTransaction.MPOSOrderDetail();
		orderDetail.setTransactionId(cursor.getInt(cursor
				.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
		orderDetail.setOrderDetailId(cursor.getInt(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
		orderDetail.setProductId(cursor.getInt(cursor
				.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
		orderDetail.setProductName(cursor.getString(cursor
				.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
		orderDetail.setQty(cursor.getFloat(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
		orderDetail.setPricePerUnit(cursor.getFloat(cursor
				.getColumnIndex(ProductsTable.COLUMN_PRODUCT_PRICE)));
		orderDetail.setTotalRetailPrice(cursor.getFloat(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
		orderDetail.setTotalSalePrice(cursor.getFloat(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
		orderDetail.setVatType(cursor.getInt(cursor
				.getColumnIndex(ProductsTable.COLUMN_VAT_TYPE)));
		orderDetail.setMemberDiscount(cursor.getFloat(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_MEMBER_DISCOUNT)));
		orderDetail.setPriceDiscount(cursor.getFloat(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
		orderDetail.setDiscountType(cursor.getInt(cursor
				.getColumnIndex(OrderDetailTable.COLUMN_DISCOUNT_TYPE)));

		// generate order set detail
		List<MPOSOrderTransaction.OrderSet.OrderSetDetail> orderSetDetailLst = listOrderSetDetail(
				orderDetail.getTransactionId(), orderDetail.getOrderDetailId());
		if (orderSetDetailLst.size() > 0) {
			orderDetail.setOrderSetDetailLst(orderSetDetailLst);
		}
		return orderDetail;
	}

	public static String formatReceiptNo(String header, int year, int month,
			int id) {
		String receiptYear = String.format(Locale.US, "%04d", year);
		String receiptMonth = String.format(Locale.US, "%02d", month);
		String receiptId = String.format(Locale.US, "%06d", id);
		return header + receiptMonth + receiptYear + "/" + receiptId;
	}

	/**
	 * @param saleDate
	 * @return List<MPOSOrderTransaction>
	 */
	public List<MPOSOrderTransaction> listTransaction(String saleDate) {
		List<MPOSOrderTransaction> transLst = new ArrayList<MPOSOrderTransaction>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ ComputerTable.COLUMN_COMPUTER_ID + ", "
						+ OrderTransactionTable.COLUMN_PAID_TIME + ", "
						+ OrderTransactionTable.COLUMN_TRANS_NOTE + ", "
						+ OrderTransactionTable.COLUMN_RECEIPT_NO + ", "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=? AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?, ?)",
				new String[] { saleDate, String.valueOf(TRANS_STATUS_VOID),
						String.valueOf(TRANS_STATUS_SUCCESS) });
		if (cursor.moveToFirst()) {
			do {
				MPOSOrderTransaction trans = new MPOSOrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionStatusId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
				trans.setTransactionNote(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
				trans.setPaidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	/**
	 * @param saleDate
	 * @return List<MPOSOrderTransaction>
	 */
	public List<MPOSOrderTransaction> listSuccessTransaction(String saleDate) {
		List<MPOSOrderTransaction> transLst = new ArrayList<MPOSOrderTransaction>();
		Cursor cursor = getReadableDatabase()
				.rawQuery(
						" SELECT "
								+ OrderTransactionTable.COLUMN_TRANSACTION_ID
								+ ", " + ComputerTable.COLUMN_COMPUTER_ID
								+ ", " + OrderTransactionTable.COLUMN_PAID_TIME
								+ ", "
								+ OrderTransactionTable.COLUMN_TRANS_NOTE
								+ ", "
								+ OrderTransactionTable.COLUMN_RECEIPT_NO
								+ " FROM "
								+ OrderTransactionTable.TABLE_ORDER_TRANS
								+ " WHERE "
								+ OrderTransactionTable.COLUMN_SALE_DATE + "=?"
								+ " AND "
								+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
						new String[] { saleDate,
								String.valueOf(TRANS_STATUS_SUCCESS) });
		if (cursor.moveToFirst()) {
			do {
				MPOSOrderTransaction trans = new MPOSOrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
				trans.setPaidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	/**
	 * @param saleDate
	 * @return List<MPOSOrderTransaction>
	 */
	public List<MPOSOrderTransaction> listHoldOrder(String saleDate) {
		List<MPOSOrderTransaction> transLst = new ArrayList<MPOSOrderTransaction>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ", " + " a." + ComputerTable.COLUMN_COMPUTER_ID
						+ ", " + " a." + OrderTransactionTable.COLUMN_OPEN_TIME
						+ ", " + " a."
						+ OrderTransactionTable.COLUMN_TRANS_NOTE + ", "
						+ " b." + StaffTable.COLUMN_STAFF_CODE + ", " + " b."
						+ StaffTable.COLUMN_STAFF_NAME + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " a "
						+ " LEFT JOIN " + StaffTable.TABLE_STAFF + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_OPEN_STAFF
						+ "=" + " b." + StaffTable.COLUMN_STAFF_ID
						+ " WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE
						+ "=?" + " AND a."
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
				new String[] { saleDate, String.valueOf(TRANS_STATUS_HOLD) });
		if (cursor.moveToFirst()) {
			do {
				MPOSOrderTransaction trans = new MPOSOrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
				trans.setOpenTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_TIME)));
				trans.setStaffName(cursor.getString(cursor
						.getColumnIndex(StaffTable.COLUMN_STAFF_CODE))
						+ ":"
						+ cursor.getString(cursor
								.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	/**
	 * @return max transactionId
	 */
	public int getMaxTransaction() {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS, null);
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return transactionId + 1;
	}

	/**
	 * @param year
	 * @param month
	 * @return max receiptId
	 */
	public int getMaxReceiptId(int year, int month) {
		int maxReceiptId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderTransactionTable.COLUMN_RECEIPT_ID + ") "
						+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_RECEIPT_YEAR
						+ "=?" + " AND "
						+ OrderTransactionTable.COLUMN_RECEIPT_MONTH + "=?",
				new String[] { String.valueOf(year), String.valueOf(month) });
		if (cursor.moveToFirst()) {
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return maxReceiptId + 1;
	}

	/**
	 * Get current transactionId
	 * 
	 * @param saleDate
	 * @return transactionId
	 */
	public int getCurrTransactionId(String saleDate) {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT " + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=?" + " AND "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=?",
				new String[] { String.valueOf(TRANS_STATUS_NEW), saleDate });
		if (cursor.moveToFirst()) {
			if (cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		return transactionId;
	}

	/**
	 * @param shopId
	 * @param computerId
	 * @param sessionId
	 * @param staffId
	 * @param vatRate
	 * @return transactionId
	 * @throws SQLException
	 */
	public int openTransaction(int shopId, int computerId, int sessionId,
			int staffId, double vatRate) throws SQLException {
		int transactionId = getMaxTransaction();
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(BaseColumn.COLUMN_UUID, getUUID());
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ShopTable.COLUMN_SHOP_ID, shopId);
		cv.put(SessionTable.COLUMN_SESS_ID, sessionId);
		cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, staffId);
		cv.put(DocumentTypeTable.COLUMN_DOC_TYPE, 8);
		cv.put(OrderTransactionTable.COLUMN_OPEN_TIME,
				dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_SALE_DATE, date.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_YEAR,
				date.get(Calendar.YEAR));
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_MONTH,
				date.get(Calendar.MONTH) + 1);
		cv.put(ProductsTable.COLUMN_VAT_RATE, vatRate);
		long rowId = getWritableDatabase().insertOrThrow(
				OrderTransactionTable.TABLE_ORDER_TRANS, null, cv);
		if (rowId == -1)
			transactionId = 0;
		return transactionId;
	}

	/**
	 * @param transactionId
	 * @param staffId
	 * @return row affected
	 */
	public int closeTransaction(int transactionId, int staffId) {
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		int receiptId = getMaxReceiptId(date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1);
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_SUCCESS);
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_ID, receiptId);
		cv.put(OrderTransactionTable.COLUMN_CLOSE_TIME,
				dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_PAID_TIME,
				dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_PAID_STAFF_ID, staffId);
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, staffId);
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_NO,
				formatReceiptNo("", date.get(Calendar.YEAR),
						date.get(Calendar.MONTH) + 1, receiptId));
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int prepareTransaction(int transactionId) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_NEW);
		cv.put(OrderTransactionTable.COLUMN_TRANS_NOTE, "");
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * Cancel transaction
	 * 
	 * @param transactionId
	 */
	public void cancelTransaction(int transactionId) {
		deleteOrderSet(transactionId);
		deleteOrderDetail(transactionId);
		deleteTransaction(transactionId);
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteTransaction(int transactionId) {
		return getWritableDatabase().delete(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @return total transaction that not sent
	 */
	public int countTransNotSend() {
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(" + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=? AND "
						+ BaseColumn.COLUMN_SEND_STATUS + "=?",
				new String[] {
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(MPOSDatabase.NOT_SEND) });
		if (cursor.moveToFirst()) {
			total = cursor.getInt(0);
		}
		cursor.close();
		return total;
	}

	/**
	 * @param saleDate
	 * @return number of hold transaction
	 */
	public int countHoldOrder(String saleDate) {
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT COUNT(" + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?"
						+ " AND " + OrderTransactionTable.COLUMN_SALE_DATE
						+ "=?",
				new String[] { String.valueOf(TRANS_STATUS_HOLD), saleDate });
		if (cursor.moveToFirst()) {
			total = cursor.getInt(0);
		}
		cursor.close();
		return total;
	}

	/**
	 * @param transactionId
	 * @param note
	 * @return row affected
	 */
	public int holdTransaction(int transactionId, String note) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_HOLD);
		cv.put(OrderTransactionTable.COLUMN_TRANS_NOTE, note);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @param staffId
	 * @return row affected
	 */
	public int updateTransaction(int transactionId, int staffId) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, staffId);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int updateTransactionSendStatus(int transactionId) {
		ContentValues cv = new ContentValues();
		cv.put(BaseColumn.COLUMN_SEND_STATUS, MPOSDatabase.ALREADY_SEND);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(TRANS_STATUS_SUCCESS),
						String.valueOf(TRANS_STATUS_VOID) });
	}

	/**
	 * @param saleDate
	 * @return row affected
	 */
	public int updateTransactionSendStatus(String saleDate) {
		ContentValues cv = new ContentValues();
		cv.put(BaseColumn.COLUMN_SEND_STATUS, MPOSDatabase.ALREADY_SEND);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
				OrderTransactionTable.COLUMN_SALE_DATE + "=?" + " AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] { saleDate, String.valueOf(TRANS_STATUS_SUCCESS),
						String.valueOf(TRANS_STATUS_VOID) });
	}

	/**
	 * @param transactionId
	 * @param totalSalePrice
	 * @return row affected
	 */
	protected int updateTransactionVat(int transactionId) {
		MPOSOrderTransaction.MPOSOrderDetail summOrder = getSummaryOrder(transactionId);
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_VAT, summOrder.getVat());
		cv.put(OrderTransactionTable.COLUMN_TRANS_VATABLE,
				summOrder.getTotalSalePrice() + summOrder.getVatExclude());
		cv.put(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT,
				summOrder.getVatExclude());
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return rows affected
	 */
	public int summary(int transactionId) {
		return updateTransactionVat(transactionId);
	}

	/**
	 * @param transactionId
	 * @return can confirm discount ?
	 */
	public boolean confirmDiscount(int transactionId) {
		boolean isSuccess = false;
		deleteOrderDetail(transactionId);
		try {
			getWritableDatabase().execSQL(
					" INSERT INTO " + OrderDetailTable.TABLE_ORDER
							+ " SELECT * FROM "
							+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
							+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "="
							+ transactionId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param vatType
	 * @param vatRate
	 * @param salePrice
	 * @param discount
	 * @param discountType
	 * @return row affected
	 */
	public int discountEatchProduct(int transactionId, int orderDetailId,
			int vatType, double vatRate, double salePrice, double discount,
			int discountType) {
		double vat = Util.calculateVatAmount(salePrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, discount);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, salePrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == ProductsDao.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		else
			cv.put(OrderDetailTable.COLUMN_DISCOUNT_TYPE, discountType);
		return getWritableDatabase().update(
				OrderDetailTable.TABLE_ORDER_TMP,
				cv,
				OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(orderDetailId),
						String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteOrderDetail(int transactionId) {
		return getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return row affected
	 */
	public int deleteOrderDetail(int transactionId, int orderDetailId) {
		return getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });
	}

	/**
	 * @param transactionId
	 * @return can copy order to temp table
	 */
	public boolean prepareDiscount(int transactionId) {
		boolean isSuccess = false;
		cancelDiscount(transactionId);
		try {
			getWritableDatabase().execSQL(
					" INSERT INTO " + OrderDetailTable.TABLE_ORDER_TMP
							+ " SELECT * FROM " + OrderDetailTable.TABLE_ORDER
							+ " WHERE "
							+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "="
							+ transactionId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int cancelDiscount(int transactionId) {
		return getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER_TMP,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 * @return row affected
	 */
	public int updateOrderDetail(int transactionId, int orderDetailId,
			int vatType, double vatRate, double orderQty, double pricePerUnit) {
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Util
				.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductsTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == ProductsDao.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, 0);
		return getWritableDatabase().update(
				OrderDetailTable.TABLE_ORDER,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });
	}

	/**
	 * @param transactionId
	 * @param computerId
	 * @param productId
	 * @param productCode
	 * @param productName
	 * @param productType
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 * @return current orderDetailId
	 */
	public int addOrderDetail(int transactionId, int computerId, int productId,
			String productCode, String productName, 
			int productType, int vatType, double vatRate, 
			double orderQty, double pricePerUnit) {
		int orderDetailId = getMaxOrderDetail(transactionId);
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Util
				.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ProductsTable.COLUMN_PRODUCT_ID, productId);
		cv.put(ProductsTable.COLUMN_PRODUCT_CODE, productCode);
		cv.put(ProductsTable.COLUMN_PRODUCT_NAME, productName);
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductsTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(ProductsTable.COLUMN_VAT_TYPE, vatType);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		cv.put(ProductsTable.COLUMN_PRODUCT_TYPE_ID, productType);
		if (vatType == ProductsDao.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		long rowId = getWritableDatabase().insertOrThrow(
				OrderDetailTable.TABLE_ORDER, null, cv);
		if (rowId == -1)
			orderDetailId = 0;
		return orderDetailId;
	}

	/**
	 * @param transactionId
	 * @return max orderDetailId
	 */
	public int getMaxOrderDetail(int transactionId) {
		int orderDetailId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderDetailTable.COLUMN_ORDER_ID + ") "
						+ " FROM " + OrderDetailTable.TABLE_ORDER + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			orderDetailId = cursor.getInt(0);
		}
		cursor.close();
		return orderDetailId + 1;
	}

	/**
	 * @param transactionId
	 * @param staffId
	 * @param reason
	 * @return row affected
	 */
	public int voidTransaction(int transactionId, int staffId, String reason) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_VOID);
		cv.put(OrderTransactionTable.COLUMN_VOID_STAFF_ID, staffId);
		cv.put(OrderTransactionTable.COLUMN_VOID_REASON, reason);
		cv.put(BaseColumn.COLUMN_SEND_STATUS, MPOSDatabase.NOT_SEND);
		cv.put(OrderTransactionTable.COLUMN_VOID_TIME, Util.getDate()
				.getTimeInMillis());
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? ",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param sessionDate
	 * @return total receipt specific by sale date
	 */
	public int getTotalReceipt(String sessionDate) {
		int totalReceipt = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT (" + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=? "
						+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID
						+ " IN (?,?)",
				new String[] { sessionDate,
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(TransactionDao.TRANS_STATUS_VOID) });
		if (cursor.moveToFirst()) {
			totalReceipt = cursor.getInt(0);
		}
		cursor.close();
		return totalReceipt;
	}

	/**
	 * @param sessionDate
	 * @return total receipt amount
	 */
	public double getTotalReceiptAmount(String sessionDate) {
		double totalReceiptAmount = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT SUM (" + OrderTransactionTable.COLUMN_TRANS_VATABLE
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=? "
						+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=?",
				new String[] { sessionDate,
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS) });
		if (cursor.moveToFirst()) {
			totalReceiptAmount = cursor.getFloat(0);
		}
		cursor.close();
		return totalReceiptAmount;
	}

	/**
	 * Orders Set
	 * 
	 * @param transactionId
	 * @param orderDetailId
	 * @return List<MPOSOrderTransaction.OrderSet>
	 */
	public List<MPOSOrderTransaction.OrderSet> listOrderSet(int transactionId,
			int orderDetailId) {
		List<MPOSOrderTransaction.OrderSet> productSetLst = new ArrayList<MPOSOrderTransaction.OrderSet>();
		Cursor mainCursor = getReadableDatabase().rawQuery(
				" SELECT b." + ProductComponentTable.COLUMN_PGROUP_ID + ", "
						+ " b."
						+ ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ", "
						+ " b."
						+ ProductComponentGroupTable.COLUMN_SET_GROUP_NAME
						+ ", " + " b."
						+ ProductComponentGroupTable.COLUMN_REQ_AMOUNT
						+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " a "
						+ " LEFT JOIN "
						+ ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP
						+ " b " + " ON a."
						+ ProductComponentTable.COLUMN_PGROUP_ID + "=" + " b."
						+ ProductComponentTable.COLUMN_PGROUP_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?"
						+ " GROUP BY b."
						+ ProductComponentTable.COLUMN_PGROUP_ID,
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });

		if (mainCursor.moveToFirst()) {
			do {
				int pcompGroupId = mainCursor
						.getInt(mainCursor
								.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID));
				MPOSOrderTransaction.OrderSet group = new MPOSOrderTransaction.OrderSet();
				group.setTransactionId(transactionId);
				group.setOrderDetailId(orderDetailId);
				group.setProductGroupId(pcompGroupId);
				group.setGroupNo(mainCursor.getInt(mainCursor
						.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
				group.setGroupName(mainCursor.getString(mainCursor
						.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME)));
				group.setRequireAmount(mainCursor.getDouble(mainCursor
						.getColumnIndex(ProductComponentGroupTable.COLUMN_REQ_AMOUNT)));
				// query set detail
				Cursor detailCursor = getReadableDatabase()
						.query(OrderSetTable.TABLE_ORDER_SET,
								new String[] {
										OrderSetTable.COLUMN_ORDER_SET_ID,
										ProductsTable.COLUMN_PRODUCT_ID,
										ProductsTable.COLUMN_PRODUCT_NAME,
										OrderSetTable.COLUMN_ORDER_SET_QTY },
								OrderTransactionTable.COLUMN_TRANSACTION_ID
										+ "=? "
										+ " AND "
										+ OrderDetailTable.COLUMN_ORDER_ID
										+ "=? "
										+ " AND "
										+ ProductComponentTable.COLUMN_PGROUP_ID
										+ "=?",
								new String[] { String.valueOf(transactionId),
										String.valueOf(orderDetailId),
										String.valueOf(pcompGroupId) }, null,
								null, null);

				if (detailCursor.moveToFirst()) {
					do {
						MPOSOrderTransaction.OrderSet.OrderSetDetail detail = new MPOSOrderTransaction.OrderSet.OrderSetDetail();
						detail.setOrderSetId(detailCursor.getInt(detailCursor
								.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_ID)));
						detail.setProductId(detailCursor.getInt(detailCursor
								.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
						detail.setProductName(detailCursor.getString(detailCursor
								.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
						detail.setOrderSetQty(detailCursor.getDouble(detailCursor
								.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_QTY)));
						group.mProductLst.add(detail);
					} while (detailCursor.moveToNext());
				}
				detailCursor.close();

				// productSet to list
				productSetLst.add(group);

			} while (mainCursor.moveToNext());
		}
		mainCursor.close();

		return productSetLst;
	}

	/**
	 * List order set detail
	 * 
	 * @param transactionId
	 * @param orderDetailId
	 * @return List<OrderSet.OrderSetDetail
	 */
	public List<OrderSet.OrderSetDetail> listOrderSetDetail(int transactionId,
			int orderDetailId) {
		List<OrderSet.OrderSetDetail> orderSetDetailLst = new ArrayList<OrderSet.OrderSetDetail>();
		// query set detail
		Cursor detailCursor = getReadableDatabase().query(
				OrderSetTable.TABLE_ORDER_SET,
				new String[] { OrderSetTable.COLUMN_ORDER_SET_ID,
						ProductsTable.COLUMN_PRODUCT_ID,
						ProductsTable.COLUMN_PRODUCT_NAME,
						OrderSetTable.COLUMN_ORDER_SET_QTY },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) }, null, null, null);

		if (detailCursor.moveToFirst()) {
			do {
				MPOSOrderTransaction.OrderSet.OrderSetDetail detail = new MPOSOrderTransaction.OrderSet.OrderSetDetail();
				detail.setOrderSetId(detailCursor.getInt(detailCursor
						.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_ID)));
				detail.setProductId(detailCursor.getInt(detailCursor
						.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
				detail.setProductName(detailCursor.getString(detailCursor
						.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
				detail.setOrderSetQty(detailCursor.getDouble(detailCursor
						.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_QTY)));
				orderSetDetailLst.add(detail);
			} while (detailCursor.moveToNext());
		}
		detailCursor.close();
		return orderSetDetailLst;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param pcompGroupId
	 * @return total qty of group
	 */
	public double getOrderSetTotalQty(int transactionId, int orderDetailId,
			int pcompGroupId) {
		double totalQty = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + OrderSetTable.COLUMN_ORDER_SET_QTY + ") "
						+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
						+ " AND " + ProductComponentTable.COLUMN_PGROUP_ID
						+ "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId),
						String.valueOf(pcompGroupId) });

		if (cursor.moveToFirst()) {
			totalQty = cursor.getDouble(0);
		}
		cursor.close();
		return totalQty;
	}

	/**
	 * @param transactionId
	 */
	public void deleteOrderSet(int transactionId) {
		getWritableDatabase().delete(OrderSetTable.TABLE_ORDER_SET,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? ",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId) {
		getWritableDatabase().delete(
				OrderSetTable.TABLE_ORDER_SET,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderSetId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId,
			int orderSetId) {
		getWritableDatabase().delete(
				OrderSetTable.TABLE_ORDER_SET,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ OrderSetTable.COLUMN_ORDER_SET_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId),
						String.valueOf(orderSetId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderSetId
	 * @param productId
	 * @param orderSetQty
	 */
	public void updateOrderSet(int transactionId, int orderDetailId,
			int orderSetId, int productId, double orderSetQty) {
		ContentValues cv = new ContentValues();
		cv.put(OrderSetTable.COLUMN_ORDER_SET_QTY, orderSetQty);
		getWritableDatabase().update(
				OrderSetTable.TABLE_ORDER_SET,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ OrderSetTable.COLUMN_ORDER_SET_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId),
						String.valueOf(orderSetId) });
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param productId
	 * @param productName
	 * @param pcompGroupId
	 * @param reqAmount
	 */
	public void addOrderSet(int transactionId, int orderDetailId,
			int productId, String productName, int pcompGroupId,
			double reqAmount) {
		int maxOrderSetId = getMaxOrderSetId(transactionId, orderDetailId);
		ContentValues cv = new ContentValues();
		cv.put(OrderSetTable.COLUMN_ORDER_SET_ID, maxOrderSetId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(ProductsTable.COLUMN_PRODUCT_ID, productId);
		cv.put(ProductsTable.COLUMN_PRODUCT_NAME, productName);
		cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pcompGroupId);
		cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, reqAmount);
		cv.put(OrderSetTable.COLUMN_ORDER_SET_QTY, 1);
		getWritableDatabase().insertOrThrow(OrderSetTable.TABLE_ORDER_SET,
				ProductsTable.COLUMN_PRODUCT_NAME, cv);
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return max orderSetId 0 if no row
	 */
	public int getMaxOrderSetId(int transactionId, int orderDetailId) {
		int maxOrderSetId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT MAX (" + OrderSetTable.COLUMN_ORDER_SET_ID + ")"
						+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ " =? AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });
		if (cursor.moveToFirst()) {
			maxOrderSetId = cursor.getInt(0);
		}
		cursor.close();
		return maxOrderSetId + 1;
	}

	public static class OrderTransactionTable {
		public static final String TABLE_ORDER_TRANS = "OrderTransaction";
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

		private static final String SQL_CREATE = "CREATE TABLE "
				+ TABLE_ORDER_TRANS + " ( " + BaseColumn.COLUMN_UUID
				+ " TEXT, " + COLUMN_TRANSACTION_ID + " INTEGER, "
				+ ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, "
				+ ShopTable.COLUMN_SHOP_ID + " INTEGER, " + COLUMN_OPEN_TIME
				+ " TEXT, " + COLUMN_OPEN_STAFF + " INTEGER, "
				+ COLUMN_PAID_TIME + " TEXT, " + COLUMN_PAID_STAFF_ID
				+ " INTEGER, " + COLUMN_CLOSE_TIME + " TEXT, "
				+ COLUMN_CLOSE_STAFF + " INTEGER, " + COLUMN_STATUS_ID
				+ " INTEGER DEFAULT 1, " + DocumentTypeTable.COLUMN_DOC_TYPE
				+ " INTEGER DEFAULT 8, " + COLUMN_RECEIPT_YEAR + " INTEGER, "
				+ COLUMN_RECEIPT_MONTH + " INTEGER, " + COLUMN_RECEIPT_ID
				+ " INTEGER, " + COLUMN_RECEIPT_NO + " TEXT, "
				+ COLUMN_SALE_DATE + " TEXT, " + SessionTable.COLUMN_SESS_ID
				+ " INTEGER, " + COLUMN_VOID_STAFF_ID + " INTEGER, "
				+ COLUMN_VOID_REASON + " TEXT, " + COLUMN_VOID_TIME + " TEXT, "
				+ COLUMN_MEMBER_ID + " INTEGER, " + COLUMN_TRANS_VAT
				+ " REAL DEFAULT 0, " + COLUMN_TRANS_EXCLUDE_VAT
				+ " REAL DEFAULT 0, " + COLUMN_TRANS_VATABLE
				+ " REAL DEFAULT 0, " + COLUMN_TRANS_NOTE + " TEXT, "
				+ COLUMN_OTHER_DISCOUNT + " REAL DEFAULT 0, "
				+ BaseColumn.COLUMN_SEND_STATUS + " INTEGER DEFAULT 0, "
				+ ProductsTable.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, "
				+ ProductsTable.COLUMN_VAT_RATE + " REAL DEFAULT 0, "
				+ "PRIMARY KEY (" + COLUMN_TRANSACTION_ID + ") ); ";

		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

		}
	}

	public static class OrderDetailTable {

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

		private static final String ORDER_SQL_CREATE = "CREATE TABLE "
				+ TABLE_ORDER + " ( " + COLUMN_ORDER_ID + " INTEGER, "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER, "
				+ ComputerDao.ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, "
				+ ProductsTable.COLUMN_PRODUCT_ID + " INTEGER, "
				+ ProductsTable.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, "
				+ COLUMN_ORDER_QTY + " REAL DEFAULT 1, "
				+ ProductsTable.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, "
				+ COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, "
				+ ProductsTable.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, "
				+ ProductsTable.COLUMN_PRODUCT_CODE + " TEXT, "
				+ ProductsTable.COLUMN_PRODUCT_NAME + " TEXT, "
				+ COLUMN_TOTAL_VAT + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, "
				+ COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, "
				+ COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, "
				+ ProductsTable.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, "
				+ "PRIMARY KEY (" + COLUMN_ORDER_ID + ", "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ") );";

		private static final String ORDER_TMP_SQL_CREATE = "CREATE TABLE "
				+ TABLE_ORDER_TMP + " ( " + COLUMN_ORDER_ID + " INTEGER, "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER, "
				+ ComputerDao.ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, "
				+ ProductsTable.COLUMN_PRODUCT_ID + " INTEGER, "
				+ ProductsTable.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, "
				+ COLUMN_ORDER_QTY + " REAL DEFAULT 1, "
				+ ProductsTable.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, "
				+ COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, "
				+ ProductsTable.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, "
				+ ProductsTable.COLUMN_PRODUCT_CODE + " TEXT, "
				+ ProductsTable.COLUMN_PRODUCT_NAME + " TEXT, "
				+ COLUMN_TOTAL_VAT + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, "
				+ COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, "
				+ COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, "
				+ COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, "
				+ ProductsTable.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, "
				+ "PRIMARY KEY (" + COLUMN_ORDER_ID + ", "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ") );";

		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(ORDER_SQL_CREATE);
			db.execSQL(ORDER_TMP_SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

		}
	}

	public static class OrderSetTable {

		public static final String TABLE_ORDER_SET = "OrderSet";
		public static final String COLUMN_ORDER_SET_ID = "order_set_id";
		public static final String COLUMN_ORDER_SET_QTY = "order_set_qty";

		private static final String SQL_CREATE = "CREATE TABLE "
				+ TABLE_ORDER_SET + "( " + COLUMN_ORDER_SET_ID
				+ " INTEGER NOT NULL," + OrderDetailTable.COLUMN_ORDER_ID
				+ " INTEGER NOT NULL, "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID
				+ " INTEGER NOT NULL, "
				+ ProductComponentTable.COLUMN_PGROUP_ID
				+ " INTEGER NOT NULL, " + ProductsTable.COLUMN_PRODUCT_ID
				+ " INTEGER NOT NULL, " + ProductsTable.COLUMN_PRODUCT_NAME
				+ " TEXT, " + ProductComponentGroupTable.COLUMN_REQ_AMOUNT
				+ " REAL NOT NULL DEFAULT 0, " + COLUMN_ORDER_SET_QTY
				+ " REAL NOT NULL DEFAULT 0, " + "PRIMARY KEY ("
				+ COLUMN_ORDER_SET_ID + ", " + OrderDetailTable.COLUMN_ORDER_ID
				+ ", " + OrderTransactionTable.COLUMN_TRANSACTION_ID + ")"
				+ ");";

		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

		}
	}
}
