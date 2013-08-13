package com.syn.pos.mobile.mpos.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.Formatter;

public class MPOSTransaction extends POSUtil implements IMPOSTransaction, IOrderDetail, IPaymentDetail {
	
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
		cv.put("open_time", date.getTime());
		cv.put("open_staff_id", staffId);
		cv.put("sale_date", date.getTime());
		cv.put("receipt_year", Calendar.getInstance().get(Calendar.YEAR));
		cv.put("receipt_month", Calendar.getInstance().get(Calendar.MONTH));
		
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
		cv.put("vat_type", vatType);
		
		if(vatType == 1)
			cv.put("vat", calculateVat(productPrice, productAmount, 7).toString());
		else if(vatType == 2)
			cv.put("vat_exclude", calculateVat(productPrice, productAmount, 7).toString());
		
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
				" vat_type, member_discount, each_product_discount " +
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
			orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
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
				" product_amount, product_price, vat_type, " +
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
				orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
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
	public OrderTransaction.OrderDetail getSummary(int transactionId) {
		OrderTransaction.OrderDetail orderDetail = 
					new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT SUM(product_amount) AS TotalAmount," +
				" SUM(product_price * product_amount) AS TotalPrice, SUM(vat) AS TotalVat," +
				" SUM(vat_exclude) AS TotalVatExclude, " +
				" SUM(service_charge) AS TotalServiceCharge," +
				" SUM(service_charge_vat) AS TotalServiceChargeVat, " +
				" SUM(each_product_discount) AS TotalProductDiscount," +
				" SUM(member_discount) AS TotalMemberDiscount "+
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){			
			orderDetail.setProductAmount(cursor.getFloat(cursor.getColumnIndex("TotalAmount")));
			orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
			orderDetail.setVat(cursor.getFloat(cursor.getColumnIndex("TotalVat")));
			orderDetail.setVatExclude(cursor.getFloat(cursor.getColumnIndex("TotalVatExclude")));
			orderDetail.setServiceCharge(cursor.getFloat(cursor.getColumnIndex("TotalServiceCharge")));
			orderDetail.setServiceChargeVat(cursor.getFloat(cursor.getColumnIndex("TotalServiceChargeVat")));
			orderDetail.setEachProductDiscount(cursor.getFloat(cursor.getColumnIndex("TotalProductDiscount")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("TotalMemberDiscount")));
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		return orderDetail;
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

	@Override
	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount, float productPrice) {
		boolean isSucc = false;
		
		BigDecimal vat = calculateVat(productPrice, productAmount, 7);
		
		String strSql = "UPDATE order_detail SET product_amount=" + productAmount +
				", product_price=" + productPrice;
		
		if(vatType == 1)
			strSql += ", vat=" + vat;
		else if(vatType == 2)
			strSql += ", vat_exclude=" + vat;
		
		strSql +=" WHERE transaction_id=" + transactionId +
				" AND order_detail_id=" + orderDetailId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSucc = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSucc;
	}

	@Override
	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount, float productPrice,
			float eatchProductDiscount, float memberDiscount) {
		boolean isSucc = false;

		BigDecimal vat = calculateVat(productPrice, productAmount, 7);
		
		String strSql = "UPDATE order_detail SET product_amount=" + productAmount +
				", product_price=" + productPrice;
		
		if(vatType == 1)
			strSql += ", vat=" + vat;
		else if(vatType == 2)
			strSql += ", vat_exclude=" + vat;
		
		strSql += ", eatch_product_discount=" + eatchProductDiscount +
				", member_discount=" + memberDiscount +
				" WHERE transaction_id=" + transactionId +
				" AND order_detail_id=" + orderDetailId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSucc = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSucc;
	}

	@Override
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		dbHelper.open();
		
		String strSql = "SELECT MAX(payment_detail_id) " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		Cursor cursor = dbHelper.rawQuery(strSql);
		
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		
		dbHelper.close();
		return maxPaymentId + 1;
	}

	@Override
	public boolean addPaymentDetail(int transactionId,
			int computerId, int payTypeId) {
		boolean isSuccess = false;
		int paymentId = getMaxPaymentDetailId(transactionId, computerId);
		
		ContentValues cv = new ContentValues();
		cv.put("payment_detail_id", paymentId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("pay_type_id", payTypeId);
		
		dbHelper.open();
		isSuccess = dbHelper.insert("payment_detail", cv);
		dbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean updatePaymentDetail(int paymentId, int payTypeId,
			float paymentAmount, String creditCardNo, int expireMonth,
			int expireYear, String bankName, int creditCardTypeId) {
		boolean isSuccess = false;
		
		String strSql = "UPDATE payment_detail SET " +
				" pay_type_id=" + payTypeId + ", " +
				" payment_amount=" + paymentAmount + ", " +
				" credit_card_no='" + creditCardNo + "', " +
				" expire_month=" + expireMonth + ", " +
				" expire_year=" + expireYear + ", " +
				" bank_name='" + bankName + "', " + 
				" credit_card_type=" + creditCardTypeId +
				" WHERE payment_detail_id=" + paymentId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSuccess;
	}

	@Override
	public void deletePaymentDetail(int paymentId) {
		String strSql = "DELETE FROM payment_detail " +
				" WHERE payment_detail_id=" + paymentId;
		
		dbHelper.open();
		dbHelper.execSQL(strSql);
		dbHelper.close();
	}

}
