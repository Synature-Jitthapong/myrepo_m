package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.provider.PaymentDetail;
import com.syn.mpos.provider.Transaction;
import com.syn.pos.Payment;

import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends Activity  implements OnClickListener {
	public static final int REQUEST_CREDIT_PAY = 1;
	public static final int RESULT_ENOUGH = 1;
	public static final int RESULT_NOT_ENOUGH = -1;
	private int resultCreditCode = RESULT_NOT_ENOUGH;
	
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
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 960;
	    params.height= 500;
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
		mBtnCash = (Button) findViewById(R.id.btnCash);
		mBtnCredit = (Button) findViewById(R.id.btnCredit);
		
		mTransaction = new Transaction(MPOSApplication.getWriteDatabase());
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mStrTotalPay = new StringBuilder();
		mBtnCredit.setOnClickListener(this);
		mBtnCash.setOnClickListener(this);
		mLvPayment.setAdapter(mPaymentAdapter);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CREDIT_PAY){
			resultCreditCode = resultCode;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mTransaction.getTransaction(mTransactionId, 
				mComputerId).getTransactionStatusId() == Transaction.TRANS_STATUS_SUCCESS){
			finish();
		}else{
			summary();
			loadPayDetail();
			if(resultCreditCode == RESULT_ENOUGH)
				confirm();
		}
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
		mTotalSalePrice = mTransaction.getTransactionVatable(mTransactionId, mComputerId);
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
			String payTypeName = payment.getPayTypeID() == PaymentDetail.PAY_TYPE_CASH ? payTypeCash : payTypeCredit;
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
					mComputerId, PaymentDetail.PAY_TYPE_CASH, mTotalPay, 
					mTotalPay >= mPaymentLeft ? mPaymentLeft : mTotalPay, "",
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
			startActivityForResult(intent, 1);
		}
	}
	
	private void sendSale(){
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				MPOSUtil.doSendSale(mStaffId, new ProgressListener(){

					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						MPOSUtil.makeToask(PaymentActivity.this, 
								PaymentActivity.this.getString(R.string.send_sale_data_success));
					}

					@Override
					public void onError(String msg) {
						MPOSUtil.makeToask(PaymentActivity.this, msg);
					}
					
				});
			}
			
		}, 1000);
	}
	
	private void print(){
		new Handler().post(new Runnable(){

			@Override
			public void run() {
				PrintReceipt printReceipt = new PrintReceipt();
				printReceipt.printReceipt(mTransactionId, mComputerId);
			}
			
		});
	}
	
	public void confirm() {
		if(mTotalPaid >=mTotalSalePrice){
			if(mTransaction.successTransaction(mTransactionId, 
					mComputerId, mStaffId)){
				mChange = mTotalPaid - mTotalSalePrice;
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
							print();
							sendSale();
							finish();
						}
					})
					.show();
				}else{
					print();
					sendSale();
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
		case R.id.btnCash:
			mBtnCash.setPressed(true);
			break;
		case R.id.btnCredit:
			creditPay();
			break;
		}
	}
}
