package com.synature.mpos;

import java.lang.reflect.Type;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SaleTransaction;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.SaleTransaction.POSData_EndDaySaleTransaction;
import com.synature.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.synature.util.Logger;

public abstract class EnddayBase {
	
	protected Context mContext;

	protected TransactionDao mTrans;
	protected SessionDao mSession;
	protected SaleTransaction mSaleTrans;
	protected int mShopId;
	protected int mComputerId;
	protected int mStaffId;
	
	protected WebServiceWorkingListener mListener;
	
	public EnddayBase(Context context, int shopId, int computerId, int staffId, WebServiceWorkingListener listener){
		mContext = context;
		mSaleTrans = new SaleTransaction(context);
		mTrans = new TransactionDao(context);
		mSession = new SessionDao(context);
		mShopId = shopId;
		mComputerId = computerId;
		mStaffId = staffId;
		mListener = listener;
	}
	
	protected String generateSale(int transactionId, int sessionId){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getTransaction(transactionId, sessionId), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME,
					" Error at generate json sale : " + e.getMessage());
		}
		return json;
	}
	
	protected String generateEnddayUnSendSale(String sessionDate){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_EndDaySaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getEndDayUnSendTransaction(sessionDate), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME,
					" Error at generate json unsend end day : " + e.getMessage());
		}
		return json;
	}
	
	protected String generateEnddaySale(String sessionDate){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_EndDaySaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getEndDayTransaction(sessionDate), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME,
					" Error at generate json end day : " + e.getMessage());
		}
		return json;
	}
	
	protected void setFailStatus(String sessionDate){
		mSession.updateSessionEnddayDetail(sessionDate, MPOSDatabase.NOT_SEND);
		mTrans.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
	}
	
	protected void setSuccessStatus(String sessionDate){
		mSession.updateSessionEnddayDetail(sessionDate, MPOSDatabase.ALREADY_SEND);
		mTrans.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
	}
}
