package com.syn.mpos;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.syn.mpos.R;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.CreditCard;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class CreditPayActivity extends Activity {
	private int mTransactionId;
	private int mComputerId;
	private int mBankId;
	private int mCardTypeId;
	private int mExpYear;
	private int mExpMonth;
	
	private GlobalProperty mGlobalProp;
	private PaymentDetail mPayment;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	private float mTotalCreditPay;
	
	private EditText mTxtTotalPrice;
	private EditText mTxtTotalPay;
	private EditText mTxtCardNo;
	private Spinner mSpBank;
	private Spinner mSpCardType;
	private Spinner mSpExpYear;
	private Spinner mSpExpMonth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_pay);
	
		mTxtTotalPrice = (EditText) findViewById(R.id.editTextCreditTotalPrice);
		mTxtTotalPay = (EditText) findViewById(R.id.editTextCreditPayAmount);
		mTxtCardNo = (EditText) findViewById(R.id.editTextCreditNo);
		mSpBank = (Spinner) findViewById(R.id.spinnerBank);
		mSpCardType = (Spinner) findViewById(R.id.spinnerCardType);
		mSpExpYear = (Spinner) findViewById(R.id.spExpYear);
		mSpExpMonth = (Spinner) findViewById(R.id.spExpMonth);
		mTxtTotalPay.setSelectAllOnFocus(true);
		
		mSpExpYear.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				String year = (String)parent.getItemAtPosition(position);
				mExpYear = Integer.parseInt(year);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mSpExpMonth.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				String month = (String)parent.getItemAtPosition(position);
				mExpMonth = Integer.parseInt(month) + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		init();
	}

	private void createSpinnerYear(){
		String[] years = new String[10];
		Calendar c = Calendar.getInstance(Locale.getDefault());
		for(int i = 0; i < years.length; i++){
			years[i] = String.valueOf(c.get(Calendar.YEAR) + i);
		}
		SpinnerAdapter adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, years);
		mSpExpYear.setAdapter(adapter);
	}
	
	private void createSpinnerMonth(){
		//String[] months = new DateFormatSymbols(Locale.getDefault()).getMonths();
		String[] months = new String[12];
		for(int i = 0; i < months.length; i++){
			months[i] = String.valueOf(i + 1);
		}
		SpinnerAdapter adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, months);
		mSpExpMonth.setAdapter(adapter);
		Calendar c = Calendar.getInstance(Locale.getDefault());
		mSpExpMonth.setSelection(c.get(Calendar.MONTH));
	}
	
	private void init(){
		mGlobalProp = new GlobalProperty(this);
		mPayment = new PaymentDetail(this);
		
		displayTotalPrice();
		loadCreditCardType();
		loadBankName();
		createSpinnerMonth();
		createSpinnerYear();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mGlobalProp.currencyFormat(PaymentActivity.sTotalSalePrice));
	}
	
	private void addPayment(){
		String cardNo = mTxtCardNo.getText().toString();
		try {
			mTotalCreditPay = Float.parseFloat(mTxtTotalPay.getText().toString());
		} catch (NumberFormatException e) {
			mTotalCreditPay = 0.0f;
			e.printStackTrace();
		}
		
		mTxtTotalPay.setText(mGlobalProp.currencyFormat(mTotalCreditPay));
		if(!cardNo.isEmpty() && mTotalCreditPay > 0){
			if(mPayment.addPaymentDetail(mTransactionId, 
					mComputerId, PaymentActivity.PAY_TYPE_CREDIT, 
						mTotalCreditPay, cardNo, mExpMonth, mExpYear, mBankId, mCardTypeId)){
				finish();
			}
		}else{
			if(cardNo.isEmpty()){
				new AlertDialog.Builder(CreditPayActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.payment)
				.setMessage(R.string.promp_card_no)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.show();
			}else if(mTotalCreditPay == 0){
				new AlertDialog.Builder(CreditPayActivity.this)
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
	}
	
	private void loadCreditCardType(){
		CreditCard credit = new CreditCard(CreditPayActivity.this);
		mCreditCardLst = credit.listAllCreditCardType();
		
		ArrayAdapter<CreditCardType> adapter = 
				new ArrayAdapter<CreditCardType>(CreditPayActivity.this, 
						android.R.layout.simple_dropdown_item_1line, mCreditCardLst);
		mSpCardType.setAdapter(adapter);
		mSpCardType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				CreditCardType card = (CreditCardType) parent.getItemAtPosition(position);
				mCardTypeId = card.getCreditCardTypeId();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void loadBankName(){
		Bank bank = new Bank(CreditPayActivity.this);
		mBankLst = bank.listAllBank();
		
		ArrayAdapter<BankName> adapter = 
				new ArrayAdapter<BankName>(CreditPayActivity.this, 
						android.R.layout.simple_dropdown_item_1line, mBankLst);
		mSpBank.setAdapter(adapter);
		mSpBank.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				BankName bank = (BankName) parent.getItemAtPosition(position);
				mBankId = bank.getBankNameId();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_confirm, menu);
		menu.findItem(R.id.itemClose).setVisible(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemCancel:
			finish();
			return true;
		case R.id.itemConfirm:
			addPayment();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}
}
