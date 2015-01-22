package com.synature.mpos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

public class SaleSenderService extends Service{

	public static final String TAG = SaleSenderService.class.getSimpleName();
	
	public static final int SEND_PARTIAL_SALE = 1;
	public static final int SEND_ENDDAY = 2;
	
	public static final int RESULT_ERROR = 0;
	public static final int RESULT_SUCCESS = 1;
	
	public static boolean sIsRunning = false;
	
	private ExecutorService mExecutor;
	private ResultReceiver mReceiver;
	
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mExecutor = Executors.newFixedThreadPool(5);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mExecutor.shutdown();
		Log.i(TAG, "Stop sale sender service");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Call start command as id " + startId);
		if(intent != null){
			mReceiver = (ResultReceiver) intent.getParcelableExtra("sendSaleReceiver");
			int what = intent.getIntExtra("what", 1);
			mShopId = intent.getIntExtra("shopId", 0);
			mComputerId = intent.getIntExtra("computerId", 0);
			mStaffId = intent.getIntExtra("staffId", 0);
			if(what == SEND_PARTIAL_SALE){
				sendPartialSale();
			}else if(what == SEND_ENDDAY){
				sendUnSendEndday();
			}
		}
		return START_NOT_STICKY;
	}

	private class SendSaleReceiver extends ResultReceiver{

		private TransactionDao transDao;
		private String sessionDate;
		private String jsonSale;
		
		public SendSaleReceiver(Handler handler, TransactionDao trans, 
				String sessionDate, String jsonSale) {
			super(handler);
			transDao = trans;
			this.sessionDate = sessionDate;
			this.jsonSale = jsonSale;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				transDao.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				transDao.updateTransactionWasteSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				if(!TextUtils.isEmpty(jsonSale)){
					try {
						JSONSaleLogFile.appendSale(getApplicationContext(), jsonSale);
					} catch (Exception e) {}
					Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
							"Send partial successfully");
				}
				if(mReceiver != null){
					mReceiver.send(RESULT_SUCCESS, null);
				}
				break;
			case MPOSServiceBase.RESULT_ERROR:
				transDao.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				transDao.updateTransactionWasteSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				String msg = resultData.getString("msg");
				if(mReceiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					mReceiver.send(RESULT_ERROR, b);
				}
				break;
			}
			stopSelf();
		}
		
	}
	
	private void sendPartialSale(){
		TransactionDao transDao = new TransactionDao(getApplicationContext());
		SessionDao sessionDao = new SessionDao(getApplicationContext());
		String sessionDate = sessionDao.getLastSessionDate();
		if(!TextUtils.isEmpty(sessionDate)){
			JSONSaleGenerator jsonGenerator = 
					new JSONSaleGenerator(getApplicationContext());
			String jsonSale = jsonGenerator.generateSale(sessionDate);
			mExecutor.execute(new PartialSaleSender(getApplicationContext(), 
					mShopId, mComputerId, mStaffId, jsonSale,
					new SendSaleReceiver(new Handler(), transDao, sessionDate, jsonSale)));
		}else{
			stopSelf();
		}
	}
	
	private class SendUnSendEnddayReceiver extends ResultReceiver{

		private SessionDao sessionDao;
		private TransactionDao transDao;
		private String sessionDate;
		private String jsonSale;
		
		public SendUnSendEnddayReceiver(Handler handler, SessionDao sessionDao, 
				TransactionDao transDao, String sessionDate, String jsonSale) {
			super(handler);
			this.sessionDao = sessionDao;
			this.transDao = transDao;
			this.sessionDate = sessionDate;
			this.jsonSale = jsonSale;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				sessionDao.updateSessionEnddayDetail(sessionDate, MPOSDatabase.ALREADY_SEND);
				transDao.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				transDao.updateTransactionWasteSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				try {
					JSONSaleLogFile.appendEnddaySale(getApplicationContext(), sessionDate, jsonSale);
				} catch (Exception e) {}
				Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
						"Send unsend endday successfully.");
				sendUnSendEndday();
				break;
			case MPOSServiceBase.RESULT_ERROR:
				sessionDao.updateSessionEnddayDetail(sessionDate, MPOSDatabase.NOT_SEND);
				transDao.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				transDao.updateTransactionWasteSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				String msg = resultData.getString("msg");
				Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
						"Send unsend endday fail: " + msg + "\n" + jsonSale);
				if(mReceiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					mReceiver.send(RESULT_ERROR, b);
				}
				break;
			}
		}
		
	}
	
	/**
	 * Send unsend endday
	 */
	private void sendUnSendEndday(){
		final SessionDao sessionDao = new SessionDao(getApplicationContext());
		final TransactionDao transDao = new TransactionDao(getApplicationContext());
		String sessionDate = sessionDao.getUnSendSessionEndday();
		if(!TextUtils.isEmpty(sessionDate)){
			JSONSaleGenerator jsonGenerator = new JSONSaleGenerator(this);
			String jsonEndday = jsonGenerator.generateEnddaySale(sessionDate);
			mExecutor.execute(new EndDaySaleSender(getApplicationContext(), mShopId, mComputerId, mStaffId, jsonEndday,
					new SendUnSendEnddayReceiver(new Handler(), sessionDao, transDao, sessionDate, jsonEndday)));
		}else{
			if(mReceiver != null)
				mReceiver.send(RESULT_SUCCESS, null);
			stopSelf();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
