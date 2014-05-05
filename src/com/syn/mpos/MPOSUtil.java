package com.syn.mpos;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.MPOSWebServiceClient.AuthenDeviceListener;
import com.syn.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.SaleTransactionDataSource;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.SyncSaleLogDataSource;
import com.syn.mpos.database.OrdersDataSource;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransactionDataSource.POSData_SaleTransaction;
import com.syn.mpos.database.table.OrderDetailTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.PaymentDetailTable;
import com.syn.mpos.database.table.SessionDetailTable;
import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.SyncSaleLogTable;

public class MPOSUtil {

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param transactionId
	 * @param staffId
	 * @param listener
	 */
	public static void doSendSaleBySpecificTransaction(final Context context,
			final int shopId, final int computerId, final int transactionId, 
			final int staffId, final ProgressListener listener) {
		new LoadSaleTransactionByTransactionId(context.getApplicationContext(), String.valueOf(Util.getDate().getTimeInMillis()),
				transactionId, new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans,
					final String sessionDate) {

				JSONUtil jsonUtil = new JSONUtil();
				Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
				final String jsonSale = jsonUtil.toJson(type, saleTrans);
 
				new MPOSWebServiceClient.SendPartialSaleTransaction(context.getApplicationContext(), 
						staffId, shopId, computerId, jsonSale, new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						OrdersDataSource trans = new OrdersDataSource(context.getApplicationContext());
						trans.updateTransactionSendStatus(transactionId);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				}).execute(MPOSApplication.getFullUrl(context));
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
	
	public static void doSendSale(final Context context, final int shopId, final int computerId, 
			final int staffId, final ProgressListener listener) {
		new LoadSaleTransaction(context.getApplicationContext(), String.valueOf(Util.getDate().getTimeInMillis()),
				new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans,
					final String sessionDate) {

				JSONUtil jsonUtil = new JSONUtil();
				Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
				final String jsonSale = jsonUtil.toJson(type, saleTrans);

				new MPOSWebServiceClient.SendPartialSaleTransaction(context.getApplicationContext(), 
						staffId, shopId, computerId, jsonSale, new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						OrdersDataSource trans = new OrdersDataSource(context.getApplicationContext());
						trans.updateTransactionSendStatus(sessionDate);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				}).execute(MPOSApplication.getFullUrl(context));
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
	public static void doEndday(final Context context, final int shopId, final int computerId, final int sessionId,
			final int closeStaffId, final double closeAmount,
			final boolean isEndday, final ProgressListener listener) {
		
		// sync sale log
		final SyncSaleLogDataSource syncSaleLog = 
				new SyncSaleLogDataSource(context.getApplicationContext());
		// check is main computer
		final ComputerDataSource computer = new ComputerDataSource(context.getApplicationContext());
		if (computer.checkIsMainComputer(computer.getComputerId())) {
			LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans,
						final String sessionDate) {

					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					final String jsonSale = jsonUtil.toJson(type, saleTrans);
					// Log.v("SaleTrans", saleJson);

					new MPOSWebServiceClient.SendSaleTransaction(context,
							SendSaleTransaction.SEND_SALE_TRANS_METHOD,
							closeStaffId, shopId, computerId, jsonSale, new ProgressListener() {

								@Override
								public void onError(String mesg) {
									syncSaleLog.updateSyncSaleLog(sessionDate, SyncSaleLogDataSource.SYNC_FAIL);
									listener.onError(mesg);
								}

								@Override
								public void onPre() {
								}

								@Override
								public void onPost() {
									syncSaleLog.updateSyncSaleLog(sessionDate, 
											SyncSaleLogDataSource.SYNC_SUCCESS);
									try {
										OrdersDataSource trans = new OrdersDataSource(context.getApplicationContext());
										SessionDataSource sess = new SessionDataSource(context.getApplicationContext());
										sess.addSessionEnddayDetail(sessionDate,
												trans.getTotalReceipt(sessionDate),
												trans.getTotalReceiptAmount(sessionDate));
										sess.closeSession(sessionId,
											closeStaffId, closeAmount, isEndday);
										listener.onPost();
									} catch (SQLException e) {
										listener.onError(e.getMessage());
									}
								}
							}).execute(MPOSApplication.getFullUrl(context));
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

			};

			// loop load sale by date that not successfully sync
			List<String> dateLst = syncSaleLog.listSessionDate();
			if (dateLst != null) {
				for (String date : dateLst) {
					new LoadSaleTransactionForEndday(context, date, loadSaleListener)
							.execute();
				}
			}else{
				new LoadSaleTransactionForEndday(context, String.valueOf(Util.getDate().getTimeInMillis()), 
						loadSaleListener).execute();
			}
		} else {
			listener.onError(context.getString(R.string.cannot_endday) + " "
					+ context.getString(R.string.because) + " "
					+ context.getString(R.string.not_main_computer));
		}
	}

	/**
	 * @author j1tth4
	 * task for load sale OrderTransaction by transactionId
	 */
	public static class LoadSaleTransactionByTransactionId extends LoadSaleTransaction{

		public LoadSaleTransactionByTransactionId(Context context, 
				String sessionDate, int transactionId, LoadSaleTransactionListener listener) {
			super(context, sessionDate, transactionId, listener);
		}

		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleSaleTransactionByTransactionId();
		}
		
	}
	
