package com.synature.mpos;

import com.synature.mpos.database.SyncMasterLog;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MasterDataService extends Service{

	public static final String TAG = MasterDataService.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
				Utils.LOG_FILE_NAME, 
				TAG + ": Service Start Command");
		SyncMasterLog sync = new SyncMasterLog(getApplicationContext());
		if(!sync.IsAlreadySync()){
			MPOSWebServiceClient.authenDevice(getApplicationContext(), new MPOSWebServiceClient.AuthenDeviceListener() {
				
				@Override
				public void onPreExecute() {
				}
				
				@Override
				public void onPostExecute() {
				}
				
				@Override
				public void onError(String msg) {
				}
				
				@Override
				public void onPost(final int shopId) {
					// load shop data
					MPOSWebServiceClient.loadShopData(getApplicationContext(), shopId, new WebServiceWorkingListener(){
	
						@Override
						public void onPreExecute() {
						}
	
						@Override
						public void onPostExecute() {
							// load product datat
							MPOSWebServiceClient.loadProductData(getApplicationContext(), shopId, new WebServiceWorkingListener(){
	
								@Override
								public void onPreExecute() {
								}
	
								@Override
								public void onPostExecute() {
									Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
											Utils.LOG_FILE_NAME, 
											TAG + ": Service Success.");
									stopSelf();
								}
	
								@Override
								public void onError(String msg) {
								}
								
							});
						}
	
						@Override
						public void onError(String msg) {
						}
						
					});
				}
			});
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
