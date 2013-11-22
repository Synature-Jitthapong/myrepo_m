package com.syn.mpos.database.transaction;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.mpos.database.MPOSSQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MPOSPayment {
	
	private SQLiteHelper mDbHelper;
	
	public MPOSPayment(Context context) {
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public boolean deleteAllPaymentDetail(int transactionId, int computerId) {
		boolean isSuccess = false;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DELETE FROM payment_detail " +
				" WHERE transaction_id=" + transactionId + 
				" AND computer_id=" + computerId);
		mDbHelper.close();
		
		return isSuccess;
	}
	
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		mDbHelper.open();
		
		String strSql = "SELECT MAX(pay_detail_id) " +
				" FROM payment_detail " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		Cursor cursor = mDbHelper.rawQuery(strSql);
		
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		
		mDbHelper.close();
		return maxPaymentId + 1;
	}

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
		
		mDbHelper.open();
		isSuccess = mDbHelper.insert("payment_detail", cv);
		mDbHelper.close();
		return isSuccess;
	}

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
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	public boolean deletePaymentDetail(int paymentId) {
		String strSql = "DELETE FROM payment_detail " +
				" WHERE pay_detail_id=" + paymentId;
		
		boolean isSuccess = false;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		
		return isSuccess;
	}
	
	public float getTotalPaid(int transactionId, int computerId){
		float totalPaid = 0.0f;
	
		String strSql = "SELECT SUM(pay_amount) " +
				" FROM payment_detail " +
				" WHERE transaction_id=" + transactionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			totalPaid = cursor.getFloat(0);
		}
		cursor.close();
				
		mDbHelper.close();
		
		return totalPaid;
	}
	
	public List<com.syn.pos.Payment.PaymentDetail> listPayment(int transactionId, int computerId){
		List<com.syn.pos.Payment.PaymentDetail> paymentLst = 
				new ArrayList<com.syn.pos.Payment.PaymentDetail>();
		
		String strSql = "SELECT a.*, b.pay_type_code, b.pay_type_name " +
				" FROM payment_detail a " +
				" LEFT JOIN pay_type b " + 
				" ON a.pay_type_id=b.pay_type_id " +
				" WHERE a.transaction_id=" + transactionId +
				" AND a.computer_id=" + computerId;
		
		mDbHelper.open();
		
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
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
		mDbHelper.close();
		
		return paymentLst;
	}
	
}
