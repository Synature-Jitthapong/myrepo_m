package com.synature.mpos;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.Transaction;
import com.synature.util.Logger;

import android.content.Context;
import android.text.TextUtils;

public class PartialSaleSenderExcecutor extends JSONSaleDataGenerator implements Runnable{

	private Transaction mTrans;
	
	private int mSessionId;
	private int mTransactionId;
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	private WebServiceWorkingListener mListener;
	
	public PartialSaleSenderExcecutor(Context context, int sessionId, int transactionId,
			int shopId, int computerId, int staffId, WebServiceWorkingListener listener) {
		super(context);
		mSessionId = sessionId;
		mTransactionId = transactionId;
		mShopId = shopId;
		mComputerId = computerId;
		mStaffId = staffId;
		mTrans = new Transaction(context);
		mListener = listener;
	}

	@Override
	public void run() {
		final String json = super.generateSale(mTransactionId, mSessionId);
		if(!TextUtils.isEmpty(json)){
			new PartialSaleSender(mContext, 
					mShopId, mComputerId, mStaffId, json, new WebServiceWorkingListener() {
				@Override
				public void onPreExecute() {
					Logger.appendLog(mContext, Utils.LOG_PATH, 
							Utils.LOG_FILE_NAME, "Start send partial " + json);
				}

				@Override
				public void onPostExecute() {
					mTrans.updateTransactionSendStatus(mTransactionId, MPOSDatabase.ALREADY_SEND);
					JSONSaleLogFile.appendSale(mContext, json);
					Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
							"Send partial successfully");
					mListener.onPostExecute();
				}

				@Override
				public void onError(String msg) {
					mTrans.updateTransactionSendStatus(mTransactionId, MPOSDatabase.NOT_SEND);
					Utils.logServerResponse(mContext, "Send patial fail : " + msg);
					mListener.onError(msg);
				}

				@Override
				public void onProgressUpdate(int value) {
				}
			}).execute(Utils.getFullUrl(mContext));
		}
	}

}
