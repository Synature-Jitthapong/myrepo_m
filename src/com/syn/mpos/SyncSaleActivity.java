package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.MPOSDatabase;
import com.syn.mpos.provider.Transaction;
import com.syn.mpos.provider.SaleTransaction.POSData_SaleTransaction;
import com.syn.pos.OrderTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncSaleActivity extends Activity{
	private int mStaffId;
	private List<OrderTransaction> mTransLst;
	private SyncItemAdapter mSyncAdapter;
	private ListView mLvSyncItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		setContentView(R.layout.sync_sale_layout);
		
		mLvSyncItem = (ListView) findViewById(R.id.listView1);
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		
		listNotSendTransaction();
		mSyncAdapter = new SyncItemAdapter();
		mLvSyncItem.setAdapter(mSyncAdapter);
		mLvSyncItem.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				final ViewHolder holder = (ViewHolder) v.getTag();
				final OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				sendSale(holder, trans);
			}
			
		});
	}
	
	private void sendSale(final ViewHolder holder, final OrderTransaction trans){
		MPOSUtil.doSendSaleBySelectedTransaction(trans.getTransactionId(), mStaffId, new MPOSUtil.LoadSaleTransactionListener() {
			
			@Override
			public void onPre() {
				holder.progress.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPost() {
				trans.setSendStatus(MPOSDatabase.ALREADY_SEND);
				mSyncAdapter.notifyDataSetChanged();
				holder.progress.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onError(String msg) {
				holder.progress.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onPost(POSData_SaleTransaction saleTrans, String sessionDate) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.action_sync_sale, menu);
//		return true;
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemSendAll:
			
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}


	private void listNotSendTransaction(){
		mTransLst = new ArrayList<OrderTransaction>();
		SQLiteDatabase sqlite = MPOSApplication.getWriteDatabase();
		Cursor cursor = sqlite.query(Transaction.TABLE_TRANSACTION, 
				new String[]{
					Transaction.COLUMN_TRANSACTION_ID,
					Computer.COLUMN_COMPUTER_ID,
					Transaction.COLUMN_RECEIPT_NO,
					MPOSDatabase.COLUMN_SEND_STATUS
				}, Transaction.COLUMN_STATUS_ID + "=? AND " +
					MPOSDatabase.COLUMN_SEND_STATUS + "=?", 
				new String[]{
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
				 	String.valueOf(MPOSDatabase.NOT_SEND)
				}, null, null, Transaction.COLUMN_TRANSACTION_ID);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COLUMN_COMPUTER_ID)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(Transaction.COLUMN_RECEIPT_NO)));
				trans.setSendStatus(cursor.getInt(cursor.getColumnIndex(MPOSDatabase.COLUMN_SEND_STATUS)));
				mTransLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
	}
	
	public class SyncItemAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		
		public SyncItemAdapter(){
			mInflater = (LayoutInflater)
					SyncSaleActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mTransLst != null ? mTransLst.size() : 0;
		}

		@Override
		public OrderTransaction getItem(int position) {
			return mTransLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderTransaction trans = mTransLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.sync_sale_template, null);
				holder = new ViewHolder();
				holder.imgSyncStatus = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.tvItem = (TextView) convertView.findViewById(R.id.textView1);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvItem.setText(trans.getReceiptNo());
			if(trans.getSendStatus() == MPOSDatabase.ALREADY_SEND){
				holder.imgSyncStatus.setVisibility(View.VISIBLE);
			}else{
				holder.imgSyncStatus.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}
	
	public static class ViewHolder{
		ImageView imgSyncStatus;
		TextView tvItem;
		ProgressBar progress;
	}
}
