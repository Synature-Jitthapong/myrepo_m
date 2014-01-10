package com.syn.mpos;

import java.lang.reflect.Type;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.SaleTransaction;
import com.syn.mpos.database.SyncSaleLog;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;

public class MPOSUtil {
	
	public static void sendRealTimeSale(final int staffId, 
			final int transactionId, final int computerId){
		final LoadSaleTransactionListener loadSaleListener = 
				new LoadSaleTransactionListener(){

					@Override
					public void loadSuccess(POSData_SaleTransaction saleTrans,
							long sessionDate) {
						
						JSONUtil jsonUtil = new JSONUtil();
						Type type = 
								new TypeToken<POSData_SaleTransaction>() {}.getType();
						String jsonSale = jsonUtil.toJson(type, saleTrans);
						
						MPOSService.OnServiceProcessListener sendSaleListener = 
								new MPOSService.OnServiceProcessListener() {
									
									@Override
									public void onSuccess() {
										// do update transaction already send
										Transaction trans = new Transaction(MPOSApplication.getContext());
										trans.updateTransactionSendStatus(transactionId, computerId);
									}
									
									@Override
									public void onError(String msg) {
										Log.e("MPOSUtil", msg);
									}
								};
								
						MPOSService service = new MPOSService();
						service.sendPartialSaleTransaction(staffId, jsonSale, sendSaleListener);
					}

					@Override
					public void loadFail(String mesg) {
						// TODO Auto-generated method stub
						
					}
			
		};
		
		new LoadSaleTransactionTask(Util.getDate().getTimeInMillis(), 
				loadSaleListener).execute();
	}
	
	public static void doEndday(final Context c, int computerId, 
			int sessionId, final int closeStaffId, float closeAmount, 
			boolean isEndday, final OnEnddayListener listener){
		
		Session sess = new Session(c);
		Transaction trans = new Transaction(c);
		
		if(sess.closeSession(sessionId, computerId, 
				closeStaffId, closeAmount, isEndday)){
			long enddayDate = sess.getSessionDate(sessionId, computerId);
		
			sess.addSessionEnddayDetail(enddayDate, 
					trans.getTotalReceipt(enddayDate),
					trans.getTotalReceiptAmount(enddayDate));
			
			final SyncSaleLog syncLog = new SyncSaleLog(c);
			// add sync log
			syncLog.addSyncSaleLog(enddayDate);
			
			final LoadSaleTransactionListener loadSaleListener = 
					new LoadSaleTransactionListener(){

				@Override
				public void loadSuccess(POSData_SaleTransaction saleTrans, final long sessionDate) {
					JSONUtil jsonUtil = new JSONUtil();
					Type type = 
							new TypeToken<POSData_SaleTransaction>() {}.getType();
					String jsonSale = jsonUtil.toJson(type, saleTrans);
					//Log.v("SaleTrans", saleJson);
					
					MPOSService.OnServiceProcessListener sendSaleServiceListener = 
							new MPOSService.OnServiceProcessListener() {
								
								@Override
								public void onSuccess() {
									// update synclog
									syncLog.updateSyncSaleLog(sessionDate, 
											SyncSaleLog.SYNC_SUCCESS);
									listener.enddaySuccess();
								}
								
								@Override
								public void onError(String mesg) {
									syncLog.updateSyncSaleLog(sessionDate, 
											SyncSaleLog.SYNC_FAIL);
									listener.enddayFail(mesg);
								}
					};
					
					// send sale service
					MPOSService service = new MPOSService();
					service.sendSaleDataTransaction(closeStaffId, 
							jsonSale, sendSaleServiceListener);
				}

				@Override
				public void loadFail(String mesg) {
					listener.enddayFail(mesg);
				}
				
			};
			
			// loop load sale by date that not successfully sync
			List<Long> dateLst = syncLog.listSessionDate();
			if(dateLst != null){
				for(long date : dateLst){
					new LoadSaleTransactionTask(date, loadSaleListener).execute();
				}
			}
		}
	}
	
	public static interface OnEnddayListener{
		void enddaySuccess();
		void enddayFail(String mesg);
	}
	
	// task for load transaction data
	public static class LoadSaleTransactionTask extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		private LoadSaleTransactionListener mListener;
		private SaleTransaction mSaleTrans;
		private long mSessionDate;
		
		public LoadSaleTransactionTask(long sessionDate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(MPOSApplication.getContext(), sessionDate);
			mSessionDate = sessionDate;
		}
		
		@Override
		protected void onPostExecute(POSData_SaleTransaction saleTrans) {
			if(saleTrans != null){
				mListener.loadSuccess(saleTrans, mSessionDate);
			}else{
				mListener.loadFail("");
			}
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener{
		void loadSuccess(POSData_SaleTransaction saleTrans, long sessionDate);
		void loadFail(String mesg);
	}
}
