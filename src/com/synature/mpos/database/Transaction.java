package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;

import com.synature.mpos.Utils;
import com.synature.mpos.database.model.Comment;
import com.synature.mpos.database.model.OrderComment;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.database.model.OrderSet;
import com.synature.mpos.database.model.OrderTransaction;
import com.synature.mpos.database.table.BaseColumn;
import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.MenuCommentTable;
import com.synature.mpos.database.table.OrderDetailTable;
import com.synature.mpos.database.table.OrderTransactionTable;
import com.synature.mpos.database.table.ProductComponentGroupTable;
import com.synature.mpos.database.table.ProductComponentTable;
import com.synature.mpos.database.table.ProductTable;
import com.synature.mpos.database.table.PromotionPriceGroupTable;
import com.synature.mpos.database.table.SessionTable;
import com.synature.mpos.database.table.ShopTable;
import com.synature.mpos.database.table.StaffTable;

/**
 * 
 * @author j1tth4
 * 
 */
public class Transaction extends MPOSDatabase {

	/**
	 * New transaction status
	 */
	public static final int TRANS_STATUS_NEW = 1;

	/**
	 * Success transaction status
	 */
	public static final int TRANS_STATUS_SUCCESS = 2;

	/**
	 * void transaction status
	 */
	public static final int TRANS_STATUS_VOID = 8;

	/**
	 * hold transaction status
	 */
	public static final int TRANS_STATUS_HOLD = 9;
	
	/**
	 * All columns
	 */
	public static final String[] ALL_TRANS_COLUMNS = {
		BaseColumn.COLUMN_UUID,
		OrderTransactionTable.COLUMN_TRANS_ID,
		ComputerTable.COLUMN_COMPUTER_ID,
		ShopTable.COLUMN_SHOP_ID,
		OrderTransactionTable.COLUMN_OPEN_STAFF,
		OrderTransactionTable.COLUMN_OPEN_TIME,
		OrderTransactionTable.COLUMN_CLOSE_TIME,
		OrderTransactionTable.COLUMN_DOC_TYPE_ID,
		OrderTransactionTable.COLUMN_STATUS_ID,
		OrderTransactionTable.COLUMN_RECEIPT_YEAR,
		OrderTransactionTable.COLUMN_RECEIPT_MONTH,
		OrderTransactionTable.COLUMN_RECEIPT_ID,
		OrderTransactionTable.COLUMN_RECEIPT_NO,
		OrderTransactionTable.COLUMN_SALE_DATE,
		OrderTransactionTable.COLUMN_TRANS_VAT,
		OrderTransactionTable.COLUMN_TRANS_VATABLE,
		SessionTable.COLUMN_SESS_ID,
		OrderTransactionTable.COLUMN_VOID_STAFF_ID,
		OrderTransactionTable.COLUMN_VOID_REASON,
		OrderTransactionTable.COLUMN_VOID_TIME,
		OrderTransactionTable.COLUMN_TRANS_NOTE,
		ProductTable.COLUMN_SALE_MODE,
		ProductTable.COLUMN_VAT_RATE,
		OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT
	};

	/**
	 * All order columns
	 */
	public static final String[] ALL_ORDER_COLUMNS = {
		OrderDetailTable.COLUMN_ORDER_ID,
		OrderTransactionTable.COLUMN_TRANS_ID,
		ComputerTable.COLUMN_COMPUTER_ID,
		OrderDetailTable.COLUMN_ORDER_QTY,
		OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE,
		OrderDetailTable.COLUMN_TOTAL_SALE_PRICE,
		OrderDetailTable.COLUMN_PRICE_DISCOUNT,
		OrderDetailTable.COLUMN_TOTAL_VAT,
		OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE,
		OrderDetailTable.COLUMN_REMARK,
		OrderDetailTable.COLUMN_PARENT_ORDER_ID,
		ProductTable.COLUMN_PRODUCT_ID,
		ProductTable.COLUMN_PRODUCT_TYPE_ID,
		ProductTable.COLUMN_PRODUCT_PRICE,
		ProductTable.COLUMN_SALE_MODE,
		ProductTable.COLUMN_VAT_TYPE,
		PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID,
		PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID,
		PromotionPriceGroupTable.COLUMN_COUPON_HEADER
	};
	
	public Transaction(Context context) {
		super(context);
	}

	/**
	 * get transactionIds
	 * @param saleDate
	 * @return transId like "1,2,3"
	 */
	public String getSeperateTransactionId(String saleDate){
		String transactionIds = "";
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS, 
				new String[]{
						OrderTransactionTable.COLUMN_TRANS_ID
				}, 
				OrderTransactionTable.COLUMN_SALE_DATE + "=?  AND " 
				+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?)", 
				new String[]{
						saleDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)
				}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				transactionIds += cursor.getString(0);
				if(!cursor.isLast())
					transactionIds += ",";
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transactionIds;
	}
	
	/**
	 * Count order that not confirm
	 * @param saleDate
	 * @return total order that not confirm
	 */
	public int countOrderStatusNotSuccess(String saleDate){
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(b." + OrderDetailTable.COLUMN_ORDER_ID + ")"
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "
				+ " LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b "
				+ " ON a." + OrderTransactionTable.COLUMN_TRANS_ID + "=b." + OrderTransactionTable.COLUMN_TRANS_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_STATUS_ID + " in (?, ?)"
				+ " AND a." + OrderTransactionTable.COLUMN_SALE_DATE + "=?", 
				new String[]{
					String.valueOf(Transaction.TRANS_STATUS_NEW),
					String.valueOf(Transaction.TRANS_STATUS_HOLD),
					saleDate
				});
		if(cursor.moveToFirst()){
			total = cursor.getInt(0);
		}
		cursor.close();
		return total;
	}
	
	/**
	 * @param saleDate
	 * @return OrderTransaction
	 */
	public OrderTransaction getTransaction(String saleDate) {
		OrderTransaction trans = new OrderTransaction();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransactionTable.COLUMN_TRANS_ID + ", "
				+ ComputerTable.COLUMN_COMPUTER_ID + ", "
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_VATABLE + ")"
				+ " AS " + OrderTransactionTable.COLUMN_TRANS_VATABLE + ","
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_VAT + ")"
				+ " AS " + OrderTransactionTable.COLUMN_TRANS_VAT + ","
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ")"
				+ " AS " + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ","
				+ OrderTransactionTable.COLUMN_STATUS_ID + ","
				+ OrderTransactionTable.COLUMN_PAID_TIME + ","
				+ OrderTransactionTable.COLUMN_VOID_TIME + ","
				+ OrderTransactionTable.COLUMN_VOID_STAFF_ID + ","
				+ OrderTransactionTable.COLUMN_VOID_REASON + ","
				+ OrderTransactionTable.COLUMN_RECEIPT_NO + ","
				+ OrderTransactionTable.COLUMN_OPEN_STAFF
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?"
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " in(?,?)",
				new String[] {
						saleDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)
				});
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
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
				trans.setVoidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_TIME)));
				trans.setVoidStaffId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_STAFF_ID)));
				trans.setVoidReason(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_REASON)));
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
	 * @param transactionId
	 * @param computerId
	 * @return OrderTransaction
	 */
	public OrderTransaction getTransaction(int transactionId) {
		OrderTransaction trans = new OrderTransaction();
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				new String[] { 
						OrderTransactionTable.COLUMN_TRANS_ID,
						ComputerTable.COLUMN_COMPUTER_ID,
						OrderTransactionTable.COLUMN_TRANS_VATABLE,
						OrderTransactionTable.COLUMN_TRANS_VAT,
						OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT,
						OrderTransactionTable.COLUMN_STATUS_ID,
						OrderTransactionTable.COLUMN_PAID_TIME,
						OrderTransactionTable.COLUMN_VOID_TIME,
						OrderTransactionTable.COLUMN_VOID_STAFF_ID,
						OrderTransactionTable.COLUMN_VOID_REASON,
						OrderTransactionTable.COLUMN_RECEIPT_NO,
						OrderTransactionTable.COLUMN_OPEN_STAFF,
						PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID
				},
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) }, null, null,
				OrderTransactionTable.COLUMN_SALE_DATE);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
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
				trans.setVoidTime(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_TIME)));
				trans.setVoidStaffId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_STAFF_ID)));
				trans.setVoidReason(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_VOID_REASON)));
				trans.setReceiptNo(cursor.getString(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				trans.setOpenStaffId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
				trans.setPromotionPriceGroupId(cursor.getInt(cursor.getColumnIndex(
						PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID)));
			}
			cursor.close();
		}
		return trans;
	}

	/**
	 * Get summary order for discount
	 * 
	 * @param transactionId
	 * @return
	 */
