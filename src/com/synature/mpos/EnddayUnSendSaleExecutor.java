package com.synature.mpos;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Transaction;
import com.synature.util.Logger;

import android.content.Context;
import android.text.TextUtils;

public class EnddayUnSendSaleExecutor extends JSONSaleDataGenerator implements Runnable{

	private Transaction mTrans;
	private Session mSession;
	
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	
	private WebServiceWorkingListener mListener;
	
	public EnddayUnSendSaleExecutor(Context context, int shopId, int computerId, 
			int staffId, WebServiceWorkingListener listener) {
		super(context);
		mTrans = new Transaction(context);
		mSession = new Session(context);
		mShopId = shopId;
		mComputerId = computerId;
		mStaffId = staffId;
		mListener = listener;
	}

	@Override
	public void run() {
		final String sessionDate = mSession.getLastSessionDate();
		final String json = generateEnddayUnSendSale(sessionDate);
		if(!TextUtils.isEmpty(json)){
			new EndDayUnSendSaleSender(mContext, mShopId, mComputerId,
					mStaffId, json, new WebServiceWorkingListener() {
	
						@Override
						public void onError(String mesg) {
							Utils.logServerResponse(mContext, " Send endday unsend trans fail " + mesg);
							mListener.onError(mesg);
						}
	
						@Override
						public void onPreExecute() {
							Logger.appendLog(mContext, Utils.LOG_PATH, 
									Utils.LOG_FILE_NAME, "Start send endday unsend transaction " + json);
							mListener.onPreExecute();
						}
	
						@Override
						public void onPostExecute() {
							mSession.updateSessionEnddayDetail(sessionDate, MPOSDatabase.ALREADY_SEND);
							mTrans.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
							Logger.appendLog(mContext, Utils.LOG_PATH, 
									Utils.LOG_FILE_NAME, "Send endday unsend trans successfully");
							mListener.onPostExecute();
						}
	
						@Override
						public void onProgressUpdate(int value) {
						}
				}).execute(Utils.getFullUrl(mContext));
		}
	}
}
