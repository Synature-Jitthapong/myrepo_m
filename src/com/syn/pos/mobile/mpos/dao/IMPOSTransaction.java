package com.syn.pos.mobile.mpos.dao;

import com.syn.pos.mobile.model.OrderTransaction;

public interface IMPOSTransaction {
	public int getMaxTransaction(int computerId);

	public int getMaxReceiptId(int transactionId, int computerId, int year,
			int month);

	public int getCurrTransaction(int computerId);

	public int openTransaction(int computerId, int shopId, int sessionId,
			int staffId);

	public void updateTransaction(int transactionId, int computerId,
			int staffId, float transVat, float transExclVat,
			float serviceCharge, float serviceChargeVat);

	public void cancelTransaction(int transactionId);
	
	public OrderTransaction.OrderDetail getSummary(int transactionId);
	
}
