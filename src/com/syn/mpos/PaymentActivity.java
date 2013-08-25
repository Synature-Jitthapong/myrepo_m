package com.syn.mpos;

import java.math.BigDecimal;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.db.MPOSTransaction;
import com.syn.mpos.model.OrderTransaction;
import com.syn.mpos.model.Payment;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
	private float totalPaid;
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
	}
	
	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	private void init(){
		context = PaymentActivity.this;
		format = new Formatter(context);
		
		mposTrans = new MPOSTransaction(context);
		loadTotalPrice();
		loadPayDetail();
	}
	
	private void loadTotalPrice(){
		OrderTransaction.OrderDetail order = 
				mposTrans.getSummary(transactionId);
		
		totalPrice = order.getProductPrice();
		
		displayTotalPrice();
	}
	
	private void loadPayDetail(){
		List<Payment.PaymentDetail> payLst = mposTrans.listPayment(transactionId, computerId);
		totalPaid = mposTrans.getTotalPaid(transactionId, computerId);
		float tobePaid = totalPrice - totalPaid; 
		
		LayoutInflater inflater = LayoutInflater.from(context);
		tableLayoutPaydetail.removeAllViews();
		for(final Payment.PaymentDetail payment : payLst){
			View v = inflater.inflate(R.layout.payment_detail_template, null);
			TextView tvPayType = (TextView) v.findViewById(R.id.textViewPayType);
			TextView tvPayDetail = (TextView) v.findViewById(R.id.textViewPayDetail);
			TextView tvPayAmount = (TextView) v.findViewById(R.id.textViewPayAmount);
			
			String payTypeName = payment.getPayTypeID() == 1 ? "Cash" : "Credit";
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(format.currencyFormat(payment.getPayAmount()));
			
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					deletePayment(payment.getPaymentDetailID());
				}
				
			});
			tableLayoutPaydetail.addView(v);
		}
		
		txtTotalPaid.setText(format.currencyFormat(totalPaid));
		if(tobePaid < 0)
			tobePaid = 0.0f;
		
		txtTobePaid.setText(format.currencyFormat(tobePaid));
	}
	
	private void deletePayment(int paymentId){
		mposTrans.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(totalPay > 0){
				mposTrans.addPaymentDetail(transactionId, computerId, 1, totalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
	}
	
	private void displayTotalPrice(){
		tvTotalPayment.setText(format.currencyFormat(totalPrice));

		strTotalPay = new StringBuilder();
		displayTotalPaid();
	}
	
	private void displayChange(){
//		float change = totalPay - totalPrice;
//		txtChange.setText(format.currencyFormat(change));
	}
	
	private void displayTotalPaid(){
		try {
			totalPay = Float.parseFloat(strTotalPay.toString());
		} catch (NumberFormatException e) {
			totalPay = 0.0f;
		}
		txtTotalPay.setText(format.currencyFormat(totalPay));
	}
	
	public void creditPayClicked(final View v){
		Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
		intent.putExtra("transactionId", transactionId);
		intent.putExtra("computerId", computerId);
		startActivity(intent);
	}
	
	public void onCancelClicked(final View v){
		mposTrans.deleteAllPaymentDetail(transactionId, computerId);
		finish();
	}
	
	public void onOkClicked(final View v){
		if(totalPaid >=totalPrice){
			mposTrans.successTransaction(transactionId, computerId);
			if(totalPaid - totalPrice > 0){
				new AlertDialog.Builder(context)
				.setTitle(R.string.change)
				.setMessage(format.currencyFormat(totalPaid - totalPrice))
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
			new AlertDialog.Builder(context)
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
		case R.id.btnPay20:
			strTotalPay.append("20");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		case R.id.btnPay50:
			strTotalPay.append("50");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		case R.id.btnPay100:
			strTotalPay.append("100");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		case R.id.btnPay500:
			strTotalPay.append("500");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		case R.id.btnPay1000:
			strTotalPay.append("1000");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
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
			addPayment();
			strTotalPay = new StringBuilder();
			displayChange();
			break;
		}
		displayTotalPaid();
	}
}