//	public OrderTransaction.MPOSOrderDetail getSummaryOrderForDiscount(
//			int transactionId) {
//		OrderTransaction.MPOSOrderDetail orderDetail = new OrderTransaction.MPOSOrderDetail();
//		Cursor cursor = getReadableDatabase().rawQuery(
//				"SELECT SUM (" + OrderDetailTable.COLUMN_ORDER_QTY + ") "
//						+ " AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
//						+ " SUM (" + ProductTable.COLUMN_PRODUCT_PRICE + ") "
//						+ " AS " + ProductTable.COLUMN_PRODUCT_PRICE + ", "
//						+ " SUM (" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
//						+ " AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " 
//						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
//						+ " AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
//						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") "
//						+ " AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", " 
//						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT + ") "
//						+ " AS " + OrderDetailTable.COLUMN_TOTAL_VAT + ", "
//						+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") "
//						+ " AS " + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
//						+ " FROM " + OrderDetailTable.TABLE_ORDER_TMP
//						+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + "=?",
//				new String[] { 
//						String.valueOf(transactionId)
//				});
//		if (cursor.moveToFirst()) {
//			double vatExclude = cursor.getDouble(cursor
//					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE));
//			double totalSalePrice = 
//					cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)) + vatExclude;
//			orderDetail.setQty(cursor.getDouble(cursor
//					.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
//			orderDetail.setPricePerUnit(cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
//			orderDetail.setPriceDiscount(cursor.getDouble(cursor
//					.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
//			orderDetail.setTotalRetailPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
//			orderDetail.setTotalSalePrice(totalSalePrice);
//			orderDetail.setVat(cursor.getDouble(cursor
//					.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
//			orderDetail.setVatExclude(vatExclude);
//		}
//		cursor.close();
//		return orderDetail;
//	}

	/**
	 * Get summary of void order in day
	 * @param saleDate
	 * @return OrderDetail
	 */
	public OrderDetail getSummaryVoidOrderInDay(String saleDate) {
		OrderDetail orderDetail = new OrderDetail();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", ");
		sql.append("COUNT(a." + OrderTransactionTable.COLUMN_TRANS_ID + ") AS TotalVoid, ");
		sql.append("( SELECT SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") ");
		sql.append("FROM " + OrderDetailTable.TABLE_ORDER);
		sql.append("WHERE " + OrderTransactionTable.COLUMN_TRANS_ID); 
		sql.append("=a." + OrderTransactionTable.COLUMN_TRANS_ID + ") ");
		sql.append("AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE);
		sql.append("FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a ");
		sql.append("WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + "=?");
		sql.append("AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?");
		Cursor cursor = getReadableDatabase().rawQuery(
				sql.toString(), 
				new String[] {
					saleDate, String.valueOf(Transaction.TRANS_STATUS_VOID)
				}
		);
		if (cursor.moveToFirst()) {
			orderDetail.setOrderQty(cursor.getDouble(cursor.getColumnIndex("TotalVoid")));
			orderDetail.setTotalSalePrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
		}
		cursor.close();
		return orderDetail;
	}
	
	/**
	 * Get summary order by sale date
	 * for print summary sale
	 * @param dateFrom
	 * @return OrderTransaction.MPOSOrderDetail
	 */
	public OrderDetail getSummaryOrderInDay(String dateFrom, String dateTo) {
		OrderDetail orderDetail = new OrderDetail();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT SUM (b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_ORDER_QTY + ", ");
		sql.append(" SUM (b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "); 
		sql.append(" SUM (b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", ");
		sql.append(" SUM (b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "); 
		sql.append(" SUM (b." + OrderDetailTable.COLUMN_TOTAL_VAT + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_TOTAL_VAT + ", ");
		sql.append(" SUM (b." + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") AS "); 
		sql.append(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE);
		sql.append(" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "); 
		sql.append(" LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b ");
		sql.append(" ON a." + OrderTransactionTable.COLUMN_TRANS_ID + "=b." + OrderTransactionTable.COLUMN_TRANS_ID);
		sql.append(" WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?");
		sql.append(" AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?");
		Cursor cursor = getReadableDatabase().rawQuery(
				sql.toString(), 
				new String[] {
					dateFrom,
					dateTo,
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		if (cursor.moveToFirst()) {
			double vatExclude = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE));
			double totalSalePrice = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)) + vatExclude;
			orderDetail.setOrderQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			orderDetail.setPriceDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			orderDetail.setTotalRetailPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			orderDetail.setTotalSalePrice(totalSalePrice);
			orderDetail.setVat(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
			orderDetail.setVatExclude(vatExclude);
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
	public OrderDetail getSummaryOrder(int transactionId) {
		OrderDetail orderDetail = new OrderDetail();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", ");
		sql.append(" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "); 
		sql.append(" SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", ");
		sql.append(" SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "); 
		sql.append(" SUM(b." + OrderDetailTable.COLUMN_TOTAL_VAT + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_TOTAL_VAT + ", ");
		sql.append(" SUM(b." + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") ");
		sql.append(" AS " + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ",");
		sql.append(" c." + PromotionPriceGroupTable.COLUMN_PROMOTION_NAME + ",");
		sql.append(" c." + PromotionPriceGroupTable.COLUMN_BUTTON_NAME);
		sql.append(" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a ");
		sql.append(" LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b ");
		sql.append(" ON a." + OrderTransactionTable.COLUMN_TRANS_ID + "=b." + OrderTransactionTable.COLUMN_TRANS_ID);
		sql.append(" LEFT JOIN " + PromotionPriceGroupTable.TABLE_PROMOTION_PRICE_GROUP + " c ");
		sql.append(" ON a." + PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID + "=c." + PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID);
		sql.append(" WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"); 
		Cursor cursor = getReadableDatabase().rawQuery(
				sql.toString(),
				new String[] { 
					String.valueOf(transactionId)
				});
		if (cursor.moveToFirst()) {
			double vatExclude = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE));
			double totalSalePrice = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)) + vatExclude;
			String promotionName = cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_NAME));
			String buttonName = cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_BUTTON_NAME));
			orderDetail.setPromotionName(TextUtils.isEmpty(promotionName) ? (TextUtils.isEmpty(buttonName) ? "" : buttonName) : promotionName);
			orderDetail.setOrderQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			orderDetail.setPriceDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			orderDetail.setTotalRetailPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			orderDetail.setTotalSalePrice(totalSalePrice);
			orderDetail.setVat(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
			orderDetail.setVatExclude(vatExclude);
		}
		cursor.close();
		return orderDetail;
	}
	
	/**
	 * @param transactionId
	 * @return max total retail price
	 */
	public double getMaxTotalRetailPrice(int transactionId){
		double maxTotalRetailPrice = 0.0d;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT MAX(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ")"
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + "=?", 
				new String[]{
					String.valueOf(transactionId)
				});
		if(cursor.moveToFirst()){
			maxTotalRetailPrice = cursor.getDouble(0);
		}
		cursor.close();
		return maxTotalRetailPrice;
	}

	/**
	 * @param saleDate
	 * @return max receipt no
	 */
	public String getMaxReceiptNo(String saleDate){
		String receiptNo = "";
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransactionTable.COLUMN_RECEIPT_NO
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) "
				+ " ORDER BY " + OrderTransactionTable.COLUMN_TRANS_ID
				+ " DESC LIMIT 1", 
				new String[]{
					saleDate, 
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
					String.valueOf(Transaction.TRANS_STATUS_VOID)
				});
		if(cursor.moveToFirst()){
			receiptNo = cursor.getString(0);
		}
		cursor.close();
		return receiptNo;
	}
	
	public String getMinReceiptNo(String saleDate){
		String receiptNo = "";
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransactionTable.COLUMN_RECEIPT_NO
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) "
				+ " ORDER BY " + OrderTransactionTable.COLUMN_TRANS_ID
				+ " ASC LIMIT 1", 
				new String[]{
					saleDate, 
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
					String.valueOf(Transaction.TRANS_STATUS_VOID)
				});
		if(cursor.moveToFirst()){
			receiptNo = cursor.getString(0);
		}
		cursor.close();
		return receiptNo;
	}
	
	/**
	 * Summary order for update transaction
	 * @param transactionId
	 * @return summary of order
	 */
	private OrderDetail getSummaryOrderIgnoreNoVat(int transactionId) {
		OrderDetail orderDetail = new OrderDetail();
		String sql = "SELECT SUM (" + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " 
				+ OrderDetailTable.COLUMN_ORDER_QTY + ", "
				+ " SUM (" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS " 
				+ OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " 
				+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " 
				+ OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
				+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS " 
				+ OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", " 
				+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT + ") AS " 
				+ OrderDetailTable.COLUMN_TOTAL_VAT + ", "
				+ " SUM (" + OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") AS " 
				+ OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE
				+ " FROM " + OrderDetailTable.TABLE_ORDER 
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + ProductTable.COLUMN_VAT_TYPE + " != ?";
		Cursor cursor = getReadableDatabase().rawQuery(
				sql, 
				new String[] {
					String.valueOf(transactionId), 
					String.valueOf(Products.NO_VAT) 
				});
		if (cursor.moveToFirst()) {
			orderDetail.setOrderQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			orderDetail.setPriceDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			orderDetail.setTotalRetailPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			orderDetail.setTotalSalePrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			orderDetail.setVat(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT)));
			orderDetail.setVatExclude(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE)));
		}
		cursor.close();
		return orderDetail;
	}

	/**
	 * @param transactionId
	 * @return List<OrderTransaction.MPOSOrderDetail>
	 */
