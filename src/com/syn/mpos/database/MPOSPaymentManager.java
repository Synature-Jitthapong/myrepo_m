package com.syn.mpos.database;

import android.content.Context;

/**
 * @author j1tth4
 * Manage payment
 */
public class MPOSPaymentManager {

	/*
	 * payment data source
	 */
	private PaymentDetailDataSource mPaymentDetail;
	
	public MPOSPaymentManager(Context context){
		mPaymentDetail = new PaymentDetailDataSource(context);
	}
}
