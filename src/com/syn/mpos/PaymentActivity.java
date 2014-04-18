package com.syn.mpos;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.PaymentAmountButtonDataSource;
import com.syn.mpos.database.PaymentDetailDataSource;
import com.syn.mpos.database.OrderTransactionDataSource;
import com.syn.pos.Payment;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

public class PaymentActivity extends Activity  implements OnClickListener {
	
	public static final int REQUEST_CREDIT_PAY = 1;
	public static final int RESULT_ENOUGH = 1;
	public static final int RESULT_NOT_ENOUGH = -1;
	private int resultCreditCode = RESULT_NOT_ENOUGH;
	
	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;
	private OrderTransactionDataSource mTransaction;
	private PaymentDetailDataSource mPayment;
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	private PaymentButtonAdapter mPaymentButtonAdapter;
	
	private double mTotalSalePrice;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	
	private StringBuilder mStrTotalPay;
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
		
		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		mTransaction = new OrderTransactionDataSource(mSqlite);
		mPayment = new PaymentDetailDataSource(mSqlite);
		
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mPaymentButtonAdapter = new PaymentButtonAdapter();
		mStrTotalPay = new StringBuilder();
		mLvPayment.setAdapter(mPaymentAdapter);
		mGvPaymentButton.setAdapter(mPaymentButtonAdapter);
		loadPayType();
		
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
		if(mTransaction.getTransaction(mTransactionId, 
				mComputerId).getTransactionStatusId() == OrderTransactionDataSource.TRANS_STATUS_SUCCESS){
			finish();
		}else{
			summary();
			loadPayDetail();
			if(resultCreditCode == RESULT_ENOUGH)
				confirm();
		}
		super.onResume();
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
			String payTypeName = payment.getPayTypeID() == PaymentDetailDataSource.PAY_TYPE_CASH ? payTypeCash : payTypeCredit;
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, payment.getPayAmount()));
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
		mChange = mTotalPaid - mTotalSalePrice;
		mTxtTotalPaid.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0d;
		if(mChange < 0)
			mChange = 0.0d;
		mTxtPaymentLeft.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mPaymentLeft));
		mTxtChange.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mChange));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(int payTypeId, String remark){
		if(mTotalPay > 0 && mPaymentLeft > 0){ 
			mPayment.addPaymentDetail(mTransactionId, 
					mComputerId, payTypeId, mTotalPay, 
					mTotalPay >= mPaymentLeft ? mPaymentLeft : mTotalPay, "",
					0, 0, 0, 0, remark);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		displayEnterPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mTotalSalePrice));
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
		mTxtEnterPrice.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mTotalPay));
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
			mTransaction.successTransaction(mTransactionId, 
					mComputerId, mStaffId);
			mChange = mTotalPaid - mTotalSalePrice;
			if(mChange > 0){
				LayoutInflater inflater = (LayoutInflater) 
						PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
				tvChange.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mChange));
				
				new AlertDialog.Builder(PaymentActivity.this)
				.setTitle(R.string.change)
				.setCancelable(false)
				.setView(tvChange)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.putExtra("transactionId", mTransactionId);
						intent.putExtra("computerId", mComputerId);
						setResult(RESULT_OK, intent);
						finish();
					}
				})
				.show();
			}else{
				Intent intent = new Intent();
				intent.putExtra("transactionId", mTransactionId);
				intent.putExtra("computerId", mComputerId);
				setResult(RESULT_OK, intent);
				finish();
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
//		case R.id.btnCash:
//			mBtnCash.setPressed(true);
//			break;
//		case R.id.btnCredit:
//			creditPay();
//			break;
		}
	}
	
	private void popupOtherPayment(String payTypeName, final int payTypeId){
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.other_payment_layout, null);
		final EditText txtAmount = (EditText) v.findViewById(R.id.txtAmount);
		final EditText txtRemark = (EditText) v.findViewById(R.id.txtRemark);
		txtAmount.setText(GlobalPropertyDataSource.currencyFormat(mSqlite, mTotalSalePrice));
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
						mStrTotalPay.append(GlobalPropertyDataSource.currencyFormat(
								mSqlite, paid));
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
		private List<Payment.PaymentAmountButton> mPaymentButtonLst;
		private LayoutInflater mInflater;
		
		public PaymentButtonAdapter(){
			mInflater = (LayoutInflater)
					PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			PaymentAmountButtonDataSource payButton = new PaymentAmountButtonDataSource(mSqlite);
			mPaymentButtonLst = payButton.listPaymentButton();
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
			holder.btnPayment.setText(GlobalPropertyDataSource.currencyFormat(
					mSqlite, paymentButton.getPaymentAmount()));
			holder.btnPayment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mStrTotalPay = new StringBuilder();
					mStrTotalPay.append(GlobalPropertyDataSource.currencyFormat(
							mSqlite, paymentButton.getPaymentAmount()));
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
