package com.syn.mpos;

import java.util.List;

import com.syn.mpos.data.Bank;
import com.syn.mpos.data.CreditCard;
import com.syn.mpos.data.MPOSTransaction;
import com.syn.mpos.model.BankName;
import com.syn.mpos.model.CreditCardType;
import com.syn.mpos.model.OrderTransaction;
import com.syn.pos.mobile.mpos.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class CreditPayActivity extends Activity {
	private Context context;
	private int transactionId;
	private int computerId;
	private float totalPrice;
	private float totalPay;
	private Formatter format;
	private MPOSTransaction mposTrans;
	private List<BankName> bankLst;
	private List<CreditCardType> creditCardLst;
	
	private StringBuilder strTotalPaid;
	private EditText txtTotalPrice;
	private EditText txtTotalPay;
	private EditText txtCardNo;
	private Spinner spinnerBank;
	private Spinner spinnerCardType;
	private Spinner spinnerExpireMonth;
	private Spinner spinnerExpireYear;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_pay);
		context = CreditPayActivity.this;
	
		txtTotalPrice = (EditText) findViewById(R.id.editTextCreditTotalPrice);
		txtTotalPay = (EditText) findViewById(R.id.editTextCreditPayAmount);
		txtCardNo = (EditText) findViewById(R.id.editTextCreditNo);
		spinnerBank = (Spinner) findViewById(R.id.spinnerBank);
		spinnerCardType = (Spinner) findViewById(R.id.spinnerCardType);
		spinnerExpireMonth = (Spinner) findViewById(R.id.spinnerExpMonth);
		spinnerExpireYear = (Spinner) findViewById(R.id.spinnerExpYear);
		
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
		format = new Formatter(context);
		mposTrans = new MPOSTransaction(context);
		strTotalPaid = new StringBuilder();
		
		loadTotalPrice();
		loadCreditCardType();
		loadBankName();
	}
	
	private void loadTotalPrice(){
		OrderTransaction.OrderDetail order = 
				mposTrans.getSummary(transactionId);
		
		totalPrice = order.getProductPrice(); 
		
		displayTotalPrice();
	}
	
	private void displayTotalPrice(){
		txtTotalPrice.setText(format.currencyFormat(totalPrice));
		
		displayTotalPaid();
	}

	private void displayTotalPaid(){
		try {
			totalPay = Float.parseFloat(strTotalPaid.toString());
		} catch (NumberFormatException e) {
			totalPay = 0.0f;
			e.printStackTrace();
		}
		
		txtTotalPay.setText(format.currencyFormat(totalPay));
	}
	
	private void addPayment(){
		if(mposTrans.addPaymentDetail(transactionId, computerId, 2, 
					totalPay, "xx", 7, 2017, 1, 1))
			finish();
	}
	
	private void loadCreditCardType(){
		CreditCard credit = new CreditCard(context);
		creditCardLst = credit.listAllCreditCardType();
		
		ArrayAdapter<CreditCardType> adapter = 
				new ArrayAdapter<CreditCardType>(context, 
						android.R.layout.simple_dropdown_item_1line, creditCardLst);
		spinnerCardType.setAdapter(adapter);
		spinnerCardType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void loadBankName(){
		Bank bank = new Bank(context);
		bankLst = bank.listAllBank();
		
		ArrayAdapter<BankName> adapter = 
				new ArrayAdapter<BankName>(context, 
						android.R.layout.simple_dropdown_item_1line, bankLst);
		spinnerBank.setAdapter(adapter);
		spinnerBank.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void creditPayClicked(final View v){
		switch (v.getId()) {
		case R.id.btnCredit0:
			strTotalPaid.append("0");
			break;
		case R.id.btnCredit1:
			strTotalPaid.append("1");
			break;
		case R.id.btnCredit2:
			strTotalPaid.append("2");
			break;
		case R.id.btnCredit3:
			strTotalPaid.append("3");
			break;
		case R.id.btnCredit4:
			strTotalPaid.append("4");
			break;
		case R.id.btnCredit5:
			strTotalPaid.append("5");
			break;
		case R.id.btnCredit6:
			strTotalPaid.append("6");
			break;
		case R.id.btnCredit7:
			strTotalPaid.append("7");
			break;
		case R.id.btnCredit8:
			strTotalPaid.append("8");
			break;
		case R.id.btnCredit9:
			strTotalPaid.append("9");
			break;
		case R.id.btnCreditDel:
			try {
				strTotalPaid.deleteCharAt(strTotalPaid.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btnCreditDot:
			strTotalPaid.append(".");
			break;
		}

		displayTotalPaid();
	}
	
	public void cancelPayClicked(final View v){
		finish();
	}
	
	public void okPayClicked(final View v){
		addPayment();
	}
}
