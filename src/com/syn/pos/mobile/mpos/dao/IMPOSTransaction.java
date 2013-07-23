package com.syn.pos.mobile.mpos.dao;

public interface IMPOSTransaction {
	public int getMaxTransaction(int shopId, int computerId);

	public void openTransaction(int transactionId, int computerId, int shopId,
			int sessionId, int staffId);

	public void updateTransaction(int transactionId, int computerId,
			int staffId, double transVat, double transExclVat,
			double serviceCharge);

	public void closeTransaction();
}
