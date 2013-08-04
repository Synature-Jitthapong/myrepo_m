package com.syn.pos.mobile.mpos.dao;

import com.syn.pos.mobile.model.OrderTransaction;

public interface IMPOSTransaction {
	public long getMaxTransaction(int computerId);

	public long getMaxReceiptId(long transactionId, int computerId, int year,
			int month);

	public long getCurrTransaction(int computerId);

	public long openTransaction(int computerId, int shopId, int sessionId,
			int staffId);

	public void updateTransaction(long transactionId, int computerId,
			int staffId, double transVat, double transExclVat,
			double serviceCharge);

	public void cancelTransaction(long transactionId);
	
	public OrderTransaction.OrderDetail getSummary(long transactionId);
	
	public double calculateVat(double productPrice, double productAmount);
}
