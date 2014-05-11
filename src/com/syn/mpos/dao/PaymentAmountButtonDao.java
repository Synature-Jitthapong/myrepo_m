package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.dao.table.PaymentButtonTable;
import com.syn.pos.Payment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PaymentAmountButtonDao extends MPOSDatabase {
	
	public PaymentAmountButtonDao(Context context) {
		super(context);
	}
	
	/**
	 * @return List<Payment.PaymentAmountButton>
	 */
	public List<Payment.PaymentAmountButton> listPaymentButton(){
		List<Payment.PaymentAmountButton> paymentButtonLst = 
				new ArrayList<Payment.PaymentAmountButton>();
		Cursor cursor = getReadableDatabase().query(PaymentButtonTable.TABLE_PAYMENT_BUTTON, 
				new String[]{
				PaymentButtonTable.COLUMN_PAYMENT_AMOUNT_ID,
				PaymentButtonTable.COLUMN_PAYMENT_AMOUNT
				},
				null, null, null, null, 
				PaymentButtonTable.COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentAmountButton payButton = 
						new Payment.PaymentAmountButton();
				payButton.setPaymentAmountID(cursor.getInt(cursor.getColumnIndex(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT_ID)));
				payButton.setPaymentAmount(cursor.getDouble(cursor.getColumnIndex(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT)));
				paymentButtonLst.add(payButton);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentButtonLst;
	}
	
	/**
	 * @param paymentAmountLst
	 */
	public void insertPaymentAmountButton(List<Payment.PaymentAmountButton> paymentAmountLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PaymentButtonTable.TABLE_PAYMENT_BUTTON, null, null);
			for(Payment.PaymentAmountButton payButton : paymentAmountLst){
				ContentValues cv = new ContentValues();
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT_ID, payButton.getPaymentAmountID());
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT, payButton.getPaymentAmount());
				getWritableDatabase().insertOrThrow(PaymentButtonTable.TABLE_PAYMENT_BUTTON, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class PaymentButtonTable{

		public static final String TABLE_PAYMENT_BUTTON = "PaymentAmountButton";
		public static final String COLUMN_PAYMENT_AMOUNT_ID = "payment_amount_id";
		public static final String COLUMN_PAYMENT_AMOUNT = "payment_amount";
		public static final String COLUMN_ORDERING = "ordering";

		private static final String SQL_CREATE = 
				"CREATE TABLE " + TABLE_PAYMENT_BUTTON + "( " +
				COLUMN_PAYMENT_AMOUNT_ID + " INTEGER, " +
				COLUMN_PAYMENT_AMOUNT + " REAL DEFAULT 0, " +
				COLUMN_ORDERING + " INTEGER DEFAULT 0 );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}
