package com.synature.mpos;

import java.util.Iterator;
import java.util.List;

import com.synature.mpos.Utils.LoadEndDayTransaction;
import com.synature.mpos.Utils.LoadEndDaySaleTransactionListener;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SaleTransaction.POSData_EndDaySaleTransaction;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class EnddaySaleService extends Service{
	
	public static final String TAG = EnddaySaleService.class.getSimpleName();
	
	private String mStatMsg; 
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Create Service");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final int staffId = intent.getIntExtra("staffId", 0);
		final int shopId = intent.getIntExtra("shopId", 0);
		final int computerId = intent.getIntExtra("computerId", 0);
		
		final Session sess = new Session(getApplicationContext());
		List<String> sessLst = sess.listSessionEnddayNotSend();
		if(sessLst.size() > 0){
			Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
				Utils.LOG_FILE_NAME, 
				TAG + ": Service Start Command \n"
				+ "staffId=" + staffId + "\n"
				+ "shopId=" + shopId + "\n"
				+ "computerId=" + computerId);
			final Iterator<String> it = sessLst.iterator();
			while(it.hasNext()){
				final String sessionDate = it.next();
				/* 
				 * execute load transaction json task
				 * and send to hq 
				 */
				new LoadEndDayTransaction(getApplicationContext(), sessionDate, 
						new LoadEndDaySaleTransactionListener() {
	
					@Override
					public void onPost(POSData_EndDaySaleTransaction enddaySale) {
	
						final String jsonEndDaySale = Utils.generateJSONEndDaySale(getApplicationContext(), enddaySale);
						
						if(jsonEndDaySale != null && !TextUtils.isEmpty(jsonEndDaySale)){
							
							new EndDaySaleTransactionSender(getApplicationContext(),
									shopId, computerId, staffId, jsonEndDaySale, new WebServiceWorkingListener() {
			
										@Override
										public void onError(String mesg) {
											Utils.logServerResponse(getApplicationContext(), mesg);
	
											sess.updateSessionEnddayDetail(sessionDate, 
													MPOSDatabase.NOT_SEND);
											Transaction trans = new Transaction(getApplicationContext());
											trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
										}
			
										@Override
										public void onPreExecute() {
										}
			
										@Override
										public void onPostExecute() {
											try {
												sess.updateSessionEnddayDetail(sessionDate, 
														MPOSDatabase.ALREADY_SEND);
												Transaction trans = new Transaction(getApplicationContext());
												trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);

												// log endday sale json when send to server success
												JSONSaleLogFile.appendEnddaySale(getApplicationContext(), sessionDate, jsonEndDaySale);
												
												if(!it.hasNext()){
													mStatMsg = getApplication().getString(R.string.send_sale_data_success);
													stopSelf();
												}
											} catch (SQLException e) {
												Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
														Utils.LOG_FILE_NAME, 
														" Error when update " 
														+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL + " : "
														+ e.getMessage());
											}
										}

										@Override
										public void onProgressUpdate(int value) {
											// TODO Auto-generated method stub
											
										}
									}).execute(Utils.getFullUrl(getApplicationContext()));
						}
					}
	
					@Override
					public void onError(String mesg) {
						mStatMsg = mesg;
					}
	
					@Override
					public void onPreExecute() {
					}
	
					@Override
					public void onPostExecute() {
					}

					@Override
					public void onProgressUpdate(int value) {
						// TODO Auto-generated method stub
						
					}
	
				}).execute();
			}
		}else{
			stopSelf();
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		if(!TextUtils.isEmpty(mStatMsg)){
			Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
					Utils.LOG_FILE_NAME, 
					TAG + ": Send Endday Complete");
			Utils.makeToask(getApplicationContext(), mStatMsg);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
