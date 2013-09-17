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
import com.syn.pos.Report;
import com.syn.pos.transaction.OrderCreation;
import com.syn.pos.transaction.TransactionCreation;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSTransaction extends Util implements TransactionCreation, OrderCreation{
	
	private SQLiteHelper mDbHelper;
	
	public MPOSTransaction(Context context) {
		mDbHelper = new MPOSSQLiteHelper(context);
	}

	@Override
	public int getMaxTransaction(int computerId) {
		int transactionId = 0;
		
		String strSql = "SELECT MAX(transaction_id) FROM order_transaction " +
				" WHERE computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		
		return transactionId + 1;
	}

	@Override
	public int openTransaction(int computerId, int shopId,
			int sessionId, int staffId) {

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
		cv.put("open_time", dateTime.getTimeInMillis());
		cv.put("open_staff_id", staffId);
		cv.put("sale_date", date.getTimeInMillis());
		cv.put("receipt_year", dateTime.get(Calendar.YEAR));
		cv.put("receipt_month", dateTime.get(Calendar.MONTH));
		
		mDbHelper.open();
		
		if(!mDbHelper.insert("order_transaction", cv))
			transactionId = 0;
		
		mDbHelper.close();
		
		return transactionId;
	}

	@Override
	public boolean successTransaction(int transactionId, int computerId, int staffId){
		boolean isSuccess = false;
		
		Calendar calendar = getDateTime();
		int receiptId = getMaxReceiptId(computerId, calendar.get(Calendar.YEAR), 
				calendar.get(Calendar.MONTH));
		
		String strSql = "UPDATE order_transaction " +
				" SET transaction_status_id=2, " +
				" receipt_id=" + receiptId + ", " +
				" close_time='" + calendar.getTimeInMillis() + "', " +
				" paid_time='" + calendar.getTimeInMillis() + "', " + 
				" paid_staff_id=" + staffId +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
	
	@Override
	public boolean deleteTransaction(int transactionId, int computerId){
		boolean isSuccess = false;
		mDbHelper.open();
		mDbHelper.execSQL("DELETE FROM order_transaction " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mDbHelper.close();
		return isSuccess;
	}

	@Override
	public int getMaxReceiptId(int computerId, int year, int month) {
		int maxReceiptId = 0;
		
		String strSql = "SELECT MAX(receipt_id) FROM order_transaction " +
				" WHERE computer_id=" + computerId +
				" AND receipt_year=" + year + 
				" AND receipt_month=" + month + 
				" AND transaction_status_id = 2";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			maxReceiptId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		
		return maxReceiptId + 1;
	}

	@Override
	public int getCurrTransaction(int computerId) {
		int transactionId = 0;
		
		String strSql = "SELECT transaction_id FROM order_transaction " +
				" WHERE computer_id = " + computerId + 
				" AND transaction_status_id = 1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			if(cursor.getLong(0) != 0)
				transactionId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		return transactionId;
	}
	
	@Override
	public boolean prepareTransaction(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		String strSql = "UPDATE order_transaction SET " +
				" transaction_status_id=1, remark='' " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
		return isSuccess;
	}

	@Override
	public boolean holdTransaction(int transactionId, int computerId, String remark) {
		boolean isSuccess = false;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("UPDATE order_transaction SET transaction_status_id = 9, " +
				" remark='" + remark + "' " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mDbHelper.close();
		return isSuccess;
	}
	
	@Override
	public boolean updateTransactionVat(int transactionId, int computerId, 
			float totalSalePrice, float vatExclude) {
		boolean isSuccess = false;
		float vat = calculateVat(totalSalePrice, 7);
		
		String strSql = "UPDATE order_transaction SET " +
				" transaction_vat=" + vat + ", " +
				" transaction_vatable=" + totalSalePrice + ", " +
				" transaction_exclude_vat=" + vatExclude +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	public OrderTransaction getTransaction(int transactionId, int computerId){
		OrderTransaction trans = new OrderTransaction();
		
		String strSql = "SELECT * FROM order_transaction " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			trans.setTransactionVatable(cursor.getFloat(cursor.getColumnIndex("transaction_vatable")));
			trans.setTransactionVat(cursor.getFloat(cursor.getColumnIndex("transaction_vat")));
			trans.setTransactionVatExclude(cursor.getFloat(cursor.getColumnIndex("transaction_exclude_vat")));
			trans.setSaleDate(cursor.getLong(cursor.getColumnIndex("sale_date")));
			trans.setReceiptYear(cursor.getInt(cursor.getColumnIndex("receipt_year")));
			trans.setReceiptMonth(cursor.getInt(cursor.getColumnIndex("receipt_month")));
			trans.setReceiptId(cursor.getInt(cursor.getColumnIndex("receipt_id")));
		}
		cursor.close();
		mDbHelper.close();
		return trans;
	}

	@Override
	public boolean confirmDiscount(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		if(deleteOrderDetail(transactionId, computerId)){
			String strSql = "INSERT INTO order_detail " +
					" SELECT * FROM order_detail_tmp " +
					" WHERE transaction_id=" + transactionId + 
					" AND computer_id=" + computerId;
			
			mDbHelper.open();
			isSuccess = mDbHelper.execSQL(strSql);
			mDbHelper.close();
		}
		return isSuccess;
	}

	@Override
	public boolean discountEatchProduct(int orderDetailId, int transactionId,
			int computerId, int vatType, float totalPrice, float discount) {
		boolean isSuccess = false;
		
		float priceAfterDiscount = totalPrice - discount;
		float vat = vatType == 2 ? calculateVat(priceAfterDiscount, 7) : 0.0f;
		
		String strSql = "UPDATE order_detail_tmp SET " +
				" price_discount=" + discount + ", " +
				" total_sale_price=" + priceAfterDiscount + ", " +
				" vat=" + vat + 
				" WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;

		mDbHelper.open();		
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
		return isSuccess;
	}

	@Override
	public boolean deleteAllOrderDetail(int transactionId, int computerId) {
		boolean isSuccess = false;
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DELETE FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mDbHelper.close();
		return isSuccess;
	}

	private boolean deleteOrderDetail(int transactionId, int computerId){
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
		return isSuccess;
	}
	
	@Override
	public boolean deleteOrderDetail(int transactionId, int computerId, int orderDetailId) {
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail " +
				" WHERE order_detail_id=" + orderDetailId + 
				" AND transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
		return isSuccess;
	}
	
	@Override
	public int getMaxOrderDetail(int transactionId, int computerId) {
		int orderDetailId = 0;
		
		String strSql = "SELECT MAX(order_detail_id) FROM order_detail";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderDetailId = cursor.getInt(0);
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		
		return orderDetailId + 1;
	}
	
	@Override
	public boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float qty, float pricePerUnit) {
		boolean isSucc = false;
		
		float totalRetailPrice = pricePerUnit * qty;
		float vat = vatType == 2 ? calculateVat(totalRetailPrice, 7) : 0.0f;
		
		String strSql = "UPDATE order_detail SET " +
				" qty=" + qty + ", " +
				" price_per_unit=" + pricePerUnit + ", " +
				" total_retail_price=" + totalRetailPrice + ", " +
				" total_sale_price=" + totalRetailPrice + ", " +
				" vat=" + vat + ", " +
				" price_discount=0 " +
				" WHERE transaction_id=" + transactionId +
				" AND order_detail_id=" + orderDetailId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSucc = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSucc;
	}
	
	@Override
	public int addOrderDetail(int transactionId, int computerId,
			int productId, int productType, int vatType,
			String productName, float qty, float pricePerUnit) {
		
		int orderDetailId = getMaxOrderDetail(transactionId, computerId);
		float totalRetailPrice = pricePerUnit * qty;
		float vat = vatType == 2 ? calculateVat(totalRetailPrice, 7) : 0.0f;
		
		ContentValues cv = new ContentValues();
		cv.put("order_detail_id", orderDetailId);
		cv.put("transaction_id", transactionId);
		cv.put("computer_id", computerId);
		cv.put("product_id", productId);
		cv.put("product_name", productName);
		cv.put("qty", qty);
		cv.put("price_per_unit", pricePerUnit);
		cv.put("total_retail_price", totalRetailPrice);
		cv.put("total_sale_price", totalRetailPrice);
		cv.put("vat_type", vatType);
		cv.put("vat", vat);
		
		mDbHelper.open();
		if(!mDbHelper.insert("order_detail", cv))
			orderDetailId = 0;
		mDbHelper.close();
		
		return orderDetailId;
	}

	@Override
	public boolean cancelDiscount(int transactionId, int computerId) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean deleteOrderDetailTmp(int transactionId, int computerId){
		boolean isSuccess = false;
		
		String strSql = "DELETE FROM order_detail_tmp " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
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
			
			mDbHelper.open();
			isSuccess = mDbHelper.execSQL(strSql);
			mDbHelper.close();
		}
		return isSuccess;
	}
	
	public boolean voidTransaction(int transactionId, int computerId, int staffId, String reason){
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();
		
		String strSql = "UPDATE order_transaction " +
				" SET transaction_status_id=9, " +
				" void_staff_id=" + staffId + ", " + 
				" void_reason='" + reason + "', " +
				" void_time='" + dateTime.getTimeInMillis() + "' " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
	
	public List<OrderTransaction> listTransaction(long saleDate){
		List<OrderTransaction> transLst = 
				new ArrayList<OrderTransaction>();
		String strSql = "SELECT * FROM order_transaction " +
				" WHERE sale_date='" + saleDate + "' " +
				" AND transaction_status_id=2 " +
				" ORDER BY sale_date";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				String receiptYear = String.format("%04d", cursor.getInt(cursor.getColumnIndex("receipt_year")));
				String receiptMonth = String.format("%02d", cursor.getInt(cursor.getColumnIndex("receipt_month")));
				String receiptId = String.format("%06d", cursor.getInt(cursor.getColumnIndex("receipt_id")));
				
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex("computer_id")));
				trans.setPaidTime(cursor.getLong(cursor.getColumnIndex("paid_time")));
				trans.setReceiptNo(receiptMonth + receiptYear + receiptId);
				
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return transLst;
	}
	
	public OrderTransaction.OrderDetail getSummaryTmp(int transactionId, int computerId) {
		OrderTransaction.OrderDetail orderDetail = 
					new OrderTransaction.OrderDetail();

		String strSql = "SELECT SUM(qty) AS TotalQty," +
				" SUM(total_retail_price) AS SubTotal, " +
				" SUM(total_sale_price) AS TotalPrice, " +
				" SUM(vat) AS TotalVat, " +
				" SUM(price_discount) AS TotalPriceDiscount," +
				" SUM(member_discount) AS TotalMemberDiscount "+
				" FROM order_detail_tmp " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){			
			orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("TotalQty")));
			orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex("SubTotal")));
			orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
			orderDetail.setVat(cursor.getFloat(cursor.getColumnIndex("TotalVat")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex("TotalPriceDiscount")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("TotalMemberDiscount")));
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		return orderDetail;
	}

	public OrderTransaction.OrderDetail getSummary(int transactionId, int computerId) {
		OrderTransaction.OrderDetail orderDetail = 
					new OrderTransaction.OrderDetail();

		String strSql = "SELECT SUM(qty) AS TotalQty," +
				" SUM(total_retail_price) AS SubTotal, " +
				" SUM(total_sale_price) AS TotalPrice, " +
				" SUM(vat) AS TotalVat, " +
				" SUM(price_discount) AS TotalPriceDiscount," +
				" SUM(member_discount) AS TotalMemberDiscount "+
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){			
			orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("TotalQty")));
			orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex("SubTotal")));
			orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex("TotalPrice")));
			orderDetail.setVat(cursor.getFloat(cursor.getColumnIndex("TotalVat")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex("TotalPriceDiscount")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("TotalMemberDiscount")));
			cursor.moveToNext();
		}
		cursor.close();
		mDbHelper.close();
		return orderDetail;
	}

	public OrderTransaction.OrderDetail getOrder(int transactionId, int computerId, int orderDetailId){
		OrderTransaction.OrderDetail orderDetail = 
				new OrderTransaction.OrderDetail();
		
		String strSql = "SELECT order_detail_id, " +
				" product_id, product_name, qty, price_per_unit, " +
				" vat_type, member_discount, price_discount " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId + 
				" AND order_detail_id=" + orderDetailId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex("order_detail_id")));
			orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
			orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("qty")));
			orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex("price_per_unit")));
			orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
			orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("member_discount")));
			orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex("price_discount")));
		}
		cursor.close();
		mDbHelper.close();
		return orderDetail;
	}
	
	public List<OrderTransaction.OrderDetail> listAllOrdersTmp(int transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderDetailLst 
				= new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, product_name, " +
				" qty, price_per_unit, total_retail_price, total_sale_price, " +
				" vat_type, member_discount, price_discount " +
				" FROM order_detail_tmp " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction.OrderDetail orderDetail = 
						new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("qty")));
				orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex("price_per_unit")));
				orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex("total_retail_price")));
				orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex("total_sale_price")));
				orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
				orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("member_discount")));
				orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex("price_discount")));
				
				orderDetailLst.add(orderDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		
		return orderDetailLst;
	}

	public List<OrderTransaction.OrderDetail> listAllOrders(int transactionId, int computerId){
		List<OrderTransaction.OrderDetail> orderDetailLst 
				= new ArrayList<OrderTransaction.OrderDetail>();
		
		String strSql = "SELECT order_detail_id, product_id, product_name, " +
				" qty, price_per_unit, total_retail_price, total_sale_price, " +
				" vat_type, member_discount, price_discount " +
				" FROM order_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction.OrderDetail orderDetail = 
						new OrderTransaction.OrderDetail();
				orderDetail.setOrderDetailId(cursor.getInt(cursor.getColumnIndex("order_detail_id")));
				orderDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				orderDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				orderDetail.setQty(cursor.getFloat(cursor.getColumnIndex("qty")));
				orderDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex("price_per_unit")));
				orderDetail.setTotalRetailPrice(cursor.getFloat(cursor.getColumnIndex("total_retail_price")));
				orderDetail.setTotalSalePrice(cursor.getFloat(cursor.getColumnIndex("total_sale_price")));
				orderDetail.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
				orderDetail.setMemberDiscount(cursor.getFloat(cursor.getColumnIndex("member_discount")));
				orderDetail.setPriceDiscount(cursor.getFloat(cursor.getColumnIndex("price_discount")));
				
				orderDetailLst.add(orderDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		
		return orderDetailLst;
	}
	
	public List<OrderTransaction> listHoldOrder(int computerId){
		List<OrderTransaction> transLst = 
				new ArrayList<OrderTransaction>();
		
		String strSql = "SELECT a.transaction_id, a.computer_id, " +
				" a.open_time, a.remark, " +
				" b.staff_code, b.staff_name " +
				" FROM order_transaction a " +
				" LEFT JOIN staffs b " +
				" ON a.open_staff_id=b.staff_id " +
				" WHERE a.computer_id=" + computerId + 
				" AND a.transaction_status_id=9";
		
		mDbHelper.open();
		
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex("transaction_id")));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex("computer_id")));
				trans.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
				trans.setOpenTime(cursor.getLong(cursor.getColumnIndex("open_time")));
				trans.setStaffName(cursor.getString(cursor.getColumnIndex("staff_code")) + ":" + 
						cursor.getString(cursor.getColumnIndex("staff_name")));
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		
		return transLst;
	}
}
