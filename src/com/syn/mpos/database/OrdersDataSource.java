package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.syn.mpos.database.StockDocument.DocumentTypeTable;
import com.syn.mpos.database.table.BaseColumn;
import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.OrderDetailTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.ProductsTable;
import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.ShopTable;

/**
 * 
 * @author j1tth4
 * 
 */
public class OrdersDataSource extends MPOSDatabase {

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

	private OrderSetDataSource mOrderSet;
	
	public OrdersDataSource(Context context) {
		super(context);
		mOrderSet = new OrderSetDataSource(context);
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
				new String[] { String.valueOf(transactionId)}, null, null,
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
//	public MPOSOrderTransaction getTransaction(long saleDate) {
//		MPOSOrderTransaction trans = new MPOSOrderTransaction();
//		Cursor cursor = getReadableDatabase().query(
//				OrderTransactionTable.TABLE_ORDER_TRANS,
//				new String[] { OrderTransactionTable.COLUMN_TRANSACTION_ID,
//						ComputerTable.COLUMN_COMPUTER_ID,
//						OrderTransactionTable.COLUMN_TRANS_VATABLE,
//						OrderTransactionTable.COLUMN_TRANS_VAT,
//						OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT,
//						OrderTransactionTable.COLUMN_PAID_TIME,
//						OrderTransactionTable.COLUMN_RECEIPT_NO,
//						OrderTransactionTable.COLUMN_OPEN_STAFF },
//				OrderTransactionTable.COLUMN_SALE_DATE + "=? AND "
//						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
//				new String[] { String.valueOf(saleDate),
//						String.valueOf(TRANS_STATUS_SUCCESS), }, null, null,
//				OrderTransactionTable.COLUMN_SALE_DATE);
//
//		if (cursor != null) {
//			if (cursor.moveToFirst()) {
//				trans.setTransactionId(cursor.getInt(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
//				trans.setTransactionVatable(cursor.getDouble(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)));
//				trans.setTransactionVat(cursor.getDouble(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT)));
//				trans.setTransactionVatExclude(cursor.getDouble(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT)));
//				trans.setComputerId(cursor.getInt(cursor
//						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
//				trans.setPaidTime(cursor.getString(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
//				trans.setReceiptNo(cursor.getString(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
//				trans.setOpenStaffId(cursor.getInt(cursor
//						.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
//			}
//			cursor.close();
//		}
//		return trans;
//	}

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
						+ ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT
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
						+ ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT
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
				" SELECT "
						+ " a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_ID + ", " + " a."
						+ OrderDetailTable.COLUMN_ORDER_QTY + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " a." + ProductsTable.COLUMN_VAT_TYPE + ", "
						+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE
						+ ", " + " b." + ProductsTable.COLUMN_PRODUCT_NAME
						+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
						+ " LEFT JOIN " + ProductsTable.TABLE_PRODUCTS + " b "
						+ " ON a." + ProductsTable.COLUMN_PRODUCT_ID + "="
						+ " b." + ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
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
				" SELECT "
						+ " a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_ID + ", " + " a."
						+ OrderDetailTable.COLUMN_ORDER_QTY + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " a." + ProductsTable.COLUMN_VAT_TYPE + ", "
						+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE
						+ ", " + " b." + ProductsTable.COLUMN_PRODUCT_NAME
						+ " FROM " + OrderDetailTable.TABLE_ORDER_TMP + " a "
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
				" SELECT "
						+ " a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_ID + ", " + " SUM(a."
						+ OrderDetailTable.COLUMN_ORDER_QTY + ") AS "
						+ OrderDetailTable.COLUMN_ORDER_QTY + ", " + " SUM(a."
						+ ProductsTable.COLUMN_PRODUCT_PRICE + ") AS "
						+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " SUM(a."
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
	public List<MPOSOrderTransaction.MPOSOrderDetail> listAllOrder(int transactionId) {
		List<MPOSOrderTransaction.MPOSOrderDetail> orderDetailLst = 
				new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT "
						+ " a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_ID + ", " + " a."
						+ OrderDetailTable.COLUMN_ORDER_QTY + ", " + " a."
						+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " a."
						+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
						+ ", " + " a." + ProductsTable.COLUMN_VAT_TYPE + ", "
						+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT
						+ ", " + " a." + OrderDetailTable.COLUMN_DISCOUNT_TYPE
						+ ", " + " b." + ProductsTable.COLUMN_PRODUCT_NAME
						+ " FROM " + OrderDetailTable.TABLE_ORDER + " a"
						+ " LEFT JOIN " + ProductsTable.TABLE_PRODUCTS + " b"
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

	private MPOSOrderTransaction.MPOSOrderDetail toOrderDetail(Cursor cursor) {
		MPOSOrderTransaction.MPOSOrderDetail orderDetail = 
				new MPOSOrderTransaction.MPOSOrderDetail();
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
		List<MPOSOrderTransaction.OrderSet.OrderSetDetail> orderSetDetailLst = 
				mOrderSet.listOrderSetDetail(orderDetail.getTransactionId(), 
						orderDetail.getOrderDetailId());
		if(orderSetDetailLst.size() > 0){
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
				" SELECT a." + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ", " + " a." + ComputerTable.COLUMN_COMPUTER_ID
						+ ", " + " a." + OrderTransactionTable.COLUMN_PAID_TIME
						+ ", " + " a."
						+ OrderTransactionTable.COLUMN_TRANS_NOTE + ", "
						+ " a." + OrderTransactionTable.COLUMN_RECEIPT_YEAR
						+ ", " + " a."
						+ OrderTransactionTable.COLUMN_RECEIPT_MONTH + ", "
						+ " a." + OrderTransactionTable.COLUMN_RECEIPT_ID
						+ ", " + " a."
						+ OrderTransactionTable.COLUMN_RECEIPT_NO + ", "
						+ " b." + StaffTable.COLUMN_STAFF_CODE + ", " + " b."
						+ StaffTable.COLUMN_STAFF_NAME + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " a "
						+ " LEFT JOIN " + StaffTable.TABLE_NAME + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_OPEN_STAFF
						+ "=b." + StaffTable.COLUMN_STAFF_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=?"
						+ " AND a." + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=?",
				new String[] {
						saleDate,
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
				trans.setStaffName(cursor.getString(cursor
						.getColumnIndex(StaffTable.COLUMN_STAFF_CODE))
						+ ":"
						+ cursor.getString(cursor
								.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
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
						+ " LEFT JOIN " + StaffTable.TABLE_NAME + " b "
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
						+ ") " + " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS,
				null);
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
		return getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS,
				cv, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
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
		return getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS,
				cv, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * Cancel transaction
	 * 
	 * @param transactionId
	 */
	public void cancelTransaction(int transactionId) {
		deleteOrderDetail(transactionId);
		deleteTransaction(transactionId);
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteTransaction(int transactionId) {
		return getWritableDatabase().delete(OrderTransactionTable.TABLE_ORDER_TRANS,
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
						+ ") " + " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=? AND " + BaseColumn.COLUMN_SEND_STATUS + "=?",
				new String[] {
						String.valueOf(OrdersDataSource.TRANS_STATUS_SUCCESS),
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
						+ ") " + " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=?" + " AND "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=?",
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
		return getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS,
				cv, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
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
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
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
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return rows affected
	 */
	public int summary(int transactionId){
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
	 * @param orderDetailId
	 * @param transactionId
	 * @param vatType
	 * @param vatRate
	 * @param salePrice
	 * @param discount
	 * @param discountType
	 * @return row affected
	 */
	public int discountEatchProduct(int orderDetailId, int transactionId,
			int vatType, double vatRate, double salePrice, double discount,
			int discountType) {
		double vat = Util.calculateVatAmount(salePrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, discount);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, salePrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == ProductsDataSource.VAT_TYPE_EXCLUDE)
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
		if (vatType == ProductsDataSource.VAT_TYPE_EXCLUDE)
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
	 * @param productType
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 * @return row affected
	 */
	public int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, double vatRate, double orderQty,
			double pricePerUnit) {
		int orderDetailId = getMaxOrderDetail(transactionId);
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Util
				.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ProductsTable.COLUMN_PRODUCT_ID, productId);
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductsTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(ProductsTable.COLUMN_VAT_TYPE, vatType);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		cv.put(ProductsTable.COLUMN_PRODUCT_TYPE_ID, productType);
		if (vatType == ProductsDataSource.VAT_TYPE_EXCLUDE)
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
		return getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS,
				cv, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? ",
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
						+ ") " + " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE
						+ "=? " + " AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN (?,?)",
				new String[] { sessionDate,
						String.valueOf(OrdersDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(OrdersDataSource.TRANS_STATUS_VOID) });
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
		Cursor cursor = getReadableDatabase()
				.rawQuery(
						"SELECT SUM ("
								+ OrderTransactionTable.COLUMN_TRANS_VATABLE
								+ ") " + " FROM "
								+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
								+ OrderTransactionTable.COLUMN_SALE_DATE
								+ "=? " + " AND "
								+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
						new String[] {
								sessionDate,
								String.valueOf(OrdersDataSource.TRANS_STATUS_SUCCESS) });
		if (cursor.moveToFirst()) {
			totalReceiptAmount = cursor.getFloat(0);
		}
		cursor.close();
		return totalReceiptAmount;
	}
}
