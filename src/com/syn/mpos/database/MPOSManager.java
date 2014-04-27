package com.syn.mpos.database;

public interface MPOSManager {
	abstract int openTransaction(int computerId, int shopId, int sessionId, 
			int staffId, double vatRate);
	
}
