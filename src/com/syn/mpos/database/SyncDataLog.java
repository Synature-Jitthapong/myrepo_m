package com.syn.mpos.database;

public class SyncDataLog {
	public static final int SYNC_FAIL = 0;
	public static final int SYNC_SUCCESS = 1;
	
	public static final String TB_SYNC_TRANS = "SyncTransactionLog";
	public static final String COL_BEGIN_SYNC_TIME = "BeginSyncTime";
	public static final String COL_FINISH_SYNC_TIME = "FinishSyncTime";
	public static final String COL_SYNC_STATUS = "SyncStatus";
}
