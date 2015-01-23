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

public class EnddaySenderService extends SaleSenderServiceBase{

	public static final String TAG = EnddaySenderService.class.getSimpleName();
	
	public static final int SEND_CURRENT = 1;
	public static final int SEND_ALL = 2;
	
	public static final String RECEIVER_NAME = "enddaySenderReceiver";
	
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_NAME);
			int whatToDo = intent.getIntExtra(WHAT_TO_DO_PARAM, SEND_CURRENT);
			int shopId = intent.getIntExtra(SHOP_ID_PARAM, 0);
			int computerId = intent.getIntExtra(COMPUTER_ID_PARAM, 0);
			int staffId = intent.getIntExtra(STAFF_ID_PARAM, 0);
			if(whatToDo == SEND_ALL){
				sendUnSendEndday(shopId, computerId, staffId, receiver);
			}else if(whatToDo == SEND_CURRENT){
				sendEndday(SEND_CURRENT, shopId, computerId, staffId, receiver);
			}else{
				stopSelf();
			}
		}else{
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	private class SendUnSendEnddayReceiver extends ResultReceiver{

		private ResultReceiver receiver;
		private String sessionDate;
		private String jsonSale;
		private int shopId;
		private int computerId;
		private int staffId;
		
		/**
		 * @param handler
		 * @param sessionDate
		 * @param jsonSale
		 * @param shopId
		 * @param computerId
		 * @param staffId
		 * @param receiver
		 */
		public SendUnSendEnddayReceiver(Handler handler, String sessionDate, String jsonSale, 
				int shopId, int computerId, int staffId, ResultReceiver receiver) {
			super(handler);
			this.sessionDate = sessionDate;
			this.jsonSale = jsonSale;
			this.receiver = receiver;
			this.shopId = shopId;
			this.computerId = computerId;
			this.staffId = staffId;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case RESULT_SUCCESS:
				flagSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				try {
					JSONSaleLogFile.appendEnddaySale(getApplicationContext(), sessionDate, jsonSale);
				} catch (Exception e) {}
				Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
						"Send unsend endday successfully.");
				sendUnSendEndday(shopId, computerId, staffId, receiver);
				break;
			case RESULT_ERROR:
				if(countTransUnSend(sessionDate) == 0){
					flagSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				}else{
					flagSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				}
				String msg = resultData.getString("msg");
				Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
						"Send unsend endday fail: " + msg + "\n" + jsonSale);
				if(receiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					receiver.send(RESULT_ERROR, b);
				}
				stopSelf();
				break;
			}
		}
		
	}
	
	/**
	 * @author j1tth4
	 * The receiver for send endday
	 */
	private class EnddayReceiver extends ResultReceiver{
		
		private ResultReceiver receiver;
		private String sessionDate;
		private String jsonEndday;
		private int sendMode;
		private int shopId;
		private int computerId;
		private int staffId;
		
		/**
		 * @param handler
		 * @param sessionDate
		 * @param jsonEndday
		 * @param shopId
		 * @param computerId
		 * @param staffId
		 * @param sendMode
		 * @param receiver
		 */
		public EnddayReceiver(Handler handler, String sessionDate, String jsonEndday, 
				int shopId, int computerId, int staffId, int sendMode, ResultReceiver receiver) {
			super(handler);
			this.sessionDate = sessionDate;
			this.jsonEndday = jsonEndday;
			this.receiver = receiver;
			this.shopId = shopId;
			this.computerId = computerId;
			this.staffId = staffId;
			this.sendMode = sendMode;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case RESULT_SUCCESS:
				flagSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
				try {
					JSONSaleLogFile.appendEnddaySale(getApplicationContext(), sessionDate, jsonEndday);
				} catch (Exception e) {}
				if(receiver != null){
					receiver.send(RESULT_SUCCESS, null);
				}
				stopSelf();
				break;
			case RESULT_ERROR:
				String msg = resultData.getString("msg");
				if(sendMode != SEND_ALL){
					// try again with send all
					sendEndday(SEND_ALL, shopId, computerId, staffId, receiver);
					Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
							"Send endday with unsend sale fail: " + msg + "\n" + jsonEndday);
				}else{
					// if send all not work 
					flagSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
					Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME, 
							"Send all endday fail: " + msg + "\n" + jsonEndday);
					stopSelf();
				}
				break;
			}
		}
		
	}

	/**
	 * Send current endday data
	 * @param sendMode
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param receiver
	 */
	private void sendEndday(int sendMode, int shopId, int computerId, int staffId, ResultReceiver receiver){
		SessionDao session = new SessionDao(getApplicationContext());
		JSONSaleGenerator jsonGenerator = new JSONSaleGenerator(this);
		String sessionDate = session.getLastSessionDate();
		String jsonEndday;
		if(sendMode == SEND_CURRENT){ 
			jsonEndday = jsonGenerator.generateEnddayUnSendSale(sessionDate);
			if(!TextUtils.isEmpty(jsonEndday)){
				EndDayUnSendSaleSender sender = new EndDayUnSendSaleSender(
						getApplicationContext(), 
						shopId, computerId, staffId, jsonEndday, 
						new EnddayReceiver(new Handler(), 
								sessionDate, jsonEndday, shopId, 
								computerId, staffId, sendMode, receiver));
				mExecutor.execute(sender);
			}
		}else if(sendMode == SEND_ALL){
			jsonEndday = jsonGenerator.generateEnddaySale(sessionDate);
			if(!TextUtils.isEmpty(jsonEndday)){
				EndDaySaleSender sender = new EndDaySaleSender(
						getApplicationContext(), 
						shopId, computerId, staffId, jsonEndday, 
						new EnddayReceiver(new Handler(), 
								sessionDate, jsonEndday, shopId, 
								computerId, staffId, sendMode, receiver));
				mExecutor.execute(sender);
			}
		}
	}
	
	/**
	 * Send unsend endday data
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param receiver
	 */
	private void sendUnSendEndday(int shopId, int computerId, int staffId, ResultReceiver receiver){
		SessionDao sessionDao = new SessionDao(getApplicationContext());
		String sessionDate = sessionDao.getUnSendSessionEndday();
		if(!TextUtils.isEmpty(sessionDate)){
			JSONSaleGenerator jsonGenerator = new JSONSaleGenerator(this);
			String jsonEndday = jsonGenerator.generateEnddaySale(sessionDate);
			EndDaySaleSender sender = new EndDaySaleSender(
					getApplicationContext(), 
					shopId, computerId, staffId, jsonEndday,
					new SendUnSendEnddayReceiver(
							new Handler(), 
							sessionDate, jsonEndday,
							shopId, computerId, staffId, receiver));
			mExecutor.execute(sender);
		}else{
			if(receiver != null)
				receiver.send(RESULT_SUCCESS, null);
			stopSelf();
		}
	}
	
	/**
	 * @param sessionDate
	 * @return total unsend transaction
	 */
	private int countTransUnSend(String sessionDate){
		TransactionDao trans = new TransactionDao(getApplicationContext());
		return trans.countTransUnSend(sessionDate);
	}
	
	/**
	 * @param sessionDate
	 * @param status
	 */
	private void flagSendStatus(String sessionDate, int status){
		SessionDao session = new SessionDao(getApplicationContext());
		TransactionDao trans = new TransactionDao(getApplicationContext());
		
		session.updateSessionEnddayDetail(sessionDate, status);
		trans.updateTransactionSendStatus(sessionDate, status);
		trans.updateTransactionWasteSendStatus(sessionDate, status);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
