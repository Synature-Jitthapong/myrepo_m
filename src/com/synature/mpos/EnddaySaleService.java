package com.synature.mpos;

import java.util.Iterator;
import java.util.List;

import com.synature.mpos.Utils.LoadSaleTransaction;
import com.synature.mpos.Utils.LoadSaleTransactionListener;
import com.synature.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
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
		
		Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_DIR, 
				MPOSApplication.LOG_FILE_NAME, 
				TAG + ": Service Start Command \n"
				+ "staffId=" + staffId + "\n"
				+ "shopId=" + shopId + "\n"
				+ "computerId=" + computerId);
		
		final Session sess = new Session(getApplicationContext());
		List<String> sessLst = sess.listSessionEnddayNotSend();
		final Iterator<String> it = sessLst.iterator();
		while(it.hasNext()){
			final String sessionDate = it.next();
			/* 
			 * execute load transaction json task
			 * and send to hq 
			 */
			new LoadSaleTransaction(getApplicationContext(), sessionDate, 
					true, new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans) {

					final String jsonSale = Utils.generateJSONSale(getApplicationContext(), saleTrans);
					if(jsonSale != null && !jsonSale.isEmpty()){
						new MPOSWebServiceClient.SendSaleTransaction(getApplicationContext(),
								SendSaleTransaction.SEND_SALE_TRANS_METHOD,
								staffId, shopId, computerId, jsonSale, new ProgressListener() {
		
									@Override
									public void onError(String mesg) {
										Utils.logServerResponse(getApplicationContext(), mesg);

										sess.updateSessionEnddayDetail(sessionDate, 
												MPOSDatabase.NOT_SEND);
										Transaction trans = new Transaction(getApplicationContext());
										trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
									}
		
									@Override
									public void onPre() {
									}
		
									@Override
									public void onPost() {
										try {
											sess.updateSessionEnddayDetail(sessionDate, 
													MPOSDatabase.ALREADY_SEND);
											Transaction trans = new Transaction(getApplicationContext());
											trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
											if(!it.hasNext()){
												mStatMsg = getApplication().getString(R.string.send_sale_data_success);
												stopSelf();
											}
										} catch (SQLException e) {
											Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_DIR, 
													MPOSApplication.LOG_FILE_NAME, 
													" Error when update " 
													+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL + " : "
													+ e.getMessage());
										}
									}
								}).execute(MPOSApplication.getFullUrl(getApplicationContext()));
					}
				}

				@Override
				public void onError(String mesg) {
					mStatMsg = mesg;
				}

				@Override
				public void onPre() {
				}

				@Override
				public void onPost() {
				}

			}).execute();
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_DIR, 
				MPOSApplication.LOG_FILE_NAME, 
				TAG + ": Send Endday Complete");
		Utils.makeToask(getApplicationContext(), mStatMsg);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
