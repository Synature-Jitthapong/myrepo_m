package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.Payment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PaymentAmountButton extends MPOSDatabase {
	
	public PaymentAmountButton(Context c) {
		super(c);
	}
	
	public List<Payment.PaymentAmountButton> listPaymentButton(){
		List<Payment.PaymentAmountButton> paymentButtonLst = 
				new ArrayList<Payment.PaymentAmountButton>();
		Cursor cursor = mSqlite.query(PaymentButtonTable.TABLE_NAME, 
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
	
	public void insertPaymentAmountButton(List<Payment.PaymentAmountButton> paymentAmountLst){
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(PaymentButtonTable.TABLE_NAME, null, null);
			for(Payment.PaymentAmountButton payButton : paymentAmountLst){
				ContentValues cv = new ContentValues();
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT_ID, payButton.getPaymentAmountID());
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT, payButton.getPaymentAmount());
				mSqlite.insert(PaymentButtonTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally{
			mSqlite.endTransaction();
		}
	}
}
