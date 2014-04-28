package com.syn.mpos.database;

import android.database.sqlite.SQLiteDatabase;

public class MPOSManager {
	
	private SQLiteDatabase mSqlite;
	
	private int mShopId;
	private int mStaffId;
	private int mTransactionId;
	private int mComputerId;
	
	private double mTotalSalePrice;
}
