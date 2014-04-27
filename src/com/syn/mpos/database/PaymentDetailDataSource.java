package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.table.BankTable;
import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.CreditCardTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.PayTypeTable;
import com.syn.mpos.database.table.PaymentDetailTable;
import com.syn.pos.Payment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDetailDataSource extends MPOSDatabase {
	
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;

	public PaymentDetailDataSource(SQLiteDatabase db) {
		super(db);
	}
	
	public List<Payment.PayType> listPayType(){
		List<Payment.PayType> payTypeLst = 
				new ArrayList<Payment.PayType>();
		Cursor cursor = mSqlite.query(PayTypeTable.TABLE_NAME,
				new String[]{
				PayTypeTable.COLUMN_PAY_TYPE_ID,
				PayTypeTable.COLUMN_PAY_TYPE_CODE,
				PayTypeTable.COLUMN_PAY_TYPE_NAME
				}, null, null, null, null, PayTypeTable.COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Payment.PayType payType = 
						new Payment.PayType();
				payType.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payType.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payType.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
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
				"SELECT a." + PayTypeTable.COLUMN_PAY_TYPE_ID + ", " +
				" SUM(a." + PaymentDetailTable.COLUMN_PAID + ") AS " + PaymentDetailTable.COLUMN_PAID + ", " +
				" SUM(a." + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") AS " + PaymentDetailTable.COLUMN_PAY_AMOUNT + ", " +
				" b." + PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " +
				" b." + PayTypeTable.COLUMN_PAY_TYPE_NAME + 
				" FROM " + PaymentDetailTable.TABLE_NAME + " a " +
				" LEFT JOIN " + PayTypeTable.TABLE_NAME + " b " +
				" ON a." + PayTypeTable.COLUMN_PAY_TYPE_ID + "=b." + PayTypeTable.COLUMN_PAY_TYPE_ID +
				" WHERE a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=?" +
				" GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID +
				" ORDER BY a." + PaymentDetailTable.COLUMN_PAY_ID,
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentDetail payment = 
						new Payment.PaymentDetail();
				payment.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payment.setPaid(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAID)));
				payment.setPayAmount(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_AMOUNT)));
				payment.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payment.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
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
					" DELETE FROM " + PaymentDetailTable.TABLE_NAME +
					" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=" + transactionId + 
					" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + PaymentDetailTable.COLUMN_PAY_ID + ") " +
				" FROM " + PaymentDetailTable.TABLE_NAME + 
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=" + transactionId +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=" + computerId, null);
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
		cv.put(PaymentDetailTable.COLUMN_PAY_ID, paymentId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payTypeId);
		cv.put(PaymentDetailTable.COLUMN_PAID, paid);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, amount);
		cv.put(CreditCardTable.COLUMN_CREDITCARD_NO, creditCardNo);
		cv.put(CreditCardTable.COLUMN_EXP_MONTH, expireMonth);
		cv.put(CreditCardTable.COLUMN_EXP_YEAR, expireYear);
		cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, creditCardTypeId);
		cv.put(BankTable.COLUMN_BANK_ID, bankId);
		cv.put(PaymentDetailTable.COLUMN_REMARK, remark);
		return mSqlite.insertOrThrow(PaymentDetailTable.TABLE_NAME, null, cv);
	}

	public int updatePaymentDetail(int transactionId, int computerId, int payTypeId,
			double paid, double amount){
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailTable.COLUMN_PAID, paid);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, amount);
		return mSqlite.update(PaymentDetailTable.TABLE_NAME, cv, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + ComputerTable.COLUMN_COMPUTER_ID + "=? "
								+ " AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(computerId),
					String.valueOf(payTypeId)
				}
		);
	}

	public int deletePaymentDetail(int payTypeId){
		return mSqlite.delete(PaymentDetailTable.TABLE_NAME, 
				PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(payTypeId)});
	}
	
	public double getTotalPaid(int transactionId, int computerId){
		double totalPaid = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAID + ") " +
				" FROM " + PaymentDetailTable.TABLE_NAME +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=?", 
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
				" SELECT a." + PaymentDetailTable.COLUMN_PAY_ID + ", " +
				" a." + PayTypeTable.COLUMN_PAY_TYPE_ID + ", " +
				" SUM(a." + PaymentDetailTable.COLUMN_PAID + ") AS " + 
				PaymentDetailTable.COLUMN_PAID + ", " +
				" a." + PaymentDetailTable.COLUMN_REMARK + ", " +
				" b." + PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " +
				" b." + PayTypeTable.COLUMN_PAY_TYPE_NAME +
				" FROM " + PaymentDetailTable.TABLE_NAME + " a " +
				" LEFT JOIN " + PayTypeTable.TABLE_NAME + " b " + 
				" ON a." + PayTypeTable.COLUMN_PAY_TYPE_ID + "=" +
				" b." + PayTypeTable.COLUMN_PAY_TYPE_ID +
				" WHERE a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=?" + 
				" GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID,
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(computerId)});
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_ID)));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				payDetail.setPayAmount(cursor.getFloat(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAID)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(PaymentDetailTable.COLUMN_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	public void insertPaytype(List<Payment.PayType> payTypeLst){
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(PayTypeTable.TABLE_NAME, null, null);
			for(Payment.PayType payType : payTypeLst){
				ContentValues cv = new ContentValues();
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payType.getPayTypeID());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_CODE, payType.getPayTypeCode());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_NAME, payType.getPayTypeName());
				mSqlite.insertOrThrow(PayTypeTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
}
