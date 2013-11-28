package com.syn.mpos;

import java.util.List;

import android.content.Context;

import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.pos.Payment;

public class MPOSPayment {
	public static String mErr;
	private Context mContext;
	private PaymentDetail mPayment;
	
	public MPOSPayment(Context c){
		mContext = c;
		mPayment = new PaymentDetail(c);
	}
	
	public float getTotalPaid(int transactionId, int computerId){
		return mPayment.getTotalPaid(transactionId, computerId);
	}
	
	public List<Payment.PaymentDetail> listPayment(int transactionId, int computerId){
		return mPayment.listPayment(transactionId, computerId);
	}
	
	public boolean deleteAllPaymentDetail(int transactionId, int computerId){
		return mPayment.deleteAllPaymentDetail(transactionId, computerId);
	}
	
	public boolean deletePaymentDetail(int paymentId){
		return mPayment.deletePaymentDetail(paymentId);
	}
	
	public boolean addPaymentDetail(int transactionId, int computerId, int payTypeId,
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
