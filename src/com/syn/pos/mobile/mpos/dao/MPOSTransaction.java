package com.syn.pos.mobile.mpos.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.model.OrderTransaction.OrderDetail;
import com.syn.pos.mobile.mpos.Formatter;

public class MPOSTransaction extends Vat implements IMPOSTransaction, IOrderDetail {
	
	private ISqliteHelper dbHelper;
	private Formatter format;
	
	public MPOSTransaction (Context c, Formatter formatter){
		dbHelper = new MPOSSqliteHelper(c);
		format = formatter;
	}
	
	@Override
	public long getMaxTransaction(int computerId) {
		long transactionId = 0;
		
		String strSql = "SELECT MAX(transaction_id) FROM order_transaction " +
				" WHERE computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			transactionId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return transactionId + 1;
	}

	@Override
	public long openTransaction(int computerId, int shopId,
			int sessionId, int staffId) {
		
		Date date = new Date();
		long transactionId = getMaxTransaction(computerId);
		long receiptId = getMaxReceiptId(transactionId, computerId, date.getYear(), date.getMonth());
		
		ContentValues cv = new ContentValues();
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("shop_id", shopId);
		cv.put("session_id", sessionId);
		cv.put("open_staff_id", staffId);
		cv.put("open_time", format.dateTimeFormat(date));
		cv.put("open_staff_id", staffId);
		cv.put("sale_date", format.dateFormat(date));
		cv.put("receipt_year", date.getYear());
		cv.put("receipt_month", date.getMonth());
		cv.put("receipt_id", receiptId);
		
		dbHelper.open();
		
		if(!dbHelper.insert("order_transaction", cv))
			transactionId = 0;
		
		dbHelper.close();
		
		return transactionId;
	}

	@Override
	public void updateTransaction(long transactionId, int computerId,
			int staffId, double transVat, double transExclVat,
			double serviceCharge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelTransaction(long transactionId) {
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM order_transaction WHERE transaction_id=" + transactionId);
		dbHelper.execSQL("DELETE FROM order_detail WHERE transaction_id=" + transactionId);
		dbHelper.close();
	}

	@Override
	public long getMaxOrderDetail(long transactionId, int computerId) {
		long orderDetailId = 0;
		
		String strSql = "SELECT MAX(order_detail_id) FROM order_detail";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderDetailId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return orderDetailId + 1;
	}

	@Override
	public long addOrderDetail(long transactionId, int computerId,
			int productId, int productType, int vatType, double serviceCharge,
			String productName, double productAmount, double productPrice) {
		
		long orderDetailId = getMaxOrderDetail(transactionId, computerId);
		double vat = calculateVat(productPrice, productAmount, 0.07);
		
		ContentValues cv = new ContentValues();
		cv.put("order_detail_id", orderDetailId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("product_id", productId);
		cv.put("product_name", productName);
		cv.put("product_amount", productAmount);
		cv.put("product_price", productPrice);
		cv.put("vat", vat);
		
		dbHelper.open();
		if(!dbHelper.insert("order_detail", cv))
			orderDetailId = 0;
		dbHelper.close();
		
		return orderDetailId;
	}

	@Override
	public boolean deleteOrderDetail(long transactionId, long orderDetailId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getMaxReceiptId(long transactionId, int computerId, int year, int month) {
		long maxReceiptId = 0;
		
		String strSql = "SELECT MAX(receipt_id) FROM order_transaction " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId +
				" AND receipt_year=" + year + 
				" AND receipt_month=" + month;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			maxReceiptId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return maxReceiptId + 1;
	}

	@Override
	public long getCurrTransaction(int computerId) {
		long transactionId = 0;
		
		String strSql = "SELECT transaction_id FROM order_transaction " +
				" WHERE computer_id = " + computerId + 
				" AND transaction_status_id = 1";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			if(cursor.getLong(0) != 0)
				transactionId = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		return transactionId;
	}
	
	public OrderTransaction.OrderDetail getOrder(long transactionId, int computerId, long orderDetailId){
		OrderTransaction.OrderDetail orderDetail =
				new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT order_detail_id, product_id, " +
				" product_name, product_amount, product_price, " +
				" vat, exclude_vat, member_discount, each_product_discount," +
				" service_charge " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId + 
				" AND order_detail_id=" + orderDetailId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){	
			orderDetail.setOrderDetailId(cursor.getLong(cursor.getColumnIndex("order_detail_id")));
			orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
			orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			orderDetail.setProductAmount(cursor.getDouble(cursor.getColumnIndex("product_amount")));
			orderDetail.setProductPrice(cursor.getDouble(cursor.getColumnIndex("product_price")));
			orderDetail.setVat(cursor.getDouble(cursor.getColumnIndex("vat")));
			orderDetail.setExcludeVat(cursor.getDouble(cursor.getColumnIndex("exclude_vat")));
			orderDetail.setMemberDiscount(cursor.getDouble(cursor.getColumnIndex("member_discount")));
			orderDetail.setEachProductDiscount(cursor.getDouble(cursor.getColumnIndex("each_product_discount")));
			orderDetail.setServiceCharge(cursor.getDouble(cursor.getColumnIndex("service_charge")));
		}
		cursor.close();
		dbHelper.close();
		return orderDetail;
	}
	
	public List<OrderTransaction.OrderDetail> listAllOrders(long transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderLst =
			new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, " +
				" product_name, product_amount, product_price," +
				" vat, exclude_vat, member_discount, each_product_discount," +
				" service_charge " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction.OrderDetail orderDetail = 
						new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getLong(cursor.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				orderDetail.setProductAmount(cursor.getDouble(cursor.getColumnIndex("product_amount")));
				orderDetail.setProductPrice(cursor.getDouble(cursor.getColumnIndex("product_price")));
				orderDetail.setVat(cursor.getDouble(cursor.getColumnIndex("vat")));
				orderDetail.setExcludeVat(cursor.getDouble(cursor.getColumnIndex("exclude_vat")));
				orderDetail.setMemberDiscount(cursor.getDouble(cursor.getColumnIndex("member_discount")));
				orderDetail.setEachProductDiscount(cursor.getDouble(cursor.getColumnIndex("each_product_discount")));
				orderDetail.setServiceCharge(cursor.getDouble(cursor.getColumnIndex("service_charge")));
				
				orderLst.add(orderDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();
		
		return orderLst;
	}

	@Override
	public OrderTransaction.OrderDetail getSummary(long transactionId) {
		OrderTransaction.OrderDetail order = 
				new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT SUM(product_amount) AS TotalAmount," +
				" SUM(product_price) AS TotalPrice, SUM(vat) AS TotalVat "+
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			order.setProductAmount(cursor.getDouble(cursor.getColumnIndex("TotalAmount")));
			order.setProductPrice(cursor.getDouble(cursor.getColumnIndex("TotalPrice")));
			order.setVat(cursor.getDouble(cursor.getColumnIndex("TotalVat")));
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		return order;
	}

	@Override
	public boolean deleteAllOrderDetail(long transactionId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void payment() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discount() {
		// TODO Auto-generated method stub
		
	}

}
