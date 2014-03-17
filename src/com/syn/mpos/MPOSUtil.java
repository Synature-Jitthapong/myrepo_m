package com.syn.mpos;

import java.lang.reflect.Type;
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
import com.syn.mpos.MPOSService.SendSaleTransaction;
import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.PaymentDetail;
import com.syn.mpos.provider.SaleTransaction;
import com.syn.mpos.provider.Session;
import com.syn.mpos.provider.Session.SessionEntry;
import com.syn.mpos.provider.SyncSaleLog;
import com.syn.mpos.provider.Transaction;
import com.syn.mpos.provider.Transaction.OrderDetailEntry;
import com.syn.mpos.provider.Transaction.TransactionEntry;
import com.syn.mpos.provider.Util;
import com.syn.mpos.provider.SaleTransaction.POSData_SaleTransaction;

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
	
	public static void doSendSaleBySelectedTransaction(final int transactionId, final int staffId,
			final ProgressListener listener) {
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
						Transaction trans = 
								new Transaction(MPOSApplication.getWriteDatabase());
						trans.updateTransactionSendStatus(transactionId);
						MPOSApplication.writeLog("Send parial sale => " + jsonSale);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				};
				new MPOSService.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						staffId, jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransactionByTransactionId(String.valueOf(Util.getDate().getTimeInMillis()),
				transactionId, loadSaleListener).execute();
	}
	
	public static void doSendSale(final int staffId,
			final ProgressListener listener) {
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
						Transaction trans = 
								new Transaction(MPOSApplication.getWriteDatabase());
						trans.updateTransactionSendStatus(sessionDate);
						MPOSApplication.writeLog("Send parial sale => " + jsonSale);
						listener.onPost();
					}

					@Override
					public void onError(String msg) {
						listener.onError(msg);
					}
				};
				new MPOSService.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						staffId, jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransaction(String.valueOf(Util.getDate().getTimeInMillis()),
				loadSaleListener).execute();
	}
	
	public static void doEndday(final int computerId, final int sessionId,
			final int closeStaffId, final float closeAmount,
			final boolean isEndday, final ProgressListener listener) {
		
		// check is main computer
		Computer comp = new Computer(MPOSApplication.getReadDatabase());
		if (comp.checkIsMainComputer(computerId)) {
			final SyncSaleLog syncLog = 
					new SyncSaleLog(MPOSApplication.getWriteDatabase());
			LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

				@Override
				public void onPost(POSData_SaleTransaction saleTrans,
						final String sessionDate) {

					JSONUtil jsonUtil = new JSONUtil();
					Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
					final String jsonSale = jsonUtil.toJson(type, saleTrans);
					// Log.v("SaleTrans", saleJson);

					new MPOSService.SendSaleTransaction(
							SendSaleTransaction.SEND_SALE_TRANS_METHOD,
							closeStaffId, jsonSale, new ProgressListener() {

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
										Transaction trans = 
												new Transaction(MPOSApplication.getWriteDatabase());
										Session sess = 
												new Session(MPOSApplication.getWriteDatabase());
										sess.addSessionEnddayDetail(sessionDate,
												trans.getTotalReceipt(sessionDate),
												trans.getTotalReceiptAmount(sessionDate));
										sess.closeSession(sessionId, computerId,
											closeStaffId, closeAmount, isEndday);
										MPOSApplication.writeLog("Send endday sale => " + jsonSale);
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
					new LoadSaleTransactionForEndday(date, loadSaleListener)
							.execute();
				}
			}else{
				new LoadSaleTransactionForEndday(String.valueOf(Util.getDate().getTimeInMillis()), loadSaleListener)
				.execute();
			}
		} else {
			Context context = MPOSApplication.getContext();
			listener.onError(context.getString(R.string.cannot_endday) + " "
					+ context.getString(R.string.because) + " "
					+ context.getString(R.string.not_main_computer));
		}
	}

	public static class LoadSaleTransactionByTransactionId extends LoadSaleTransaction{

		public LoadSaleTransactionByTransactionId(String sessionDate,
				int transactionId, LoadSaleTransactionListener listener) {
			super(sessionDate, transactionId, listener);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected POSData_SaleTransaction doInBackground(Void... params) {
			return mSaleTrans.listSaleSaleTransactionByTransactionId();
		}
		
	}
	
	// load sale transaction for endday
	public static class LoadSaleTransactionForEndday extends LoadSaleTransaction{

		public LoadSaleTransactionForEndday(String sessionDate,
				LoadSaleTransactionListener listener) {
			super(sessionDate, listener);
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
			
		public LoadSaleTransaction(String sessionDate, int transactionId,
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(MPOSApplication.getWriteDatabase(), sessionDate, transactionId);
			mSessionDate = sessionDate;
		}
		
		public LoadSaleTransaction(String sessionDate, 
				LoadSaleTransactionListener listener){
			mListener = listener;
			mSaleTrans = new SaleTransaction(MPOSApplication.getWriteDatabase(), sessionDate);
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
	
	public static void sendSaleData(final Context c, int staffId){
		final ProgressDialog progress = new ProgressDialog(c);
		progress.setTitle(R.string.send_sale_data);
		progress.setMessage(c.getString(R.string.send_sale_data_progress));
		MPOSUtil.doSendSale(staffId, new ProgressListener(){

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
		SQLiteDatabase sqlite = MPOSApplication.getWriteDatabase();
		sqlite.delete(OrderDetailEntry.TABLE_ORDER, null, null);
		sqlite.delete(OrderDetailEntry.TABLE_ORDER_TMP, null, null);
		sqlite.delete(TransactionEntry.TABLE_TRANSACTION, null, null);
		sqlite.delete(PaymentDetail.TABLE_PAYMENT, null, null);
		sqlite.delete(SessionEntry.TABLE_SESSION, null, null);
		sqlite.delete(SessionEntry.TABLE_SESSION_DETAIL, null, null);	
		sqlite.delete(SyncSaleLog.TABLE_SYNC_SALE_LOG, null, null);	
	}
	
	public static void updateData(final Context c){
		final ProgressDialog progress = new ProgressDialog(c);
		final MPOSService mPOSService = new MPOSService();
		mPOSService.loadShopData(new ProgressListener(){

			@Override
			public void onPre() {
				progress.setTitle(R.string.update_data);
				progress.setMessage(c.getString(R.string.update_shop_progress));
				progress.show();
			}

			@Override
			public void onPost() {
				mPOSService.loadProductData(new ProgressListener(){

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
	
	public static double stringToDouble(String text) throws ParseException{
		double value = 0.0d;
		NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
		Number num = format.parse(text);
		value = num.doubleValue();
		return value;
	}
}
