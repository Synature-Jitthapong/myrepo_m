package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.Payment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PaymentAmountButton extends MPOSDatabase {
	
	public PaymentAmountButton(SQLiteDatabase db) {
		super(db);
	}
	
	public List<Payment.PaymentAmountButton> listPaymentButton(){
		List<Payment.PaymentAmountButton> paymentButtonLst = 
				new ArrayList<Payment.PaymentAmountButton>();
		Cursor cursor = mSqlite.query(PaymentButtonEntry.TABLE_PAYMENT_AMOUNT, 
				new String[]{
					PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT_ID,
					PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT
				},
				null, null, null, null, PaymentButtonEntry.COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentAmountButton payButton = 
						new Payment.PaymentAmountButton();
				payButton.setPaymentAmountID(cursor.getInt(cursor.getColumnIndex(PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT_ID)));
				payButton.setPaymentAmount(cursor.getDouble(cursor.getColumnIndex(PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT)));
				paymentButtonLst.add(payButton);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentButtonLst;
	}
	
	public void insertPaymentAmountButton(List<Payment.PaymentAmountButton> paymentAmountLst){
		mSqlite.delete(PaymentButtonEntry.TABLE_PAYMENT_AMOUNT, null, null);
		for(Payment.PaymentAmountButton payButton : paymentAmountLst){
			ContentValues cv = new ContentValues();
			cv.put(PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT_ID, payButton.getPaymentAmountID());
			cv.put(PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT, payButton.getPaymentAmount());
			mSqlite.insert(PaymentButtonEntry.TABLE_PAYMENT_AMOUNT, null, cv);
		}
	}
	
	public static abstract class PaymentButtonEntry{
		public static final String TABLE_PAYMENT_AMOUNT = "PaymentAmountButton";
		public static final String COLUMN_PAYMENT_AMOUNT_ID = "payment_amount_id";
		public static final String COLUMN_PAYMENT_AMOUNT = "payment_amount";
		public static final String COLUMN_ORDERING = "ordering";
	}
}
