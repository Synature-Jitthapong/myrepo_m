package com.syn.pos.mobile.mpos.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.Formatter;

public class MPOSTransaction extends POSUtil implements IMPOSTransaction, IOrderDetail {
	
	private ISqliteHelper dbHelper;
	private Formatter format;
	
	public MPOSTransaction (Context c, Formatter formatter){
		dbHelper = new MPOSSqliteHelper(c);
		format = formatter;
	}
	
	@Override
	public int getMaxTransaction(int computerId) {
		int transactionId = 0;
		
		String strSql = "SELECT MAX(transaction_id) FROM order_transaction " +
				" WHERE computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return transactionId + 1;
	}

	@Override
	public int openTransaction(int computerId, int shopId,
			int sessionId, int staffId) {
		
		Date date = new Date();
		int transactionId = getMaxTransaction(computerId);
		
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
		
		dbHelper.open();
		
		if(!dbHelper.insert("order_transaction", cv))
			transactionId = 0;
		
		dbHelper.close();
		
		return transactionId;
	}

	@Override
	public void updateTransaction(int transactionId, int computerId,
			int staffId, float transVat, float transExclVat,
			float serviceCharge, float serviceChargeVat) {

		String strSql = "";
	}

	@Override
	public void cancelTransaction(int transactionId) {
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM order_transaction WHERE transaction_id=" + transactionId);
		dbHelper.execSQL("DELETE FROM order_detail WHERE transaction_id=" + transactionId);
		dbHelper.close();
	}

	@Override
	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;
		
		String strSql = "SELECT MAX(order_detail_id) FROM order_detail";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderDetailId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return orderDetailId + 1;
	}

	@Override
	public int addOrderDetail(int transactionId, int computerId,
			int productId, int productType, int vatType, float serviceCharge,
			String productName, float productAmount, float productPrice) {
		
		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
		
		ContentValues cv = new ContentValues();
		cv.put("order_detail_id", orderDetailId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("product_id", productId);
		cv.put("product_name", productName);
		cv.put("product_amount", productAmount);
		cv.put("product_price", productPrice);
		
		dbHelper.open();
		if(!dbHelper.insert("order_detail", cv))
			orderDetailId = 0;
		dbHelper.close();
		
		return orderDetailId;
	}

	@Override
	public boolean deleteOrderDetail(int transactionId, int orderDetailId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxReceiptId(int transactionId, int computerId, int year, int month) {
		int maxReceiptId = 0;
		
		String strSql = "SELECT MAX(receipt_id) FROM order_transaction " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId +
				" AND receipt_year=" + year + 
				" AND receipt_month=" + month + 
				" AND document_status_id = 2";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return maxReceiptId + 1;
	}

	@Override
	public int getCurrTransaction(int computerId) {
		int transactionId = 0;
		
		String strSql = "SELECT transaction_id FROM order_transaction " +
				" WHERE computer_id = " + computerId + 
				" AND transaction_status_id = 1";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			if(cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		return transactionId;
	}
	
	public OrderTransaction.OrderDetail getOrder(int transactionId, int computerId, int orderDetailId){
		OrderTransaction.OrderDetail orderDetail = 
				new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT order_detail_id, " +
				" product_id, product_name, product_amount, product_price, " +
				" member_discount, each_product_discount " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId + 
				" AND order_detail_id=" + orderDetailId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex("order_detail_id")));
			orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
			orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			orderDetail.setProductAmount(cursor.getFloat(cursor.getColumnIndex("product_amount")));
			orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex("product_price")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("member_discount")));
			orderDetail.setEachProductDiscount(cursor.getFloat(cursor.getColumnIndex("each_product_discount")));
		}
		cursor.close();
		dbHelper.close();
		return orderDetail;
	}
	
	public List<OrderTransaction.OrderDetail> listAllOrders(long transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderDetailLst 
				= new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, product_name, " +
				" product_amount, product_price, " +
				" member_discount, each_product_discount " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction.OrderDetail orderDetail = 
						new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				orderDetail.setProductAmount(cursor.getFloat(cursor.getColumnIndex("product_amount")));
				orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex("product_price")));
				orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("member_discount")));
				orderDetail.setEachProductDiscount(cursor.getFloat(cursor.getColumnIndex("each_product_discount")));
				
				orderDetailLst.add(orderDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();
		
		return orderDetailLst;
	}

	@Override
	public OrderTransaction getSummary(int transactionId) {
		OrderTransaction orderTrans = 
				new OrderTransaction();
		
		String strSql = "SELECT a.transaction_vat, a.transaction_exclude_vat " +
				" a.service_charge, a.service_charge_vat, SUM(b.product_amount) AS TotalAmount," +
				" SUM(b.product_price) AS TotalPrice "+
				" FROM order_transaction a " +
				" INNER JOIN order_detail b " +
				" ON a.transaction_id=b.transaction_id " +
				" AND a.computer_id=b.computer_id " +
				" WHERE a.transaction_id=" + transactionId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderTrans.setTransactionVat(cursor.getFloat(cursor.getColumnIndex("transaction_vat")));
			orderTrans.setTransactionVatExclude(cursor.getFloat(cursor.getColumnIndex("transaction_exclude_vat")));
			orderTrans.setServiceCharge(cursor.getFloat(cursor.getColumnIndex("service_charge")));
			orderTrans.setServiceChargeVat(cursor.getFloat(cursor.getColumnIndex("service_charge_vat")));
			
			orderTrans.orderDetail = new OrderTransaction.OrderDetail();
			orderTrans.orderDetail.setProductAmount(cursor.getFloat(cursor.getColumnIndex("TotalAmount")));
			orderTrans.orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
			
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		return orderTrans;
	}

	@Override
	public boolean deleteAllOrderDetail(int transactionId) {
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
