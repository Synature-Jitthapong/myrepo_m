package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.R;
import com.syn.mpos.provider.HeaderFooterReceipt;
import com.syn.mpos.provider.PaymentDetail;
import com.syn.mpos.provider.Transaction;
import com.syn.mpos.provider.Util;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PaymentActivity extends Activity  implements OnClickListener {
	//private final String TAG = "PaymentActivity";
	private double mTotalSalePrice;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private Transaction mTransaction;
	private PaymentDetail mPayment;
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	
	private StringBuilder mStrTotalPay;
	private double mTotalPay;
	private double mTotalPaid;
	private double mPaymentLeft;
	private double mChange;
	
	private ListView mLvPayment;
	private Button mBtnCash;
	private Button mBtnCredit;
	private EditText mTxtEnterPrice;
	private EditText mTxtTotalPaid;
	private EditText mTxtPaymentLeft;
	private EditText mTxtTotalPrice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mLvPayment = (ListView) findViewById(R.id.lvPayDetail);
		mTxtEnterPrice = (EditText) findViewById(R.id.txtEnterPrice);
		mTxtTotalPaid = (EditText) findViewById(R.id.txtTotalPaid);
		mTxtPaymentLeft = (EditText) findViewById(R.id.txtPaymentLeft);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		mBtnCash = (Button) findViewById(R.id.btnCash);
		mBtnCredit = (Button) findViewById(R.id.btnCredit);
		mBtnCredit.setOnClickListener(this);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_payment, menu);
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

	@Override
	protected void onResume() {
		super.onResume();
		init();
		mBtnCash.setPressed(true);
	}

	private void init(){
		mTransaction = new Transaction(MPOSApplication.getWriteDatabase());
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mLvPayment.setAdapter(mPaymentAdapter);
		mStrTotalPay = new StringBuilder();
		
		if(mTransaction.getTransaction(
				mTransactionId, mComputerId).getTransactionStatusId() == 
				Transaction.TRANS_STATUS_SUCCESS)
		{
			finish();
		}else{
			summary();
			loadPayDetail();
		}
	}
	
	private void summary(){ 
		mTotalSalePrice = mTransaction.getTransactionVatable(mTransactionId, mComputerId);
		displayTotalPrice();
	}
	
	private class PaymentAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public PaymentAdapter(){
			inflater = LayoutInflater.from(PaymentActivity.this);
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
			
			String payTypeName = payment.getPayTypeID() == PaymentDetail.PAY_TYPE_CASH ? "Cash" : "Credit";
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(MPOSApplication.getGlobalProperty().currencyFormat(payment.getPayAmount()));
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
		mPayLst = mPayment.listPayment(mTransactionId, mComputerId);
		mPaymentAdapter.notifyDataSetChanged();
		
		mTotalPaid = mPayment.getTotalPaid(mTransactionId, mComputerId);
		
		mPaymentLeft = mTotalSalePrice - mTotalPaid; 

		mTxtTotalPaid.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0f;
		
		mTxtPaymentLeft.setText(MPOSApplication.getGlobalProperty().currencyFormat(mPaymentLeft));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(mTotalPay > 0 && mPaymentLeft > 0){
				mPayment.addPaymentDetail(mTransactionId, 
						mComputerId, PaymentDetail.PAY_TYPE_CASH, mTotalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		displayEnterPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalSalePrice));
		displayEnterPrice();
	}
	
	private void calculateInputPrice(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPay.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
		}
	}
	
	private void displayEnterPrice(){
		calculateInputPrice();
		mTxtEnterPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalPay));
	}
	
	public void creditPay(){
		if(mTotalSalePrice > 0 && mPaymentLeft > 0){
			Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputerId);
			intent.putExtra("paymentLeft", mPaymentLeft);
			startActivity(intent);
		}
	}

	/*
	 * pay key pad
	 */
	public void onBtnPriceClick(final View v){
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
		case R.id.btnPay20:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("20");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay50:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("50");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay100:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("100");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay500:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("500");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay1000:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("1000");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPayC:
			mStrTotalPay = new StringBuilder();
			displayEnterPrice();
			break;
		case R.id.btnPayDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			displayEnterPrice();
			break;
		case R.id.btnPayDot:
			mStrTotalPay.append(".");
			displayEnterPrice();
			break;
		case R.id.btnPayEnter:
			if(!mStrTotalPay.toString().isEmpty()){
				addPayment();
			}
			break;
		}
	}
	
	public void confirm() {
		if(mTotalPaid >=mTotalSalePrice){
			if(mTransaction.successTransaction(mTransactionId, 
					mComputerId, mStaffId)){
				
				mChange = mTotalPaid - mTotalSalePrice;
				
				new Handler().post(new Runnable(){

					@Override
					public void run() {
						PrintReceipt printReceipt = new PrintReceipt();
						printReceipt.printReceipt(mTransactionId, mComputerId);
					}
					
				});
				
				// send real time sale
				new Handler().post(new Runnable(){

					@Override
					public void run() {
						MPOSUtil.doSendSale(mStaffId, new ProgressListener(){

							@Override
							public void onPre() {
							}

							@Override
							public void onPost() {
							}

							@Override
							public void onError(String msg) {
							}
							
						});
					}
					
				});
				
				if(mChange > 0){
					LayoutInflater inflater = (LayoutInflater) 
							PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
					tvChange.setText(MPOSApplication.getGlobalProperty().currencyFormat(mChange));
					
					new AlertDialog.Builder(PaymentActivity.this)
					.setTitle(R.string.change)
					.setCancelable(false)
					.setView(tvChange)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.show();
				}else{
					finish();
				}
				
			}else{
				
			}
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
		mPayment.deleteAllPaymentDetail(mTransactionId, 
				mComputerId);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCash:
			
			break;
		case R.id.btnCredit:
			creditPay();
			break;
		}
	}
}
