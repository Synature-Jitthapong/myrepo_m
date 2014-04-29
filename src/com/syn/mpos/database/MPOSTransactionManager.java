package com.syn.mpos.database;

import android.content.Context;

/**
 * @author j1tth4
 * Manage transaction, order, payment...
 */
public class MPOSTransactionManager {
	
	/*
	 * transacton data source
	 */
	private OrderTransactionDataSource mTransaction;
	
	/*
	 * staffId 
	 */
	private int mStaffId;
	
	/*
	 * transactionId
	 */
	private int mTransactionId;
	
	/*
	 * computerId
	 */
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
	
	public MPOSTransactionManager(Context context, int sessionId, 
			int shopId, int staffId, double companyVatRate){
		mShopId = shopId;
		mStaffId = staffId;
		mSessionId = sessionId;
		mCompanyVatRate = companyVatRate;
		mTransaction = new OrderTransactionDataSource(context);
	}
	
	public void closeTransaction(){
		mTransaction.successTransaction(mTransactionId, mStaffId);
	}
	
	public void openTransaction(){
		mTransaction.openTransaction(mShopId, mComputerId, 
				mSessionId, mStaffId, mCompanyVatRate);
	}

	public int getmStaffId() {
		return mStaffId;
	}

	public void setmStaffId(int mStaffId) {
		this.mStaffId = mStaffId;
	}

	public int getmTransactionId() {
		return mTransactionId;
	}

	public void setmTransactionId(int mTransactionId) {
		this.mTransactionId = mTransactionId;
	}

	public int getmComputerId() {
		return mComputerId;
	}

	public void setmComputerId(int mComputerId) {
		this.mComputerId = mComputerId;
	}

	public double getmTotalPrice() {
		return mTotalPrice;
	}

	public void setmTotalPrice(double mTotalPrice) {
		this.mTotalPrice = mTotalPrice;
	}

	public double getmTotalDiscount() {
		return mTotalDiscount;
	}

	public void setmTotalDiscount(double mTotalDiscount) {
		this.mTotalDiscount = mTotalDiscount;
	}

	public double getmTotalSalePrice() {
		return mTotalSalePrice;
	}

	public void setmTotalSalePrice(double mTotalSalePrice) {
		this.mTotalSalePrice = mTotalSalePrice;
	}

	public double getmTotalVat() {
		return mTotalVat;
	}

	public void setmTotalVat(double mTotalVat) {
		this.mTotalVat = mTotalVat;
	}
}
