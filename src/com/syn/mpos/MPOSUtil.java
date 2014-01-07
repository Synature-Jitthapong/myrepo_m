package com.syn.mpos;

import java.lang.reflect.Type;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.SaleTransaction;
import com.syn.mpos.database.SyncSaleLog;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;

public class MPOSUtil {
	
	public static void doEndday(Context c, int sessionId, int computerId, int closeStaffId,
			float closeAmount, boolean isEndday, final OnEnddayListener listener){
		
		Session sess = new Session(c);
		Transaction trans = new Transaction(c);
		
		if(sess.closeSession(sessionId, computerId, closeStaffId, closeAmount, isEndday)){
			String sessionDate = sess.getSessionDate(sessionId, computerId);
		
			sess.addSessionEnddayDetail(sessionDate, trans.getTotalReceipt(sessionDate), 
					trans.getTotalReceiptAmount(sessionDate));
			
			// add sync log
			SyncSaleLog syncLog = new SyncSaleLog(c);
			syncLog.addSyncSaleLog(sessionDate);
			
			new LoadSaleTransactionTask(c, Util.getDate().getTimeInMillis(), new LoadSaleTransactionListener(){

				@Override
				public void loadSuccess(POSData_SaleTransaction saleTrans) {
					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					String saleJson = jsonUtil.toJson(type, saleTrans);
					//Log.v("SaleTrans", saleJson);
					
					listener.enddaySuccess();
				}

				@Override
				public void loadFail(String mesg) {
					listener.enddayFail(mesg);
				}
				
			}).execute();
		};
	}
	
	public static interface OnEnddayListener{
		void enddaySuccess();
		void enddayFail(String mesg);
	}
	
	// task for load transaction data
	public static class LoadSaleTransactionTask extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		private LoadSaleTransactionListener mListener;
		private SaleTransaction mSaleTrans;
		private Context mContext;
		private ProgressDialog mProgress;
		
		public LoadSaleTransactionTask(Context c, long sessionDate, 
				LoadSaleTransactionListener listener){
			mContext = c;
			mListener = listener;
			mSaleTrans = new SaleTransaction(c, sessionDate);
			mProgress = new ProgressDialog(c);
		}
		
		@Override
		protected void onPostExecute(POSData_SaleTransaction saleTrans) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			
			if(saleTrans != null){
				mListener.loadSuccess(saleTrans);
			}else{
				mListener.loadFail("");
			}
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(mContext.getString(R.string.load_sale_trans_progress));
			mProgress.show();
		}

		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener{
		void loadSuccess(POSData_SaleTransaction saleTrans);
		void loadFail(String mesg);
	}
}
