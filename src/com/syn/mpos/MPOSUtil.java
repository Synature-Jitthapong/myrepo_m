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
import com.syn.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.MPOSShop;
import com.syn.mpos.database.MPOSTransaction;
import com.syn.mpos.database.SaleTransactionDataSource;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.ShopDataSource;
import com.syn.mpos.database.SyncSaleLogDataSource;
import com.syn.mpos.database.OrderTransactionDataSource;
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
	 * send sale transaction by specific transactionId
	 * @param sqlite
	 * @param shopId
	 * @param computerId
	 * @param transactionId
	 * @param staffId
	 * @param listener
	 */
	public static void doSendSaleBySpecificTransaction( 
			final int shopId, final int computerId, final int transactionId, 
			final int staffId, final ProgressListener listener) {
		final LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

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

				ProgressListener sendSaleListener = new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						OrderTransactionDataSource trans = new OrderTransactionDataSource(sqlite);
						trans.updateTransactionSendStatus(transactionId);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				};
				new MPOSWebServiceClient.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						staffId, shopId, computerId, jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransactionByTransactionId(sqlite, String.valueOf(Util.getDate().getTimeInMillis()),
				transactionId, loadSaleListener).execute();
	}
	
	/**
	 * TO DO send sale transaction
	 * @param sqlite
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param listener
	 */
	public static void doSendSale(final SQLiteDatabase sqlite, final int shopId, final int computerId, 
			final int staffId, final ProgressListener listener) {
		final LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

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

				ProgressListener sendSaleListener = new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						OrderTransactionDataSource trans = new OrderTransactionDataSource(sqlite);
						trans.updateTransactionSendStatus(sessionDate);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				};
				new MPOSWebServiceClient.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						staffId, shopId, computerId, jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransaction(sqlite, String.valueOf(Util.getDate().getTimeInMillis()),
				loadSaleListener).execute();
	}
	
	/**
	 * TO DO Endday
	 * @param sqlite
	 * @param shopId
	 * @param computerId
	 * @param sessionId
	 * @param closeStaffId
	 * @param closeAmount
	 * @param isEndday
	 * @param listener
	 */
	public static void doEndday(final int shopId, final int computerId, final int sessionId,
			final int closeStaffId, final double closeAmount,
			final boolean isEndday, final ProgressListener listener) {
		
		// check is main computer
		final MPOSShop shop = new MPOSShop(MPOSApplication.getContext());
		if (shop.checkIsMainComputer()) {
			LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans,
						final String sessionDate) {

					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					final String jsonSale = jsonUtil.toJson(type, saleTrans);
					// Log.v("SaleTrans", saleJson);

					new MPOSWebServiceClient.SendSaleTransaction(
							SendSaleTransaction.SEND_SALE_TRANS_METHOD,
							closeStaffId, shopId, computerId, jsonSale, new ProgressListener() {

								@Override
								public void onError(String mesg) {
									shop.updateSyncSaleLog(sessionDate, SyncSaleLogDataSource.SYNC_FAIL);
									listener.onError(mesg);
								}

								@Override
								public void onPre() {
								}

								@Override
								public void onPost() {
									shop.updateSyncSaleLog(sessionDate, 
											SyncSaleLogDataSource.SYNC_SUCCESS);
									try {
										OrderTransactionDataSource trans = new OrderTransactionDataSource(sqlite);
										SessionDataSource sess = new SessionDataSource(sqlite);
										sess.addSessionEnddayDetail(sessionDate,
												trans.getTotalReceipt(sessionDate),
												trans.getTotalReceiptAmount(sessionDate));
										sess.closeSession(sessionId, computerId,
											closeStaffId, closeAmount, isEndday);
										listener.onPost();
									} catch (SQLException e) {
										listener.onError(e.getMessage());
									}
								}
							}).execute(MPOSApplication.getFullUrl());
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
			List<String> dateLst = syncLog.listSessionDate();
			if (dateLst != null) {
				for (String date : dateLst) {
					new LoadSaleTransactionForEndday(sqlite, date, loadSaleListener)
							.execute();
				}
			}else{
				new LoadSaleTransactionForEndday(sqlite, String.valueOf(Util.getDate().getTimeInMillis()), 
						loadSaleListener).execute();
			}
		} else {
			Context context = MPOSApplication.getContext();
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

		public LoadSaleTransactionByTransactionId(final SQLiteDatabase sqlite, 
				String sessionDate, int transactionId, LoadSaleTransactionListener listener) {
			super(sqlite, sessionDate, transactionId, listener);
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

		public LoadSaleTransactionForEndday(SQLiteDatabase sqlite, 
				String sessionDate, LoadSaleTransactionListener listener) {
			super(sqlite, sessionDate, listener);
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
			
		public LoadSaleTransaction(SQLiteDatabase sqlite, String sessionDate, int transactionId,
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransactionDataSource(sqlite, sessionDate, transactionId);
			mSessionDate = sessionDate;
		}
		
		public LoadSaleTransaction(SQLiteDatabase sqlite, String sessionDate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransactionDataSource(sqlite, sessionDate);
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
	
	public static void sendSaleData(final SQLiteDatabase sqlite, final Context c, 
			int shopId, int computerId, int staffId){
		final ProgressDialog progress = new ProgressDialog(c);
		progress.setTitle(R.string.send_sale_data);
		progress.setMessage(c.getString(R.string.send_sale_data_progress));
		MPOSUtil.doSendSale(sqlite, shopId, computerId, staffId, new ProgressListener(){

			@Override
			public void onPre() {
				progress.show();
			}

			@Override
			public void onPost() {
				if(progress.isShowing())
					progress.dismiss();
				new AlertDialog.Builder(c)
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
				new AlertDialog.Builder(c)
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
	
	public static void clearSale(){
		MPOSSQLiteHelper mSqliteHelper = new MPOSSQLiteHelper(MPOSApplication.getContext());
		SQLiteDatabase sqlite = mSqliteHelper.getReadableDatabase();
		sqlite.delete(OrderDetailTable.TABLE_ORDER, null, null);
		sqlite.delete(OrderDetailTable.TABLE_ORDER_TMP, null, null);
		sqlite.delete(OrderTransactionTable.TABLE_NAME, null, null);
		sqlite.delete(PaymentDetailTable.TABLE_NAME, null, null);
		sqlite.delete(SessionTable.TABLE_NAME, null, null);
		sqlite.delete(SessionDetailTable.TABLE_NAME, null, null);	
		sqlite.delete(SyncSaleLogTable.TABLE_NAME, null, null);	
		mSqliteHelper.close();
	}
	
	public static void updateData(final SQLiteDatabase sqlite, final Context c){
		final ProgressDialog progress = new ProgressDialog(c);
		final MPOSWebServiceClient mPOSService = new MPOSWebServiceClient();
		mPOSService.loadShopData(sqlite, new ProgressListener(){

			@Override
			public void onPre() {
				progress.setTitle(R.string.update_data);
				progress.setMessage(c.getString(R.string.update_shop_progress));
				progress.show();
			}

			@Override
			public void onPost() {
				ShopDataSource shop = new ShopDataSource(sqlite);
				mPOSService.loadProductData(sqlite, 
						shop.getShopProperty().getShopID(), new ProgressListener(){

					@Override
					public void onPre() {
						progress.setMessage(c.getString(R.string.update_product_progress));
					}

					@Override
					public void onPost() {
						if(progress.isShowing())
							progress.dismiss();
						
						new AlertDialog.Builder(c)
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
						new AlertDialog.Builder(c)
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
				new AlertDialog.Builder(c)
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
