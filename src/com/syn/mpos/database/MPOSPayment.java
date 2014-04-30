package com.syn.mpos.database;

import android.content.Context;

/**
 * @author j1tth4
 * Manage payment
 */
public class MPOSPayment {

	/*
	 * payment data source
	 */
	private PaymentDetailDataSource mPaymentDetail;
	
	public MPOSPayment(Context context){
		mPaymentDetail = new PaymentDetailDataSource(context);
	}
}
