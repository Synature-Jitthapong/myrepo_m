package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.ComputerTable;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.OrderTransactionTable;
import com.syn.mpos.database.OrderTransactionDataSource;
import com.syn.mpos.database.Util;
import com.syn.pos.OrderTransaction;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReprintActivity extends Activity {
	
	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;
	private boolean mIsOnPrint;
	private ReprintTransAdapter mTransAdapter;
	private int mStaffId;
	private ListView mLvTrans;
	
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
		setContentView(R.layout.activity_reprint);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mLvTrans = (ListView) findViewById(R.id.listView1);
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		mTransAdapter = new ReprintTransAdapter(this, 
				listTransaction(String.valueOf(Util.getDate().getTimeInMillis())));
		mLvTrans.setAdapter(mTransAdapter);
	}

	private List<OrderTransaction> listTransaction(String saleDate){
		List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		Cursor cursor = mSqlite.query(OrderTransactionTable.TABLE_NAME, 
				new String[]{
				OrderTransactionTable.COLUMN_TRANSACTION_ID,
				ComputerTable.COLUMN_COMPUTER_ID,
				OrderTransactionTable.COLUMN_RECEIPT_NO
				}, 
				OrderTransactionTable.COLUMN_SALE_DATE + "=? AND " +
				OrderTransactionTable.COLUMN_STATUS_ID + "=?", 
				new String[]{
					saleDate,
				 	String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS)
				}, null, null, OrderTransactionTable.COLUMN_TRANSACTION_ID);
		if(cursor.moveToFirst()){
			do{
				OrderTransaction trans = new OrderTransaction();
				trans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				trans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				trans.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				transLst.add(trans);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transLst;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			if(!mIsOnPrint)
				finish();
			return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public class ReprintTransAdapter extends OrderTransactionAdapter{

		public ReprintTransAdapter(Context c, List<OrderTransaction> transLst) {
			super(c, transLst);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction trans = mTransLst.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.reprint_trans_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.textView2);
				holder.tvItem = (TextView) convertView.findViewById(R.id.textView1);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar1);
				holder.btnPrint = (Button) convertView.findViewById(R.id.button1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvNo.setText(String.valueOf(position + 1) + ".");
			holder.tvItem.setText(trans.getReceiptNo());
			holder.btnPrint.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new Reprint(trans.getTransactionId(), trans.getComputerId(), new Reprint.PrintStatusListener() {
						
						@Override
						public void onPrintSuccess() {
							mIsOnPrint = false;
							holder.progress.setVisibility(View.GONE);
							holder.btnPrint.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onPrintFail(String msg) {
							mIsOnPrint = false;
							holder.progress.setVisibility(View.GONE);
							holder.btnPrint.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onPrepare() {
							mIsOnPrint = true;
							holder.progress.setVisibility(View.VISIBLE);
							holder.btnPrint.setVisibility(View.GONE);
						}
					}).execute();
				}
				
			});
			return convertView;
		}
		
		public class ViewHolder {
			TextView tvNo;
			TextView tvItem;
			ProgressBar progress;
			Button btnPrint;
		}
	}

	
	public class Reprint extends PrintReceipt{
		private int mTransactionId;
		private int mComputerId;
		
		public Reprint(int transactionId, int computerId, PrintStatusListener listener) {
			super(ReprintActivity.this, mSqlite, mStaffId, listener);
			mTransactionId = transactionId;
			mComputerId = computerId;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if(MPOSApplication.getInternalPrinterSetting()){
				printReceiptWintec(mTransactionId, mComputerId);
			}else{
				printReceipt(mTransactionId, mComputerId);	
			}
			return null;
		}
	}
}
