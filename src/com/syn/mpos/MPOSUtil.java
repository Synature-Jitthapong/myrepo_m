package com.syn.mpos;

import java.lang.reflect.Type;
import java.util.List;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.MPOSService.SendSaleTransaction;
import com.syn.mpos.database.SaleTransaction;
import com.syn.mpos.database.SyncSaleLog;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;

public class MPOSUtil {
	public static void doSendSale(final int staffId,
			final ProgressListener listener) {
		final LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans,
					long sessionDate) {

				JSONUtil jsonUtil = new JSONUtil();
				Type type = new TypeToken<POSData_SaleTransaction>() {
				}.getType();
				String jsonSale = jsonUtil.toJson(type, saleTrans);

				ProgressListener sendSaleListener = new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						Transaction trans = new Transaction(
								MPOSApplication.getWriteDatabase());
						trans.updateTransactionSendStatus(String.valueOf(Util
								.getDate().getTimeInMillis()));
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						Log.e("MPOSUtil", msg);
						listener.onError(msg);
					}
				};
				new MPOSService.SendPartialSaleTransaction(
						MPOSApplication.getContext(), staffId, jsonSale,
						sendSaleListener);
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransactionTask(Util.getDate().getTimeInMillis(),
				loadSaleListener).execute();
	}
	
	public static void doEndday(int computerId, int sessionId,
			final int closeStaffId, float closeAmount, boolean isEndday,
			final ProgressListener listener) {

		Session sess = new Session(MPOSApplication.getWriteDatabase());
		Transaction trans = new Transaction(MPOSApplication.getWriteDatabase());

		if (sess.closeSession(sessionId, computerId, closeStaffId, closeAmount,
				isEndday)) {
			String enddayDate = sess.getSessionDate(sessionId, computerId);

			boolean canEndday = false;
			String enddayErr = "";
			try {
				canEndday = sess.addSessionEnddayDetail(enddayDate,
						trans.getTotalReceipt(enddayDate),
						trans.getTotalReceiptAmount(enddayDate));
			} catch (SQLException e) {
				enddayErr = e.getMessage();
			}

			if (canEndday) {
				// send sale process
				final SyncSaleLog syncLog = new SyncSaleLog(
						MPOSApplication.getWriteDatabase());
				// add sync log
				syncLog.addSyncSaleLog(enddayDate);

				final LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

					@Override
					public void onPost(POSData_SaleTransaction saleTrans,
							final long sessionDate) {
						JSONUtil jsonUtil = new JSONUtil();
						Type type = new TypeToken<POSData_SaleTransaction>() {
						}.getType();
						String jsonSale = jsonUtil.toJson(type, saleTrans);
						// Log.v("SaleTrans", saleJson);

						ProgressListener sendSaleListener = new ProgressListener() {

							@Override
							public void onError(String mesg) {
								syncLog.updateSyncSaleLog(sessionDate,
										SyncSaleLog.SYNC_FAIL);
								listener.onError(mesg);
							}

							@Override
							public void onPre() {
								// TODO Auto-generated method stub

							}

							@Override
							public void onPost() {
								syncLog.updateSyncSaleLog(sessionDate,
										SyncSaleLog.SYNC_SUCCESS);
								listener.onPost();
							}
						};

						new MPOSService.SendSaleTransaction(
								SendSaleTransaction.SEND_SALE_TRANS_METHOD,
								closeStaffId, jsonSale, sendSaleListener);
					}

					@Override
					public void onError(String mesg) {
						listener.onError(mesg);
					}

					@Override
					public void onPre() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onPost() {
						// TODO Auto-generated method stub

					}

				};

				// loop load sale by date that not successfully sync
				List<Long> dateLst = syncLog.listSessionDate();
				if (dateLst != null) {
					for (long date : dateLst) {
						new LoadSaleTransactionTask(date, loadSaleListener)
								.execute();
					}
				}
			} else {
				listener.onError(enddayErr);
			}
		}
	}

	// task for load transaction data
	public static class LoadSaleTransactionTask extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		private LoadSaleTransactionListener mListener;
		private SaleTransaction mSaleTrans;
		private long mSessionDate;
		
		public LoadSaleTransactionTask(long sessionDate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(MPOSApplication.getWriteDatabase(), sessionDate);
			mSessionDate = sessionDate;
		}
		
		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(POSData_SaleTransaction saleTrans) {
			if(saleTrans != null){
				mListener.onPost(saleTrans, mSessionDate);
			}
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener extends ProgressListener{
		void onPost(POSData_SaleTransaction saleTrans, long sessionDate);
	}
}
