package com.syn.pos.mobile.mpos;

import java.util.List;

import com.syn.pos.mobile.model.BankName;
import com.syn.pos.mobile.model.CreditCardType;
import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.dao.Bank;
import com.syn.pos.mobile.mpos.dao.CreditCard;
import com.syn.pos.mobile.mpos.dao.MPOSTransaction;

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
	private float totalPaid;
	private Formatter format;
	private MPOSTransaction mposTrans;
	private List<BankName> bankLst;
	private List<CreditCardType> creditCardLst;
	
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
	
		txtTotalPrice = (EditText) findViewById(R.id.editTextCreditTotalPrice);
		txtTotalPay = (EditText) findViewById(R.id.editTextCreditPayAmount);
		txtCardNo = (EditText) findViewById(R.id.editTextCreditNo);
		spinnerBank = (Spinner) findViewById(R.id.spinnerBank);
		spinnerCardType = (Spinner) findViewById(R.id.spinnerCardType);
		spinnerExpireMonth = (Spinner) findViewById(R.id.spinnerExpMonth);
		spinnerBank = (Spinner) findViewById(R.id.spinnerExpYear);
		
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
		mposTrans = new MPOSTransaction(context, format);
		
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
	}
	
	private void loadCreditCardType(){
		CreditCard credit = new CreditCard(context);
		creditCardLst = credit.listAllCreditCardType();
		
		ArrayAdapter<CreditCardType> adapter = 
				new ArrayAdapter<CreditCardType>(context, android.R.layout.simple_spinner_item, creditCardLst);
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
				new ArrayAdapter<BankName>(context, android.R.layout.simple_spinner_item, bankLst);
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
	
	public void cancelPayClicked(final View v){
		finish();
	}
	
	public void okPayClicked(final View v){
		finish();
	}
}