//	public List<OrderDetail> listAllOrderForDiscount(int transactionId) {
//		List<OrderDetail> orderDetailLst = new ArrayList<OrderDetail>();
//		Cursor cursor = getReadableDatabase().rawQuery(
//				" SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", "
//				+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
//				+ " a." + ProductTable.COLUMN_PRODUCT_ID + ", "
//				+ " a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
//				+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ", "
//				+ " a." + ProductTable.COLUMN_PRODUCT_PRICE + ", "
//				+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", " 
//				+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
//				+ " a." + ProductTable.COLUMN_VAT_TYPE + ", "
//				+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
//				+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
//				+ " a." + OrderDetailTable.COLUMN_PRICE_OR_PERCENT + ", "
//				+ " a." + BaseColumn.COLUMN_REMARK + ", "
//				+ " b." + ProductTable.COLUMN_PRODUCT_NAME 
//				+ " FROM " + OrderDetailTable.TABLE_ORDER_TMP + " a "
//				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
//				+ " ON a."  + ProductTable.COLUMN_PRODUCT_ID + " =b." + ProductTable.COLUMN_PRODUCT_ID
//				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?",
//				new String[] { 
//					String.valueOf(transactionId) 
//				});
//		if (cursor.moveToFirst()) {
//			do {
//				orderDetailLst.add(toOrderDetail(cursor));
//			} while (cursor.moveToNext());
//		}
//		cursor.close();
//		return orderDetailLst;
//	}

	/**
	 * list all order group by productId
	 * @param transactionId
	 * @return List<OrderDetail>
	 */
	public List<OrderDetail> listAllOrderGroupByProduct(int transactionId) {
		List<OrderDetail> orderDetailLst = new ArrayList<OrderDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", "
				+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
				+ " a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
				+ " a." + ProductTable.COLUMN_PRODUCT_ID + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", " 
				+ " SUM(a." + ProductTable.COLUMN_PRODUCT_PRICE + " * " + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + ProductTable.COLUMN_PRODUCT_PRICE + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", " 
				+ " a." + ProductTable.COLUMN_VAT_TYPE + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS " + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
				+ " SUM(a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " 
				+ " a." + OrderDetailTable.COLUMN_PRICE_OR_PERCENT + ", "
				+ " a." + BaseColumn.COLUMN_REMARK + ", "
				+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ", "
				+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ", "
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + " =b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?, ?, ?, ?) "
				+ " GROUP BY a." + ProductTable.COLUMN_PRODUCT_ID
				+ " ORDER BY a." + OrderDetailTable.COLUMN_ORDER_ID,
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(Products.NORMAL_TYPE),
					String.valueOf(Products.SET_CAN_SELECT),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(Products.COMMENT_NOT_HAVE_PRICE),
				});
		if (cursor.moveToFirst()) {
			do {
				orderDetailLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderDetailLst;
	}

	/**
	 * list all order
	 * @param transactionId
	 * @return List<OrderDetail>
	 */
	public List<OrderDetail> listAllOrder(int transactionId) {
		List<OrderDetail> ordLst = new ArrayList<OrderDetail>();
		Cursor cursor = queryOrderDetail(
				"a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?, ?) ",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(Products.NORMAL_TYPE),
					String.valueOf(Products.SET_CAN_SELECT)
				});
		if (cursor.moveToFirst()) {
			do {
				ordLst.add(toOrderDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return ordLst;
	}
	
	/**
	 * Query order detail by parsing selection string and selectionArgs
	 * @param selection
	 * @param selectionArgs
	 * @return Cursor
	 */
	private Cursor queryOrderDetail(String selection, String[] selectionArgs){
		String sql = "SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ","
				+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ","
				+ " a." + ProductTable.COLUMN_PRODUCT_ID + ","
				+ " a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ","
				+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ","
				+ " a." + ProductTable.COLUMN_PRODUCT_PRICE + ","
				+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + "," 
				+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ","
				+ " a." + ProductTable.COLUMN_VAT_TYPE + ","
				+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ","
				+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ","
				+ " a." + OrderDetailTable.COLUMN_PRICE_OR_PERCENT + ","
				+ " a." + OrderDetailTable.COLUMN_ORDER_SET_QTY + ", "
				+ " a." + OrderDetailTable.COLUMN_ORDER_SET_PRICE + ", "
				+ " a." + BaseColumn.COLUMN_REMARK + ","
				+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ", "
				+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a"
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b"
				+ " ON a."  + ProductTable.COLUMN_PRODUCT_ID + " =b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE " + selection;
		return getReadableDatabase().rawQuery(sql, selectionArgs);
	}
	
	/**
	 * @param cursor
	 * @return OrderDetail
	 */
	private OrderDetail toOrderDetail(Cursor cursor){
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
		orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
		orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
		orderDetail.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID)));
		orderDetail.setProductName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
		orderDetail.setProductName1(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
		orderDetail.setOrderQty(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
		orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
		orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
		orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
		orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
		orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_MEMBER_DISCOUNT)));
		orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
		orderDetail.setPriceOrPercent(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_OR_PERCENT)));
		orderDetail.setOrderComment(cursor.getString(cursor.getColumnIndex(BaseColumn.COLUMN_REMARK)));
		orderDetail.setOrdSetDetailLst(listOrderSetDetail(orderDetail.getTransactionId(), orderDetail.getOrderDetailId()));
		return orderDetail;
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return OrderDetail
	 */
	public OrderDetail getOrder(int transactionId, int orderDetailId){
		OrderDetail order = new OrderDetail();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
						+ " a." + ProductTable.COLUMN_PRODUCT_ID + ", "
						+ " a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
						+ " a." + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " a." + ProductTable.COLUMN_PRODUCT_PRICE + ", "
						+ " a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", " 
						+ " a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ", "
						+ " a." + ProductTable.COLUMN_VAT_TYPE + ", "
						+ " a." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
						+ " a." + OrderDetailTable.COLUMN_PRICE_OR_PERCENT + ", "
						+ " a." + BaseColumn.COLUMN_REMARK + ", "
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME 
						+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
						+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
						+ " ON a."  + ProductTable.COLUMN_PRODUCT_ID
						+ " =b." + ProductTable.COLUMN_PRODUCT_ID
						+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
						+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { String.valueOf(transactionId), String.valueOf(orderDetailId) });
		if(cursor.moveToFirst()){
			order = toOrderDetail(cursor);
		}
		cursor.close();
		return order;
	}
	
	/**
	 * @param cursor
	 * @return OrderTransaction.MPOSOrderDetail
	 */
