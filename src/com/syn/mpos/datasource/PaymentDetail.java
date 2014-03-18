package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.datasource.Bank.BankEntry;
import com.syn.mpos.datasource.Computer.ComputerEntry;
import com.syn.mpos.datasource.CreditCard.CreditCardEntry;
import com.syn.mpos.datasource.Transaction.TransactionEntry;
import com.syn.pos.Payment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDetail extends MPOSDatabase {
	
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;
	
	public PaymentDetail(SQLiteDatabase db) {
		super(db);
	}
	
	public List<Payment.PayType> listPayType(){
		List<Payment.PayType> payTypeLst = 
				new ArrayList<Payment.PayType>();
		Cursor cursor = mSqlite.query(PayTypeEntry.TABLE_PAY_TYPE,
				new String[]{
				PayTypeEntry.COLUMN_PAY_TYPE_ID,
				PayTypeEntry.COLUMN_PAY_TYPE_CODE,
				PayTypeEntry.COLUMN_PAY_TYPE_NAME
				}, null, null, null, null, PayTypeEntry.COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Payment.PayType payType = 
						new Payment.PayType();
				payType.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_ID)));
				payType.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_CODE)));
				payType.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_NAME)));
				payTypeLst.add(payType);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return payTypeLst;
	}
	
	public List<Payment.PaymentDetail> listPaymentGroupByType(int transactionId, int computerId){
		List<Payment.PaymentDetail> paymentLst = 
				new ArrayList<Payment.PaymentDetail>();
		Cursor cursor = mSqlite.rawQuery(
				"SELECT a." + PayTypeEntry.COLUMN_PAY_TYPE_ID + ", " +
				" SUM(a." + PaymentDetailEntry.COLUMN_PAID + ") AS " + PaymentDetailEntry.COLUMN_PAID + ", " +
				" SUM(a." + PaymentDetailEntry.COLUMN_PAY_AMOUNT + ") AS " + PaymentDetailEntry.COLUMN_PAY_AMOUNT + ", " +
				" b." + PayTypeEntry.COLUMN_PAY_TYPE_CODE + ", " +
				" b." + PayTypeEntry.COLUMN_PAY_TYPE_NAME + 
				" FROM " + PaymentDetailEntry.TABLE_PAYMENT + " a " +
				" LEFT JOIN " + PayTypeEntry.TABLE_PAY_TYPE + " b " +
				" ON a." + PayTypeEntry.COLUMN_PAY_TYPE_ID + "=b." + PayTypeEntry.COLUMN_PAY_TYPE_ID +
				" WHERE a." + TransactionEntry.COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + ComputerEntry.COLUMN_COMPUTER_ID + "=?" +
				" GROUP BY a." + PayTypeEntry.COLUMN_PAY_TYPE_ID +
				" ORDER BY a." + PaymentDetailEntry.COLUMN_PAY_ID,
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentDetail payment = 
						new Payment.PaymentDetail();
				payment.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_ID)));
				payment.setPaid(cursor.getDouble(cursor.getColumnIndex(PaymentDetailEntry.COLUMN_PAID)));
				payment.setPayAmount(cursor.getDouble(cursor.getColumnIndex(PaymentDetailEntry.COLUMN_PAY_AMOUNT)));
				payment.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_CODE)));
				payment.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_NAME)));
				paymentLst.add(payment);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	public boolean deleteAllPaymentDetail(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + PaymentDetailEntry.TABLE_PAYMENT +
					" WHERE " + TransactionEntry.COLUMN_TRANSACTION_ID + "=" + transactionId + 
					" AND " + ComputerEntry.COLUMN_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + PaymentDetailEntry.COLUMN_PAY_ID + ") " +
				" FROM " + PaymentDetailEntry.TABLE_PAYMENT + 
				" WHERE " + TransactionEntry.COLUMN_TRANSACTION_ID + "=" + transactionId +
				" AND " + ComputerEntry.COLUMN_COMPUTER_ID + "=" + computerId, null);
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		return maxPaymentId + 1;
	}

	public long addPaymentDetail(int transactionId, int computerId, 
			int payTypeId, double paid, double amount , String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId, String remark) throws SQLException {
		int paymentId = getMaxPaymentDetailId(transactionId, computerId);
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailEntry.COLUMN_PAY_ID, paymentId);
		cv.put(TransactionEntry.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerEntry.COLUMN_COMPUTER_ID, computerId);
		cv.put(PayTypeEntry.COLUMN_PAY_TYPE_ID, payTypeId);
		cv.put(PaymentDetailEntry.COLUMN_PAID, paid);
		cv.put(PaymentDetailEntry.COLUMN_PAY_AMOUNT, amount);
		cv.put(CreditCardEntry.COLUMN_CREDITCARD_NO, creditCardNo);
		cv.put(CreditCardEntry.COLUMN_EXP_MONTH, expireMonth);
		cv.put(CreditCardEntry.COLUMN_EXP_YEAR, expireYear);
		cv.put(CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID, creditCardTypeId);
		cv.put(BankEntry.COLUMN_BANK_ID, bankId);
		cv.put(PaymentDetailEntry.COLUMN_REMARK, remark);
		return mSqlite.insertOrThrow(PaymentDetailEntry.TABLE_PAYMENT, null, cv);
	}

	public int updatePaymentDetail(int transactionId, int computerId, int payTypeId,
			double paid, double amount){
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailEntry.COLUMN_PAID, paid);
		cv.put(PaymentDetailEntry.COLUMN_PAY_AMOUNT, amount);
		return mSqlite.update(PaymentDetailEntry.TABLE_PAYMENT, cv, 
				TransactionEntry.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + ComputerEntry.COLUMN_COMPUTER_ID + "=? "
								+ " AND " + PayTypeEntry.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId),
					String.valueOf(payTypeId)
				}
		);
	}

	public int deletePaymentDetail(int payTypeId){
		return mSqlite.delete(PaymentDetailEntry.TABLE_PAYMENT, 
				PayTypeEntry.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(payTypeId)});
	}
	
	public double getTotalPaid(int transactionId, int computerId){
		double totalPaid = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + PaymentDetailEntry.COLUMN_PAID + ") " +
				" FROM " + PaymentDetailEntry.TABLE_PAYMENT +
				" WHERE " + TransactionEntry.COLUMN_TRANSACTION_ID + "=?" +
				" AND " + ComputerEntry.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId),
						String.valueOf(computerId)
				});
		if(cursor.moveToFirst()){
			totalPaid = cursor.getFloat(0);
		}
		cursor.close();
		return totalPaid;
	}
	
	public List<com.syn.pos.Payment.PaymentDetail> listPayment(int transactionId, int computerId){
		List<com.syn.pos.Payment.PaymentDetail> paymentLst = 
				new ArrayList<com.syn.pos.Payment.PaymentDetail>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT a." + PaymentDetailEntry.COLUMN_PAY_ID + ", " +
				" a." + PayTypeEntry.COLUMN_PAY_TYPE_ID + ", " +
				" SUM(a." + PaymentDetailEntry.COLUMN_PAID + ") AS " + 
				PaymentDetailEntry.COLUMN_PAID + ", " +
				" a." + PaymentDetailEntry.COLUMN_REMARK + ", " +
				" b." + PayTypeEntry.COLUMN_PAY_TYPE_CODE + ", " +
				" b." + PayTypeEntry.COLUMN_PAY_TYPE_NAME +
				" FROM " + PaymentDetailEntry.TABLE_PAYMENT + " a " +
				" LEFT JOIN " + PayTypeEntry.TABLE_PAY_TYPE + " b " + 
				" ON a." + PayTypeEntry.COLUMN_PAY_TYPE_ID + "=" +
				" b." + PayTypeEntry.COLUMN_PAY_TYPE_ID +
				" WHERE a." + TransactionEntry.COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + ComputerEntry.COLUMN_COMPUTER_ID + "=?" + 
				" GROUP BY a." + PayTypeEntry.COLUMN_PAY_TYPE_ID,
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)});
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex(PaymentDetailEntry.COLUMN_PAY_ID)));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeEntry.COLUMN_PAY_TYPE_NAME)));
				payDetail.setPayAmount(cursor.getFloat(cursor.getColumnIndex(PaymentDetailEntry.COLUMN_PAID)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(PaymentDetailEntry.COLUMN_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	public void insertPaytype(List<Payment.PayType> payTypeLst){
		mSqlite.delete(PayTypeEntry.TABLE_PAY_TYPE, null, null);
		for(Payment.PayType payType : payTypeLst){
			ContentValues cv = new ContentValues();
			cv.put(PayTypeEntry.COLUMN_PAY_TYPE_ID, payType.getPayTypeID());
			cv.put(PayTypeEntry.COLUMN_PAY_TYPE_CODE, payType.getPayTypeCode());
			cv.put(PayTypeEntry.COLUMN_PAY_TYPE_NAME, payType.getPayTypeName());
			mSqlite.insert(PayTypeEntry.TABLE_PAY_TYPE, null, cv);
		}
	}
	
	public static abstract class PayTypeEntry{
		public static final String TABLE_PAY_TYPE = "PayType";
		public static final String COLUMN_PAY_TYPE_ID = "pay_type_id";
		public static final String COLUMN_PAY_TYPE_CODE = "pay_type_code";
		public static final String COLUMN_PAY_TYPE_NAME = "pay_type_name";
		public static final String COLUMN_ORDERING = "ordering";
	}
	
	public static abstract class PaymentDetailEntry{
		public static final String TABLE_PAYMENT = "PaymentDetail";
		public static final String COLUMN_PAY_ID = "pay_detail_id";
		public static final String COLUMN_PAY_AMOUNT = "pay_amount";
		public static final String COLUMN_PAID = "pad";
		public static final String COLUMN_REMARK = "remark";
	}
}
