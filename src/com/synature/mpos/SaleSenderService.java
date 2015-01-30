package com.synature.mpos;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.TransactionDao;
import com.synature.util.Logger;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextUtils;

public class SaleSenderService extends SaleSenderServiceBase{

	public static final String TAG = SaleSenderService.class.getSimpleName();
	
	public static final int SEND_PARTIAL = 1;
	public static final int SEND_LAST_SALE_TRANS = 2;
	
	public static final String RECEIVER_NAME = "saleSenderReceiver";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			ResultReceiver receiver = (ResultReceiver) b.getParcelable(RECEIVER_NAME);
			String sessionDate = b.getString(SESSION_DATE_PARAM);
			int whatToDo = b.getInt(WHAT_TO_DO_PARAM, SEND_PARTIAL);
			int shopId = b.getInt(SHOP_ID_PARAM, 0);
			int computerId = b.getInt(COMPUTER_ID_PARAM, 0);
			int staffId = b.getInt(STAFF_ID_PARAM, 0);
			if(whatToDo == SEND_PARTIAL){
				sendPartialSale(sessionDate, shopId, computerId, staffId, receiver);
			}else if(whatToDo == SEND_LAST_SALE_TRANS){
				sendLastSaleTransaction(sessionDate, shopId, computerId, staffId);
			}
		}
		
	}

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
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
		if(intent != null){
			Message msg = mServiceHandler.obtainMessage();
			msg.arg1 = startId;
			Bundle b = intent.getExtras();;
			msg.setData(b);
			mServiceHandler.sendMessage(msg);
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
		JSONSaleSerialization jsonGenerator = 
				new JSONSaleSerialization(getApplicationContext());
		String json = jsonGenerator.generateSale(sessionDate);
		new PartialSaleSender(getApplicationContext(), shopId, computerId,staffId, json, 
				new SendSaleReceiver(new Handler(), 
						sessionDate, json, receiver)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Utils.getFullUrl(getApplicationContext()));
	}
	
	/**
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 */
	private void sendLastSaleTransaction(String sessionDate, int shopId, 
			int computerId, int staffId){
		JSONSaleSerialization jsonGenerator = 
				new JSONSaleSerialization(getApplicationContext());
		String jsonSale = jsonGenerator.generateLastSaleTransaction(sessionDate);
		new PartialSaleSender(getApplicationContext(), shopId, computerId, staffId, jsonSale, 
				new SendSaleReceiver(new Handler(), 
						sessionDate, jsonSale, null)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Utils.getFullUrl(getApplicationContext()));
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
		// TODO Auto-generated method stub
		return null;
	}
}
