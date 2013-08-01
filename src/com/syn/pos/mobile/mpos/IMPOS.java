package com.syn.pos.mobile.mpos;

public interface IMPOS {
	public void onInit();
	public void checkCurrentTransaction(long transactionId);
}
