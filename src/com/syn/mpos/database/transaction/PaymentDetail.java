package com.syn.mpos.database.transaction;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.CreditCard;
import com.syn.mpos.database.MPOSDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PaymentDetail extends MPOSDatabase {

	public static final String TB_PAYMENT = "PaymentDetail";
	public static final String COL_PAY_ID = "PayDetailId";
	public static final String COL_PAY_AMOUNT = "PayAmount";
	public static final String COL_REMARK = "Remark";
	
	public static final String TB_PAY_TYPE = "PayType";
	public static final String COL_PAY_TYPE_ID = "PayTypeId";
	public static final String COL_PAY_TYPE_CODE = "PayTypeCode";
	public static final String COL_PAY_TYPE_NAME = "PayTypeName";
	
	public PaymentDetail(SQLiteDatabase db) {
		super(db);
	}
	
	public boolean deleteAllPaymentDetail(int transactionId, int computerId) throws SQLException{
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_PAYMENT +
					" WHERE " + Transaction.COL_TRANS_ID + "=" + transactionId + 
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public int getMaxPaymentDetailId(int transactionId, int computerId) {
		int maxPaymentId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COL_PAY_ID + ") " +
				" FROM " + TB_PAYMENT + 
				" WHERE " + Transaction.COL_TRANS_ID + "=" + transactionId +
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		return maxPaymentId + 1;
	}

	public boolean addPaymentDetail(int transactionId,
			int computerId, int payTypeId, float payAmount,
			String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId) throws SQLException {
		boolean isSuccess = false;
		int paymentId = getMaxPaymentDetailId(transactionId, computerId);
		ContentValues cv = new ContentValues();
		cv.put(COL_PAY_ID, paymentId);
		cv.put(Transaction.COL_TRANS_ID, transactionId);
		cv.put(Computer.COL_COMPUTER_ID, computerId);
		cv.put(COL_PAY_TYPE_ID, payTypeId);
		cv.put(COL_PAY_AMOUNT, payAmount);
		cv.put(CreditCard.COL_CREDIT_CARD_NO, creditCardNo);
		cv.put(CreditCard.COL_EXP_MONTH, expireMonth);
		cv.put(CreditCard.COL_EXP_YEAR, expireYear);
		cv.put(CreditCard.COL_CREDIT_CARD_TYPE_ID, creditCardTypeId);
		cv.put(Bank.COL_BANK_ID, bankId);
		
		try {
			mSqlite.insertOrThrow(TB_PAYMENT, null, cv);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean updatePaymentDetail(int transactionId, int computerId, int payTypeId,
			float paymentAmount, String creditCardNo, int expireMonth,
			int expireYear, int bankId, int creditCardTypeId) throws SQLException {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" UPDATE " + TB_PAYMENT + 
					" SET " +
					COL_PAY_TYPE_ID + "=" + payTypeId + ", " +
					COL_PAY_AMOUNT + "=" + paymentAmount + ", " +
					CreditCard.COL_CREDIT_CARD_NO + "='" + creditCardNo + "', " +
					CreditCard.COL_EXP_MONTH + "=" + expireMonth + ", " +
					CreditCard.COL_EXP_YEAR + "=" + expireYear + ", " + 
					CreditCard.COL_CREDIT_CARD_TYPE_ID + "=" + creditCardTypeId + ", " +
					Bank.COL_BANK_ID + "=" + bankId +
					" WHERE " + Transaction.COL_TRANS_ID + "=" + transactionId +
					" AND " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean deletePaymentDetail(int paymentId) throws SQLException {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(
					" DELETE FROM " + TB_PAYMENT +
					" WHERE " + COL_PAY_ID + "=" + paymentId);
			isSuccess = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public float getTotalPaid(int transactionId, int computerId){
		float totalPaid = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + COL_PAY_AMOUNT + ") " +
				" FROM " + TB_PAYMENT +
				" WHERE " + Transaction.COL_TRANS_ID + "=" + transactionId +
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
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
				" SELECT a.*, " +
				" b." + COL_PAY_TYPE_CODE + ", " +
				" b." + COL_PAY_TYPE_NAME +
				" FROM " + TB_PAYMENT + " a " +
				" LEFT JOIN " + TB_PAY_TYPE + " b " + 
				" ON a." + COL_PAY_TYPE_ID + "=" +
				" b." + COL_PAY_TYPE_ID +
				" WHERE a." + Transaction.COL_TRANS_ID + "=" + transactionId +
				" AND a." + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Payment.PaymentDetail payDetail
					= new com.syn.pos.Payment.PaymentDetail();
				payDetail.setPaymentDetailID(cursor.getInt(cursor.getColumnIndex(COL_PAY_ID)));
				payDetail.setPayTypeID(cursor.getInt(cursor.getColumnIndex(COL_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(COL_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(COL_PAY_TYPE_NAME)));
				payDetail.setPayAmount(cursor.getFloat(cursor.getColumnIndex(COL_PAY_AMOUNT)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(COL_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
}