	/**
	 * @author j1tth4
	 * task for load OrderTransaction when endday
	 */
	public static class LoadSaleTransactionForEndday extends LoadSaleTransaction{

		public LoadSaleTransactionForEndday(Context context, 
				String sessionDate, LoadSaleTransactionListener listener) {
			super(context, sessionDate, listener);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listAllSaleTransactionInSaleDate();
		}
	}
	
	/**
	 * @author j1tth4
	 * task for load OrderTransaction 
	 */
	public static class LoadSaleTransaction extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		protected LoadSaleTransactionListener mListener;
		protected SaleTransactionDataSource mSaleTrans;
		protected String mSessionDate;
			
		public LoadSaleTransaction(Context context, String sessionDate, int transactionId,
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransactionDataSource(context, sessionDate, transactionId);
			mSessionDate = sessionDate;
		}
		
		public LoadSaleTransaction(Context context, String sessionDate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransactionDataSource(context, sessionDate);
			mSessionDate = sessionDate;
		}
		
		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(POSData_SaleTransaction saleTrans) {
			mListener.onPost(saleTrans, mSessionDate);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleTransaction();
		}
	}
	
	public static interface LoadSaleTransactionListener extends ProgressListener{
		void onPost(POSData_SaleTransaction saleTrans, String sessionDate);
	}
	
	public static void sendSaleData(final Context context, 
			int shopId, int computerId, int staffId){
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setTitle(R.string.send_sale_data);
		progress.setMessage(context.getString(R.string.send_sale_data_progress));
		MPOSUtil.doSendSale(context, shopId, computerId, staffId, new ProgressListener(){

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
	
	public static void clearSale(Context context){
		MPOSSQLiteHelper mSqliteHelper = MPOSSQLiteHelper.getInstance(context.getApplicationContext());
		SQLiteDatabase sqlite = mSqliteHelper.getWritableDatabase();
		sqlite.delete(OrderDetailTable.TABLE_ORDER, null, null);
		sqlite.delete(OrderDetailTable.TABLE_ORDER_TMP, null, null);
		sqlite.delete(OrderTransactionTable.TABLE_ORDER_TRANS, null, null);
		sqlite.delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL, null, null);
		sqlite.delete(SessionTable.TABLE_SESSION, null, null);
		sqlite.delete(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, null, null);	
		sqlite.delete(SyncSaleLogTable.TABLE_SYNC_LOG, null, null);
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
}
