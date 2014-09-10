package com.synature.mpos;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author j1tth4
 * Service for send background sale data
 */
public class EnddaySaleService extends Service{
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final int staffId = intent.getIntExtra("staffId", 0);
		final int shopId = intent.getIntExtra("shopId", 0);
		final int computerId = intent.getIntExtra("computerId", 0);
		new EnddaySenderExecutor(getApplicationContext(), 
				shopId, computerId, staffId, new WebServiceWorkingListener(){

					@Override
					public void onPreExecute() {
					}

					@Override
					public void onProgressUpdate(int value) {
					}

					@Override
					public void onPostExecute() {
						stopSelf();
					}

					@Override
					public void onError(String msg) {
						stopSelf();
					}
			
		}).run();
		return START_REDELIVER_INTENT;
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
