package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.transaction.Transaction;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class VoidBillActivity extends Activity {
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private Transaction mTransaction;
	private List<OrderTransaction> mTransLst;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private BillAdapter mBillAdapter;
	private BillDetailAdapter mBillDetailAdapter;
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

	    btnBillDate.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
	    btnBillDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mDate = mCalendar.getTimeInMillis();
						
						btnBillDate.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
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
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				c.setTimeInMillis(trans.getPaidTime());
				
				mTransactionId = trans.getTransactionId();
				mComputerId = trans.getComputerId();
				mReceiptNo = trans.getReceiptNo();
				mReceiptDate = MPOSApplication.getGlobalProperty().dateTimeFormat(c.getTime());
				
				mItemConfirm.setEnabled(true);
				searchVoidItem();
			}
		});
		
	    Intent intent = getIntent();
	    mStaffId = intent.getIntExtra("staffId", 0);
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
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemCancel:
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
		mTransaction = new Transaction(MPOSApplication.getWriteDatabase());
		mTransLst = new ArrayList<OrderTransaction>();
		mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
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
		public OrderTransaction getItem(int position) {
			return mTransLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction trans = mTransLst.get(position);
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
				c.setTimeInMillis(trans.getPaidTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			holder.tvReceiptNo.setText(trans.getReceiptNo());
			holder.tvPaidTime.setText(MPOSApplication.getGlobalProperty().dateTimeFormat(c.getTime()));
			
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
			holder.tvQty.setText(MPOSApplication.getGlobalProperty().qtyFormat(order.getQty()));
			holder.tvPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(order.getPricePerUnit()));
			holder.tvTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(order.getTotalRetailPrice()));
			
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
		mTransLst = mTransaction.listTransaction(mDate);
		mBillAdapter.notifyDataSetChanged();
	}
	
	private void searchVoidItem(){
		txtReceiptNo.setText(mReceiptNo);
		txtReceiptDate.setText(mReceiptDate);
		
		mOrderLst = mTransaction.listAllOrder(mTransactionId, mComputerId);
		mBillDetailAdapter.notifyDataSetChanged();
	}

	public void confirm() {
		final EditText txtVoidReason = new EditText(VoidBillActivity.this);
		txtVoidReason.setHint(R.string.reason);
		
		new AlertDialog.Builder(VoidBillActivity.this)
		.setTitle(R.string.void_bill)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setView(txtVoidReason)
		.setMessage(R.string.confirm_void_bill)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String voidReason = txtVoidReason.getText().toString();
				if(!voidReason.isEmpty()){
					if(mTransaction.voidTransaction(mTransactionId,
							mComputerId, mStaffId, voidReason)){
						
						mItemConfirm.setEnabled(false);
						init();
					}
				}else{
					new AlertDialog.Builder(getApplicationContext())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.void_bill)
					.setMessage(R.string.enter_reason)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.show();
				}
			}
		})
		.show();
	}

	public void cancel() {
		finish();
	}
}
