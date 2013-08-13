package com.syn.pos.mobile.mpos.dao;

public interface IPaymentDetail {
	public int getMaxPaymentDetail(int transactionId, int computerId);
	public int addPaymentDetail(int paymentId, int transactionId, int computerId);
	public boolean updatePaymentDetail(int paymentId, int payTypeId, float paymentAmount, 
			String creditCardNo, int expireMonth, int expireYear, String bankName, 
			int creditCardTypeId);
}
