package com.syn.mpos;

import java.lang.reflect.Type;
import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.MPOSService.SendSaleTransaction;
import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.SaleTransaction;
import com.syn.mpos.provider.Session;
import com.syn.mpos.provider.SyncSaleLog;
import com.syn.mpos.provider.Transaction;
import com.syn.mpos.provider.Util;
import com.syn.mpos.provider.SaleTransaction.POSData_SaleTransaction;

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
					final String sessionDate) {

				JSONUtil jsonUtil = new JSONUtil();
				Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
				String jsonSale = jsonUtil.toJson(type, saleTrans);

				ProgressListener sendSaleListener = new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						Transaction trans = 
								new Transaction(MPOSApplication.getWriteDatabase());
						trans.updateTransactionSendStatus(sessionDate);
						
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				};
				new MPOSService.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						staffId, jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransaction(String.valueOf(Util.getDate().getTimeInMillis()),
				loadSaleListener).execute();
	}
	
	public static void doEndday(final int computerId, final int sessionId,
			final int closeStaffId, final float closeAmount,
			final boolean isEndday, final ProgressListener listener) {
		
		// check is main computer
		Computer comp = new Computer(MPOSApplication.getReadDatabase());
		if (comp.checkIsMainComputer(computerId)) {
			final SyncSaleLog syncLog = 
					new SyncSaleLog(MPOSApplication.getWriteDatabase());
			LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans,
						final String sessionDate) {

					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					String jsonSale = jsonUtil.toJson(type, saleTrans);
					// Log.v("SaleTrans", saleJson);

					new MPOSService.SendSaleTransaction(
							SendSaleTransaction.SEND_SALE_TRANS_METHOD,
							closeStaffId, jsonSale, new ProgressListener() {

								@Override
								public void onError(String mesg) {
									syncLog.updateSyncSaleLog(sessionDate,
											SyncSaleLog.SYNC_FAIL);
									listener.onError(mesg);
								}

								@Override
								public void onPre() {
								}

								@Override
								public void onPost() {
									syncLog.updateSyncSaleLog(sessionDate,
											SyncSaleLog.SYNC_SUCCESS);

									try {
										Transaction trans = 
												new Transaction(MPOSApplication.getWriteDatabase());
										Session sess = 
												new Session(MPOSApplication.getWriteDatabase());
										sess.addSessionEnddayDetail(sessionDate,
												trans.getTotalReceipt(sessionDate),
												trans.getTotalReceiptAmount(sessionDate));
										sess.closeSession(sessionId, computerId,
											closeStaffId, closeAmount, isEndday);
										listener.onPost();
									} catch (SQLException e) {
										listener.onError(e.getMessage());
									}
								}
							}).execute(MPOSApplication.getFullUrl());
				}

				@Override
				public void onError(String mesg) {
					listener.onError(mesg);
				}

				@Override
				public void onPre() {
					listener.onPre();
				}

				@Override
				public void onPost() {
				}

			};

			// loop load sale by date that not successfully sync
			List<String> dateLst = syncLog.listSessionDate();
			if (dateLst != null) {
				for (String date : dateLst) {
					new LoadSaleTransactionForEndday(date, loadSaleListener)
							.execute();
				}
			}
		} else {
			Context context = MPOSApplication.getContext();
			listener.onError(context.getString(R.string.cannot_endday) + " "
					+ context.getString(R.string.because) + " "
					+ context.getString(R.string.not_main_computer));
		}
	}

	// load sale transaction for endday
	public static class LoadSaleTransactionForEndday extends LoadSaleTransaction{

		public LoadSaleTransactionForEndday(String sessionDate,
				LoadSaleTransactionListener listener) {
			super(sessionDate, listener);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listAllSaleTransactionInSaleDate();
		}
	}
	
	// load sale transaction for send realtime
	public static class LoadSaleTransaction extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		protected LoadSaleTransactionListener mListener;
		protected SaleTransaction mSaleTrans;
		protected String mSessionDate;
		
		public LoadSaleTransaction(String sessionDate, 
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
			mListener.onPost(saleTrans, mSessionDate);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener extends ProgressListener{
		void onPost(POSData_SaleTransaction saleTrans, String sessionDate);
	}
}