//	private OrderTransaction.MPOSOrderDetail toOrderDetailGroupByProduct(Cursor cursor) {
//		OrderTransaction.MPOSOrderDetail orderDetail = new OrderTransaction.MPOSOrderDetail();
//		orderDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
//		orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
//		orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
//		orderDetail.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID)));
//		orderDetail.setProductName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
//		orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
//		orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
//		orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
//		orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
//		orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
//		orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_MEMBER_DISCOUNT)));
//		orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
//		orderDetail.setDiscountType(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_OR_PERCENT)));
//		orderDetail.setOrderComment(cursor.getString(cursor.getColumnIndex(BaseColumn.COLUMN_REMARK)));
//		
//		// generate order set detail
//		List<OrderTransaction.OrderSet.OrderSetDetail> orderSetDetailLst = listOrderSetDetailGroupByProduct(
//				orderDetail.getTransactionId(), orderDetail.getOrderDetailId());
//		if (orderSetDetailLst.size() > 0) {
//			orderDetail.setOrderSetDetailLst(orderSetDetailLst);
//		}
//		// list order comment
//		List<Comment.Comment> commentLst = listOrderComment(orderDetail.getTransactionId(), 
//				orderDetail.getOrderDetailId());
//		if(commentLst.size() > 0){
//			orderDetail.setOrderCommentLst(commentLst);
//		}
//		return orderDetail;
//	}

	public String formatReceiptNo(int year, int month, int day, int id) {
		Computer computer = new Computer(getContext());
		String receiptHeader = computer.getReceiptHeader();
		String receiptYear = String.format(Locale.US, "%04d", year);
		String receiptMonth = String.format(Locale.US, "%02d", month);
		String receiptDay = String.format(Locale.US, "%02d", day);
		String receiptId = String.format(Locale.US, "%04d", id);
		return receiptHeader + receiptDay + receiptMonth + receiptYear + "/" + receiptId;
	}

	/**
	 * @param saleDate
	 * @return List<OrderTransaction>
	 */
	public List<OrderTransaction> listTransaction(String saleDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS, 
				new String[]{
					OrderTransactionTable.COLUMN_TRANS_ID,
					ComputerTable.COLUMN_COMPUTER_ID,
					SessionTable.COLUMN_SESS_ID,
					OrderTransactionTable.COLUMN_PAID_TIME,
					OrderTransactionTable.COLUMN_TRANS_NOTE,
					OrderTransactionTable.COLUMN_RECEIPT_NO,
					OrderTransactionTable.COLUMN_STATUS_ID
				}, 
				OrderTransactionTable.COLUMN_SALE_DATE + "=? AND "
				+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?)", 
				new String[] { 
					saleDate, 
					String.valueOf(TRANS_STATUS_VOID),
					String.valueOf(TRANS_STATUS_SUCCESS) 
				}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setSessionId(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
				trans.setTransactionStatusId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
				trans.setPaidTime(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_PAID_TIME)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	/**
	 * @param saleDate
	 * @return List<OrderTransaction>
	 */
	public List<OrderTransaction> listSuccessTransaction(String saleDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = getReadableDatabase()
				.rawQuery(
						" SELECT "
								+ OrderTransactionTable.COLUMN_TRANS_ID
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
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
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
	 * List hold transaction
	 * @param sessionDate
	 * @return List<OrderTransaction>
	 */
	public List<OrderTransaction> listHoldOrder(String sessionDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", " 
						+ " a." + ComputerTable.COLUMN_COMPUTER_ID + ", " 
						+ " a." + OrderTransactionTable.COLUMN_OPEN_TIME + ", " 
						+ " a." + OrderTransactionTable.COLUMN_TRANS_NOTE + ", "
						+ " b." + StaffTable.COLUMN_STAFF_CODE + ", " 
						+ " b." + StaffTable.COLUMN_STAFF_NAME 
						+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "
						+ " LEFT JOIN " + StaffTable.TABLE_STAFF + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_OPEN_STAFF + "=b." + StaffTable.COLUMN_STAFF_ID
						+ " WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
						+ " AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?",
				new String[] { 
						sessionDate, 
						String.valueOf(TRANS_STATUS_HOLD) 
				});
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setTransactionNote(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
				trans.setOpenTime(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_TIME)));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_CODE))
						+ ":"
						+ cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}

	/**
	 * Get max transactionId
	 * @return max transactionId
	 */
	public int getMaxTransaction() {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderTransactionTable.COLUMN_TRANS_ID + ") " 
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS, null);
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
		}
		cursor.close();
		return transactionId + 1;
	}

	/**
	 * Get max receiptId
	 * @param year
	 * @param month
	 * @return max receiptId
	 */
	public int getMaxReceiptId(String saleDate) {
		int maxReceiptId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderTransactionTable.COLUMN_RECEIPT_ID + ") "
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?",
				new String[] { 
					saleDate 
				});
		if (cursor.moveToFirst()) {
			maxReceiptId = cursor.getInt(0);
		}
		cursor.close();
		return maxReceiptId + 1;
	}

	/**
	 * Get current transactionId
	 * @param sessionId
	 * @return 0 if not have opened transaction
	 */
	public int getCurrentTransactionId(int sessionId) {
		int transactionId = 0;
		Cursor cursor = getReadableDatabase()
				.query(OrderTransactionTable.TABLE_ORDER_TRANS, 
					new String[]{
						OrderTransactionTable.COLUMN_TRANS_ID
					}, 
					OrderTransactionTable.COLUMN_STATUS_ID + "=?" 
					+ " AND " + SessionTable.COLUMN_SESS_ID + "=?", 
					new String[] { 
						String.valueOf(TRANS_STATUS_NEW),
						String.valueOf(sessionId)
					}, null, null, null);
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
		}
		cursor.close();
		return transactionId;
	}

	/**
	 * @param saleDate
	 * @param shopId
	 * @param computerId
	 * @param sessionId
	 * @param staffId
	 * @param vatRate
	 * @return current transactionId
	 * @throws SQLException
	 */
	public int openTransaction(String saleDate, int shopId, int computerId, int sessionId,
			int staffId, double vatRate) throws SQLException {
		int transactionId = getMaxTransaction();
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(Long.parseLong(saleDate));
		Calendar dateTime = Utils.getCalendar();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_UUID, getUUID());
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ShopTable.COLUMN_SHOP_ID, shopId);
		cv.put(SessionTable.COLUMN_SESS_ID, sessionId);
		cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, staffId);
		cv.put(OrderTransactionTable.COLUMN_DOC_TYPE_ID, 8);
		cv.put(OrderTransactionTable.COLUMN_OPEN_TIME,
				dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_SALE_DATE, date.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_YEAR,
				date.get(Calendar.YEAR));
		cv.put(OrderTransactionTable.COLUMN_RECEIPT_MONTH,
				date.get(Calendar.MONTH) + 1);
		cv.put(ProductTable.COLUMN_VAT_RATE, vatRate);
		long rowId = getWritableDatabase().insertOrThrow(
				OrderTransactionTable.TABLE_ORDER_TRANS, null, cv);
		if (rowId == -1)
			transactionId = 0;
		return transactionId;
	}

	/**
	 * @param transactionId
	 * @param staffId
	 * @param totalSalePrice
	 * @param vatType
	 * @param vatRate
	 */
	public void closeTransaction(int transactionId, int staffId, double totalSalePrice, 
			int vatType, double vatRate) {
		Calendar date = Utils.getDate();
		Calendar dateTime = Utils.getCalendar();
		int receiptId = getMaxReceiptId(String.valueOf(date.getTimeInMillis()));
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
				formatReceiptNo(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, 
						date.get(Calendar.DAY_OF_MONTH), receiptId));
		getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
		
		updateTransactionVatable(transactionId, totalSalePrice, vatType, vatRate);
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
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	
	/**
	 * Cancel transaction
	 * @param transactionId
	 */
	public void cancelTransaction(int transactionId) {
		cancelOrder(transactionId);
		deleteTransaction(transactionId);
	}

	/**
	 * Delete OrderDetail and OrderSet by transactionId
	 * @param transactionId
	 */
	public void cancelOrder(int transactionId){
		deleteOrderDetail(transactionId);
	}
	
	/**
	 * Delete OrderDetail and OrderSet by transactonId and orderDetailId
	 * @param transactionId
	 * @param orderDetailId
	 */
	public void deleteOrder(int transactionId, int orderDetailId){
		deleteOrderComment(transactionId, orderDetailId);
		deleteOrderSet(transactionId, orderDetailId);
		deleteOrderDetail(transactionId, orderDetailId);
	}
	
	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteTransaction(int transactionId) {
		return getWritableDatabase().delete(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	public List<OrderTransaction> listTransactionNotSend(){
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = getReadableDatabase().query(OrderTransactionTable.TABLE_ORDER_TRANS, 
				new String[]{
					OrderTransactionTable.COLUMN_TRANS_ID,
					ComputerTable.COLUMN_COMPUTER_ID,
					SessionTable.COLUMN_SESS_ID
				}, OrderTransactionTable.COLUMN_STATUS_ID + "=? AND " +
					BaseColumn.COLUMN_SEND_STATUS + " =? ", 
				new String[]{
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
				 	String.valueOf(MPOSDatabase.NOT_SEND)
				}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setSessionId(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	/**
	 * @return total transaction that not sent
	 */
	public int countTransNotSend() {
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(" + OrderTransactionTable.COLUMN_TRANS_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_STATUS_ID + "=? AND "
						+ COLUMN_SEND_STATUS + "=?",
				new String[] {
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
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
				" SELECT COUNT(" + OrderTransactionTable.COLUMN_TRANS_ID
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
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * Update set promotion_price_group_id
	 * @param transactionId
	 * @param proPriceGroupId
	 */
	public void updateTransactionPromotion(int transactionId, int proPriceGroupId){
		ContentValues cv = new ContentValues();
		cv.put(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID, proPriceGroupId);
		getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS, cv, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?", 
				new String[]{
					String.valueOf(transactionId)
				}
		);
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
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int updateTransactionSendStatus(int transactionId, int status) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEND_STATUS, status);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?" + " AND "
						+ OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(TRANS_STATUS_SUCCESS),
						String.valueOf(TRANS_STATUS_VOID) });
	}

	/**
	 * @param saleDate
	 * @return row affected
	 */
	public int updateTransactionSendStatus(String saleDate, int status) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEND_STATUS, status);
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS,
				cv,
				OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) "
				+ " AND " + COLUMN_SEND_STATUS + "=?",
				new String[] { 
						saleDate, String.valueOf(TRANS_STATUS_SUCCESS),
						String.valueOf(TRANS_STATUS_VOID),
						String.valueOf(NOT_SEND)});
	}

	/**
	 * Update after payment
	 * @param transactionId
	 * @param totalPayment
	 * @param vatRate
	 * @return rows affected
	 */
	private int updateTransactionVatable(int transactionId, double totalPayment, int vatType, double vatRate){
		double vatable = Utils.calculateVatPrice(totalPayment, vatRate, vatType); 
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_VATABLE, vatable);
		return getWritableDatabase().update(OrderTransactionTable.TABLE_ORDER_TRANS, cv, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?", new String[]{String.valueOf(transactionId)});
	}
	
	/**
	 * Update transaction vat
	 * @param transactionId
	 * @param totalSalePrice
	 * @return row affected
	 */
	protected int updateTransactionVat(int transactionId) {
		OrderDetail summOrder = getSummaryOrderIgnoreNoVat(transactionId);
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_VAT, summOrder.getVat());
		cv.put(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT, summOrder.getVatExclude());
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
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
		getWritableDatabase().beginTransaction();
		try {
			deleteOrderDetail(transactionId);
			try {
				getWritableDatabase().execSQL(
						" INSERT INTO " + OrderDetailTable.TABLE_ORDER
								+ " SELECT * FROM "
								+ OrderDetailTable.TABLE_ORDER_TMP + " WHERE "
								+ OrderTransactionTable.COLUMN_TRANS_ID + "="
								+ transactionId);
				getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + OrderDetailTable.TABLE_ORDER_TMP);
				isSuccess = true;
				getWritableDatabase().setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			getWritableDatabase().endTransaction();
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
	 * @param priceOrPercent
	 * @param priceGroupId
	 * @param promotionTypeId
	 * @param couponHeader
	 * @return row affected
	 */
	public int discountEatchProduct(int transactionId, int orderDetailId,
			int vatType, double vatRate, double salePrice, double discount,
			int priceOrPercent, int priceGroupId, int promotionTypeId, String couponHeader){
		double vat = Utils.calculateVatAmount(salePrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, discount);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, salePrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		cv.put(OrderDetailTable.COLUMN_PRICE_OR_PERCENT, priceOrPercent);
		cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID,  promotionTypeId);
		cv.put(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID, priceGroupId);
		cv.put(PromotionPriceGroupTable.COLUMN_COUPON_HEADER, couponHeader);
		return getWritableDatabase().update(
				OrderDetailTable.TABLE_ORDER_TMP,
				cv,
				OrderDetailTable.COLUMN_ORDER_ID + "=? " + " AND "
						+ OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { 
						String.valueOf(orderDetailId),
						String.valueOf(transactionId) 
				});
	}

	/**
	 * @param transactionId
	 * @return row affected
	 */
	private int deleteOrderDetail(int transactionId) {
		return getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { 
					String.valueOf(transactionId) 
				});
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 */
	private void deleteOrderDetail(int transactionId, int orderDetailId) {
		getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { 
						String.valueOf(transactionId),
						String.valueOf(orderDetailId)
				});
		getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? AND "
						+ OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?",
				new String[] { 
						String.valueOf(transactionId),
						String.valueOf(orderDetailId)
				});
	}

	/**
	 * @param transactionId
	 * @return true if create temp table successfully
	 */
	public boolean prepareDiscount(int transactionId) {
		boolean isSuccess = false;
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + OrderDetailTable.TABLE_ORDER_TMP);
			getWritableDatabase().execSQL(
					" CREATE TABLE " + OrderDetailTable.TABLE_ORDER_TMP
							+ " AS SELECT * FROM " + OrderDetailTable.TABLE_ORDER
							+ " WHERE "
							+ OrderTransactionTable.COLUMN_TRANS_ID + "="
							+ transactionId);
			isSuccess = true;
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			isSuccess = false;
			getWritableDatabase().endTransaction();
		}
		return isSuccess;
	}
	
	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int cancelDiscount(int transactionId) {
		return getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER_TMP,
				OrderTransactionTable.COLUMN_TRANS_ID + "=?",
				new String[] { String.valueOf(transactionId) });
	}
	
	/**
	 * Update OrderComment price discount 
	 * @param transactionId
	 */
