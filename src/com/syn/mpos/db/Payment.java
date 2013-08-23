package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public interface Payment {
	int getMaxPaymentDetailId(int transactionId, int computerId);

	boolean addPaymentDetail(int transactionId, int computerId, int payTypeId,
			float payAmount, String creditCardNo, int expireMonth,
			int expireYear, int bankId, int creditCardTypeId);

	boolean updatePaymentDetail(int transactionId, int computerId,
			int payTypeId, float paymentAmount, String creditCardNo,
			int expireMonth, int expireYear, int bankId, int creditCardTypeId);

	boolean deletePaymentDetail(int paymentId);

	boolean deleteAllPaymentDetail(int transactionId, int computerId);
}
