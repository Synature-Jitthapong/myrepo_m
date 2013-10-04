package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.inventory.MPOSSaleStock;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;

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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class VoidBillActivity extends Activity implements OnConfirmClickListener {
	
	private Context mContext;
	private MPOSTransaction mTrans;
	private MPOSSaleStock mSaleStock;
	private List<OrderTransaction> mTransLst;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private BillAdapter mBillAdapter;
	private BillDetailAdapter mBillDetailAdapter;
	private Calendar mCalendar;
	private Formatter mFormat;
	private long mDate;
	private int mTransactionId;
	private int mComputerId;
	private int mShopId;
	private int mStaffId;
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
		mContext = VoidBillActivity.this;

		mFormat = new Formatter(mContext);
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
	    
	    btnBillDate.setText(mFormat.dateFormat(mCalendar.getTime()));
	    btnBillDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mDate = mCalendar.getTimeInMillis();
						
						btnBillDate.setText(mFormat.dateFormat(mCalendar.getTime()));
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
	    
	    Intent intent = getIntent();
	    mShopId = intent.getIntExtra("shopId", 0);
	    mStaffId = intent.getIntExtra("staffId", 0);
	    
	    if(mShopId == 0 || mStaffId == 0)
	    	finish();
	    
	    init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_confirm, menu);
		menu.findItem(R.id.itemClose).setVisible(false);
		mItemConfirm = menu.findItem(R.id.itemConfirm);
		mItemConfirm.setEnabled(false);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemCancel:
			onCancelClick(item.getActionView());
			return true;
		case R.id.itemConfirm:
			onConfirmClick(item.getActionView());
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}
	
	private void init(){
		mTrans = new MPOSTransaction(mContext);
		mSaleStock = new MPOSSaleStock(mContext);
		mTransLst = new ArrayList<OrderTransaction>();
		mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
		mBillAdapter = new BillAdapter();
		mBillDetailAdapter = new BillDetailAdapter();
		mLvBill.setAdapter(mBillAdapter);
		mLvBillDetail.setAdapter(mBillDetailAdapter);
		
		mLvBill.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Calendar c = Calendar.getInstance();
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				c.setTimeInMillis(trans.getPaidTime());
				
				mTransactionId = trans.getTransactionId();
				mComputerId = trans.getComputerId();
				mReceiptNo = trans.getReceiptNo();
				mReceiptDate = mFormat.dateTimeFormat(c.getTime());
				
				mItemConfirm.setEnabled(true);
				searchVoidItem();
			}
		});
	}
	
	private class BillAdapter extends BaseAdapter{
		LayoutInflater inflater;
		
		public BillAdapter(){
			inflater = LayoutInflater.from(mContext);
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
		public void notifyDataSetChanged() {
			mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
			mBillDetailAdapter.notifyDataSetChanged();
			mReceiptNo = "";
			mReceiptDate = "";
			txtReceiptNo.setText(mReceiptNo);
			txtReceiptDate.setText(mReceiptDate);
			
			mItemConfirm.setEnabled(false);
			
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction trans = mTransLst.get(position);
			ViewHolder holder;
			
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
				c.setTimeInMillis(trans.getPaidTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			holder.tvReceiptNo.setText(trans.getReceiptNo());
			holder.tvPaidTime.setText(mFormat.dateTimeFormat(c.getTime()));
			
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
			inflater = LayoutInflater.from(mContext);
		}
		
		@Override
		public int getCount() {
			return mOrderLst != null ? mOrderLst.size() : 0;
		}

		@Override
		public OrderTransaction.OrderDetail getItem(int position) {
			return mOrderLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderTransaction.OrderDetail order = mOrderLst.get(position);
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
			holder.tvQty.setText(mFormat.qtyFormat(order.getQty()));
			holder.tvPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
			holder.tvTotalPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
			
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
		mTransLst = mTrans.listTransaction(mDate);
		mBillAdapter.notifyDataSetChanged();
	}
	
	private void searchVoidItem(){
		txtReceiptNo.setText(mReceiptNo);
		txtReceiptDate.setText(mReceiptDate);
		
		mOrderLst = mTrans.listAllOrders(mTransactionId, mComputerId);
		mBillDetailAdapter.notifyDataSetChanged();
	}
	
	@Override 
	public void onSaveClick(View v){
		
	}
	
	@Override
	public void onConfirmClick(View v) {
		final EditText txtVoidReason = new EditText(mContext);
		txtVoidReason.setHint(R.string.reason);
		
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.void_bill)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setView(txtVoidReason)
		.setMessage(R.string.confirm_void_bill)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				hideKeyboard();
			}
		}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String voidReason = txtVoidReason.getText().toString();
				if(!voidReason.isEmpty()){
					if(mTrans.voidTransaction(mTransactionId, mComputerId, mStaffId, voidReason)){
				    	List<OrderTransaction.OrderDetail> orderLst = 
				    			mTrans.listAllOrders(mTransactionId, mComputerId);
						
				    	if(mSaleStock.createVoidDocument(mShopId, mStaffId, orderLst, voidReason)){
							searchBill();
							hideKeyboard();
				    	}
					}
				}else{
					Util.alert(mContext, android.R.drawable.ic_dialog_alert, 
							R.string.void_bill, R.string.enter_reason);
				}
			}
		})
		.show();
	}

	@Override
	public void onCancelClick(View v) {
		finish();
	}
	
	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}