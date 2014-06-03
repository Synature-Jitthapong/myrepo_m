package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.dao.BaseColumn;
import com.syn.mpos.dao.Computer.ComputerTable;
import com.syn.mpos.dao.MPOSDatabase;
import com.syn.mpos.dao.Transaction;
import com.syn.mpos.dao.Transaction.OrderTransactionTable;
import com.synature.exceptionhandler.ExceptionHandler;
import com.synature.pos.OrderTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncSaleActivity extends Activity{

	private boolean mIsOnSync;
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	private List<SendTransaction> mTransLst;
	private SyncItemAdapter mSyncAdapter;
	private MenuItem mItemProgress;
	private MenuItem mItemSendAll;
	private ListView mLvSyncItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME));
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 500;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sync_sale);
		
		mLvSyncItem = (ListView) findViewById(R.id.lvSync);
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);

		loadTransNotSend();
	}
	
	private void loadTransNotSend(){
		mTransLst = listNotSendTransaction();
		mSyncAdapter = new SyncItemAdapter(this, mTransLst);
		mLvSyncItem.setAdapter(mSyncAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_sync_sale, menu);
		mItemProgress = menu.findItem(R.id.itemProgress);
		mItemSendAll = menu.findItem(R.id.itemSendAll);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			if(!mIsOnSync)
				finish();
			return true;
		case R.id.itemSendAll:
			sendSale();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	private void sendSale(){
		MPOSUtil.sendSale(SyncSaleActivity.this,
				mShopId, mComputerId, mStaffId, true, new ProgressListener(){

			@Override
			public void onPre() {
				mIsOnSync = true;
				mItemProgress.setVisible(true);
				mItemSendAll.setVisible(false);
			}

			@Override
			public void onPost() {
				mIsOnSync = false;
				loadTransNotSend();
				mItemProgress.setVisible(false);
				mItemSendAll.setVisible(true);
			}

			@Override
			public void onError(String msg) {
				mIsOnSync = false;
				loadTransNotSend();
				MPOSUtil.makeToask(SyncSaleActivity.this, msg);
				mItemProgress.setVisible(false);
				mItemSendAll.setVisible(true);
			}
			
		});
	}
	
	private List<SendTransaction> listNotSendTransaction(){
		List<SendTransaction> transLst = new ArrayList<SendTransaction>();
		MPOSDatabase.MPOSOpenHelper helper = MPOSDatabase.MPOSOpenHelper.getInstance(getApplicationContext());
		Cursor cursor = helper.getReadableDatabase().query(OrderTransactionTable.TABLE_ORDER_TRANS, 
				new String[]{
					OrderTransactionTable.COLUMN_TRANSACTION_ID,
					ComputerTable.COLUMN_COMPUTER_ID,
					OrderTransactionTable.COLUMN_RECEIPT_NO,
					OrderTransactionTable.COLUMN_CLOSE_TIME,
					BaseColumn.COLUMN_SEND_STATUS
				}, OrderTransactionTable.COLUMN_STATUS_ID + "=? AND " +
					BaseColumn.COLUMN_SEND_STATUS + " IN(?,?) ", 
				new String[]{
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
				 	String.valueOf(MPOSDatabase.NOT_SEND),
				 	String.valueOf(MPOSDatabase.ALREADY_SEND)
				}, null, null, OrderTransactionTable.COLUMN_TRANSACTION_ID);
		if(cursor.moveToFirst()){
			do{
				SendTransaction trans = new SendTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				trans.setSendStatus(cursor.getInt(cursor.getColumnIndex(BaseColumn.COLUMN_SEND_STATUS)));
				trans.setCloseTime(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_CLOSE_TIME)));
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	public class SyncItemAdapter extends BaseAdapter{
		private List<SendTransaction> mSendTransLst;
		private LayoutInflater mInflater;
		
		public SyncItemAdapter(Context c, List<SendTransaction> sendTransLst){
			mInflater = (LayoutInflater) 
					c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mSendTransLst = sendTransLst;
		}
		
		public class ViewHolder {
			ImageView imgSyncStatus;
			TextView tvNo;
			TextView tvItem;
			ProgressBar progress;
		}

		@Override
		public int getCount() {
			return mSendTransLst != null ? mSendTransLst.size() : 0;
		}

		@Override
		public SendTransaction getItem(int position) {
			return mSendTransLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final SendTransaction trans = mSendTransLst.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.send_trans_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.textView2);
				holder.imgSyncStatus = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.tvItem = (TextView) convertView.findViewById(R.id.textView1);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvNo.setText(String.valueOf(position + 1) + ".");
			holder.tvItem.setText(trans.getReceiptNo());

			if(trans.onSend){
				holder.progress.setVisibility(View.VISIBLE);
				holder.imgSyncStatus.setVisibility(View.GONE);
			}else{
				holder.progress.setVisibility(View.GONE);
				holder.imgSyncStatus.setVisibility(View.VISIBLE);
			}
			if(trans.getSendStatus() == MPOSDatabase.ALREADY_SEND){
				holder.imgSyncStatus.setImageResource(R.drawable.ic_action_accept);
			}else{
				holder.imgSyncStatus.setImageResource(R.drawable.ic_action_warning);
			}
			return convertView;
		}
	}
	
	private class SendTransaction extends OrderTransaction{
		private boolean onSend = false;

		public void setOnSend(boolean onSend) {
			this.onSend = onSend;
		}
	}
}
