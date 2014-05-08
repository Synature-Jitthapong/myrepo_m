package com.syn.mpos;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.Logger;
import com.syn.mpos.MPOSWebServiceClient.AuthenDeviceListener;
import com.syn.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.SaleTransactionDataSource;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.TransactionDataSource;
import com.syn.mpos.database.SaleTransactionDataSource.POSData_SaleTransaction;
import com.syn.mpos.database.table.OrderDetailTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.PaymentDetailTable;
import com.syn.mpos.database.table.SessionDetailTable;
import com.syn.mpos.database.table.SessionTable;

public class MPOSUtil {
	
	/**
	 * @param context
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param listener
	 */
	public static void doSendSale(final Context context, 
			final int shopId, final int computerId, 
			final int staffId, final ProgressListener listener) {
		
		SessionDataSource session = new SessionDataSource(context.getApplicationContext());
		final String sessionDate = session.getSessionDate();
		new LoadSaleTransaction(context.getApplicationContext(), sessionDate,
				false, new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans) {

				final String jsonSale = generateJSONSale(context, saleTrans);
				
				if(jsonSale != null && !jsonSale.equals("")){
					new MPOSWebServiceClient.SendPartialSaleTransaction(context.getApplicationContext(), 
							staffId, shopId, computerId, jsonSale, new ProgressListener() {
						@Override
						public void onPre() {
						}

						@Override
						public void onPost() {
							// do update transaction already send
							TransactionDataSource trans = new TransactionDataSource(context.getApplicationContext());
							trans.updateTransactionSendStatus(sessionDate);
							listener.onPost();
						}

						@Override
						public void onError(String msg) {
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
	 * @param closeStaffId
	 * @param closeAmount
	 * @param isEndday
	 * @param listener
	 */
	public static void doEndday(final Context context, final int shopId, 
			final int computerId, final int sessionId,
			final int closeStaffId, final double closeAmount,
			final boolean isEndday, final ProgressListener listener) {

		final SessionDataSource sess = new SessionDataSource(context.getApplicationContext());
		final TransactionDataSource trans = new TransactionDataSource(context.getApplicationContext());
		final String sessionDate = sess.getSessionDate();
		// add session endday
		sess.addSessionEnddayDetail(sessionDate,
				trans.getTotalReceipt(sessionDate),
				trans.getTotalReceiptAmount(sessionDate));
		// close session
		sess.closeSession(sessionId,
			closeStaffId, closeAmount, isEndday);

		new LoadSaleTransaction(context, sessionDate, 
				true, new LoadSaleTransactionListener() {

			@Override
			public void onPost(POSData_SaleTransaction saleTrans) {

				final String jsonSale = generateJSONSale(context, saleTrans);
				if(jsonSale != null && !jsonSale.equals("")){
					new MPOSWebServiceClient.SendSaleTransaction(context,
							SendSaleTransaction.SEND_SALE_TRANS_METHOD,
							closeStaffId, shopId, computerId, jsonSale, new ProgressListener() {
	
								@Override
								public void onError(String mesg) {
									Logger.appendLog(context, MPOSApplication.LOG_DIR, 
											MPOSApplication.LOG_FILE_NAME, 
											" Error when send data to the server : " + mesg);
									listener.onError(mesg);
								}
	
								@Override
								public void onPre() {
								}
	
								@Override
								public void onPost() {
									sess.getWritableDatabase().beginTransaction();
									try {
										sess.updateSessionEnddayDetail(sessionDate, 
												SessionDataSource.ALREADY_ENDDAY_STATUS);
										sess.getWritableDatabase().setTransactionSuccessful();
										listener.onPost();
									} catch (SQLException e) {
										Logger.appendLog(context, MPOSApplication.LOG_DIR, 
												MPOSApplication.LOG_FILE_NAME, 
												" Error when update " 
												+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL + " : "
												+ e.getMessage());
										listener.onError(e.getMessage());
									} finally{
										sess.getWritableDatabase().endTransaction();
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
		protected SaleTransactionDataSource mSaleTrans;
			
		public LoadSaleTransaction(Context context, String sessionDate,
				boolean isListAll, LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransactionDataSource(context, sessionDate, isListAll);
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
	
	public static void sendSaleData(final Context context, 
			int shopId, int computerId, int staffId){
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setTitle(R.string.send_sale_data);
		progress.setMessage(context.getString(R.string.send_sale_data_progress));
		MPOSUtil.doSendSale(context, shopId, computerId, staffId, 
				new ProgressListener(){

			@Override
			public void onPre() {
				progress.show();
			}

			@Override
			public void onPost() {
				if(progress.isShowing())
					progress.dismiss();
				new AlertDialog.Builder(context)
					.setTitle(R.string.send_sale_data)
					.setMessage(R.string.send_sale_data_success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.show();
				
			}

			@Override
			public void onError(String msg) {
				if(progress.isShowing())
					progress.dismiss();
				new AlertDialog.Builder(context)
				.setTitle(R.string.send_sale_data)
				.setMessage(msg)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
			
		});	
	}
	
	public static void updateData(final Context context){
		final ProgressDialog progress = new ProgressDialog(context);
		final MPOSWebServiceClient mPOSService = new MPOSWebServiceClient();
		mPOSService.loadShopData(context, new AuthenDeviceListener(){

			@Override
			public void onPre() {
				progress.setTitle(R.string.update_data);
				progress.setMessage(context.getString(R.string.update_shop_progress));
				progress.show();
			}

			@Override
			public void onPost() {
			}

			@Override
			public void onPost(int shopId) {
				mPOSService.loadProductData(context, shopId, new ProgressListener(){

					@Override
					public void onPre() {
						progress.setMessage(context.getString(R.string.update_product_progress));
					}

					@Override
					public void onPost() {
						if(progress.isShowing())
							progress.dismiss();
						
						new AlertDialog.Builder(context)
						.setTitle(R.string.update_data)
						.setMessage(R.string.update_data_success)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}

					@Override
					public void onError(String msg) {
						if(progress.isShowing())
							progress.dismiss();
						new AlertDialog.Builder(context)
						.setTitle(R.string.update_data)
						.setMessage(msg)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}
					
				});
			}

			@Override
			public void onError(String msg) {
				if(progress.isShowing())
					progress.dismiss();
				new AlertDialog.Builder(context)
				.setTitle(R.string.update_data)
				.setMessage(msg)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
			
		});
	}
	
	public static void makeToask(Context c, String msg){
		Toast toast = Toast.makeText(c, 
				msg, Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static String fixesDigitLength(int length, double value){
		BigDecimal b = new BigDecimal(value);
		return b.setScale(length, BigDecimal.ROUND_HALF_UP).toString();
	}
	
	public static double stringToDouble(String text) throws ParseException{
		double value = 0.0d;
		NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
		Number num = format.parse(text);
		value = num.doubleValue();
		return value;
	}

	public static void clearSale(Context context){
		MPOSDatabase.MPOSOpenHelper mSqliteHelper = 
				MPOSDatabase.MPOSOpenHelper.getInstance(context.getApplicationContext());
		SQLiteDatabase sqlite = mSqliteHelper.getWritableDatabase();
		sqlite.delete(OrderDetailTable.TABLE_ORDER, null, null);
		sqlite.delete(OrderDetailTable.TABLE_ORDER_TMP, null, null);
		sqlite.delete(OrderTransactionTable.TABLE_ORDER_TRANS, null, null);
		sqlite.delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL, null, null);
		sqlite.delete(SessionTable.TABLE_SESSION, null, null);
		sqlite.delete(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, null, null);
	}
}