//	private void updateOrderCommentDiscount(int transactionId){
//		Cursor cursor = getReadableDatabase().query(OrderDetailTable.TABLE_ORDER, 
//				new String[]{
//					OrderDetailTable.COLUMN_PARENT_ORDER_ID,
//					ProductTable.COLUMN_PRODUCT_ID,
//					OrderDetailTable.COLUMN_PRICE_DISCOUNT
//				}, 
//				OrderTransactionTable.COLUMN_TRANS_ID + "=? "
//				+ " AND " + ProductTable.COLUMN_PRODUCT_TYPE_ID + " =?", 
//				new String[]{
//					String.valueOf(transactionId),
//					String.valueOf(Products.COMMENT_HAVE_PRICE)
//				}, null, null, null);
//		if(cursor.moveToFirst()){
//			do{
//				int parentOrderDetailId = cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_PARENT_ORDER_ID));
//				int productId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID));
//				double priceDiscount = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT));
//				ContentValues cv = new ContentValues();
//				cv.put(OrderCommentTable.COLUMN_ORDER_COMMENT_PRICE_DISCOUNT, priceDiscount);
//				getWritableDatabase().update(OrderCommentTable.TABLE_ORDER_COMMENT, cv, 
//						OrderTransactionTable.COLUMN_TRANS_ID + "=?"
//						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?"
//						+ " AND " + MenuCommentTable.COLUMN_COMMENT_ID + "=?", 
//						new String[]{
//							String.valueOf(transactionId),
//							String.valueOf(parentOrderDetailId),
//							String.valueOf(productId)
//						});
//			}while(cursor.moveToNext());
//		}
//		cursor.close();
//	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderComment
	 */
	public void updateOrderComment(int transactionId, int orderDetailId, String orderComment){
		ContentValues cv = new ContentValues();
		cv.put(BaseColumn.COLUMN_REMARK, orderComment);
		getWritableDatabase().update(OrderDetailTable.TABLE_ORDER, cv, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(orderDetailId)
				});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param vatRate
	 * @param vatType
	 * @param totalPrice
	 * @return rows affected
	 */
	public int updateOrderDetail(int transactionId, int orderDetailId, 
			double vatRate, int vatType, double totalPrice){
		double vat = Utils.calculateVatAmount(totalPrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		return getWritableDatabase().update(OrderDetailTable.TABLE_ORDER, cv, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=? AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId)
				});
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
		double vat = Utils
				.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		if (vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, 0);
		return getWritableDatabase().update(
				OrderDetailTable.TABLE_ORDER,
				cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? AND "
						+ OrderDetailTable.COLUMN_ORDER_ID + "=? ",
				new String[] { String.valueOf(transactionId),
						String.valueOf(orderDetailId) });
	}

	
	/**
	 * For add order such as CommentType, SetType7
	 * when confirm comment and settype7
	 * @param transactionId
	 * @param computerId
	 * @param productId
	 * @param productType
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 * @param parentOrderId
	 * @return current orderDetailId
	 */
	public int addOrderDetail(int transactionId, int computerId, 
			int productId, int productType, int vatType, double vatRate, 
			double orderQty, double pricePerUnit, int parentOrderId) {
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Utils.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		int orderDetailId = getMaxOrderDetailId();
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ProductTable.COLUMN_PRODUCT_ID, productId);
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(ProductTable.COLUMN_VAT_TYPE, vatType);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, productType);
		cv.put(OrderDetailTable.COLUMN_PARENT_ORDER_ID, parentOrderId);
		cv.put(OrderDetailTable.COLUMN_REMARK, "");
		if (vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		long rowId = getWritableDatabase().insertOrThrow(
				OrderDetailTable.TABLE_ORDER, null, cv);
		if (rowId == -1)
			orderDetailId = 0;
		return orderDetailId;
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
	public int addOrderDetail(int transactionId, int computerId, 
			int productId, int productType, int vatType, double vatRate, 
			double orderQty, double pricePerUnit) {

//		int orderDetailId = checkAddedOrderDetail(transactionId, productId); 
//		if(orderDetailId > 0){
//			double totalAdded = getTotalAddedOrder(transactionId, productId) + orderQty;
//			updateOrderDetail(transactionId, orderDetailId, 
//					vatType, vatRate, totalAdded, pricePerUnit);
//		}else{
		double totalRetailPrice = pricePerUnit * orderQty;
		double vat = Utils.calculateVatAmount(totalRetailPrice, vatRate, vatType);
		int orderDetailId = getMaxOrderDetailId();
		ContentValues cv = new ContentValues();
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ProductTable.COLUMN_PRODUCT_ID, productId);
		cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderQty);
		cv.put(ProductTable.COLUMN_PRODUCT_PRICE, pricePerUnit);
		cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, totalRetailPrice);
		cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, totalRetailPrice);
		cv.put(ProductTable.COLUMN_VAT_TYPE, vatType);
		cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, vat);
		cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, productType);
		cv.put(OrderDetailTable.COLUMN_REMARK, "");
		if (vatType == Products.VAT_TYPE_EXCLUDE)
			cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, vat);
		long rowId = getWritableDatabase().insertOrThrow(
				OrderDetailTable.TABLE_ORDER, null, cv);
		if (rowId == -1)
			orderDetailId = 0;	
