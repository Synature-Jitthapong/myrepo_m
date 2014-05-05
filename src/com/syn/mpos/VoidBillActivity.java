package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.MPOSOrderTransaction;
import com.syn.mpos.database.OrdersDataSource;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

public class VoidBillActivity extends Activity {
	
	private OrdersDataSource mOrders;
	private GlobalPropertyDataSource mGlobal;
	
	private List<MPOSOrderTransaction> mTransLst;
	private List<MPOSOrderTransaction.MPOSOrderDetail> mOrderLst;
	private BillAdapter mBillAdapter;
	private BillDetailAdapter mBillDetailAdapter;
	
	private int mTransactionId;
	private int mComputerId;
	private int mShopId;
	private int mStaffId;
	
	private Calendar mCalendar;
	private long mDate;
	private String mReceiptNo;
	private String mReceiptDate;
	
	private ListView mLvBill;
	private ListView mLvBillDetail;
	private EditText txtReceiptNo;
	private EditText txtReceiptDate;
	private Button btnBillDate; 
	private Button btnSearch;
	private MenuItem mItemConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_void_bill);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDate = mCalendar.getTimeInMillis();
		
		txtReceiptNo = (EditText) findViewById(R.id.txtReceiptNo);
		txtReceiptDate = (EditText) findViewById(R.id.txtSaleDate);
		mLvBill = (ListView) findViewById(R.id.lvBill);
		mLvBillDetail = (ListView) findViewById(R.id.lvBillDetail);
	    btnBillDate = (Button) findViewById(R.id.btnBillDate);
	    btnSearch = (Button) findViewById(R.id.btnSearch);

		mOrders = new OrdersDataSource(getApplicationContext());
		mGlobal = new GlobalPropertyDataSource(getApplicationContext());
		
		btnBillDate.setText(mGlobal.dateFormat(mCalendar.getTime()));
	    btnBillDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mDate = mCalendar.getTimeInMillis();
						
						btnBillDate.setText(mGlobal.dateFormat(mCalendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
		});
	    
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
				Calendar c = Calendar.getInstance();
				MPOSOrderTransaction trans = (MPOSOrderTransaction) parent.getItemAtPosition(position);
				c.setTimeInMillis(Long.parseLong(trans.getPaidTime()));
				
				mTransactionId = trans.getTransactionId();
				mComputerId = trans.getComputerId();
				mReceiptNo = trans.getReceiptNo();
				mReceiptDate = mGlobal.dateTimeFormat(c.getTime());
				
				mItemConfirm.setEnabled(true);
				searchVoidItem();
			}
		});
		
	    Intent intent = getIntent();
	    mStaffId = intent.getIntExtra("staffId", 0);
	    mShopId = intent.getIntExtra("shopId", 0);
	    init();
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
	
	private void init(){
		mTransLst = new ArrayList<MPOSOrderTransaction>();
		mOrderLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		mBillAdapter = new BillAdapter();
		mBillDetailAdapter = new BillDetailAdapter();
		mLvBill.setAdapter(mBillAdapter);
		mLvBillDetail.setAdapter(mBillDetailAdapter);
		txtReceiptNo.setText("");
		txtReceiptDate.setText("");
	}
	
	private class BillAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mTransLst != null ? mTransLst.size() : 0;
		}

		@Override
		public MPOSOrderTransaction getItem(int position) {
			return mTransLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MPOSOrderTransaction trans = mTransLst.get(position);
			ViewHolder holder;
			
			LayoutInflater inflater = (LayoutInflater) 
					VoidBillActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(convertView == null){
				convertView = inflater.inflate(R.layout.receipt_template, null);
				holder = new ViewHolder();
				holder.tvReceiptNo = (TextView) convertView.findViewById(R.id.tvReceiptNo);
				holder.tvPaidTime = (TextView) convertView.findViewById(R.id.tvPaidTime);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			Calendar c = Calendar.getInstance();
			try {
				c.setTimeInMillis(Long.parseLong(trans.getPaidTime()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			holder.tvReceiptNo.setText(trans.getReceiptNo());
			holder.tvPaidTime.setText(mGlobal.dateTimeFormat(c.getTime()));
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvReceiptNo;
			TextView tvPaidTime;
		}
	}
	
	private class BillDetailAdapter extends BaseAdapter{
		
		LayoutInflater inflater;
		
		public BillDetailAdapter(){
			inflater = LayoutInflater.from(VoidBillActivity.this);
		}
		
		@Override
		public int getCount() {
			return mOrderLst != null ? mOrderLst.size() : 0;
		}

		@Override
		public MPOSOrderTransaction.OrderDetail getItem(int position) {
			return mOrderLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MPOSOrderTransaction.OrderDetail order = mOrderLst.get(position);
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.void_item_template, null);
				holder = new ViewHolder();
				
				holder.tvItem = (TextView) convertView.findViewById(R.id.tvItem);
				holder.tvQty = (TextView) convertView.findViewById(R.id.tvQty);
				holder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
		
			holder.tvItem.setText(order.getProductName());
			holder.tvQty.setText(mGlobal.qtyFormat(order.getQty()));
			holder.tvPrice.setText(mGlobal.currencyFormat(order.getPricePerUnit()));
			holder.tvTotalPrice.setText(mGlobal.currencyFormat(order.getTotalRetailPrice()));
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvItem;
			TextView tvQty;
			TextView tvPrice;
			TextView tvTotalPrice;
		}
	}
	
	private void searchBill(){
		mTransLst = mOrders.listTransaction(String.valueOf(mDate));
		mBillAdapter.notifyDataSetChanged();
	}
	
	private void searchVoidItem(){
		txtReceiptNo.setText(mReceiptNo);
		txtReceiptDate.setText(mReceiptDate);
		
		mOrderLst = mOrders.listAllOrder(mTransactionId);
		mBillDetailAdapter.notifyDataSetChanged();
	}

	public void confirm() {
		LayoutInflater inflater = (LayoutInflater)
				VoidBillActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View inputLayout = inflater.inflate(R.layout.input_text_layout, null);
		final EditText txtVoidReason = (EditText) inputLayout.findViewById(R.id.editText1);
		txtVoidReason.setHint(R.string.reason);
		AlertDialog.Builder builder = new AlertDialog.Builder(VoidBillActivity.this);
		builder.setTitle(R.string.void_bill);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setView(inputLayout);
		builder.setMessage(R.string.confirm_void_bill);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(txtVoidReason.getWindowToken(), 0);
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		
		final AlertDialog d = builder.create();
		d.show();
		Button btnOk = d.getButton(AlertDialog.BUTTON_POSITIVE);
		
		btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String voidReason = txtVoidReason.getText().toString();
				if(!voidReason.isEmpty()){
					mOrders.voidTransaction(mTransactionId, mStaffId, voidReason);
						
					mItemConfirm.setEnabled(false);
					InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					d.dismiss();
					init();
					
					MPOSUtil.doSendSale(VoidBillActivity.this, mShopId, mComputerId, mStaffId, 
							new ProgressListener(){

						@Override
						public void onPre() {
						}

						@Override
						public void onPost() {
						}

						@Override
						public void onError(String msg) {
							new AlertDialog.Builder(VoidBillActivity.this)
							.setMessage(msg)
							.show();
						}
						
					});
				}
			}
			
		});
	}

	public void cancel() {
		finish();
	}
}
