package com.synature.mpos;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SaleService extends Service{
	
	public static final String TAG = SaleService.class.getSimpleName();
	
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder{
		SaleService getService(){
			return SaleService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param listener
	 */
	public synchronized void sendEnddaySale(final int shopId, final int computerId, final int staffId, 
			final WebServiceWorkingListener listener){
		new EnddayUnSendSaleExecutor(getApplicationContext(),
				shopId, computerId, staffId, new WebServiceWorkingListener(){

					@Override
					public void onPreExecute() {
						listener.onPreExecute();
					}

					@Override
					public void onProgressUpdate(int value) {
					}

					@Override
					public void onPostExecute() {
						listener.onPostExecute();
					}

					@Override
					public void onError(String msg) {
						sendAllEndday(shopId, computerId, staffId, listener);
					}
			
		}).execute();
	}
	
	/**
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param listener
	 */
	public synchronized void sendAllEndday(final int shopId, final int computerId, final int staffId, 
			final WebServiceWorkingListener listener){
		new EnddaySenderExecutor(getApplicationContext(), 
				shopId, computerId, staffId, listener).execute();	
	}
	
	/**
	 * @param shopId
	 * @param sessionId
	 * @param transactionId
	 * @param computerId
	 * @param staffId
	 * @param listener
	 */
	public synchronized void sendSale(int shopId, int sessionId, int transactionId, 
			int computerId, int staffId, WebServiceWorkingListener listener) {
		new PartialSaleSenderExcecutor(getApplicationContext(),
				sessionId, transactionId, shopId, computerId, staffId, listener).execute();
	}
}
