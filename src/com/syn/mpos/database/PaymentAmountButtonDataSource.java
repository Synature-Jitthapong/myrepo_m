package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.table.PaymentButtonTable;
import com.syn.pos.Payment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PaymentAmountButtonDataSource extends MPOSDatabase {
	
	public PaymentAmountButtonDataSource(Context context) {
		super(context);
	}
	
	/**
	 * @return List<Payment.PaymentAmountButton>
	 */
	protected List<Payment.PaymentAmountButton> listPaymentButton(){
		List<Payment.PaymentAmountButton> paymentButtonLst = 
				new ArrayList<Payment.PaymentAmountButton>();
		Cursor cursor = getReadableDatabase().query(PaymentButtonTable.TABLE_NAME, 
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
	protected void insertPaymentAmountButton(List<Payment.PaymentAmountButton> paymentAmountLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PaymentButtonTable.TABLE_NAME, null, null);
			for(Payment.PaymentAmountButton payButton : paymentAmountLst){
				ContentValues cv = new ContentValues();
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT_ID, payButton.getPaymentAmountID());
				cv.put(PaymentButtonTable.COLUMN_PAYMENT_AMOUNT, payButton.getPaymentAmount());
				getWritableDatabase().insertOrThrow(PaymentButtonTable.TABLE_NAME, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
}
