package com.syn.pos.mobile.mpos.dao;

public interface POSPaymentDetail {
	public int getMaxPaymentDetailId(int transactionId, int computerId);
	public boolean addPaymentDetail(int transactionId, int computerId, 
			int payTypeId, float payAmount, String creditCardNo, 
			int expireMonth, int expireYear, int bankId, int creditCardTypeId);
	public boolean updatePaymentDetail(int transactionId, int computerId, 
			int payTypeId, float paymentAmount, String creditCardNo, 
			int expireMonth, int expireYear, int bankId, int creditCardTypeId);
	public void deletePaymentDetail(int paymentId);
	public void deleteAllPaymentDetail(int transactionId);
}
