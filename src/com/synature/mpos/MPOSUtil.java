package com.synature.mpos;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synature.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.synature.mpos.dao.Formater;
import com.synature.mpos.dao.MPOSDatabase;
import com.synature.mpos.dao.SaleTransaction;
import com.synature.mpos.dao.Session;
import com.synature.mpos.dao.Transaction;
import com.synature.mpos.dao.PaymentDetail.PaymentDetailTable;
import com.synature.mpos.dao.SaleTransaction.POSData_SaleTransaction;
import com.synature.mpos.dao.Session.SessionDetailTable;
import com.synature.mpos.dao.Session.SessionTable;
import com.synature.mpos.dao.Transaction.OrderDetailTable;
import com.synature.mpos.dao.Transaction.OrderSetTable;
import com.synature.mpos.dao.Transaction.OrderTransactionTable;
import com.synature.util.Logger;

public class MPOSUtil {
	
	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param sendAll
	 * @param listener
	 */
	public static void sendSale(final Context context, 
			final int shopId, final int computerId, 
			final int staffId, boolean sendAll, final ProgressListener listener) {
		
		Session session = new Session(context.getApplicationContext());
		final String sessionDate = session.getSessionDate();
		new LoadSaleTransaction(context.getApplicationContext(), sessionDate,
				sendAll, new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans) {

				final String jsonSale = generateJSONSale(context, saleTrans);
				
				if(jsonSale != null && !jsonSale.isEmpty()){
					new MPOSWebServiceClient.SendPartialSaleTransaction(context.getApplicationContext(), 
							staffId, shopId, computerId, jsonSale, new ProgressListener() {
						@Override
						public void onPre() {
						}

						@Override
						public void onPost() {
							// do update transaction already send
							Transaction trans = new Transaction(context.getApplicationContext());
							trans.updateTransactionSendStatus(sessionDate);
							listener.onPost();
						}

						@Override
						public void onError(String msg) {
							logServerResponse(context, msg);
							listener.onError(msg);
						}
					}).execute(MPOSApplication.getFullUrl(context));
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

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param sessionId
	 * @param staffId
	 * @param closeAmount
	 * @param isEndday
	 * @param listener
	 */
	public static void endday(final Context context, final int shopId, 
			final int computerId, final int sessionId,
			final int staffId, final double closeAmount,
			final boolean isEndday, final ProgressListener listener) {
		
		final Session sess = new Session(context.getApplicationContext());
		final Transaction trans = new Transaction(context.getApplicationContext());
		final String currentSaleDate = sess.getSessionDate();
		
		try {
			// add session endday
			sess.addSessionEnddayDetail(currentSaleDate,
					trans.getTotalReceipt(currentSaleDate),
					trans.getTotalReceiptAmount(currentSaleDate));
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// close session
		sess.closeSession(sessionId,
			staffId, closeAmount, isEndday);

		// list session that is not send
		List<String> sessLst = sess.listSessionEnddayNotSend();
		for(final String sessionDate : sessLst){
			/* 
			 * execute load transaction json task
			 * and send to hq 
			 */
			new LoadSaleTransaction(context, sessionDate, 
					true, new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans) {

					final String jsonSale = generateJSONSale(context, saleTrans);
					if(jsonSale != null && !jsonSale.isEmpty()){
						new MPOSWebServiceClient.SendSaleTransaction(context,
								SendSaleTransaction.SEND_SALE_TRANS_METHOD,
								staffId, shopId, computerId, jsonSale, new ProgressListener() {
		
									@Override
									public void onError(String mesg) {
										logServerResponse(context, mesg);
										listener.onError(mesg);
									}
		
									@Override
									public void onPre() {
									}
		
									@Override
									public void onPost() {
										try {
											sess.updateSessionEnddayDetail(sessionDate, 
													Session.ALREADY_ENDDAY_STATUS);
											listener.onPost();
										} catch (SQLException e) {
											Logger.appendLog(context, MPOSApplication.LOG_DIR, 
													MPOSApplication.LOG_FILE_NAME, 
													" Error when update " 
													+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL + " : "
													+ e.getMessage());
											listener.onError(e.getMessage());
										}
									}
								}).execute(MPOSApplication.getFullUrl(context));
					}else{
						listener.onError("Wrong json sale data");
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
	 * @param context
	 * @param saleTrans
	 * @return String JSON Sale
	 */
	private static String generateJSONSale(Context context,
			POSData_SaleTransaction saleTrans) {
		String jsonSale = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_SaleTransaction>() {
			}.getType();
			jsonSale = gson.toJson(saleTrans, type);
			Logger.appendLog(context, MPOSApplication.LOG_DIR,
					MPOSApplication.LOG_FILE_NAME,
					" JSON that send to the server : " + jsonSale);
		} catch (Exception e) {
			Logger.appendLog(context, MPOSApplication.LOG_DIR,
					MPOSApplication.LOG_FILE_NAME,
					" Error when generate json sale : " + e.getMessage());
		}
		return jsonSale;
	}
	
	/**
	 * @author j1tth4
	 * task for load OrderTransaction 
	 */
	public static class LoadSaleTransaction extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		protected LoadSaleTransactionListener mListener;
		protected SaleTransaction mSaleTrans;
			
		public LoadSaleTransaction(Context context, String sessionDate,
				boolean isListAll, LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(context, sessionDate, isListAll);
		}
		
		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(POSData_SaleTransaction saleTrans) {
			mListener.onPost(saleTrans);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener extends ProgressListener{
		void onPost(POSData_SaleTransaction saleTrans);
	}

	public static void makeToask(Context c, String msg){
		Toast toast = Toast.makeText(c, 
				msg, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * @param scale
	 * @param value
	 * @return string fixes digit
	 */
	public static String fixesDigitLength(Formater format, int scale, double value){
		return format.currencyFormat(rounding(scale, value), "#,##0.0000");
	}

	/**
	 * @param scale
	 * @param value
	 * @return rounding value
	 */
	public static double rounding(int scale, double value){
		BigDecimal big = new BigDecimal(value);
		return big.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * @param price
	 * @return rounding value
	 */
	public static double roundingPrice(double price){
		double result = price;
		long iPart;		// integer part
		double fPart;	// fractional part
		iPart = (long) price;
		fPart = price - iPart;
		if(fPart < 0.25){
			fPart = 0.0d;
		}else if(fPart >= 0.25 && fPart < 0.50){
			fPart = 0.25d;
		}else if(fPart >= 0.50 && fPart < 0.75){
			fPart = 0.50d;
		}else if(fPart == 0.75){
			fPart = 0.75d;
		}else if(fPart > 0.75){
			iPart += 1;
			fPart = 0.0d;
		}
		result = iPart + fPart;
		return result;
	}

	public static double stringToDouble(String text) throws ParseException{
		double value = 0.0d;
		NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
		Number num = format.parse(text);
		value = num.doubleValue();
		return value;
	}

	public static void logServerResponse(Context context, String msg){
		Logger.appendLog(context, MPOSApplication.LOG_DIR,
				MPOSApplication.LOG_FILE_NAME,
				" Server Response : " + msg);
	}
	
	public static void clearSale(Context context){
		MPOSDatabase.MPOSOpenHelper mSqliteHelper = 
				MPOSDatabase.MPOSOpenHelper.getInstance(context.getApplicationContext());
		SQLiteDatabase sqlite = mSqliteHelper.getWritableDatabase();
		sqlite.delete(OrderDetailTable.TABLE_ORDER, null, null);
		sqlite.delete(OrderDetailTable.TABLE_ORDER_TMP, null, null);
		sqlite.delete(OrderSetTable.TABLE_ORDER_SET, null, null);
		sqlite.delete(OrderTransactionTable.TABLE_ORDER_TRANS, null, null);
		sqlite.delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL, null, null);
		sqlite.delete(SessionTable.TABLE_SESSION, null, null);
		sqlite.delete(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, null, null);
	}
}
