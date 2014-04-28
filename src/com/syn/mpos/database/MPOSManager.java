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
	
	public MPOSManager(Context c, int staffId){
		mStaffId = staffId;
		mSqliteHelper = new MPOSSQLiteHelper(c);
		SQLiteDatabase sqlite = open();
		
		mShop = new ShopDataSource(sqlite);
		mComputer = new ComputerDataSource(sqlite);
		mSession = new SessionDataSource(sqlite);
		mTransaction = new OrderTransactionDataSource(sqlite);
		mPayment = new PaymentDetailDataSource(sqlite);
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
	
	public int getCurrentSession(){
		return mSession.getCurrentSession(getComputerId(), mStaffId);
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
}
