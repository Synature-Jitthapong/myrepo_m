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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncSaleActivity extends Activity{
	private int mStaffId;
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
		
		mSyncAdapter = new SyncItemAdapter(this, listNotSendTransaction());
		mLvSyncItem.setAdapter(mSyncAdapter);
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


	private List<OrderTransaction> listNotSendTransaction(){
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
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
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	public class SyncItemAdapter extends OrderTransactionAdapter{
		
		public SyncItemAdapter(Context c, List<OrderTransaction> transLst) {
			super(c, transLst);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction trans = mTransLst.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.send_trans_template, null);
				holder = new ViewHolder();
				holder.imgSyncStatus = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.tvItem = (TextView) convertView.findViewById(R.id.textView1);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar1);
				holder.btnSend = (Button) convertView.findViewById(R.id.button1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvItem.setText(trans.getReceiptNo());
			holder.btnSend.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					MPOSUtil.doSendSaleBySelectedTransaction(trans.getTransactionId(), mStaffId, new MPOSUtil.LoadSaleTransactionListener() {
						
						@Override
						public void onPre() {
							holder.progress.setVisibility(View.VISIBLE);
							holder.btnSend.setVisibility(View.GONE);
						}
						
						@Override
						public void onPost() {
							holder.progress.setVisibility(View.GONE);
							holder.btnSend.setVisibility(View.VISIBLE);
							holder.imgSyncStatus.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onError(String msg) {
							holder.progress.setVisibility(View.GONE);
							holder.btnSend.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onPost(POSData_SaleTransaction saleTrans, String sessionDate) {
							holder.progress.setVisibility(View.GONE);
							holder.btnSend.setVisibility(View.VISIBLE);
							holder.imgSyncStatus.setVisibility(View.VISIBLE);
						}
					});
				}
				
			});
			return convertView;
		}		

		public class ViewHolder {
			ImageView imgSyncStatus;
			TextView tvItem;
			ProgressBar progress;
			Button btnSend;
		}
	}
}