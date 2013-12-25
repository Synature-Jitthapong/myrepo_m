package com.syn.mpos;

import java.lang.reflect.Type;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.SaleTransaction;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.syn.mpos.database.transaction.Session;

public class MPOSUtil {
	
	public static void doEndday(Context c, int sessionId, int computerId, int closeStaffId,
			float closeAmount, boolean isEndday){
		Session sess = new Session(c);
		if(sess.closeSession(sessionId, computerId, closeStaffId, closeAmount, isEndday)){
			new LoadSaleTransactionTask(c, Util.getDate().getTimeInMillis(), new LoadSaleTransactionListener(){

				@Override
				public void loadSuccess(POSData_SaleTransaction saleTrans) {
					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					String saleJson = jsonUtil.toJson(type, saleTrans);
					Log.d("SaleTrans", saleJson);
				}

				@Override
				public void loadFail(String mesg) {
					// TODO Auto-generated method stub
					
				}
				
			}).execute();
		}
	}
	
	// task for load transaction data
	public static class LoadSaleTransactionTask extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		private LoadSaleTransactionListener mListener;
		private Context mContext;
		private long mSaleDate;
		private ProgressDialog mProgress;
		
		public LoadSaleTransactionTask(Context c, long saleDate, 
				LoadSaleTransactionListener listener){
			mContext = c;
			mSaleDate = saleDate;
			mListener = listener;
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
			//mProgress.show();
		}

		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return SaleTransaction.listSaleTransaction(mContext, mSaleDate);
		}
	}
	
	public static interface LoadSaleTransactionListener{
		void loadSuccess(POSData_SaleTransaction saleTrans);
		void loadFail(String mesg);
	}
}
