package com.syn.pos.mobile.mpos;

import java.math.BigDecimal;

import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.dao.MPOSTransaction;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PaymentActivity extends Activity {
	private final String TAG = "PaymentActivity";
	private Context context;
	private MPOSTransaction mposTrans;
	private Formatter format;
	private int transactionId;
	private int computerId;
	private float totalPrice;
	private float changeAmount;
	
	private TextView tvTotalPayment;
	private EditText txtTotalPay;
	private EditText txtChange;
	private EditText txtTotalPaid;
	private EditText txtTobePaid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		
		tvTotalPayment = (TextView) findViewById(R.id.textViewTotalPayment);
		txtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		txtChange = (EditText) findViewById(R.id.editTextChange);
		txtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		txtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);
		
		init();
		loadSummary();
	}
	
	private void init(){
		context = PaymentActivity.this;
		format = new Formatter(context);
		mposTrans = new MPOSTransaction(context, format);
	}
	
	private void loadSummary(){
		OrderTransaction.OrderDetail order = 
				mposTrans.getSummary(transactionId);
		
		totalPrice = order.getProductPrice();
		
		displayPayment();
	}
	
	private void displayPayment(){
		tvTotalPayment.setText(format.currencyFormat(totalPrice));
	}
	
	public void onCancelClicked(final View v){
		finish();
	}
	
	public void onOkClicked(final View v){
		finish();
	}

}
