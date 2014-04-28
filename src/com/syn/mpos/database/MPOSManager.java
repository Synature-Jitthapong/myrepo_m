package com.syn.mpos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MPOSManager {
	
	private MPOSSQLiteHelper mSqliteHelper;
	
	private OrderTransactionDataSource mTransaction;
	
	private int mShopId;
	private int mStaffId;
	private int mTransactionId;
	private int mComputerId;
	
	/*
	 * total price
	 */
	private double mTotalPrice;
	
	/*
	 * total discount
	 */
	private double mTotalDiscount;
	
	/*
	 * total sale price
	 */
	private double mTotalSalePrice;
	
	/*
	 * total vat
	 */
	private double mTotalVat;
	
	public MPOSManager(Context c, int staffId){
		mSqliteHelper = new MPOSSQLiteHelper(c);
		
		
	}
	
	private SQLiteDatabase open(){
		return mSqliteHelper.getWritableDatabase();
	}
}
