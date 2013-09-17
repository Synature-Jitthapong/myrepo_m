package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class VoidBillActivity extends Activity implements OnConfirmClickListener {
	
	private Context mContext;
	private MPOSTransaction mTrans;
	private Calendar mCalendar;
	private Formatter mFormat;
	private long mDate;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private String mReceiptNo;
	private String mReceiptDate;
	
	private TableLayout tbReceipt;
	private TableLayout tbVoidItem;
	private EditText txtReceiptNo;
	private EditText txtReceiptDate;
	private Button btnBillDate; 
	private Button btnSearch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_void_bill);
		mContext = VoidBillActivity.this;
		
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.confirm_button);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	            | ActionBar.DISPLAY_SHOW_HOME);
		((TextView) actionBar.getCustomView().findViewById(R.id.textView1))
				.setText(R.string.title_activity_void_bill);
		((Button) actionBar.getCustomView().findViewById(R.id.button2))
				.setText(R.string.btn_void);

		mFormat = new Formatter(mContext);
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDate = mCalendar.getTimeInMillis();
		
		txtReceiptNo = (EditText) findViewById(R.id.txtReceiptNo);
		txtReceiptDate = (EditText) findViewById(R.id.txtSaleDate);
		tbReceipt = (TableLayout) findViewById(R.id.tbReceipt);
		tbVoidItem = (TableLayout) findViewById(R.id.tbVoidItem);
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
	    mStaffId = intent.getIntExtra("staffId", 0);
	    if(mStaffId == 0)
	    	finish();
	    
	    init();
	}

	private void init(){
		mTrans = new MPOSTransaction(mContext);
	}
	
	private void clearTbVoidItem(){
		tbVoidItem.removeAllViews();
	}
	
	private void searchVoidItem(){
		List<OrderTransaction.OrderDetail> orderLst = 
				mTrans.listAllOrders(mTransactionId, mComputerId);
		
		txtReceiptNo.setText(mReceiptNo);
		txtReceiptDate.setText(mReceiptDate);
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		clearTbVoidItem();
		for(OrderTransaction.OrderDetail order : orderLst){
			View voidItemView = inflater.inflate(R.layout.void_item_template, null);
			TextView tvItem = (TextView) voidItemView.findViewById(R.id.tvItem);
			TextView tvQty = (TextView) voidItemView.findViewById(R.id.tvQty);
			TextView tvPrice = (TextView) voidItemView.findViewById(R.id.tvPrice);
			TextView tvTotalPrice = (TextView) voidItemView.findViewById(R.id.tvTotalPrice);
			
			tvItem.setText(order.getProductName());
			tvQty.setText(mFormat.qtyFormat(order.getQty()));
			tvPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
			tvTotalPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
			
			tbVoidItem.addView(voidItemView);
		}
	}
	
	private void searchBill(){
		List<OrderTransaction> transLst = 
				mTrans.listTransaction(mDate);
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		tbReceipt.removeAllViews();
		for(final OrderTransaction trans : transLst){
			View receiptView = inflater.inflate(R.layout.receipt_template, null);
			TextView tvReceiptNo = (TextView) receiptView.findViewById(R.id.tvReceiptNo);
			TextView tvPaidTime = (TextView) receiptView.findViewById(R.id.tvPaidTime);
			
			final Calendar c = Calendar.getInstance();
			try {
				c.setTimeInMillis(trans.getPaidTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			tvReceiptNo.setText(trans.getReceiptNo());
			tvPaidTime.setText(mFormat.dateTimeFormat(c.getTime()));
			receiptView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mTransactionId = trans.getTransactionId();
					mComputerId = trans.getComputerId();
					mReceiptNo = trans.getReceiptNo();
					mReceiptDate = mFormat.dateTimeFormat(c.getTime());
					searchVoidItem();
				}
				
			});
			
			tbReceipt.addView(receiptView);
		}
	}
	
	@Override
	public void onOkClick(View v) {
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
				
			}
		}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String voidReason = txtVoidReason.getText().toString();
				if(!voidReason.isEmpty()){
					mTrans.voidTransaction(mTransactionId, mComputerId, mStaffId, voidReason);
					searchBill();
					clearTbVoidItem();
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

}
