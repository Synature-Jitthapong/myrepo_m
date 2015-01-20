package com.synature.mpos;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.table.BaseColumn;
import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.database.table.SessionTable;
import com.synature.pos.OrderTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.KeyEvent;
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
import android.widget.Toast;

public class SendSaleActivity extends Activity{
	public static final String TAG = SendSaleActivity.class.getSimpleName();
	
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	private List<SendTransaction> mTransLst;
	private SyncItemAdapter mSyncAdapter;
	private MenuItem mItemSendAll;
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
	    setFinishOnTouchOutside(false);
		setContentView(R.layout.activity_send_sale);
		
		mLvSyncItem = (ListView) findViewById(R.id.lvSync);
		
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);

		loadTransNotSend();
	}

	private void loadTransNotSend(){
		mTransLst = listNotSendTransaction();
		mSyncAdapter = new SyncItemAdapter(mTransLst);
		mLvSyncItem.setAdapter(mSyncAdapter);
		mLvSyncItem.setSelection(mSyncAdapter.getCount() - 1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_send_sale, menu);
		mItemSendAll = menu.findItem(R.id.itemSendAll);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemSendAll:
			sendSale();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("ShowToast")
	private class SendSaleReceiver extends ResultReceiver{
		
		private ProgressDialog progress;
		
		public SendSaleReceiver(Handler handler) {
			super(handler);
			progress = new ProgressDialog(SendSaleActivity.this);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(getString(R.string.please_wait));
			progress.show();
			mItemSendAll.setEnabled(false);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				progress.dismiss();
				loadTransNotSend();
				break;
			case MPOSServiceBase.RESULT_ERROR:
				mItemSendAll.setEnabled(true);
				progress.dismiss();
				Toast.makeText(SendSaleActivity.this, resultData.getString("msg"), Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
	}

	private void sendSale(){
		Intent intent = new Intent(this, SaleSenderService.class);
		intent.putExtra("what", SaleSenderService.SEND_PARTIAL_SALE);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("computerId", mComputerId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("sendSaleReceiver", new SendSaleReceiver(new Handler()));
		startService(intent);
	}
	
	private List<SendTransaction> listNotSendTransaction(){
		List<SendTransaction> transLst = new ArrayList<SendTransaction>();
		MPOSDatabase.MPOSOpenHelper helper = MPOSDatabase.MPOSOpenHelper.getInstance(getApplicationContext());
		Cursor cursor = helper.getReadableDatabase().query(OrderTransTable.TABLE_ORDER_TRANS,
				new String[]{
					OrderTransTable.COLUMN_TRANS_ID,
					ComputerTable.COLUMN_COMPUTER_ID,
					SessionTable.COLUMN_SESS_ID,
					OrderTransTable.COLUMN_RECEIPT_NO,
					OrderTransTable.COLUMN_CLOSE_TIME,
					BaseColumn.COLUMN_SEND_STATUS
				}, OrderTransTable.COLUMN_STATUS_ID + " IN(?,?) "
                    + " AND " + BaseColumn.COLUMN_SEND_STATUS + " =? ",
				new String[]{
                    String.valueOf(TransactionDao.TRANS_STATUS_VOID),
					String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
				 	String.valueOf(MPOSDatabase.NOT_SEND)
				}, null, null, OrderTransTable.COLUMN_TRANS_ID);
		if(cursor.moveToFirst()){
			do{
				SendTransaction trans = new SendTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setSessionId(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_NO)));
				trans.setSendStatus(cursor.getInt(cursor.getColumnIndex(BaseColumn.COLUMN_SEND_STATUS)));
				trans.setCloseTime(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_CLOSE_TIME)));
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	public class SyncItemAdapter extends BaseAdapter{
		private List<SendTransaction> mSendTransLst;
		private LayoutInflater mInflater;
		
		public SyncItemAdapter(List<SendTransaction> sendTransLst){
			mInflater = getLayoutInflater();
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
				convertView = mInflater.inflate(R.layout.send_trans_template, parent, false);
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
	}
}
