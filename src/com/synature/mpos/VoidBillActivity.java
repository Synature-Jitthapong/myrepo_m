package com.synature.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.synature.mpos.SaleService.LocalBinder;
import com.synature.mpos.common.MPOSActivityBase;
import com.synature.mpos.database.Computer;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.PrintReceiptLog;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.model.OrderTransaction;

import android.os.Bundle;
import android.os.IBinder;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class VoidBillActivity extends MPOSActivityBase {
	
	private SaleService mPartService;
	private boolean mBound = false;
	
	private Transaction mTrans;
	private Formater mFormat;
	
	private List<OrderTransaction> mTransLst;
	private BillAdapter mBillAdapter;
	
	private int mTransactionId;
	private int mComputerId;
	private int mSessionId;
	private int mShopId;
	private int mStaffId;
	
	private ListView mLvBill;
	private TextView tvSaleDate;
	private Button btnSearch;
	private ScrollView mScrBill;
	private MenuItem mItemConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_void_bill);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		mLvBill = (ListView) findViewById(R.id.lvBill);
	    tvSaleDate = (TextView) findViewById(R.id.tvSaleDate);
	    btnSearch = (Button) findViewById(R.id.btnSearch);
	    mScrBill = (ScrollView) findViewById(R.id.scrollView1);

		mTrans = new Transaction(getApplicationContext());
		mFormat = new Formater(getApplicationContext());
		mTransLst = new ArrayList<OrderTransaction>();
		mBillAdapter = new BillAdapter();
		mLvBill.setAdapter(mBillAdapter);
		
		tvSaleDate.setText(mFormat.dateFormat(Utils.getCalendar().getTime()));
	    
	    btnSearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				searchBill();
			}
	    	
	    });
	    
		mLvBill.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Calendar c = Utils.getCalendar();
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				c.setTimeInMillis(Long.parseLong(trans.getPaidTime()));
				
				mTransactionId = trans.getTransactionId();
				mComputerId = trans.getComputerId();
				mSessionId = trans.getSessionId();
				
				if(trans.getTransactionStatusId() == Transaction.TRANS_STATUS_SUCCESS)
					mItemConfirm.setEnabled(true);
				else if(trans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID)
					mItemConfirm.setEnabled(false);
				searchVoidItem();
			}
		});

	    Intent intent = getIntent();
	    mStaffId = intent.getIntExtra("staffId", 0);
	    mShopId = intent.getIntExtra("shopId", 0);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, SaleService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBound){
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_void_bill, menu);
		mItemConfirm = menu.findItem(R.id.itemConfirm);
		mItemConfirm.setEnabled(false);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			cancel();
			return true;
		case R.id.itemConfirm:
			confirm();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}
	
	private class BillAdapter extends BaseAdapter{

		private LayoutInflater mInflater;
		
		public BillAdapter(){
			mInflater = (LayoutInflater) 
					VoidBillActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.receipt_template, null);
				holder = new ViewHolder();
				holder.tvReceiptNo = (TextView) convertView.findViewById(R.id.tvReceiptNo);
				holder.tvPaidTime = (TextView) convertView.findViewById(R.id.tvPaidTime);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			final OrderTransaction trans = mTransLst.get(position);
			Calendar c = Calendar.getInstance();
			try {
				c.setTimeInMillis(Long.parseLong(trans.getPaidTime()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			holder.tvReceiptNo.setText(trans.getReceiptNo());
			holder.tvPaidTime.setText(mFormat.dateTimeFormat(c.getTime()));
			if(trans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID){
				holder.tvReceiptNo.setTextColor(Color.RED);
				holder.tvReceiptNo.setPaintFlags(holder.tvReceiptNo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else{
				holder.tvReceiptNo.setTextColor(Color.BLACK);
				holder.tvReceiptNo.setPaintFlags(holder.tvReceiptNo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
			}
			return convertView;
		}
		
		class ViewHolder{
			TextView tvReceiptNo;
			TextView tvPaidTime;
		}
	}
	
	private void searchBill(){	
		mTransLst = mTrans.listTransaction(String.valueOf(Utils.getDate().getTimeInMillis()));
		if(mTransLst.size() == 0){
			new AlertDialog.Builder(VoidBillActivity.this)
			.setTitle(R.string.void_bill)
			.setMessage(R.string.not_found_bill)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
		}
		mBillAdapter.notifyDataSetChanged();
	}
	
	private void searchVoidItem(){
		OrderTransaction ordTrans = mTrans.getTransaction(mTransactionId);
		if(ordTrans != null){
			if(ordTrans.getTransactionStatusId() == Transaction.TRANS_STATUS_SUCCESS)
				((TextView) mScrBill.findViewById(R.id.textView1)).setText(ordTrans.getEj());
			else if(ordTrans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID)
				((TextView) mScrBill.findViewById(R.id.textView1)).setText(ordTrans.getEjVoid());
		}
	}

	public void confirm() {
		LayoutInflater inflater = (LayoutInflater)
				VoidBillActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View inputLayout = inflater.inflate(R.layout.input_text_layout, null);
		final EditText txtVoidReason = (EditText) inputLayout.findViewById(R.id.editText1);
		txtVoidReason.setHint(R.string.reason);
		AlertDialog.Builder builder = new AlertDialog.Builder(VoidBillActivity.this);
		builder.setTitle(R.string.void_bill);
		builder.setView(inputLayout);
		builder.setMessage(R.string.confirm_void_bill);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(txtVoidReason.getWindowToken(), 0);
			}
		});
		builder.setPositiveButton(R.string.yes, null);
		
		final AlertDialog d = builder.create();
		d.show();
		Button btnOk = d.getButton(AlertDialog.BUTTON_POSITIVE);
		
		btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String voidReason = txtVoidReason.getText().toString();
				if(!voidReason.isEmpty()){
					mTrans.voidTransaction(mTransactionId, mStaffId, voidReason);
					new AlertDialog.Builder(VoidBillActivity.this)
					.setTitle(R.string.void_bill)
					.setMessage(R.string.void_bill_success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
					mItemConfirm.setEnabled(false);
					d.dismiss();
					searchBill();
					printReceipt();
					sendSale();
				}
			}
			
		});
	}
	
	private void printReceipt(){
		PrintReceiptLog printLog = 
				new PrintReceiptLog(this);
		Computer comp = new Computer(this);
		int isCopy = 0;
		for(int i = 0; i < comp.getReceiptHasCopy(); i++){
			if(i > 0)
				isCopy = 1;
			printLog.insertLog(mTransactionId, mStaffId, isCopy);
		}
		new Thread(new PrintReceipt(VoidBillActivity.this)).start();
	}
	
	private void sendSale(){
		mPartService.sendSale(mShopId, mSessionId, mTransactionId, mComputerId, 
				mStaffId, new WebServiceWorkingListener(){

			@Override
			public void onPreExecute() {
			}

			@Override
			public void onPostExecute() {
			}

			@Override
			public void onError(String msg) {
			}

			@Override
			public void onProgressUpdate(int value) {
			}
			
		});
	}

	private void cancel() {
		finish();
	}
	
	/**
	 * PartialSaleService Connection
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mPartService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
		
	};
}
