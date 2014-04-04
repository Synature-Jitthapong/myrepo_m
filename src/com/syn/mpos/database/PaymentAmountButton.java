package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.Payment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PaymentAmountButton extends MPOSDatabase {

	public static final String TABLE_PAYMENT_AMOUNT = "PaymentAmountButton";
	public static final String COLUMN_PAYMENT_AMOUNT_ID = "payment_amount_id";
	public static final String COLUMN_PAYMENT_AMOUNT = "payment_amount";
	public static final String COLUMN_ORDERING = "ordering";
	
	public PaymentAmountButton(SQLiteDatabase db) {
		super(db);
	}
	
	public List<Payment.PaymentAmountButton> listPaymentButton(){
		List<Payment.PaymentAmountButton> paymentButtonLst = 
				new ArrayList<Payment.PaymentAmountButton>();
		Cursor cursor = mSqlite.query(TABLE_PAYMENT_AMOUNT, 
				new String[]{
					COLUMN_PAYMENT_AMOUNT_ID,
					COLUMN_PAYMENT_AMOUNT
				},
				null, null, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Payment.PaymentAmountButton payButton = 
						new Payment.PaymentAmountButton();
				payButton.setPaymentAmountID(cursor.getInt(cursor.getColumnIndex(COLUMN_PAYMENT_AMOUNT_ID)));
				payButton.setPaymentAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_PAYMENT_AMOUNT)));
				paymentButtonLst.add(payButton);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentButtonLst;
	}
	
	public void insertPaymentAmountButton(List<Payment.PaymentAmountButton> paymentAmountLst){
		mSqlite.delete(TABLE_PAYMENT_AMOUNT, null, null);
		for(Payment.PaymentAmountButton payButton : paymentAmountLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PAYMENT_AMOUNT_ID, payButton.getPaymentAmountID());
			cv.put(COLUMN_PAYMENT_AMOUNT, payButton.getPaymentAmount());
			mSqlite.insert(TABLE_PAYMENT_AMOUNT, null, cv);
		}
	}
}
