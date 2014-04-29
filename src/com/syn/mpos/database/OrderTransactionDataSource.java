package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.mpos.database.StockDocument.DocumentTypeTable;
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
public class OrderTransactionDataSource extends MPOSDatabase {

	/*
	 * Status new transaction
	 */
	public static final int TRANS_STATUS_NEW = 1;
	
	/*
	 * Status success transaction
	 */
	public static final int TRANS_STATUS_SUCCESS = 2;
	
	/*
	 * Status void transaction
	 */
	public static final int TRANS_STATUS_VOID = 8;
	
	/*
	 * Status hold transaction
	 */
	public static final int TRANS_STATUS_HOLD = 9;

	public OrderTransactionDataSource(Context context) {
		super(context);
	}

	/**
	 * @param transactionId
	 * @param computerId
	 * @return MPOSOrderTransaction
	 */
	protected MPOSOrderTransaction getTransaction(int transactionId) {
		MPOSOrderTransaction trans = new MPOSOrderTransaction();
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_NAME,
				new String[] { OrderTransactionTable.COLUMN_TRANSACTION_ID,
						ComputerTable.COLUMN_COMPUTER_ID,
						OrderTransactionTable.COLUMN_STATUS_ID,
						OrderTransactionTable.COLUMN_PAID_TIME,
						OrderTransactionTable.COLUMN_RECEIPT_NO },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(TRANS_STATUS_SUCCESS) }, null, null,
				OrderTransactionTable.COLUMN_SALE_DATE);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionStatusId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
				trans.setPaidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
			}
			cursor.close();
		}
		return trans;
	}

	/**
	 * @param saleDate
	 * @return MPOSOrderTransaction
	 */
	protected MPOSOrderTransaction getTransaction(long saleDate) {
		MPOSOrderTransaction trans = new MPOSOrderTransaction();
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_NAME,
				new String[] { OrderTransactionTable.COLUMN_TRANSACTION_ID,
						ComputerTable.COLUMN_COMPUTER_ID,
						OrderTransactionTable.COLUMN_PAID_TIME,
						OrderTransactionTable.COLUMN_RECEIPT_NO },
				OrderTransactionTable.COLUMN_SALE_DATE + "=? AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
				new String[] { String.valueOf(saleDate),
						String.valueOf(TRANS_STATUS_SUCCESS), }, null, null,
				OrderTransactionTable.COLUMN_SALE_DATE);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setPaidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
			}
			cursor.close();
		}
		return trans;
	}

	/**
	 * @param transactionId
	 * @return 
	 */
