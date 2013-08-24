package com.syn.mpos.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.syn.mpos.model.OrderTransaction;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSTransaction extends Util implements Transaction, Order, Payment{
	
	private MPOSSQLiteHelper dbHelper;
	
	public MPOSTransaction (Context c){
		dbHelper = new MPOSSQLiteHelper(c);
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
	public boolean successTransaction(int transactionId, int computerId){
		boolean isSuccess = false;
		String strSql = "UPDATE order_transaction " +
				" SET transaction_status_id=2 " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSuccess;
	}
	
	@Override
	public boolean updateTransaction(int transactionId, int computerId,
			int staffId, float transVat, float transExclVat,
			float serviceCharge, float serviceChargeVat) {

		boolean isSuccess = false;
		String strSql = "UPDATE order_transaction SET " +
				" transaction_vat=" + transVat + ", " +
				" transaction_exclude_vat=" + transExclVat + ", " +
				" service_charge=" + serviceCharge + ", " + 
				" service_charge_vat=" + serviceChargeVat;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean deleteTransaction(int transactionId, int computerId){
		boolean isSuccess = false;
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM order_transaction " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		dbHelper.close();
		return isSuccess;
	}
	
	public void cancelTransaction(int transactionId, int computerId) {
		deleteTransaction(transactionId, computerId);
		deleteAllOrderDetail(transactionId, computerId);
		deleteAllPaymentDetail(transactionId, computerId);
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
		cv.put("sale_price", productPrice);
		cv.put("total_product_price", productPrice);
		cv.put("total_sale_price", productPrice);
		cv.put("vat_type", vatType);
		
		if(vatType == 1)
			cv.put("vat", calculateVat(productPrice, productAmount, 7));
		else if(vatType == 2)
			cv.put("vat_exclude", calculateVat(productPrice, productAmount, 7));
		
		dbHelper.open();
		if(!dbHelper.insert("order_detail", cv))
			orderDetailId = 0;
		dbHelper.close();
		
		return orderDetailId;
	}

	@Override
	public boolean deleteOrderDetail(int transactionId, int computerId, int orderDetailId) {
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail " +
				" WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		
		return isSuccess;
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
	
	public List<OrderTransaction.OrderDetail> listAllOrdersTmp(int transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderDetailLst 
				= new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, product_name, " +
				" product_amount, product_price, sale_price, " +
				" total_product_price AS TotalPrice, " +
				" total_sale_price AS TotalSalePrice, vat_type, " +
				" member_discount, each_product_discount " +
				" FROM order_detail_tmp " +
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
				orderDetail.setSalePrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
				orderDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
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

	
	public List<OrderTransaction.OrderDetail> listAllOrders(int transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderDetailLst 
				= new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, product_name, " +
				" product_amount, product_price, sale_price, " +
				" total_product_price AS TotalPrice, " +
				" total_sale_price AS TotalSalePrice, vat_type, " +
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
				orderDetail.setSalePrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
				orderDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
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

	public OrderTransaction.OrderDetail getSummaryTmp(int transactionId) {
		OrderTransaction.OrderDetail orderDetail = 
					new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT SUM(product_amount) AS TotalAmount," +
				" SUM(total_product_price) AS TotalPrice, " +
				" SUM(total_sale_price) AS TotalSalePrice, " +
				" SUM(vat) AS TotalVat," +
				" SUM(vat_exclude) AS TotalVatExclude, " +
				" SUM(service_charge) AS TotalServiceCharge," +
				" SUM(service_charge_vat) AS TotalServiceChargeVat, " +
				" SUM(each_product_discount) AS TotalProductDiscount," +
				" SUM(member_discount) AS TotalMemberDiscount "+
				" FROM order_detail_tmp " +
				" WHERE transaction_id=" + transactionId;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){			
			orderDetail.setProductAmount(cursor.getFloat(cursor.getColumnIndex("TotalAmount")));
			orderDetail.setProductPrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
			orderDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
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
	
	public OrderTransaction.OrderDetail getSummary(int transactionId) {
		OrderTransaction.OrderDetail orderDetail = 
					new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT SUM(product_amount) AS TotalAmount," +
				" SUM(total_product_price) AS TotalPrice, " +
				" SUM(total_sale_price) AS TotalSalePrice, " +
				" SUM(vat) AS TotalVat," +
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
			orderDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
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
	public boolean deleteAllOrderDetail(int transactionId, int computerId) {
		boolean isSuccess = false;
		dbHelper.open();
		isSuccess = dbHelper.execSQL("DELETE FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		dbHelper.close();
		return isSuccess;
	}

	private boolean deleteOrderDetail(int transactionId, int computerId){
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		
		return isSuccess;
	}
	
	private boolean deleteOrderDetailTmp(int transactionId, int computerId){
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail_tmp " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		
		return isSuccess;
	}
	
	@Override
	public boolean cancelDiscount(int transactionId, int computerId) {
		return deleteOrderDetailTmp(transactionId, computerId);
	}

	@Override
	public boolean confirmDiscount(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		if(deleteOrderDetail(transactionId, computerId)){
			String strSql = "INSERT INTO order_detail " +
					" SELECT * FROM order_detail_tmp " +
					" WHERE transaction_id=" + transactionId + 
					" AND computer_id=" + computerId;
			
			dbHelper.open();
			isSuccess = dbHelper.execSQL(strSql);
			dbHelper.close();
		}
		return isSuccess;
	}

	@Override
	public boolean copyOrderToTmp(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		if(deleteOrderDetailTmp(transactionId, computerId)){
			String strSql = "INSERT INTO order_detail_tmp " +
					" SELECT * FROM order_detail " +
					" WHERE transaction_id=" + transactionId + 
					" AND computer_id=" + computerId;
			
			dbHelper.open();
			isSuccess = dbHelper.execSQL(strSql);
			dbHelper.close();
		}
		return isSuccess;
	}

	@Override
	public boolean discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, int vatType, float amount, float discount, 
			float salePrice, float totalSalePrice) {
		boolean isSuccess = false;
		
		float vat = calculateVat(salePrice, amount, 7);
		
		String strSql = "UPDATE order_detail_tmp SET " +
				" each_product_discount=" + discount + ", " + 
				" sale_price=" + salePrice + ", " +
				" total_sale_price=" + totalSalePrice;
		
		if(vatType == 1)
			strSql += ", vat=" + vat;
		else if(vatType == 2)
			strSql += ", vat_exclude=" + vat;
		
		strSql += " WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;

		dbHelper.open();		
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		
		return isSuccess;
	}

	@Override
	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount, float productPrice) {
		boolean isSucc = false;
		
		float vat = calculateVat(productPrice, productAmount, 7);
		float totalProductPrice = productPrice * productAmount;
		
		String strSql = "UPDATE order_detail SET " +
				" product_amount=" + productAmount + ", " +
				" product_price=" + productPrice + ", " +
				" sale_price=" + productPrice + ", " +
				" total_sale_price=" + totalProductPrice + ", " +
				" total_product_price=" + totalProductPrice + ", " +
				" each_product_discount=0";
		
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
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		dbHelper.open();
		
		String strSql = "SELECT MAX(pay_detail_id) " +
				" FROM payment_detail " +
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
			int computerId, int payTypeId, float payAmount,
			String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId) {
		boolean isSuccess = false;
		int paymentId = getMaxPaymentDetailId(transactionId, computerId);
		
		ContentValues cv = new ContentValues();
		cv.put("pay_detail_id", paymentId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("pay_type_id", payTypeId);
		cv.put("pay_amount", payAmount);
		cv.put("credit_card_no", creditCardNo);
		cv.put("expire_month", expireMonth);
		cv.put("expire_year", expireYear);
		cv.put("bank_id", bankId);
		cv.put("credit_card_type", creditCardTypeId);
		
		dbHelper.open();
		isSuccess = dbHelper.insert("payment_detail", cv);
		dbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean updatePaymentDetail(int transactionId, int computerId, int payTypeId,
			float paymentAmount, String creditCardNo, int expireMonth,
			int expireYear, int bankId, int creditCardTypeId) {
		boolean isSuccess = false;
		
		String strSql = "UPDATE payment_detail SET " +
				" pay_type_id=" + payTypeId + ", " +
				" pay_amount=" + paymentAmount + ", " +
				" credit_card_no='" + creditCardNo + "', " +
				" expire_month=" + expireMonth + ", " +
				" expire_year=" + expireYear + ", " +
				" bank_id='" + bankId + "', " + 
				" credit_card_type=" + creditCardTypeId +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean deletePaymentDetail(int paymentId) {
		String strSql = "DELETE FROM payment_detail " +
				" WHERE pay_detail_id=" + paymentId;
		
		boolean isSuccess = false;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL(strSql);
		dbHelper.close();
		
		return isSuccess;
	}

	public float getTotalPaid(int transactionId, int computerId){
		float totalPaid = 0.0f;
	
		String strSql = "SELECT SUM(pay_amount) " +
				" FROM payment_detail " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		dbHelper.open();
		
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			totalPaid = cursor.getFloat(0);
		}
		cursor.close();
				
		dbHelper.close();
		
		return totalPaid;
	}
	
	public List<com.syn.mpos.model.Payment.PaymentDetail> listPayment(int transactionId, int computerId){
		List<com.syn.mpos.model.Payment.PaymentDetail> paymentLst = 
				new ArrayList<com.syn.mpos.model.Payment.PaymentDetail>();
		
		String strSql = "SELECT a.*, b.pay_type_code, b.pay_type_name " +
				" FROM payment_detail a " +
				" LEFT JOIN pay_type b " + 
				" ON a.pay_type_id=b.pay_type_id " +
				" WHERE a.transaction_id=" + transactionId +
				" AND a.computer_id=" + computerId;
		
		dbHelper.open();
		
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				com.syn.mpos.model.Payment.PaymentDetail payDetail
					= new com.syn.mpos.model.Payment.PaymentDetail();
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex("pay_detail_id")));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex("pay_type_id")));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex("pay_type_code")));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex("pay_type_name")));
				payDetail.setPayAmount(cursor.getFloat(cursor.getColumnIndex("pay_amount")));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();
		
		return paymentLst;
	}

	@Override
	public boolean deleteAllPaymentDetail(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL("DELETE FROM payment_detail " +
				" WHERE transactionId=" + transactionId + 
				" AND computerId=" + computerId);
		dbHelper.close();
		
		return isSuccess;
	}

	@Override
	public boolean holdTransaction(int transactionId, int computerId, String remark) {
		boolean isSuccess = false;
		
		dbHelper.open();
		isSuccess = dbHelper.execSQL("UPDATE order_transaction SET transaction_status_id = 9, " +
				" remark='" + remark + "' " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		dbHelper.close();
		return isSuccess;
	}
}
