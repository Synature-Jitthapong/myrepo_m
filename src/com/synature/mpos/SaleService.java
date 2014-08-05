package com.synature.mpos;

import java.util.Iterator;
import java.util.List;

import com.synature.mpos.Utils.LoadSaleTransaction;
import com.synature.mpos.Utils.LoadSaleTransactionListener;
import com.synature.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.util.Logger;

import android.app.Service;
import android.content.Intent;
import android.database.SQLException;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

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
	 * @param staffId
	 * @param shopId
	 * @param computerId
	 * @param listener
	 */
	public void sendEnddaySale(final int staffId, final int shopId, final int computerId, 
			final ProgressListener listener){
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
					if(jsonSale != null && !TextUtils.isEmpty(jsonSale)){
						
						new MPOSWebServiceClient.SendSaleTransaction(getApplicationContext(),
								SendSaleTransaction.SEND_SALE_TRANS_METHOD,
								shopId, computerId, staffId, jsonSale, new ProgressListener() {
		
									@Override
									public void onError(String mesg) {
										sess.updateSessionEnddayDetail(sessionDate, 
												MPOSDatabase.NOT_SEND);
										Transaction trans = new Transaction(getApplicationContext());
										trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
										Utils.logServerResponse(getApplicationContext(), mesg);
										listener.onError(mesg);
									}
		
									@Override
									public void onPre() {
										listener.onPre();
									}
		
									@Override
									public void onPost() {
										try {
											sess.updateSessionEnddayDetail(sessionDate, 
													MPOSDatabase.ALREADY_SEND);
											Transaction trans = new Transaction(getApplicationContext());
											trans.updateTransactionSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
											// log json sale if send to server success
											JSONSaleLogFile.appendEnddaySale(getApplicationContext(), sessionDate, jsonSale);
											if(!it.hasNext()){
												listener.onPost();
											}
										} catch (SQLException e) {
											Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
													Utils.LOG_FILE_NAME, 
													" Send endday sale error " 
													+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL + " : "
													+ e.getMessage());
										}
									}
								}).execute(Utils.getFullUrl(getApplicationContext()));
					}
				}

				@Override
				public void onError(String mesg) {
					listener.onError(mesg);
				}

				@Override
				public void onPre() {
					listener.onPre();
				}

				@Override
				public void onPost() {
				}

			}).execute();
		}	
	}
	
	/**
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param sendAll
	 * @param listener
	 */
	public void sendSale(final int shopId, final int transactionId, final int computerId, 
			final int staffId, final ProgressListener listener) {
		Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
				Utils.LOG_FILE_NAME, 
				TAG + ": Start Send PartialSale \n"
				+ "staffId=" + staffId + "\n"
				+ "shopId=" + shopId + "\n"
				+ "transactionId=" + transactionId + "\n"
				+ "computerId=" + computerId);
		
		Session session = new Session(getApplicationContext());
		final String sessionDate = session.getCurrentSessionDate();
		new LoadSaleTransaction(getApplicationContext(), sessionDate,
				transactionId, new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans) {

				final String jsonSale = Utils.generateJSONSale(getApplicationContext(), saleTrans);
				
				Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, jsonSale);
				
				if(jsonSale != null && !TextUtils.isEmpty(jsonSale)){
					
					new MPOSWebServiceClient.SendPartialSaleTransaction(getApplicationContext(), 
							shopId, computerId, staffId, jsonSale, new ProgressListener() {
						@Override
						public void onPre() {
						}

						@Override
						public void onPost() {
							// do update transaction already send
							Transaction trans = new Transaction(getApplicationContext());
							trans.updateTransactionSendStatus(transactionId, MPOSDatabase.ALREADY_SEND);
							
							// log partial sale json when send to server success
							JSONSaleLogFile.appendSale(getApplicationContext(), jsonSale);
							
							Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
									Utils.LOG_FILE_NAME, 
									TAG + ": Send PartialSale Complete");
							
							listener.onPost();
						}

						@Override
						public void onError(String msg) {
							Transaction trans = new Transaction(getApplicationContext());
							trans.updateTransactionSendStatus(transactionId, MPOSDatabase.NOT_SEND);
							Utils.logServerResponse(getApplicationContext(), msg);
							listener.onError(msg);
						}
					}).execute(Utils.getFullUrl(getApplicationContext()));
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
