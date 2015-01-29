package com.synature.mpos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.util.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

public class SaleSenderService extends SaleSenderServiceBase{

	public static final String TAG = SaleSenderService.class.getSimpleName();
	
	public static final int SEND_PARTIAL = 1;
	public static final int SEND_CLOSE_SHIFT = 2;
	
	public static final String RECEIVER_NAME = "saleSenderReceiver";
	
	private ExecutorService mExecutor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mExecutor = Executors.newFixedThreadPool(THREAD_NUM);
		Log.i(TAG, "Create " + TAG);
	}

	@Override
	public void onDestroy() {
		mExecutor.shutdown();
		Log.i(TAG, "Destroy " + TAG);
		super.onDestroy();
	}

	/**
	 * Intent request 
	 * ResultReceiver, 
	 * sessionDate, 
	 * shopId, 
	 * computerId, 
	 * staffId  
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Call start command as id " + startId);
		if(intent != null){
			ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_NAME);
			String sessionDate = intent.getStringExtra(SESSION_DATE_PARAM);
			int whatToDo = intent.getIntExtra(WHAT_TO_DO_PARAM, SEND_PARTIAL);
			int shopId = intent.getIntExtra(SHOP_ID_PARAM, 0);
			int computerId = intent.getIntExtra(COMPUTER_ID_PARAM, 0);
			int staffId = intent.getIntExtra(STAFF_ID_PARAM, 0);
			if(whatToDo == SEND_PARTIAL){
				sendPartialSale(sessionDate, shopId, computerId, staffId, receiver);
			}else if(whatToDo == SEND_CLOSE_SHIFT){
				sendCloseShiftSale(shopId, computerId, staffId);
			}else{
				stopSelf();
			}
		}else{
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	/**
	 * @author j1tth4
	 * The receiver for send partial sale data
	 */
	private class SendSaleReceiver extends ResultReceiver{

		private ResultReceiver receiver;
		private String sessionDate;
		private String jsonSale;
		
		public SendSaleReceiver(Handler handler, String sessionDate, 
				String jsonSale, ResultReceiver receiver) {
			super(handler);
			this.sessionDate = sessionDate;
			this.jsonSale = jsonSale;
			this.receiver = receiver;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case RESULT_SUCCESS:
				flagSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				if(!TextUtils.isEmpty(jsonSale)){
					try {
						JSONSaleLogFile.appendSale(getApplicationContext(), jsonSale);
					} catch (Exception e) {}
					Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
							"Send partial successfully");
				}
				if(receiver != null){
					receiver.send(RESULT_SUCCESS, null);
				}
				break;
			case RESULT_ERROR:
				flagSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				String msg = resultData.getString("msg");
				if(receiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					receiver.send(RESULT_ERROR, b);
				}
				break;
			}
			stopSelf();
		}
		
	}
	
	/**
	 * Send partial sale data
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param receiver
	 */
	private void sendPartialSale(String sessionDate, int shopId, int computerId, 
			int staffId, ResultReceiver receiver){
		JSONSaleGenerator jsonGenerator = 
				new JSONSaleGenerator(getApplicationContext());
		String jsonSale = jsonGenerator.generateSale(sessionDate);
		mExecutor.execute(new PartialSaleSender(getApplicationContext(), 
				shopId, computerId, staffId, jsonSale,
				new SendSaleReceiver(new Handler(), sessionDate, jsonSale, receiver)));
	}
	
	/**
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 */
	private void sendCloseShiftSale(int shopId, int computerId, int staffId){
		SessionDao sessionDao = new SessionDao(getApplicationContext());
		String sessionDate = sessionDao.getLastSessionDate();
		if(!TextUtils.isEmpty(sessionDate)){
			JSONSaleGenerator jsonGenerator = 
					new JSONSaleGenerator(getApplicationContext());
			String jsonSale = jsonGenerator.generateCloseShiftSale(sessionDate);
			mExecutor.execute(new PartialSaleSender(getApplicationContext(), 
					shopId, computerId, staffId, jsonSale,
					new SendSaleReceiver(new Handler(), sessionDate, jsonSale, null)));
		}else{
			stopSelf();
		}
	}
	
	/**
	 * @param sessionDate
	 * @param status
	 */
	private void flagSendStatus(String sessionDate, int status){
		TransactionDao trans = new TransactionDao(getApplicationContext());
		trans.updateTransactionSendStatus(sessionDate, status);
		trans.updateTransactionWasteSendStatus(sessionDate, status);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
