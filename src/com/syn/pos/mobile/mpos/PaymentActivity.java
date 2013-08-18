package com.syn.pos.mobile.mpos;

import java.math.BigDecimal;
import java.util.List;

import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.model.Payment;
import com.syn.pos.mobile.mpos.dao.MPOSTransaction;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class PaymentActivity extends Activity {
	private final String TAG = "PaymentActivity";
	private Context context;
	private MPOSTransaction mposTrans;
	private Formatter format;
	private int transactionId;
	private int computerId;
	
	private StringBuilder strTotalPay;
	private float totalPrice;
	private float totalPay;
	private float changeAmount;
	
	private TableLayout tableLayoutPaydetail;
	private TextView tvTotalPayment;
	private EditText txtTotalPay;
	private EditText txtChange;
	private EditText txtTotalPaid;
	private EditText txtTobePaid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		
		tableLayoutPaydetail = (TableLayout) findViewById(R.id.tableLayoutPaydetail);
		tvTotalPayment = (TextView) findViewById(R.id.textViewTotalPayment);
		txtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		txtChange = (EditText) findViewById(R.id.editTextChange);
		txtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		txtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);
		
		init();
	}
	
	private void init(){
		context = PaymentActivity.this;
		format = new Formatter(context);
		
		mposTrans = new MPOSTransaction(context, format);
		loadSummary();
		loadPayDetail();
	}
	
	private void loadSummary(){
		OrderTransaction.OrderDetail order = 
				mposTrans.getSummary(transactionId);
		
		totalPrice = order.getProductPrice();
		
		displayTotalPrice();
	}
	
	private void loadPayDetail(){
		List<Payment.PaymentDetail> payLst = mposTrans.listPayment(transactionId, computerId);
		float totalPaid = mposTrans.getTotalPaid(transactionId, computerId);
		float tobePaid = totalPrice - totalPaid; 
		
		LayoutInflater inflater = LayoutInflater.from(context);
		tableLayoutPaydetail.removeAllViews();
		for(final Payment.PaymentDetail payment : payLst){
			View v = inflater.inflate(R.layout.payment_detail_template, null);
			TextView tvPayType = (TextView) v.findViewById(R.id.textViewPayType);
			TextView tvPayDetail = (TextView) v.findViewById(R.id.textViewPayDetail);
			TextView tvPayAmount = (TextView) v.findViewById(R.id.textViewPayAmount);
			
			tvPayType.setText(Integer.toString(payment.getPayTypeID()));
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(format.currencyFormat(payment.getPayAmount()));
			
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					deletePayment(payment.getPayTypeID());
				}
				
			});
			tableLayoutPaydetail.addView(v);
		}
		
		txtTotalPaid.setText(format.currencyFormat(totalPaid));
		if(tobePaid >= 0)
			txtTobePaid.setText(format.currencyFormat(tobePaid));
	}
	
	private void deletePayment(int paymentId){
		mposTrans.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(int payType){
		if(payType == 1){
			mposTrans.addPaymentDetail(transactionId, computerId, 1, totalPay, "",
					1, 2017, 1, 1);
		}else{
			mposTrans.updatePaymentDetail(transactionId, computerId, payType, totalPay, "",
					1, 2017, 1, 1);
		}
		loadPayDetail();
	}
	
	private void displayTotalPrice(){
		tvTotalPayment.setText(format.currencyFormat(totalPrice));

		strTotalPay = new StringBuilder();
		displayTotalPaid();
	}
	
	private void displayChange(){
		float change = totalPay - totalPrice;
		txtChange.setText(format.currencyFormat(change));
	}
	
	private void displayTotalPaid(){
		try {
			totalPay = Float.parseFloat(strTotalPay.toString());
		} catch (NumberFormatException e) {
			totalPay = 0.0f;
		}
		txtTotalPay.setText(format.currencyFormat(totalPay));
	}
	
	public void onCancelClicked(final View v){
		finish();
	}
	
	public void onOkClicked(final View v){
		finish();
	}

	/*
	 * pay key pad
	 */
	public void onBtnPriceClick(final View v){
		switch(v.getId()){
		case R.id.btnPay0:
			strTotalPay.append("0");
			break;
		case R.id.btnPay1:
			strTotalPay.append("1");
			break;
		case R.id.btnPay2:
			strTotalPay.append("2");
			break;
		case R.id.btnPay3:
			strTotalPay.append("3");
			break;
		case R.id.btnPay4:
			strTotalPay.append("4");
			break;
		case R.id.btnPay5:
			strTotalPay.append("5");
			break;
		case R.id.btnPay6:
			strTotalPay.append("6");
			break;
		case R.id.btnPay7:
			strTotalPay.append("7");
			break;
		case R.id.btnPay8:
			strTotalPay.append("8");
			break;
		case R.id.btnPay9:
			strTotalPay.append("9");
			break;
		case R.id.btnPayC:
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPayDel:
			try {
				strTotalPay.deleteCharAt(strTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btnPayDot:
			strTotalPay.append(".");
			break;
		case R.id.btnPayEnter:
			addPayment(1);
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		}
		displayTotalPaid();
	}
}
