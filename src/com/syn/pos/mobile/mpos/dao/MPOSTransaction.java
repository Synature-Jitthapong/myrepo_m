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

public class MPOSTransaction implements IMPOSTransaction, IOrderDetail {
	
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
	public void closeTransaction() {
		// TODO Auto-generated method stub
		
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
			int productId, String productName, double productAmount, double productPrice) {
		
		long orderDetailId = getMaxOrderDetail(transactionId, computerId);
		
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
	
	public OrderTransaction getOrder(long transactionId, int computerId, long orderDetailId){
		OrderTransaction trans =
				new OrderTransaction();
		
		String strSql = "SELECT a.transaction_id, a.computer_id, " +
				" a.transaction_vat, a.service_charge, " +
				" b.order_detail_id, b.product_id, b.product_name, b.product_amount, " +
				" b.product_price " +
				" FROM order_transaction a " +
				" INNER JOIN order_detail b " +
				" ON a.transaction_id = b.transaction_id " +
				" AND a.computer_id = b.computer_id " +
				" WHERE a.transaction_id=" + transactionId + 
				" AND a.computer_id=" + computerId + 
				" AND b.order_detail_id=" + orderDetailId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			trans.orderDetail =
					 new OrderTransaction.OrderDetail();	
			trans.orderDetail.setOrderDetailId(cursor.getLong(cursor.getColumnIndex("order_detail_id")));
			trans.orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
			trans.orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			trans.orderDetail.setProductAmount(cursor.getDouble(cursor.getColumnIndex("product_amount")));
			trans.orderDetail.setProductPrice(cursor.getDouble(cursor.getColumnIndex("product_price")));
		
		}
		cursor.close();
		dbHelper.close();
		return trans;
	}
	
	public OrderTransaction listAllOrders(long transactionId, int computerId){
		OrderTransaction trans = new OrderTransaction();
		
		String strSql = "SELECT a.transaction_id, a.computer_id, " +
				" a.transaction_vat, a.service_charge, " +
				" b.order_detail_id, b.product_id, b.product_name, b.product_amount, " +
				" b.product_price " +
				" FROM order_transaction a " +
				" INNER JOIN order_detail b " +
				" ON a.transaction_id = b.transaction_id " +
				" AND a.computer_id = b.computer_id" +
				" WHERE a.transaction_id=" + transactionId + 
				" AND a.computer_id=" + computerId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		trans.orderDetailLst =
				new ArrayList<OrderTransaction.OrderDetail>();
		if(cursor.moveToFirst()){
			do{
				trans.setTransactionId(cursor.getLong(cursor.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex("computer_id")));
				
				OrderTransaction.OrderDetail od = 
						new OrderTransaction.OrderDetail();
				od.setOrderDetailId(cursor.getLong(cursor.getColumnIndex("order_detail_id")));
				od.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				od.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				od.setProductAmount(cursor.getDouble(cursor.getColumnIndex("product_amount")));
				od.setProductPrice(cursor.getDouble(cursor.getColumnIndex("product_price")));
				
				trans.orderDetailLst.add(od);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();
		
		return trans;
	}

}
