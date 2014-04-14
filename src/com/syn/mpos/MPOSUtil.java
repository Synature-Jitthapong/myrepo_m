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
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.OrderDetailTable;
import com.syn.mpos.database.OrderTransactionTable;
import com.syn.mpos.database.PaymentDetailTable;
import com.syn.mpos.database.SaleTransaction;
import com.syn.mpos.database.Session;
import com.syn.mpos.database.SessionDetailTable;
import com.syn.mpos.database.SessionTable;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.SyncSaleLog;
import com.syn.mpos.database.SyncSaleLogTable;
import com.syn.mpos.database.Transaction;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransaction.POSData_SaleTransaction;

public class MPOSUtil {
	public static LinearLayout createDetailColumn(Context c, String[] detailText){
		if(detailText.length < 1)
			return null;
		
		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout detailColumn = new LinearLayout(c);
		for(int i = 0; i < detailText.length; i++){
			TextView tvHeader = (TextView) inflater.inflate(R.layout.tv_column_detail, null);
			tvHeader.setText(detailText[i]);
			detailColumn.addView(tvHeader);
		}
		return detailColumn;
	}
	
	public static LinearLayout createHeaderColumn(Context c, String[] headerText){
		if(headerText.length < 1)
			return null;
		
		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout headerColumn = new LinearLayout(c);
		for(int i = 0; i < headerText.length; i++){
			TextView tvHeader = (TextView) inflater.inflate(R.layout.tv_column_header, null);
			tvHeader.setText(headerText[i]);
			headerColumn.addView(tvHeader);
		}
		return headerColumn;
	}
	
	public static void doSendSaleBySelectedTransaction(final SQLiteDatabase sqlite, 
			final int shopId, final int computerId, final int transactionId, 
			final int staffId, double vatRate, final ProgressListener listener) {
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
						Transaction trans = new Transaction(sqlite);
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
				transactionId, vatRate, loadSaleListener).execute();
	}
	
	public static void doSendSale(final SQLiteDatabase sqlite, final int shopId, final int computerId, 
			final int staffId, double vatRate, final ProgressListener listener) {
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
						Transaction trans = new Transaction(sqlite);
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
		new LoadSaleTransaction(sqlite, String.valueOf(Util.getDate().getTimeInMillis()), vatRate,
				loadSaleListener).execute();
	}
	
	public static void doEndday(final SQLiteDatabase sqlite, 
			final int shopId, final int computerId, final int sessionId,
			final int closeStaffId, final double closeAmount, double vatRate,
			final boolean isEndday, final ProgressListener listener) {
		
		// check is main computer
		Computer comp = new Computer(sqlite);
		if (comp.checkIsMainComputer(computerId)) {
			final SyncSaleLog syncLog = 
					new SyncSaleLog(sqlite);
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
									syncLog.updateSyncSaleLog(sessionDate,
											SyncSaleLog.SYNC_FAIL);
									listener.onError(mesg);
								}

								@Override
								public void onPre() {
								}

								@Override
								public void onPost() {
									syncLog.updateSyncSaleLog(sessionDate,
											SyncSaleLog.SYNC_SUCCESS);
									try {
										Transaction trans = new Transaction(sqlite);
										Session sess = new Session(sqlite);
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
					new LoadSaleTransactionForEndday(sqlite, date, vatRate, loadSaleListener)
							.execute();
				}
			}else{
				new LoadSaleTransactionForEndday(sqlite, String.valueOf(Util.getDate().getTimeInMillis()), 
						vatRate, loadSaleListener).execute();
			}
		} else {
			Context context = MPOSApplication.getContext();
			listener.onError(context.getString(R.string.cannot_endday) + " "
					+ context.getString(R.string.because) + " "
					+ context.getString(R.string.not_main_computer));
		}
	}

	public static class LoadSaleTransactionByTransactionId extends LoadSaleTransaction{

		public LoadSaleTransactionByTransactionId(final SQLiteDatabase sqlite, 
				String sessionDate, int transactionId, 
				double vatRate, LoadSaleTransactionListener listener) {
			super(sqlite, sessionDate, transactionId, vatRate, listener);
		}

		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleSaleTransactionByTransactionId();
		}
		
	}
	
	// load sale transaction for endday
	public static class LoadSaleTransactionForEndday extends LoadSaleTransaction{

		public LoadSaleTransactionForEndday(SQLiteDatabase sqlite, 
				String sessionDate, double vatRate,
				LoadSaleTransactionListener listener) {
			super(sqlite, sessionDate, vatRate, listener);
		}
		
		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listAllSaleTransactionInSaleDate();
		}
	}
	
	// load sale transaction for send realtime
	public static class LoadSaleTransaction extends AsyncTask<Void, Void, POSData_SaleTransaction>{

		protected LoadSaleTransactionListener mListener;
		protected SaleTransaction mSaleTrans;
		protected String mSessionDate;
			
		public LoadSaleTransaction(SQLiteDatabase sqlite, String sessionDate, int transactionId, double vatRate,
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(sqlite, sessionDate, transactionId, vatRate);
			mSessionDate = sessionDate;
		}
		
		public LoadSaleTransaction(SQLiteDatabase sqlite, String sessionDate, double vatRate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(sqlite, sessionDate, vatRate);
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
			int shopId, int computerId, int staffId, double vatRate){
		final ProgressDialog progress = new ProgressDialog(c);
		progress.setTitle(R.string.send_sale_data);
		progress.setMessage(c.getString(R.string.send_sale_data_progress));
		MPOSUtil.doSendSale(sqlite, shopId, computerId, staffId, vatRate, new ProgressListener(){

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
				Shop shop = new Shop(sqlite);
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