//		}
		return orderDetailId;
	}

	private int checkAddedOrderDetail(int transactionId, int productId){
		int orderDetailId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderDetailTable.COLUMN_ORDER_ID
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(productId)});
		if(cursor.moveToFirst()){
			orderDetailId = cursor.getInt(0);
		}
		cursor.close();
		return orderDetailId;
	}
	
	private double getTotalAddedOrder(int transactionId, int productId){
		double totalAdded = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT SUM(" + OrderDetailTable.COLUMN_ORDER_QTY + ")"
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(productId)});
		if(cursor.moveToFirst()){
			totalAdded = cursor.getDouble(0);
		}
		cursor.close();
		return totalAdded;
	}
	
	/**
	 * @return max orderId
	 */
	public int getMaxOrderDetailId() {
		int orderDetailId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + OrderDetailTable.COLUMN_ORDER_ID + ") "
						+ " FROM " + OrderDetailTable.TABLE_ORDER, null);
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
		cv.put(COLUMN_SEND_STATUS, MPOSDatabase.NOT_SEND);
		cv.put(OrderTransactionTable.COLUMN_VOID_TIME, Utils.getCalendar()
				.getTimeInMillis());
		return getWritableDatabase().update(
				OrderTransactionTable.TABLE_ORDER_TRANS, cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? ",
				new String[] { String.valueOf(transactionId) });
	}

	/**
	 * @param sessionDate
	 * @return total receipt specific by sale date
	 */
	public int getTotalReceipt(String sessionDate) {
		int totalReceipt = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT (" + OrderTransactionTable.COLUMN_TRANS_ID
						+ ") " + " FROM "
						+ OrderTransactionTable.TABLE_ORDER_TRANS + " WHERE "
						+ OrderTransactionTable.COLUMN_SALE_DATE + "=? "
						+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID
						+ " IN (?,?)",
				new String[] { sessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID) });
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
				"SELECT "
				+ " SUM (" + OrderTransactionTable.COLUMN_TRANS_VATABLE + ") " 
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS 
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=? "
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID
				+ " IN(?,?)",
				new String[] { sessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)});
		if (cursor.moveToFirst()) {
			totalReceiptAmount = cursor.getFloat(0);
		}
		cursor.close();
		return totalReceiptAmount;
	}

	public List<OrderSet> listOrderSet(int transId, int ordId) {
		List<OrderSet> productSetLst = new ArrayList<OrderSet>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "," 
				+ " b." + ProductComponentTable.COLUMN_PGROUP_ID + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME + ", " 
				+ " b." + ProductComponentGroupTable.COLUMN_REQ_AMOUNT + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_REQ_MIN_AMOUNT
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP + " b " 
				+ " ON a." + ProductComponentTable.COLUMN_PGROUP_ID + "=b." + ProductComponentTable.COLUMN_PGROUP_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=? "
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?"
				+ " GROUP BY b." + ProductComponentTable.COLUMN_PGROUP_ID,
				new String[] { 
					String.valueOf(transId),
					String.valueOf(ordId)
				});

		if (cursor.moveToFirst()) {
			do {
				int pgId = cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID));
				int prnOrdId = cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_PARENT_ORDER_ID));
				OrderSet group = new OrderSet();
				group.setTransId(transId);
				group.setOrdId(prnOrdId);
				group.setSetGroupId(pgId);
				group.setSetGroupNo(cursor.getInt(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
				group.setSetGroupName(cursor.getString(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME)));
				group.setReqAmount(cursor.getDouble(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_REQ_AMOUNT)));
				group.setReqMinAmount(cursor.getDouble(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_REQ_MIN_AMOUNT)));
				group.setOrderSetDetail(listOrderSetDetail(transId, prnOrdId, pgId));
				productSetLst.add(group);

			} while (cursor.moveToNext());
		}
		cursor.close();

		return productSetLst;
	}

	public List<OrderSet.OrderSetDetail> listOrderSetDetailGroupByProduct(int transactionId, int orderDetailId) {
		List<OrderSet.OrderSetDetail> orderSetDetailLst = new ArrayList<OrderSet.OrderSetDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
				+ " a." + ProductTable.COLUMN_PRODUCT_ID + ", " 
				+ " SUM (a." + OrderDetailTable.COLUMN_ORDER_SET_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_SET_QTY + ", "
				+ " SUM (a." + OrderDetailTable.COLUMN_ORDER_SET_PRICE + ") AS " + OrderDetailTable.COLUMN_ORDER_SET_PRICE + ", "
				+ " b." + ProductTable.COLUMN_PRODUCT_NAME
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + "=?"
				+ " GROUP by a." + ProductTable.COLUMN_PRODUCT_ID,
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE)
				});
		if (cursor.moveToFirst()) {
			do {
				OrderSet.OrderSetDetail detail = toOrderSetDetail(cursor);
				orderSetDetailLst.add(detail);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return orderSetDetailLst;
	}
	
//	public List<OrderSet.OrderSetDetail> listOrderSetDetailHavePrice(int transactionId, int orderDetailId){
//		List<OrderSet.OrderSetDetail> orderSetDetailLst = 
//				new ArrayList<OrderSet.OrderSetDetail>();
//		Cursor cursor = getReadableDatabase().rawQuery(
//				"SELECT a. " + ProductTable.COLUMN_PRODUCT_ID + ","
//				+ " a." + OrderSetTable.COLUMN_ORDER_SET_QTY + ","
//				+ " a." + OrderSetTable.COLUMN_ORDER_SET_PRICE + ", "
//				+ " b." + ProductTable.COLUMN_VAT_TYPE + ","
//				+ " b." + ProductTable.COLUMN_VAT_RATE
//				+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " a "
//				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
//				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID 
//				+ "=b." + ProductTable.COLUMN_PRODUCT_ID
//				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=? " 
//				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=? "
//				+ " AND a." + OrderSetTable.COLUMN_ORDER_SET_PRICE + ">?",
//				new String[]{
//					String.valueOf(transactionId),
//					String.valueOf(orderDetailId),
//					String.valueOf("0")
//				});
//		if(cursor.moveToFirst()){
//			do{
//				OrderSet.OrderSetDetail setDetail = new OrderSet.OrderSetDetail();
//				setDetail.setProductId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
//				setDetail.setOrderSetQty(cursor.getDouble(cursor.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_QTY)));
//				setDetail.setProductPrice(cursor.getDouble(cursor.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_PRICE)));
//				setDetail.setVatType(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
//				setDetail.setVatRate(cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_VAT_RATE)));
//				orderSetDetailLst.add(setDetail);
//			}while(cursor.moveToNext());
//		}
//		cursor.close();
//		return orderSetDetailLst;
//	}
	
	/**
	 * @param transId
	 * @param ordId
	 * @param pgId
	 * @return List<OrderSet.OrderSetDetail>
	 */
	public List<OrderSet.OrderSetDetail> listOrderSetDetail(int transId, int ordId, int pgId) {
		List<OrderSet.OrderSetDetail> sdl = new ArrayList<OrderSet.OrderSetDetail>();
		Cursor cursor = queryOrderDetail(
				"a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?"
				+ " AND a." + ProductComponentTable.COLUMN_PGROUP_ID + "=?",
				new String[] {
					String.valueOf(transId),
					String.valueOf(ordId),
					String.valueOf(pgId),
				});
		if (cursor.moveToFirst()) {
			do {
				sdl.add(toOrderSetDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return sdl;
	}
	
	/**
	 * @param transId
	 * @param ordId
	 * @return List<OrderSet.OrderSetDetail>
	 */
	public List<OrderSet.OrderSetDetail> listOrderSetDetail(int transId, int ordId) {
		List<OrderSet.OrderSetDetail> sdl = new ArrayList<OrderSet.OrderSetDetail>();
		Cursor cursor = queryOrderDetail(
				"a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + "=?",
				new String[] {
					String.valueOf(transId),
					String.valueOf(ordId),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE)
				});
		if (cursor.moveToFirst()) {
			do {
				sdl.add(toOrderSetDetail(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return sdl;
	}
	
	/**
	 * @param cursor
	 * @return OrderSet.OrderSetDetail
	 */
	private OrderSet.OrderSetDetail toOrderSetDetail(Cursor cursor){
		OrderSet.OrderSetDetail sd = new OrderSet.OrderSetDetail();
		sd.setOrderSetId(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
		sd.setProductId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
		sd.setProductName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
		sd.setProductName1(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
		sd.setOrderSetQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_SET_QTY)));
		sd.setProductPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_SET_PRICE)));
		return sd;
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param pcompGroupId
	 * @return total qty of group
	 */
	public double getOrderSetTotalQty(int transactionId, int orderDetailId, int pcompGroupId) {
		double totalQty = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" select sum(" + OrderDetailTable.COLUMN_ORDER_SET_QTY + ") "
				+ " from " + OrderDetailTable.TABLE_ORDER 
				+ " where " + OrderTransactionTable.COLUMN_TRANS_ID + "=? "
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? "
				+ " and " + ProductComponentTable.COLUMN_PGROUP_ID + "=? ",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(pcompGroupId) 
				});
		if (cursor.moveToFirst()) {
			totalQty = cursor.getDouble(0);
		}
		cursor.close();
		return totalQty;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId) {
		getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? ",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId) 
				});
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderSetId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId, int orderSetId) {
		getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(orderSetId) 
				});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param pCompGroupId
	 */
	public void deleteOrderSetByGroup(int transactionId, int orderDetailId, int pCompGroupId) {
		getWritableDatabase().delete(
				OrderDetailTable.TABLE_ORDER,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? " 
				+ " and " + ProductComponentTable.COLUMN_PGROUP_ID + "=?",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(pCompGroupId) 
				});
	}
	
	/**
	 * Update order_set_price_discount of OrderSetTable
	 * @param transactionId
	 */
