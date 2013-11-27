package com.syn.mpos;

import android.content.Context;

import com.syn.mpos.database.transaction.PaymentDetail;

public class MPOSPayment {
	public static String mErr;
	private Context mContext;
	private PaymentDetail mPayment;
	
	public MPOSPayment(Context c){
		mContext = c;
		mPayment = new PaymentDetail(c);
	}
	
	public boolean addPayment(int transactionId, int computerId, int payTypeId,
			float payAmount, String creditCardNo, int expMonth, int expYear,
			int bankId, int creditCardTypeId){
		
		if(mPayment.addPaymentDetail(transactionId, computerId, 
				payTypeId, payAmount, creditCardNo, expMonth, 
				expYear, bankId, creditCardTypeId)){
			return true;
		}else{
			mErr = "Cannot add payment.";
			return false;
		}
	}
}
