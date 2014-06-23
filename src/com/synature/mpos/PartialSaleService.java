package com.synature.mpos;

import com.synature.mpos.MPOSUtil.LoadSaleTransaction;
import com.synature.mpos.MPOSUtil.LoadSaleTransactionListener;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PartialSaleService extends Service{
	
	public static final String TAG = PartialSaleService.class.getSimpleName();
	
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder{
		PartialSaleService getService(){
			return PartialSaleService.this;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void sendSale(final int shopId, final int computerId, 
			final int staffId, boolean sendAll, final ProgressListener listener) {
		
		Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_DIR, 
				MPOSApplication.LOG_FILE_NAME, 
				TAG + ": Start Send PartialSale \n"
				+ "staffId=" + staffId + "\n"
				+ "shopId=" + shopId + "\n"
				+ "computerId=" + computerId);
		
		Session session = new Session(getApplicationContext());
		final String sessionDate = session.getSessionDate();
		new LoadSaleTransaction(getApplicationContext(), sessionDate,
				sendAll, new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans) {

				final String jsonSale = MPOSUtil.generateJSONSale(getApplicationContext(), saleTrans);
				
				if(jsonSale != null && !jsonSale.isEmpty()){
					new MPOSWebServiceClient.SendPartialSaleTransaction(getApplicationContext(), 
							staffId, shopId, computerId, jsonSale, new ProgressListener() {
						@Override
						public void onPre() {
						}

						@Override
						public void onPost() {
							// do update transaction already send
							Transaction trans = new Transaction(getApplicationContext());
							trans.updateTransactionSendStatus(sessionDate);
							
							Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_DIR, 
									MPOSApplication.LOG_FILE_NAME, 
									TAG + ": Send PartialSale Complete");
							
							listener.onPost();
						}

						@Override
						public void onError(String msg) {
							MPOSUtil.logServerResponse(getApplicationContext(), msg);
							listener.onError(msg);
						}
					}).execute(MPOSApplication.getFullUrl(getApplicationContext()));
				}else{
					listener.onError("Wrong json sale data");
				}
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		}).execute();
	}
}
