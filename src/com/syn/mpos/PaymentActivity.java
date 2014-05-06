package com.syn.mpos;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Drw;

import com.syn.mpos.R;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.PaymentAmountButtonDataSource;
import com.syn.mpos.database.PaymentDetailDataSource;
import com.syn.mpos.database.OrdersDataSource;
import com.syn.pos.Payment;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PaymentActivity extends Activity  implements OnClickListener{
	
	public static final int REQUEST_CREDIT_PAY = 1;
	public static final int RESULT_ENOUGH = 2;
	public static final int RESULT_NOT_ENOUGH = -1;

	/*
	 * credit pay not enough result code
	 */
	private int mResultCreditCode = RESULT_NOT_ENOUGH;
	
	/*
	 * Wintec Cash Drawer 
	 */
	private Drw mDrw;
	
	private PaymentDetailDataSource mPayment;
	private OrdersDataSource mOrders;
	private GlobalPropertyDataSource mGlobal;
	
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	private PaymentButtonAdapter mPaymentButtonAdapter;

	private StringBuilder mStrTotalPay;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private double mTotalSalePrice;
	private double mTotalPay;
	private double mTotalPaid;
	private double mPaymentLeft;
	private double mChange;
	
	private ListView mLvPayment;
	private EditText mTxtEnterPrice;
	private EditText mTxtTotalPaid;
	private EditText mTxtPaymentLeft;
	private EditText mTxtTotalPrice;
	private EditText mTxtChange;
	private GridView mGvPaymentButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    params.height= WindowManager.LayoutParams.WRAP_CONTENT;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
		setContentView(R.layout.activity_payment);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mLvPayment = (ListView) findViewById(R.id.lvPayDetail);
		mTxtEnterPrice = (EditText) findViewById(R.id.txtEnterPrice);
		mTxtTotalPaid = (EditText) findViewById(R.id.txtTotalPaid);
		mTxtPaymentLeft = (EditText) findViewById(R.id.txtPaymentLeft);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		mTxtChange = (EditText) findViewById(R.id.txtChange);
		mGvPaymentButton = (GridView) findViewById(R.id.gridView1);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mOrders = new OrdersDataSource(getApplicationContext());
		mPayment = new PaymentDetailDataSource(getApplicationContext());
		mGlobal = new GlobalPropertyDataSource(getApplicationContext());
		
		// init drw
		mDrw = new Drw(MPOSApplication.WINTEC_DEFAULT_DEVICE_PATH,
				ComIO.Baudrate.valueOf(MPOSApplication.WINTEC_DEFAULT_BAUD_RATE));
		
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mPaymentButtonAdapter = new PaymentButtonAdapter();
		mStrTotalPay = new StringBuilder();
		mLvPayment.setAdapter(mPaymentAdapter);
		mGvPaymentButton.setAdapter(mPaymentButtonAdapter);
		loadPayType();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CREDIT_PAY){
			mResultCreditCode = resultCode;
		}
	}
	
	@Override
	protected void onResume() {
		if(mOrders.getTransaction(mTransactionId).getTransactionStatusId() == 
				OrdersDataSource.TRANS_STATUS_SUCCESS){
			finish();
		}else{
			summary();
			loadPayDetail();
			if(mResultCreditCode == RESULT_ENOUGH)
				confirm();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mDrw.DRW_Close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_payment, menu);
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
	
	private void summary(){ 
		mTotalSalePrice = mOrders.getTransaction(mTransactionId).getTransactionVatable();
		displayTotalPrice();
	}
	
	private class PaymentAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public PaymentAdapter(){
			inflater = (LayoutInflater)
					PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mPayLst != null ? mPayLst.size() : 0;
		}

		@Override
		public Payment.PaymentDetail getItem(int position) {
			return mPayLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Payment.PaymentDetail payment = mPayLst.get(position);
			View rowView = convertView;
			
			rowView = inflater.inflate(R.layout.payment_detail_template, null);
			TextView tvPayType = (TextView) rowView.findViewById(R.id.tvPayType);
			TextView tvPayDetail = (TextView) rowView.findViewById(R.id.tvPayDetail);
			TextView tvPayAmount = (TextView) rowView.findViewById(R.id.tvPayAmount);
			Button imgDel = (Button) rowView.findViewById(R.id.btnDelete);
			
			String payTypeCash = PaymentActivity.this.getString(R.string.cash);
			String payTypeCredit = PaymentActivity.this.getString(R.string.credit);
			String payTypeName = payment.getPayTypeID() == PaymentDetailDataSource.PAY_TYPE_CASH ? payTypeCash : payTypeCredit;
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(mGlobal.currencyFormat(payment.getPayAmount()));
			imgDel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					deletePayment(payment.getPayTypeID());
				}
				
			});
			
			return rowView;
		}
	}
	
	private void loadPayDetail(){
		mPayLst = mPayment.listPayment(mTransactionId);
		mPaymentAdapter.notifyDataSetChanged();
		mTotalPaid = mPayment.getTotalPaid(mTransactionId);
		mPaymentLeft = mTotalSalePrice - mTotalPaid;
		mChange = mTotalPaid - mTotalSalePrice;
		mTxtTotalPaid.setText(mGlobal.currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0d;
		if(mChange < 0)
			mChange = 0.0d;
		mTxtPaymentLeft.setText(mGlobal.currencyFormat( mPaymentLeft));
		mTxtChange.setText(mGlobal.currencyFormat(mChange));
	}
	
	private void deletePayment(int payTypeId){
		mPayment.deletePaymentDetail(payTypeId);
		loadPayDetail();
	}
	
	private void addPayment(int payTypeId, String remark){
		if(mTotalPay > 0 && mPaymentLeft > 0){ 
			mPayment.addPaymentDetail(mTransactionId, mComputerId, payTypeId, mTotalPay, 
					mTotalPay >= mPaymentLeft ? mPaymentLeft : mTotalPay, "",
					0, 0, 0, 0, remark);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		displayEnterPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mGlobal.currencyFormat(mTotalSalePrice));
		displayEnterPrice();
	}
	
	private void calculateInputPrice(){
		try {
			mTotalPay = MPOSUtil.stringToDouble(mStrTotalPay.toString());
		} catch (ParseException e) {
			mTotalPay = 0.0d;
		}
	}
	
	private void displayEnterPrice(){
		calculateInputPrice();
		mTxtEnterPrice.setText(mGlobal.currencyFormat(mTotalPay));
	}
	
	public void creditPay(){
		if(mTotalSalePrice > 0 && mPaymentLeft > 0){
			Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputerId);
			intent.putExtra("paymentLeft", mPaymentLeft);
			startActivityForResult(intent, 1);
		}
	}

	public void confirm() {
		if(mTotalPaid >=mTotalSalePrice){

			// open cash drawer
			mDrw.DRW_Open();
			
			mOrders.closeTransaction(mTransactionId, mStaffId);
			
			mChange = mTotalPaid - mTotalSalePrice;
			
			Intent intent = new Intent();
			intent.putExtra("change", mChange);
			setResult(RESULT_OK, intent);
			finish();
		}else{
			new AlertDialog.Builder(PaymentActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.payment)
			.setMessage(R.string.enter_enough_money)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
			.show();
			
		}
	}

	public void cancel() {
		mPayment.deleteAllPaymentDetail(mTransactionId);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnPay0:
			mStrTotalPay.append("0");
			displayEnterPrice();
			break;
		case R.id.btnPay1:
			mStrTotalPay.append("1");
			displayEnterPrice();
			break;
		case R.id.btnPay2:
			mStrTotalPay.append("2");
			displayEnterPrice();
			break;
		case R.id.btnPay3:
			mStrTotalPay.append("3");
			displayEnterPrice();
			break;
		case R.id.btnPay4:
			mStrTotalPay.append("4");
			displayEnterPrice();
			break;
		case R.id.btnPay5:
			mStrTotalPay.append("5");
			displayEnterPrice();
			break;
		case R.id.btnPay6:
			mStrTotalPay.append("6");
			displayEnterPrice();
			break;
		case R.id.btnPay7:
			mStrTotalPay.append("7");
			displayEnterPrice();
			break;
		case R.id.btnPay8:
			mStrTotalPay.append("8");
			displayEnterPrice();
			break;
		case R.id.btnPay9:
			mStrTotalPay.append("9");
			displayEnterPrice();
			break;
		case R.id.btnPayC:
			mStrTotalPay = new StringBuilder();
			displayEnterPrice();
			break;
		case R.id.btnPayDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				mStrTotalPay = new StringBuilder();
			}
			displayEnterPrice();
			break;
		case R.id.btnPayDot:
			mStrTotalPay.append(".");
			displayEnterPrice();
			break;
		case R.id.btnPayEnter:
			if(!mStrTotalPay.toString().isEmpty()){
				addPayment(PaymentDetailDataSource.PAY_TYPE_CASH, "");
			}
			break;
		}
	}
	
	private void popupOtherPayment(String payTypeName, final int payTypeId){
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.other_payment_layout, null);
		final EditText txtAmount = (EditText) v.findViewById(R.id.txtAmount);
		final EditText txtRemark = (EditText) v.findViewById(R.id.txtRemark);
		txtAmount.setText(mGlobal.currencyFormat(mTotalSalePrice));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(payTypeName);
		builder.setView(v);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				double paid = 0;
				try {
					paid = MPOSUtil.stringToDouble(txtAmount.getText().toString());
					if(paid > 0){
						mStrTotalPay = new StringBuilder();
						mStrTotalPay.append(mGlobal.currencyFormat(paid));
						calculateInputPrice();
						addPayment(payTypeId, txtRemark.getText().toString());
						d.dismiss();
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	private void loadPayType(){
		List<Payment.PayType> payTypeLst = mPayment.listPayType();
		LinearLayout payTypeContent = (LinearLayout) findViewById(R.id.payTypeContent);
		payTypeContent.removeAllViews();
		for(final Payment.PayType payType : payTypeLst){
			final Button btnPayType = new Button(PaymentActivity.this);
			btnPayType.setMinWidth(128);
			btnPayType.setMinHeight(64);
			btnPayType.setText(payType.getPayTypeName());
			btnPayType.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(payType.getPayTypeID() == PaymentDetailDataSource.PAY_TYPE_CASH){
						
					}else if(payType.getPayTypeID() == PaymentDetailDataSource.PAY_TYPE_CREDIT){
						creditPay();
					}else{
						popupOtherPayment(payType.getPayTypeName(), payType.getPayTypeID());
					}
				}
				
			});
			payTypeContent.addView(btnPayType);
		}
	}
	
	public class PaymentButtonAdapter extends BaseAdapter{
		
		private PaymentAmountButtonDataSource mPaymentButton;
		private List<Payment.PaymentAmountButton> mPaymentButtonLst;
		private LayoutInflater mInflater;
		
		public PaymentButtonAdapter(){
			mInflater = (LayoutInflater)
					PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPaymentButton = new PaymentAmountButtonDataSource(getApplicationContext());
			mPaymentButtonLst = mPaymentButton.listPaymentButton();
		}
		
		@Override
		public int getCount() {
			return mPaymentButtonLst != null ? mPaymentButtonLst.size() : 0;
		}

		@Override
		public Payment.PaymentAmountButton getItem(int position) {
			return mPaymentButtonLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Payment.PaymentAmountButton paymentButton = 
					mPaymentButtonLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.button_template, null);
				holder.btnPayment = (Button) convertView.findViewById(R.id.button1);
				holder.btnPayment.setMinWidth(128);
				holder.btnPayment.setMinHeight(96);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}	
			holder.btnPayment.setText(mGlobal.currencyFormat(
					paymentButton.getPaymentAmount()));
			holder.btnPayment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mStrTotalPay = new StringBuilder();
					mStrTotalPay.append(mGlobal.currencyFormat(
							paymentButton.getPaymentAmount()));
					calculateInputPrice();
					addPayment(PaymentDetailDataSource.PAY_TYPE_CASH, "");
				}
				
			});
			return convertView;
		}
		
		class ViewHolder{
			Button btnPayment;
		}
	}

}
