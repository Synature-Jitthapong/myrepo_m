package com.syn.mpos.database.transaction;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.provider.Bank;
import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.CreditCard;
import com.syn.mpos.provider.MPOSDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDetail extends MPOSDatabase {
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;
	
	public static final String TABLE_PAYMENT = "PaymentDetail";
	public static final String COLUMN_PAY_ID = "pay_detail_id";
	public static final String COLUMN_PAY_AMOUNT = "pay_amount";
	public static final String COLUMN_REMARK = "remark";
	
	public static final String TABLE_PAY_TYPE = "PayType";
	public static final String COLUMN_PAY_TYPE_ID = "pay_type_id";
	public static final String COLUMN_PAY_TYPE_CODE = "pay_type_code";
	public static final String COLUMN_PAY_TYPE_NAME = "pay_type_name";
	
	public PaymentDetail(SQLiteDatabase db) {
		super(db);
	}
	
	public boolean deleteAllPaymentDetail(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TABLE_PAYMENT +
					" WHERE " + Transaction.COLUMN_TRANSACTION_ID + "=" + transactionId + 
					" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COLUMN_PAY_ID + ") " +
				" FROM " + TABLE_PAYMENT + 
				" WHERE " + Transaction.COLUMN_TRANSACTION_ID + "=" + transactionId +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId, null);
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		return maxPaymentId + 1;
	}

	public boolean addPaymentDetail(int transactionId,
			int computerId, int payTypeId, double payAmount,
			String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId) throws SQLException {
		boolean isSuccess = false;
		int paymentId = getMaxPaymentDetailId(transactionId, computerId);
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_PAY_ID, paymentId);
		cv.put(Transaction.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(Computer.COLUMN_COMPUTER_ID, computerId);
		cv.put(COLUMN_PAY_TYPE_ID, payTypeId);
		cv.put(COLUMN_PAY_AMOUNT, payAmount);
		cv.put(CreditCard.COLUMN_CREDITCARD_NO, creditCardNo);
		cv.put(CreditCard.COLUMN_EXP_MONTH, expireMonth);
		cv.put(CreditCard.COLUMN_EXP_YEAR, expireYear);
		cv.put(CreditCard.COLUMN_CREDITCARD_TYPE_ID, creditCardTypeId);
		cv.put(Bank.COLUMN_BANK_ID, bankId);
		
		try {
			mSqlite.insertOrThrow(TABLE_PAYMENT, null, cv);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean updatePaymentDetail(int transactionId, int computerId, int payTypeId,
			double paymentAmount, String creditCardNo, int expireMonth,
			int expireYear, int bankId, int creditCardTypeId) throws SQLException {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" UPDATE " + TABLE_PAYMENT + 
					" SET " +
					COLUMN_PAY_TYPE_ID + "=" + payTypeId + ", " +
					COLUMN_PAY_AMOUNT + "=" + paymentAmount + ", " +
					CreditCard.COLUMN_CREDITCARD_NO + "='" + creditCardNo + "', " +
					CreditCard.COLUMN_EXP_MONTH + "=" + expireMonth + ", " +
					CreditCard.COLUMN_EXP_YEAR + "=" + expireYear + ", " + 
					CreditCard.COLUMN_CREDITCARD_TYPE_ID + "=" + creditCardTypeId + ", " +
					Bank.COLUMN_BANK_ID + "=" + bankId +
					" WHERE " + Transaction.COLUMN_TRANSACTION_ID + "=" + transactionId +
					" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId, null);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean deletePaymentDetail(int payTypeId) throws SQLException {
		boolean isSuccess = false;
		int affectRow = mSqlite.delete(TABLE_PAYMENT, 
				COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(payTypeId)});
		
		if(affectRow > 0)
			isSuccess = true;
		return isSuccess;
	}
	
	public double getTotalPaid(int transactionId, int computerId){
		double totalPaid = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COLUMN_PAY_AMOUNT + ") " +
				" FROM " + TABLE_PAYMENT +
				" WHERE " + Transaction.COLUMN_TRANSACTION_ID + "=" + transactionId +
				" AND " + Computer.COLUMN_COMPUTER_ID + "=" + computerId, null);
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
				" SELECT a." + COLUMN_PAY_ID + ", " +
				" a." + COLUMN_PAY_TYPE_ID + ", " +
				" SUM(a." + COLUMN_PAY_AMOUNT + ") AS " + COLUMN_PAY_AMOUNT + ", " +
				" a." + COLUMN_REMARK + ", " +
				" b." + COLUMN_PAY_TYPE_CODE + ", " +
				" b." + COLUMN_PAY_TYPE_NAME +
				" FROM " + TABLE_PAYMENT + " a " +
				" LEFT JOIN " + TABLE_PAY_TYPE + " b " + 
				" ON a." + COLUMN_PAY_TYPE_ID + "=" +
				" b." + COLUMN_PAY_TYPE_ID +
				" WHERE a." + Transaction.COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + Computer.COLUMN_COMPUTER_ID + "=?" + 
				" GROUP BY a." + COLUMN_PAY_TYPE_ID,
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)});
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex(COLUMN_PAY_ID)));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex(COLUMN_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(COLUMN_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(COLUMN_PAY_TYPE_NAME)));
				payDetail.setPayAmount(cursor.getFloat(cursor.getColumnIndex(COLUMN_PAY_AMOUNT)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
}
