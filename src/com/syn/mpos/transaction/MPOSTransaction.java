package com.syn.mpos.transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Util;
import com.syn.pos.OrderTransaction;

/**
 * 
 * @author j1tth4
 * 
 */
public class MPOSTransaction extends Util {

	protected SQLiteHelper mSqlite;

	public MPOSTransaction(Context context) {
		super(context);
		mSqlite = new MPOSSQLiteHelper(context);
	}

	public int getMaxTransaction(int computerId) {
		int transactionId = 0;
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery( "SELECT MAX(transaction_id) " +
				" FROM order_transaction " +
				" WHERE computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();

		return transactionId + 1;
	}

	public int getMaxReceiptId(int computerId, int year, int month) {
		int maxReceiptId = 0;
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT MAX(receipt_id) " +
				" FROM order_transaction " +
				" WHERE computer_id=" + computerId + 
				" AND receipt_year=" + year + 
				" AND receipt_month=" + month +
				" AND transaction_status_id = 2");
		if (cursor.moveToFirst()) {
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();

		return maxReceiptId + 1;
	}

	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery(" SELECT MAX(order_detail_id) " + 
				" FROM order_detail " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			orderDetailId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();

		return orderDetailId + 1;
	}

	public int getCurrTransaction(int computerId) {
		int transactionId = 0;
		Calendar c = getDate();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT transaction_id " +
				" FROM order_transaction " + 
				" WHERE computer_id = " + computerId + 
				" AND transaction_status_id = 1 " + 
				" AND sale_date='" + c.getTimeInMillis() + "' ");
		if (cursor.moveToFirst()) {
			if (cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();
		return transactionId;
	}

	public int openTransaction(int computerId, int shopId, int sessionId,
			int staffId) {

		int transactionId = getMaxTransaction(computerId);
		Calendar dateTime = getDateTime();
		Calendar date = getDate();

		Log.d("dateTime", dateTime.getTime().toString());
		Log.i("date", date.getTime().toString());

		ContentValues cv = new ContentValues();
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("shop_id", shopId);
		cv.put("session_id", sessionId);
		cv.put("open_staff_id", staffId);
		cv.put("document_type_id", 20);
		cv.put("open_time", dateTime.getTimeInMillis());
		cv.put("open_staff_id", staffId);
		cv.put("sale_date", date.getTimeInMillis());
		cv.put("receipt_year", dateTime.get(Calendar.YEAR));
		cv.put("receipt_month", dateTime.get(Calendar.MONTH));

		mSqlite.open();

		if (!mSqlite.insert("order_transaction", cv))
			transactionId = 0;

		mSqlite.close();

		return transactionId;
	}

	public boolean successTransaction(int transactionId, int computerId,
			int staffId) {
		boolean isSuccess = false;

		Calendar calendar = getDateTime();
		int receiptId = getMaxReceiptId(computerId,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_transaction " + 
				" SET transaction_status_id=2, " + 
				" receipt_id=" + receiptId + ", " + 
				" close_time='" + calendar.getTimeInMillis() + "', " + 
				" paid_time='" + calendar.getTimeInMillis() + "', " + 
				" paid_staff_id=" + staffId + 
				" WHERE transaction_id="+ transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSuccess;
	}

	public boolean prepareTransaction(int transactionId, int computerId) {
		boolean isSuccess = false;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_transaction SET " + 
				" transaction_status_id=1, remark='' " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();

		return isSuccess;
	}

	public boolean deleteTransaction(int transactionId, int computerId) {
		boolean isSuccess = false;
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM order_transaction " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSuccess;
	}

	public int countHoldOrder(int computerId) {
		int total = 0;
		Calendar c = getDate();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT COUNT(transaction_id) " + 
				" FROM order_transaction " + 
				" WHERE transaction_status_id=9" + 
				" AND computer_id=" + computerId + 
				" AND sale_date='" + c.getTimeInMillis() + "'");
		if (cursor.moveToFirst()) {
			total = cursor.getInt(0);
		}
		cursor.close();
		mSqlite.close();
		return total;
	}

	public boolean holdTransaction(int transactionId, int computerId,
			String remark) {
		boolean isSuccess = false;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_transaction SET " +
				" transaction_status_id = 9, " + 
				" remark='" + remark + "' " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSuccess;
	}

	public boolean updateTransactionVat(int transactionId, int computerId,
			float totalSalePrice, float vatExclude) {
		boolean isSuccess = false;
		float vat = calculateVat(totalSalePrice);

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_transaction SET " + 
				" transaction_vat=" + vat + ", " + 
				" transaction_vatable=" + totalSalePrice + ", " + 
				" transaction_exclude_vat=" + vatExclude + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSuccess;
	}

	public boolean confirmDiscount(int transactionId, int computerId) {
		boolean isSuccess = false;

		if (deleteOrderDetail(transactionId, computerId)) {
			
			mSqlite.open();
			isSuccess = mSqlite.execSQL("INSERT INTO order_detail " + 
					" SELECT * FROM order_detail_tmp " + 
					" WHERE transaction_id=" + transactionId + 
					" AND computer_id=" + computerId);
			mSqlite.close();
		}
		return isSuccess;
	}

	public boolean discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, int vatType, float salePrice, float discount,
			int disType) {
		boolean isSuccess = false;

		float vat = vatType == 2 ? calculateVat(salePrice) : 0.0f;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_detail_tmp SET " + 
				" price_discount=" + discount + ", " + 
				" total_sale_price=" + salePrice + ", " + 
				" vat=" + vat + ", " + 
				" discount_type=" + disType + 
				" WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId + 
				" AND computer_id="+ computerId);
		mSqlite.close();

		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId) {
		boolean isSuccess = false;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("DELETE FROM order_detail " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();

		return isSuccess;
	}

	public boolean deleteOrderDetail(int transactionId, int computerId,
			int orderDetailId) {
		boolean isSuccess = false;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("DELETE FROM order_detail " + 
				" WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();

		return isSuccess;
	}

	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float orderQty, float pricePerUnit) {
		boolean isSucc = false;

		float totalRetailPrice = pricePerUnit * orderQty;
		float vat = vatType == 2 ? calculateVat(totalRetailPrice) : 0.0f;

		mSqlite.open();
		isSucc = mSqlite.execSQL("UPDATE order_detail SET " + 
				" order_qty=" + orderQty + ", " + 
				" unit_price=" + pricePerUnit + ", " + 
				" total_retail_price=" + totalRetailPrice + ", " + 
				" total_sale_price=" + totalRetailPrice + ", " + 
				" vat=" + vat + ", " + 
				" price_discount=0 " + 
				" WHERE transaction_id=" + transactionId + 
				" AND order_detail_id=" + orderDetailId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSucc;
	}

	public int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, float orderQty,
			float pricePerUnit) {

		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
		float totalRetailPrice = pricePerUnit * orderQty;
		float vat = vatType == 2 ? calculateVat(totalRetailPrice) : 0.0f;

		ContentValues cv = new ContentValues();
		cv.put("order_detail_id", orderDetailId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("product_id", productId);
		cv.put("order_qty", orderQty);
		cv.put("unit_price", pricePerUnit);
		cv.put("total_retail_price", totalRetailPrice);
		cv.put("total_sale_price", totalRetailPrice);
		cv.put("vat_type", vatType);
		cv.put("vat", vat);

		mSqlite.open();
		if (!mSqlite.insert("order_detail", cv))
			orderDetailId = 0;
		mSqlite.close();

		return orderDetailId;
	}

	public boolean cancelDiscount(int transactionId, int computerId) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean deleteOrderDetailTmp(int transactionId, int computerId) {
		boolean isSuccess = false;

		mSqlite.open();
		isSuccess = mSqlite.execSQL("DELETE FROM order_detail_tmp " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();

		return isSuccess;
	}

	public boolean copyOrderToTmp(int transactionId, int computerId) {
		boolean isSuccess = false;

		if (deleteOrderDetailTmp(transactionId, computerId)) {

			mSqlite.open();
			isSuccess = mSqlite.execSQL("INSERT INTO order_detail_tmp " + 
					" SELECT * FROM order_detail " + 
					" WHERE transaction_id=" + transactionId + 
					" AND computer_id=" + computerId);
			mSqlite.close();
		}
		return isSuccess;
	}

	public boolean voidTransaction(int transactionId, int computerId,
			int staffId, String reason) {
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();

		mSqlite.open();
		isSuccess = mSqlite.execSQL("UPDATE order_transaction " + 
				" SET transaction_status_id=9, " + 
				" void_staff_id=" + staffId + ", " + 
				" void_reason='" + reason + "', " + 
				" void_time='" + dateTime.getTimeInMillis() + "' " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mSqlite.close();
		return isSuccess;
	}

	public OrderTransaction getTransaction(int transactionId, int computerId) {
		OrderTransaction trans = new OrderTransaction();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT a.transaction_id, a.computer_id, " + 
				" a.paid_time, a.receipt_year, a.receipt_month, a.receipt_id, " + 
				" b.document_type_header " + " FROM order_transaction a " + 
				" LEFT JOIN document_type b " + 
				" ON a.document_type_id = b.document_type_id " + 
				" WHERE a.transaction_id=" + transactionId + 
				" AND a.computer_id=" + computerId + 
				" AND a.transaction_status_id=2 " + 
				" ORDER BY a.sale_date");
		
		if (cursor.moveToFirst()) {
				String docTypeHeader = cursor.getString(cursor
						.getColumnIndex("document_type_header"));
				String receiptYear = String.format("%04d",
						cursor.getInt(cursor.getColumnIndex("receipt_year")));
				String receiptMonth = String.format("%02d",
						cursor.getInt(cursor.getColumnIndex("receipt_month")));
				String receiptId = String.format("%06d",
						cursor.getInt(cursor.getColumnIndex("receipt_id")));

				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex("computer_id")));
				trans.setPaidTime(cursor.getLong(cursor
						.getColumnIndex("paid_time")));
				trans.setReceiptNo(docTypeHeader + receiptMonth + receiptYear
						+ receiptId);
		}
		cursor.close();
		mSqlite.close();
		return trans;
	}

	public List<OrderTransaction> listTransaction(long saleDate) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT a.transaction_id, a.computer_id, " + 
				" a.paid_time, a.receipt_year, a.receipt_month, a.receipt_id, " + 
				" b.document_type_header " + " FROM order_transaction a " + 
				" LEFT JOIN document_type b " + 
				" ON a.document_type_id = b.document_type_id " + 
				" WHERE a.sale_date='" + saleDate + "' " + 
				" AND a.transaction_status_id=2 " + 
				" ORDER BY a.sale_date");
		if (cursor.moveToFirst()) {
			do {
				String docTypeHeader = cursor.getString(cursor
						.getColumnIndex("document_type_header"));
				String receiptYear = String.format("%04d",
						cursor.getInt(cursor.getColumnIndex("receipt_year")));
				String receiptMonth = String.format("%02d",
						cursor.getInt(cursor.getColumnIndex("receipt_month")));
				String receiptId = String.format("%06d",
						cursor.getInt(cursor.getColumnIndex("receipt_id")));

				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex("computer_id")));
				trans.setPaidTime(cursor.getLong(cursor
						.getColumnIndex("paid_time")));
				trans.setReceiptNo(docTypeHeader + receiptMonth + receiptYear
						+ receiptId);

				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return transLst;
	}

	public OrderTransaction.OrderDetail getSummaryTmp(int transactionId,
			int computerId) {
		OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT SUM(order_qty) AS TotalQty," + 
				" SUM(total_retail_price) AS SubTotal, " + 
				" SUM(total_sale_price) AS TotalPrice, " + 
				" SUM(vat) AS TotalVat, " + 
				" SUM(price_discount) AS TotalPriceDiscount," + 
				" SUM(member_discount) AS TotalMemberDiscount " + 
				" FROM order_detail_tmp " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			orderDetail.setQty(cursor.getFloat(cursor
					.getColumnIndex("TotalQty")));
			orderDetail.setTotalRetailPrice(cursor.getFloat(cursor
					.getColumnIndex("SubTotal")));
			orderDetail.setTotalSalePrice(cursor.getFloat(cursor
					.getColumnIndex("TotalPrice")));
			orderDetail.setVat(cursor.getFloat(cursor
					.getColumnIndex("TotalVat")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor
					.getColumnIndex("TotalPriceDiscount")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor
					.getColumnIndex("TotalMemberDiscount")));
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();
		return orderDetail;
	}

	public OrderTransaction.OrderDetail getSummary(int transactionId,
			int computerId) {
		OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT SUM(order_qty) AS TotalQty," + 
				" SUM(total_retail_price) AS SubTotal, " + 
				" SUM(total_sale_price) AS TotalPrice, " + 
				" SUM(vat) AS TotalVat, " + 
				" SUM(price_discount) AS TotalPriceDiscount," + 
				" SUM(member_discount) AS TotalMemberDiscount " + 
				" FROM order_detail " + 
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			orderDetail.setQty(cursor.getFloat(cursor
					.getColumnIndex("TotalQty")));
			orderDetail.setTotalRetailPrice(cursor.getFloat(cursor
					.getColumnIndex("SubTotal")));
			orderDetail.setTotalSalePrice(cursor.getFloat(cursor
					.getColumnIndex("TotalPrice")));
			orderDetail.setVat(cursor.getFloat(cursor
					.getColumnIndex("TotalVat")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor
					.getColumnIndex("TotalPriceDiscount")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor
					.getColumnIndex("TotalMemberDiscount")));
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();
		return orderDetail;
	}

	public OrderTransaction.OrderDetail getOrder(int transactionId,
			int computerId, int orderDetailId) {
		OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT a.order_detail_id, " + 
				" a.product_id, b.product_name, a.order_qty, a.unit_price, " + 
				" a.vat_type, a.member_discount, a.price_discount " + 
				" FROM order_detail a " +
				" LEFT JOIN products b " +
				" ON a.product_id=b.product_id " + 
				" WHERE a.transaction_id=" + transactionId + 
				" AND a.computer_id=" + computerId + 
				" AND a.order_detail_id=" + orderDetailId);
		if (cursor.moveToFirst()) {
			orderDetail.setOrderDetailId(cursor.getInt(cursor
					.getColumnIndex("order_detail_id")));
			orderDetail.setProductId(cursor.getInt(cursor
					.getColumnIndex("product_id")));
			orderDetail.setProductName(cursor.getString(cursor
					.getColumnIndex("product_name")));
			orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("order_qty")));
			orderDetail.setPricePerUnit(cursor.getFloat(cursor
					.getColumnIndex("unit_price")));
			orderDetail.setVatType(cursor.getInt(cursor
					.getColumnIndex("vat_type")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor
					.getColumnIndex("member_discount")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor
					.getColumnIndex("price_discount")));
		}
		cursor.close();
		mSqlite.close();
		return orderDetail;
	}

	public List<OrderTransaction.OrderDetail> listAllOrdersTmp(
			int transactionId, int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = new ArrayList<OrderTransaction.OrderDetail>();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT a.order_detail_id, a.product_id," +
				" b.product_name, a.order_qty, a.unit_price, a.total_retail_price, " +
				" a.total_sale_price, a.vat_type, a.member_discount, a.price_discount, " +
				" a.discount_type " +
				" FROM order_detail_tmp a" +
				" LEFT JOIN products b " +
				" ON a.product_id=b.product_id " + 
				" WHERE a.transaction_id="+ transactionId + 
				" AND a.computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getInt(cursor
						.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor
						.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor
						.getColumnIndex("product_name")));
				orderDetail
						.setQty(cursor.getFloat(cursor.getColumnIndex("order_qty")));
				orderDetail.setPricePerUnit(cursor.getFloat(cursor
						.getColumnIndex("unit_price")));
				orderDetail.setTotalRetailPrice(cursor.getFloat(cursor
						.getColumnIndex("total_retail_price")));
				orderDetail.setTotalSalePrice(cursor.getFloat(cursor
						.getColumnIndex("total_sale_price")));
				orderDetail.setVatType(cursor.getInt(cursor
						.getColumnIndex("vat_type")));
				orderDetail.setMemberDiscount(cursor.getFloat(cursor
						.getColumnIndex("member_discount")));
				orderDetail.setPriceDiscount(cursor.getFloat(cursor
						.getColumnIndex("price_discount")));
				orderDetail.setDiscountType(cursor.getInt(cursor
						.getColumnIndex("discount_type")));

				orderDetailLst.add(orderDetail);
			} while (cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();

		return orderDetailLst;
	}

	public List<OrderTransaction.OrderDetail> listAllOrders(int transactionId,
			int computerId) {
		List<OrderTransaction.OrderDetail> orderDetailLst = 
				new ArrayList<OrderTransaction.OrderDetail>();

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT a.order_detail_id, " +
				" a.product_id, b.product_name, a.order_qty, a.unit_price, " +
				" a.total_retail_price, a.total_sale_price, " +
				" a.vat_type, a.member_discount, a.price_discount " +
				" FROM order_detail a" +
				" LEFT JOIN products b" +
				" ON a.product_id=b.product_id " +
				" WHERE a.transaction_id=" + transactionId +
				" AND a.computer_id=" + computerId);
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction.OrderDetail orderDetail = new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getInt(cursor
						.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor
						.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor
						.getColumnIndex("product_name")));
				orderDetail
						.setQty(cursor.getFloat(cursor.getColumnIndex("order_qty")));
				orderDetail.setPricePerUnit(cursor.getFloat(cursor
						.getColumnIndex("unit_price")));
				orderDetail.setTotalRetailPrice(cursor.getFloat(cursor
						.getColumnIndex("total_retail_price")));
				orderDetail.setTotalSalePrice(cursor.getFloat(cursor
						.getColumnIndex("total_sale_price")));
				orderDetail.setVatType(cursor.getInt(cursor
						.getColumnIndex("vat_type")));
				orderDetail.setMemberDiscount(cursor.getFloat(cursor
						.getColumnIndex("member_discount")));
				orderDetail.setPriceDiscount(cursor.getFloat(cursor
						.getColumnIndex("price_discount")));

				orderDetailLst.add(orderDetail);
			} while (cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();

		return orderDetailLst;
	}

	public List<OrderTransaction> listHoldOrder(int computerId) {
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();

		Calendar c = getDate();

		mSqlite.open();

		Cursor cursor = mSqlite.rawQuery("SELECT a.transaction_id, a.computer_id, " + 
				" a.open_time, a.remark, b.staff_code, b.staff_name " + 
				" FROM order_transaction a " +
				" LEFT JOIN staffs b " + 
				" ON a.open_staff_id=b.staff_id  " +
				" WHERE a.computer_id=" + computerId + 
				" AND a.sale_date='" + c.getTimeInMillis() + "' " + 
				" AND a.transaction_status_id=9");
		if (cursor.moveToFirst()) {
			do {
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor
						.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor
						.getColumnIndex("computer_id")));
				trans.setRemark(cursor.getString(cursor
						.getColumnIndex("remark")));
				trans.setOpenTime(cursor.getLong(cursor
						.getColumnIndex("open_time")));
				trans.setStaffName(cursor.getString(cursor
						.getColumnIndex("staff_code"))
						+ ":"
						+ cursor.getString(cursor.getColumnIndex("staff_name")));
				transLst.add(trans);
			} while (cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();

		return transLst;
	}
}
