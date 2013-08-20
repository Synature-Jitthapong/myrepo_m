package com.syn.mpos;

public interface IMPOS {
	public void onInit();
	public void checkCurrentTransaction(long transactionId);
}
