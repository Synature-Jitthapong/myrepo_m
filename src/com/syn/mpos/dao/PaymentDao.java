package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.dao.BankNameDao.BankTable;
import com.syn.mpos.dao.ComputerDao.ComputerTable;
import com.syn.mpos.dao.CreditCardDao.CreditCardTable;
import com.syn.mpos.dao.TransactionDao.OrderTransactionTable;
import com.syn.pos.Payment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDao extends MPOSDatabase {
	
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;

	public PaymentDao(Context context) {
		super(context);
	}
	
	/**
	 * @return List<Payment.PayType>
	 */
	public List<Payment.PayType> listPayType(){
		List<Payment.PayType> payTypeLst = 
				new ArrayList<Payment.PayType>();
		Cursor cursor = getReadableDatabase().query(PayTypeTable.TABLE_PAY_TYPE,
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
	
	/**
	 * @param transactionId
	 * @return List<Payment.PaymentDetail>
	 */
	public List<Payment.PaymentDetail> listPaymentGroupByType(int transactionId){
		List<Payment.PaymentDetail> paymentLst = 
				new ArrayList<Payment.PaymentDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + PayTypeTable.COLUMN_PAY_TYPE_ID + ", " + " a."
						+ CreditCardTable.COLUMN_CREDITCARD_TYPE_ID + ", "
						+ " a." + CreditCardTable.COLUMN_CREDITCARD_NO + ", "
						+ " SUM(a." + PaymentDetailTable.COLUMN_PAID + ") AS "
						+ PaymentDetailTable.COLUMN_PAID + ", " + " SUM(a."
						+ PaymentDetailTable.COLUMN_PAY_AMOUNT + ") AS "
						+ PaymentDetailTable.COLUMN_PAY_AMOUNT + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_NAME + " FROM "
						+ PaymentDetailTable.TABLE_PAYMENT_DETAIL + " a " + " LEFT JOIN "
						+ PayTypeTable.TABLE_PAY_TYPE + " b " + " ON a."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + "=b."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
						+ " GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID
						+ " ORDER BY a." + PaymentDetailTable.COLUMN_PAY_ID,
				new String[]{
					String.valueOf(transactionId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentDetail payment = 
						new Payment.PaymentDetail();
				payment.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payment.setCreditCardType(cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID)));
				payment.setCreaditCardNo(cursor.getString(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_NO)));
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
	
	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteAllPaymentDetail(int transactionId){
		return getWritableDatabase().delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL, 
					OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?", 
					new String[]{String.valueOf(transactionId)});
	}
	
	/**
	 * @param transactionId
	 * @return max paymentDetailId
	 */
	public int getMaxPaymentDetailId(int transactionId) {
		int maxPaymentId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + PaymentDetailTable.COLUMN_PAY_ID + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[]{String.valueOf(transactionId)});
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		return maxPaymentId + 1;
	}

	/**
	 * @param transactionId
	 * @param computerId
	 * @param payTypeId
	 * @param paid
	 * @param pay
	 * @param creditCardNo
	 * @param expireMonth
	 * @param expireYear
	 * @param bankId
	 * @param creditCardTypeId
	 * @param remark
	 * @return The ID of newly row inserted
	 * @throws SQLException
	 */
	public long addPaymentDetail(int transactionId, int computerId, 
			int payTypeId, double paid, double pay , String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId, String remark) throws SQLException {
		int paymentId = getMaxPaymentDetailId(transactionId);
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailTable.COLUMN_PAY_ID, paymentId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payTypeId);
		cv.put(PaymentDetailTable.COLUMN_PAID, paid);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, pay);
		cv.put(CreditCardTable.COLUMN_CREDITCARD_NO, creditCardNo);
		cv.put(CreditCardTable.COLUMN_EXP_MONTH, expireMonth);
		cv.put(CreditCardTable.COLUMN_EXP_YEAR, expireYear);
		cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, creditCardTypeId);
		cv.put(BankTable.COLUMN_BANK_ID, bankId);
		cv.put(PaymentDetailTable.COLUMN_REMARK, remark);
		return getWritableDatabase().insertOrThrow(PaymentDetailTable.TABLE_PAYMENT_DETAIL, null, cv);
	}

	/**
	 * @param transactionId
	 * @param payTypeId
	 * @param paid
	 * @param amount
	 * @return row affected
	 */
	public int updatePaymentDetail(int transactionId, int payTypeId,
			double paid, double amount){
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailTable.COLUMN_PAID, paid);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, amount);
		return getWritableDatabase().update(PaymentDetailTable.TABLE_PAYMENT_DETAIL, cv, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
								+ " AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(payTypeId)
				}
		);
	}

	/**
	 * @param transactionId
	 * @param payTypeId
	 * @return row affected
	 */
	public int deletePaymentDetail(int transactionId, int payTypeId){
		return getWritableDatabase().delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL,
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND "
				+ PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(payTypeId)});
	}
	
	/**
	 * @param transactionId
	 * @return total paid
	 */
	public double getTotalPay(int transactionId){
		double totalPaid = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[]{
						String.valueOf(transactionId)
				});
		if(cursor.moveToFirst()){
			totalPaid = cursor.getDouble(0);
		}
		cursor.close();
		return totalPaid;
	}
	
	/**
	 * @param transactionId
	 * @return total paid
	 */
	public double getTotalPaid(int transactionId){
		double totalPaid = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAID + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " WHERE "
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[]{
						String.valueOf(transactionId)
				});
		if(cursor.moveToFirst()){
			totalPaid = cursor.getDouble(0);
		}
		cursor.close();
		return totalPaid;
	}
	
	/**
	 * @param transactionId
	 * @return List<com.syn.pos.Payment.PaymentDetail>
	 */
	public List<com.syn.pos.Payment.PaymentDetail> listPayment(int transactionId){
		List<com.syn.pos.Payment.PaymentDetail> paymentLst = 
				new ArrayList<com.syn.pos.Payment.PaymentDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + PaymentDetailTable.COLUMN_PAY_ID + ", " + " a."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + ", " + " SUM(a."
						+ PaymentDetailTable.COLUMN_PAID + ") AS "
						+ PaymentDetailTable.COLUMN_PAID + ", " + " a."
						+ PaymentDetailTable.COLUMN_REMARK + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_NAME + " FROM "
						+ PaymentDetailTable.TABLE_PAYMENT_DETAIL + " a " + " LEFT JOIN "
						+ PayTypeTable.TABLE_PAY_TYPE + " b " + " ON a."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + "=" + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + " WHERE a."
						+ OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?"
						+ " GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID,
				new String[]{
						String.valueOf(transactionId)});
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
				payDetail.setTransactionID(transactionId);
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_ID)));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				payDetail.setPaid(cursor.getFloat(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAID)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(PaymentDetailTable.COLUMN_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	/**
	 * @param payTypeLst
	 */
	public void insertPaytype(List<Payment.PayType> payTypeLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PayTypeTable.TABLE_PAY_TYPE, null, null);
			for(Payment.PayType payType : payTypeLst){
				ContentValues cv = new ContentValues();
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payType.getPayTypeID());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_CODE, payType.getPayTypeCode());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_NAME, payType.getPayTypeName());
				getWritableDatabase().insertOrThrow(PayTypeTable.TABLE_PAY_TYPE, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class PaymentDetailTable{
		
		public static final String TABLE_PAYMENT_DETAIL = "PaymentDetail";
		public static final String COLUMN_PAY_ID = "pay_detail_id";
		public static final String COLUMN_PAY_AMOUNT = "pay_amount";
		public static final String COLUMN_PAID = "paid";
		public static final String COLUMN_REMARK = "remark";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_PAYMENT_DETAIL + " ( " +
						COLUMN_PAY_ID + " INTEGER, " +
				OrderTransactionTable.COLUMN_TRANSACTION_ID + " INTEGER, " +
				ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, " +
				PayTypeTable.COLUMN_PAY_TYPE_ID + " INTEGER DEFAULT 1, " +
				COLUMN_PAY_AMOUNT + " REAL DEFAULT 0, " +
				COLUMN_PAID + " REAL DEFAULT 0, " +
				CreditCardTable.COLUMN_CREDITCARD_NO + " TEXT, " +
				CreditCardTable.COLUMN_EXP_MONTH + " INTEGER, " +
				CreditCardTable.COLUMN_EXP_YEAR + " INTEGER, " +
				BankTable.COLUMN_BANK_ID + " INTEGER, " +
				CreditCardTable.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
				COLUMN_REMARK + " TEXT, " +
				"PRIMARY KEY (" + COLUMN_PAY_ID + " ASC, " + 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + " ASC, " + ComputerTable.COLUMN_COMPUTER_ID + " ASC) );";
		
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
	
	public static class PayTypeTable{

		public static final String TABLE_PAY_TYPE = "PayType";
		public static final String COLUMN_PAY_TYPE_ID = "pay_type_id";
		public static final String COLUMN_PAY_TYPE_CODE = "pay_type_code";
		public static final String COLUMN_PAY_TYPE_NAME = "pay_type_name";
		public static final String COLUMN_ORDERING = "ordering";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_PAY_TYPE + " ( " +
				COLUMN_PAY_TYPE_ID + " INTEGER, " +
				COLUMN_PAY_TYPE_CODE + " TEXT, " +
				COLUMN_PAY_TYPE_NAME + " TEXT, " +
				COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
				"PRIMARY KEY (" + COLUMN_PAY_TYPE_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}
