package com.syn.mpos.database;

import com.syn.pos.ShopData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class MPOSManager {
	
	/*
	 * SQLiteOpenHelper
	 */
	private MPOSSQLiteHelper mSqliteHelper;
	
	/*
	 * Shop data source
	 */
	private ShopDataSource mShop;
	
	/*
	 * computer data source
	 */
	private ComputerDataSource mComputer;
	
	/*
	 * session data source
	 */
	private SessionDataSource mSession;
	
	/*
	 * transacton data source
	 */
	private OrderTransactionDataSource mTransaction;
	
	/*
	 * payment data source
	 */
	private PaymentDetailDataSource mPayment;
	
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
	
	public MPOSManager(Context context, int staffId){
		mStaffId = staffId;
		mSqliteHelper = MPOSSQLiteHelper.getInstance(context);
		SQLiteDatabase sqlite = open();
		
		mShop = new ShopDataSource(sqlite);
		mComputer = new ComputerDataSource(sqlite);
		mSession = new SessionDataSource(sqlite);
		mTransaction = new OrderTransactionDataSource(sqlite);
		mPayment = new PaymentDetailDataSource(sqlite);
	}
	
	/**
	 * This update transaction to status success.
	 * @return rows affected
	 * 0 if fail update
	 */
	public int closeTransaction(){
		return mTransaction.successTransaction(mTransactionId, mComputerId, mStaffId);
	}
	
	/**
	 * Open transaction
	 * @return current transactionId
	 */
	public int openTransaction(){
		return mTransaction.openTransaction(getComputerId(), getShopId(), 
				getCurrentSession(), mStaffId, getCompanyVatRate());
	}
		
	/**
	 * @return current sessionId
	 * 0 if not have session
	 */
	public int getCurrentSession(){
		return mSession.getCurrentSession(getComputerId(), mStaffId);
	}
	
	/**
	 * get company vat rate
	 * @return company vat rate
	 */
	public double getCompanyVatRate(){
		return getShop().getCompanyVat();
	}
	
	/**
	 * get shopId
	 * @return shopId
	 */
	public int getShopId(){
		return getShop().getShopID();
	}
	
	/**
	 * get computerId
	 * @return computerId
	 */
	public int getComputerId(){
		return getComputer().getComputerID();
	}

	/**
	 * get shop object
	 * @return ShopData.ShopProperty
	 */
	public ShopData.ShopProperty getShop(){
		return mShop.getShopProperty();
	}
	
	/**
	 * get computer object
	 * @return ShopData.ComputerProperty
	 */
	public ShopData.ComputerProperty getComputer(){
		return mComputer.getComputerProperty();
	}
	
	/**
	 * Open database
	 * @return SQLiteDatabase
	 * @throws SQLiteException
	 */
	private SQLiteDatabase open() throws SQLiteException{
		return mSqliteHelper.getWritableDatabase();
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