//	protected double getOtherDiscount(int transactionId) {
//		double otherDiscount = 0.0f;
//		Cursor cursor = getReadableDatabase().query(
//				OrderTransactionTable.TABLE_NAME,
//				new String[] { OrderTransactionTable.COLUMN_OTHER_DISCOUNT },
//				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
//				new String[] { String.valueOf(transactionId)}, null, null, null);
//		if (cursor.moveToFirst()) {
//			otherDiscount = cursor.getDouble(0);
//		}
//		cursor.close();
//		return otherDiscount;
//	}

	/**
	 * @param transactionId
	 * @return receipt number
	 */
	protected String getReceiptNo(int transactionId) {
		String receiptNo = "";
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_NAME,
				new String[] { OrderTransactionTable.COLUMN_RECEIPT_NO },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)}, null, null, null);
		if (cursor.moveToFirst()) {
			receiptNo = cursor.getString(0);
		}
		cursor.close();
		return receiptNo;
	}

	/**
	 * @param transactionId
	 * @return transaction vat excluded
	 */
	protected double getTransactionVatExclude(int transactionId) {
		double transVatExclude = 0.0f;
		Cursor cursor = getReadableDatabase()
				.query(OrderTransactionTable.TABLE_NAME,
						new String[] { OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT },
						OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
						new String[] { String.valueOf(transactionId)}, null, null, null);
		if (cursor.moveToFirst()) {
			transVatExclude = cursor.getDouble(0);
		}
		cursor.close();
		return transVatExclude;
	}

	/**
	 * @param transactionId
	 * @return transaction vatable
	 */
	protected double getTransactionVatable(int transactionId) {
		double transVatable = 0.0f;
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_NAME,
				new String[] { OrderTransactionTable.COLUMN_TRANS_VATABLE },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)}, null, null, null);
		if (cursor.moveToFirst()) {
			transVatable = cursor.getDouble(0);
		}
		cursor.close();
		return transVatable;
	}

	/**
	 * @param transactionId
	 * @return transaction vat
	 */
	protected double getTransactionVat(int transactionId) {
		double transVat = 0.0f;
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_NAME,
				new String[] { OrderTransactionTable.COLUMN_TRANS_VAT },
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId) }, null, null, null);
		if (cursor.moveToFirst()) {
			transVat = cursor.getDouble(0);
		}
		cursor.close();
		return transVat;
	}

	/**
	 * @param transactionId
	 * @return total temp discount
	 */
	protected double getTmpPriceDiscount(int transactionId) {
		double priceDiscount = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			priceDiscount = cursor.getDouble(0);
		}
		cursor.close();
		return priceDiscount;
	}

	/**
	 * @param transactionId
	 * @return total discount
	 */
	protected double getPriceDiscount(int transactionId) {
		double priceDiscount = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			priceDiscount = cursor.getDouble(0);
		}
		cursor.close();
		return priceDiscount;
	}

	/**
	 * @param transactionId
	 * @return total temp vat exclude
	 */
	protected double getTmpTotalVatExclude(int transactionId) {
		double totalVat = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalVat = cursor.getDouble(0);
		}
		cursor.close();
		return totalVat;
	}

	/**
	 * @param transactionId
	 * @return total vat exclude
	 */
	protected double getTotalVatExclude(int transactionId) {
		double totalVat = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalVat = cursor.getDouble(0);
		}
		cursor.close();
		return totalVat;
	}

	/**
	 * @param transactionId
	 * @return total temp vat
	 */
	protected double getTmpTotalVat(int transactionId) {
		double totalVat = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_VAT + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalVat = cursor.getDouble(0);
		}
		cursor.close();
		return totalVat;
	}

	/**
	 * @param transactionId
	 * @return total vat
	 */
	protected double getTotalVat(int transactionId) {
		double totalVat = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_VAT + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalVat = cursor.getDouble(0);
		}
		cursor.close();
		return totalVat;
	}

	/**
	 * @param transactionId
	 * @return temp total sale price
	 */
	protected double getTmpTotalSalePrice(int transactionId) {
		double totalSalePrice = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalSalePrice = cursor.getDouble(0);
		}
		cursor.close();
		return totalSalePrice;
	}

	/**
	 * @param transactionId
	 * @return total sale price
	 */
	protected double getTotalSalePrice(int transactionId) {
		double totalSalePrice = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalSalePrice = cursor.getDouble(0);
		}
		cursor.close();
		return totalSalePrice;
	}

	/**
	 * @param transactionId
	 * @return temp total retail price
	 */
	protected double getTmpTotalRetailPrice(int transactionId) {
		double totalRetailPrice = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
				new String[] {String.valueOf(transactionId)});
		if (cursor.moveToFirst()) {
			totalRetailPrice = cursor.getDouble(0);
		}
		cursor.close();
		return totalRetailPrice;
	}

	public double getTotalRetailPrice(int transactionId, int computerId) {
		double totalRetailPrice = 0.0f;
		Cursor cursor = mSqlite.rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ")" + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
				+ ComputerTable.COLUMN_COMPUTER_ID + "=? ", new String[] {
				String.valueOf(transactionId), String.valueOf(computerId) });
		if (cursor.moveToFirst()) {
			totalRetailPrice = cursor.getFloat(0);
		}
		cursor.close();
		return totalRetailPrice;
	}

	public int getTotalQty(int transactionId, int computerId) {
		int totalQty = 0;
		Cursor cursor = mSqlite.rawQuery(" SELECT SUM("
				+ OrderDetailTable.COLUMN_ORDER_QTY + ") " + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
				+ ComputerTable.COLUMN_COMPUTER_ID + "=?", new String[] {
				String.valueOf(transactionId), String.valueOf(computerId) });
		if (cursor.moveToFirst()) {
			totalQty = cursor.getInt(0);
		}
		cursor.close();
		return totalQty;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return MPOSOrderTransaction.OrderDetail
	 */
	protected MPOSOrderTransaction.OrderDetail getOrder(int transactionId, int orderDetailId) {
		MPOSOrderTransaction.OrderDetail orderDetail = 
				new MPOSOrderTransaction.OrderDetail();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
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
						+ " LEFT JOIN " + ProductsTable.TABLE_NAME + " b "
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
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	protected List<MPOSOrderTransaction.OrderDetail> listAllOrderTmp(int transactionId) {
		List<MPOSOrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<MPOSOrderTransaction.OrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
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
						+ " LEFT JOIN " + ProductsTable.TABLE_NAME + " b "
						+ " ON a." + ProductsTable.COLUMN_PRODUCT_ID + "="
						+ " b." + ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)});
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
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	protected List<MPOSOrderTransaction.OrderDetail> listAllOrderGroupByProduct(int transactionId){
		List<MPOSOrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<MPOSOrderTransaction.OrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT a."
				+ OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
				+ ProductsTable.COLUMN_PRODUCT_ID + ", " + " SUM(a."
				+ OrderDetailTable.COLUMN_ORDER_QTY + ") AS "
				+ OrderDetailTable.COLUMN_ORDER_QTY + ", " + " SUM(a."
				+ ProductsTable.COLUMN_PRODUCT_PRICE + ") AS "
				+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " SUM(a."
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS "
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", " + " SUM(a."
				+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS "
				+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", " + " a."
				+ ProductsTable.COLUMN_VAT_TYPE + ", " + " SUM(a."
				+ OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS "
				+ OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", " + " SUM(a."
				+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS "
				+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " + " a."
				+ OrderDetailTable.COLUMN_DISCOUNT_TYPE + ", " + " b."
				+ ProductsTable.COLUMN_PRODUCT_NAME + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " a" + " LEFT JOIN "
				+ ProductsTable.TABLE_NAME + " b" + " ON a."
				+ ProductsTable.COLUMN_PRODUCT_ID + "=" + " b."
				+ ProductsTable.COLUMN_PRODUCT_ID + " WHERE a."
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
				+ " GROUP BY a." + ProductsTable.COLUMN_PRODUCT_ID + ", a."
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, new String[] {
				String.valueOf(transactionId)});
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
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	protected List<MPOSOrderTransaction.OrderDetail> listAllOrder(int transactionId) {
		List<MPOSOrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<MPOSOrderTransaction.OrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderDetailTable.COLUMN_ORDER_ID + ", " + " a."
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
						+ " LEFT JOIN " + ProductsTable.TABLE_NAME + " b"
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

	private MPOSOrderTransaction.OrderDetail toOrderDetail(Cursor cursor) {
		MPOSOrderTransaction.OrderDetail orderDetail = 
				new MPOSOrderTransaction.OrderDetail();
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
	protected List<MPOSOrderTransaction> listTransaction(long saleDate) {
		List<MPOSOrderTransaction> transLst = 
				new ArrayList<MPOSOrderTransaction>();
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
						+ OrderTransactionTable.TABLE_NAME + " a "
						+ " LEFT JOIN " + StaffTable.TABLE_NAME + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_OPEN_STAFF
						+ "=b." + StaffTable.COLUMN_STAFF_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=?"
						+ " AND a." + OrderTransactionTable.COLUMN_STATUS_ID
						+ "=?",
				new String[] { String.valueOf(saleDate),
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
	protected List<MPOSOrderTransaction> listHoldOrder(long saleDate) {
		List<MPOSOrderTransaction> transLst = 
				new ArrayList<MPOSOrderTransaction>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderTransactionTable.COLUMN_TRANSACTION_ID
						+ ", " + " a." + ComputerTable.COLUMN_COMPUTER_ID
						+ ", " + " a." + OrderTransactionTable.COLUMN_OPEN_TIME
						+ ", " + " a."
						+ OrderTransactionTable.COLUMN_TRANS_NOTE + ", "
						+ " b." + StaffTable.COLUMN_STAFF_CODE + ", " + " b."
						+ StaffTable.COLUMN_STAFF_NAME + " FROM "
						+ OrderTransactionTable.TABLE_NAME + " a "
						+ " LEFT JOIN " + StaffTable.TABLE_NAME + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_OPEN_STAFF
						+ "=" + " b." + StaffTable.COLUMN_STAFF_ID
						+ " WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE
						+ "=?" + " AND a."
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
				new String[] {String.valueOf(saleDate),
						String.valueOf(TRANS_STATUS_HOLD) });
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
	protected int getMaxTransaction() {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT MAX("
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ") " + " FROM "
				+ OrderTransactionTable.TABLE_NAME, null);
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
	protected int getMaxReceiptId(int year, int month) {
		int maxReceiptId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT MAX("
				+ OrderTransactionTable.COLUMN_RECEIPT_ID + ") " + " FROM "
				+ OrderTransactionTable.TABLE_NAME + " WHERE "
				+ OrderTransactionTable.COLUMN_RECEIPT_YEAR + "=?" + " AND "
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
	 * @param saleDate
	 * @return current transactionId by saleDate
	 */
	protected int getCurrTransaction(long saleDate) {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + " FROM "
				+ OrderTransactionTable.TABLE_NAME + " WHERE "
				+ OrderTransactionTable.COLUMN_STATUS_ID + "=?" + " AND "
				+ OrderTransactionTable.COLUMN_SALE_DATE + "=?", new String[] {
				String.valueOf(TRANS_STATUS_NEW),
				String.valueOf(saleDate) });
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
	protected int openTransaction(int shopId, int computerId, int sessionId,
			int staffId, double vatRate) throws SQLException {
		int transactionId = getMaxTransaction();
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_UUID, getUUID());
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
		long rowId = getWritableDatabase().insertOrThrow(OrderTransactionTable.TABLE_NAME,
				null, cv);
		if (rowId == -1)
			transactionId = 0;
		return transactionId;
	}

	/**
	 * @param transactionId
	 * @param staffId
	 * @return row affected
	 */
	protected int successTransaction(int transactionId, int staffId) {
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
				OrderTransactionTable.TABLE_NAME,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)});
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	protected int prepareTransaction(int transactionId) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_NEW);
		cv.put(OrderTransactionTable.COLUMN_TRANS_NOTE, "");
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_NAME,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)});
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	protected int deleteTransaction(int transactionId) {
		return getWritableDatabase().delete(
				OrderTransactionTable.TABLE_NAME,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)});
	}

	/**
	 * @param saleDate
	 * @return number of hold transaction
	 */
	protected int countHoldOrder(long saleDate) {
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(" SELECT COUNT("
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + ") " + " FROM "
				+ OrderTransactionTable.TABLE_NAME + " WHERE "
				+ OrderTransactionTable.COLUMN_STATUS_ID + "=?" + " AND "
				+ OrderTransactionTable.COLUMN_SALE_DATE + "=?", new String[] {
				String.valueOf(TRANS_STATUS_HOLD),
				String.valueOf(saleDate) });
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
	protected int holdTransaction(int transactionId, String note) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_HOLD);
		cv.put(OrderTransactionTable.COLUMN_TRANS_NOTE, note);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_NAME,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transactionId)});
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	protected int updateTransactionSendStatus(int transactionId) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEND_STATUS, SyncSaleLogDataSource.SYNC_SUCCESS);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_NAME,
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
	protected int updateTransactionSendStatus(long saleDate) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEND_STATUS, SyncSaleLogDataSource.SYNC_SUCCESS);
		return getWritableDatabase().update(OrderTransactionTable.TABLE_NAME, cv,
				OrderTransactionTable.COLUMN_SALE_DATE + "=?" + " AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] { String.valueOf(saleDate), String.valueOf(TRANS_STATUS_SUCCESS),
						String.valueOf(TRANS_STATUS_VOID) });
	}

	protected int updateTransactionVat(int transactionId, double totalSalePrice) {
		double totalVat = getTotalVat(transactionId, computerId);
		double totalVatExclude = getTotalVatExclude(transactionId, computerId);
		double totalVatable = totalSalePrice + totalVatExclude;
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_VAT, totalVat);
		cv.put(OrderTransactionTable.COLUMN_TRANS_VATABLE, totalVatable);
		cv.put(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT, totalVatExclude);
		return mSqlite.update(
				OrderTransactionTable.TABLE_NAME,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId) });
	}

	public int cancelDiscount(int transactionId, int computerId) {
		return deleteOrderDetailTmp(transactionId, computerId);
	}

	public boolean confirmDiscount(int transactionId, int computerId) {
		boolean isSuccess = false;
		deleteOrderDetail(transactionId, computerId);
		try {
			mSqlite.execSQL(" INSERT INTO " + OrderDetailTable.TABLE_ORDER
					+ " SELECT * FROM " + OrderDetailTable.TABLE_ORDER_TMP
					+ " WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID
					+ "=" + transactionId + " AND "
					+ ComputerTable.COLUMN_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public int discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, int vatType, double vatRate, double salePrice,
			double discount, int discountType) {
		double vat = Util.calculateVatAmount(salePrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, discount);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, salePrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == ProductsDataSource.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		else
			cv.put(OrderDetailTable.COLUMN_DISCOUNT_TYPE, discountType);
		return mSqlite.update(
				OrderDetailTable.TABLE_ORDER_TMP,
				cv,
				OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
						+ " AND " + ComputerTable.COLUMN_COMPUTER_ID + "=?",
				new String[] { String.valueOf(orderDetailId),
						String.valueOf(transactionId),
						String.valueOf(computerId) });
	}

	public int deleteOrderDetail(int transactionId, int computerId) {
		return mSqlite.delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId) });
	}

	public int deleteOrderDetail(int transactionId, int computerId,
			int orderDetailId) {
		return mSqlite.delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=?" + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId),
						String.valueOf(orderDetailId) });
	}

	public boolean copyOrderToTmp(int transactionId, int computerId) {
		boolean isSuccess = false;
		deleteOrderDetailTmp(transactionId, computerId);
		try {
			mSqlite.execSQL(" INSERT INTO " + OrderDetailTable.TABLE_ORDER_TMP
					+ " SELECT * FROM " + OrderDetailTable.TABLE_ORDER
					+ " WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID
					+ "=" + transactionId + " AND "
					+ ComputerTable.COLUMN_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private int deleteOrderDetailTmp(int transactionId, int computerId) {
		return mSqlite.delete(
				OrderDetailTable.TABLE_ORDER_TMP,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId) });
	}

	public int updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, double vatRate, double orderQty,
			double pricePerUnit) {
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
		return mSqlite.update(
				OrderDetailTable.TABLE_ORDER,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=?",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId),
						String.valueOf(computerId) });
	}

	public int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, double vatRate, double orderQty,
			double pricePerUnit) {
		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
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
		long rowId = mSqlite.insertOrThrow(OrderDetailTable.TABLE_ORDER, null,
				cv);
		if (rowId == -1)
			orderDetailId = 0;
		return orderDetailId;
	}

	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;
		Cursor cursor = mSqlite.rawQuery(" SELECT MAX("
				+ OrderDetailTable.COLUMN_ORDER_ID + ") " + " FROM "
				+ OrderDetailTable.TABLE_ORDER + " WHERE "
				+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + " AND "
				+ ComputerTable.COLUMN_COMPUTER_ID + "=?", new String[] {
				String.valueOf(transactionId), String.valueOf(computerId) });
		if (cursor.moveToFirst()) {
			orderDetailId = cursor.getInt(0);
		}
		cursor.close();
		return orderDetailId + 1;
	}

	public int voidTransaction(int transactionId, int computerId, int staffId,
			String reason) {
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_STATUS_ID, TRANS_STATUS_VOID);
		cv.put(OrderTransactionTable.COLUMN_VOID_STAFF_ID, staffId);
		cv.put(OrderTransactionTable.COLUMN_VOID_REASON, reason);
		cv.put(MPOSDatabase.COLUMN_SEND_STATUS, MPOSDatabase.NOT_SEND);
		cv.put(OrderTransactionTable.COLUMN_VOID_TIME, Util.getDate()
				.getTimeInMillis());
		return mSqlite.update(
				OrderTransactionTable.TABLE_NAME,
				cv,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " + " AND "
						+ ComputerTable.COLUMN_COMPUTER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(computerId) });
	}

	public int getTotalReceipt(String sessionDate) {
		int totalReceipt = 0;
		Cursor cursor = mSqlite
				.rawQuery(
						"SELECT COUNT ("
								+ OrderTransactionTable.COLUMN_TRANSACTION_ID
								+ ") " + " FROM "
								+ OrderTransactionTable.TABLE_NAME + " WHERE "
								+ OrderTransactionTable.COLUMN_SALE_DATE
								+ "=? " + " AND "
								+ OrderTransactionTable.COLUMN_STATUS_ID
								+ " IN (?,?)",
						new String[] {
								sessionDate,
								String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
								String.valueOf(OrderTransactionDataSource.TRANS_STATUS_VOID) });
		if (cursor.moveToFirst()) {
			totalReceipt = cursor.getInt(0);
		}
		cursor.close();
		return totalReceipt;
	}

	public double getTotalReceiptAmount(String sessionDate) {
		double totalReceiptAmount = 0.0f;
		Cursor cursor = mSqlite
				.rawQuery(
						"SELECT SUM ("
								+ OrderTransactionTable.COLUMN_TRANS_VATABLE
								+ ") " + " FROM "
								+ OrderTransactionTable.TABLE_NAME + " WHERE "
								+ OrderTransactionTable.COLUMN_SALE_DATE
								+ "=? " + " AND "
								+ OrderTransactionTable.COLUMN_STATUS_ID + "=?",
						new String[] {
								sessionDate,
								String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS) });
		if (cursor.moveToFirst()) {
			totalReceiptAmount = cursor.getFloat(0);
		}
		cursor.close();
		return totalReceiptAmount;
	}
}