//	private void updateOrderSetDiscount(int transactionId){
//		Cursor cursor = getReadableDatabase().query(OrderDetailTable.TABLE_ORDER, 
//				new String[]{
//					OrderDetailTable.COLUMN_PARENT_ORDER_ID,
//					ProductTable.COLUMN_PRODUCT_ID,
//					OrderDetailTable.COLUMN_PRICE_DISCOUNT
//				}, 
//				OrderTransactionTable.COLUMN_TRANS_ID + "=? "
//				+ " AND " + ProductTable.COLUMN_PRODUCT_TYPE_ID + "=?", 
//				new String[]{
//					String.valueOf(transactionId),
//					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE)
//				}, null, null, null);
//		if(cursor.moveToFirst()){
//			do{
//				int parentOrderDetailId = cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_PARENT_ORDER_ID));
//				int productId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID));
//				double priceDiscount = cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT));
//				ContentValues cv = new ContentValues();
//				cv.put(OrderSetTable.COLUMN_ORDER_SET_PRICE_DISCOUNT, priceDiscount);
//				getWritableDatabase().update(OrderSetTable.TABLE_ORDER_SET, cv, 
//						OrderTransactionTable.COLUMN_TRANS_ID + "=?"
//						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?"
//						+ " AND " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
//						new String[]{
//							String.valueOf(transactionId),
//							String.valueOf(parentOrderDetailId),
//							String.valueOf(productId)
//						});
//			}while(cursor.moveToNext());
//		}
//		cursor.close();
//	}
	
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
		cv.put(OrderDetailTable.COLUMN_ORDER_SET_QTY, orderSetQty);
		getWritableDatabase().update(
				OrderDetailTable.TABLE_ORDER,
				cv,
				OrderTransactionTable.COLUMN_TRANS_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? " 
				+ " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
				new String[] { 
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(orderSetId) 
				});
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param productId
	 * @param productTypeId
	 * @param productName
	 * @param pcompGroupId
	 * @param reqAmount
	 * @param reqMinAmount
	 */
	public void addOrderSet(int transactionId, int computerId, int orderDetailId,
			int productId, int productTypeId, double orderSetQty, double productPrice, 
			int pcompGroupId, double reqAmount, double reqMinAmount) {
		int maxOrderId = getMaxOrderDetailId();
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, maxOrderId);
		cv.put(OrderDetailTable.COLUMN_PARENT_ORDER_ID, orderDetailId);
		cv.put(ProductTable.COLUMN_PRODUCT_ID, productId);
		cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, productTypeId);
		cv.put(OrderDetailTable.COLUMN_ORDER_SET_PRICE, productPrice);
		cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pcompGroupId);
		cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, reqAmount);
		cv.put(ProductComponentGroupTable.COLUMN_REQ_MIN_AMOUNT, reqMinAmount);
		cv.put(OrderDetailTable.COLUMN_DEDUCT_AMOUNT, orderSetQty);
		cv.put(OrderDetailTable.COLUMN_ORDER_SET_QTY, orderSetQty);
		getWritableDatabase().insertOrThrow(OrderDetailTable.TABLE_ORDER, ProductTable.COLUMN_PRODUCT_NAME, cv);
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param productGroupId
	 * @return rows
	 */
	public int checkAddedOrderSet(int transactionId, int orderDetailId, int productGroupId){
		int added = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"select count(" + OrderDetailTable.COLUMN_ORDER_ID + ") "
				+ " from " + OrderDetailTable.TABLE_ORDER
				+ " where " + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=? "
				+ " and " + ProductComponentTable.COLUMN_PGROUP_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(productGroupId)
				});
		if(cursor.moveToFirst()){
			added = cursor.getInt(0);
		}
		cursor.close();
		return added;
	}
	
	/**
	 * @param transId
	 * @param ordId
	 * @return List<Comment> 
	 */
	public List<Comment> listOrderComment(int transId, int ordId){
		List<Comment> ordCmLst = new ArrayList<Comment>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + OrderDetailTable.COLUMN_ORDER_COMMENT_QTY + ", "
				+ " a." + OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE + ", "
				+ " b." + MenuCommentTable.COLUMN_COMMENT_NAME
 				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + MenuCommentTable.TABLE_MENU_COMMENT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + " =b." + MenuCommentTable.COLUMN_COMMENT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?,?) ",
				new String[]{
					String.valueOf(transId),
					String.valueOf(ordId),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(Products.COMMENT_NOT_HAVE_PRICE)
				});
		if(cursor.moveToFirst()){
			do{
				Comment cm = new Comment();
				cm.setCommentName(cursor.getString(cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_NAME)));
				cm.setCommentQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_COMMENT_QTY)));
				cm.setCommentPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE)));
				ordCmLst.add(cm);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return ordCmLst;
	}
	
	/**
	 * @param transId
	 * @param ordId
	 * @param cmId
	 * @return Comment
	 */
	public Comment getOrderComment(int transId, int ordId, int cmId){
		Comment ordCm = new Comment();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + ProductTable.COLUMN_PRODUCT_ID + ", "
				+ " a." + OrderDetailTable.COLUMN_ORDER_COMMENT_QTY + ", "
				+ " a." + OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE + ", "
				+ " b." + MenuCommentTable.COLUMN_COMMENT_NAME
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + MenuCommentTable.TABLE_MENU_COMMENT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + " =b." + MenuCommentTable.COLUMN_COMMENT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_ID + "=?"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?,?) ",
				new String[]{
					String.valueOf(transId),
					String.valueOf(ordId),
					String.valueOf(cmId),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(Products.COMMENT_NOT_HAVE_PRICE)
				});
		if(cursor.moveToFirst()){
			ordCm.setCommentId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
			ordCm.setCommentName(cursor.getString(cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_NAME)));
			ordCm.setCommentQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_COMMENT_QTY)));
			ordCm.setCommentPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE)));
		}
		cursor.close();
		return ordCm;
	}

	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param commentId
	 */
	public void deleteOrderComment(int transactionId, int orderDetailId, int commentId){
		getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?"
				+ " and " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
			new String[]{
				String.valueOf(transactionId),
				String.valueOf(orderDetailId),
				String.valueOf(commentId)
			});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 */
	public void deleteOrderComment(int transactionId, int orderDetailId){
		getWritableDatabase().delete(OrderDetailTable.TABLE_ORDER, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?", 
			new String[]{
				String.valueOf(transactionId),
				String.valueOf(orderDetailId)
			});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param commentId
	 * @param commentQty
	 * @throws SQLException
	 */
	public void updateOrderComment(int transactionId, int orderDetailId, 
			int commentId, double commentQty) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(ProductTable.COLUMN_PRODUCT_ID, commentId);
		cv.put(OrderDetailTable.COLUMN_ORDER_COMMENT_QTY, commentQty);
		getWritableDatabase().update(OrderDetailTable.TABLE_ORDER, cv, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?"
				+ " and " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(commentId)
				});
	}
	
	/**
	 * @param transactionId
	 * @param computerId
	 * @param orderDetailId
	 * @param commentId
	 * @param productTypeId
	 * @param commentQty
	 * @param commentPrice
	 * @throws SQLException
	 */
	public void addOrderComment(int transactionId, int computerId, int orderDetailId, int commentId,
			int productTypeId, double commentQty, double commentPrice) throws SQLException{
		int maxOrderId = getMaxOrderDetailId();
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_TRANS_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, maxOrderId);
		cv.put(OrderDetailTable.COLUMN_PARENT_ORDER_ID, orderDetailId);
		cv.put(ProductTable.COLUMN_PRODUCT_ID, commentId);
		cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, productTypeId);
		cv.put(OrderDetailTable.COLUMN_ORDER_COMMENT_QTY, commentQty);
		cv.put(OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE, commentPrice);
		getWritableDatabase().insertOrThrow(OrderDetailTable.TABLE_ORDER, null, cv);
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param commentId
	 * @return
	 */
	public boolean checkAddedComment(int transactionId, int orderDetailId, int commentId){
		boolean isAdded = false;
		Cursor cursor = getReadableDatabase().query(OrderDetailTable.TABLE_ORDER, 
				new String[]{
					ProductTable.COLUMN_PRODUCT_ID
				}, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_PARENT_ORDER_ID + "=?"
				+ " and " + ProductTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId),
					String.valueOf(commentId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			if(cursor.getInt(0) != 0)
				isAdded = true;
		}
		cursor.close();
		return isAdded;
	}
}
