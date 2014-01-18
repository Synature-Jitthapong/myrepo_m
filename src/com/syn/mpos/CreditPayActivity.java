package com.syn.mpos;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.syn.mpos.R;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.CreditCard;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	
	private float mPaymentLeft;
	private PaymentDetail mPayment;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	private float mTotalCreditPay;
	
	private EditText mTxtTotalPrice;
	private EditText mTxtTotalPay;
	private EditText mTxtCardNoSeq1;
	private EditText mTxtCardNoSeq2;
	private EditText mTxtCardNoSeq3;
	private EditText mTxtCardNoSeq4;
	private EditText mTxtCVV2;
	private Spinner mSpBank;
	private Spinner mSpCardType;
	private Spinner mSpExpYear;
	private Spinner mSpExpMonth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_pay);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mTxtTotalPrice = (EditText) findViewById(R.id.txtCardTotalPrice);
		mTxtTotalPay = (EditText) findViewById(R.id.txtCardPayAmount);
		mTxtCardNoSeq1 = (EditText) findViewById(R.id.txtCardNoSeq1);
		mTxtCardNoSeq2 = (EditText) findViewById(R.id.txtCardNoSeq2);
		mTxtCardNoSeq3 = (EditText) findViewById(R.id.txtCardNoSeq3);
		mTxtCardNoSeq4 = (EditText) findViewById(R.id.txtCardNoSeq4);
		mTxtCVV2 = (EditText) findViewById(R.id.txtCvv2);
		
		mSpBank = (Spinner) findViewById(R.id.spBank);
		mSpCardType = (Spinner) findViewById(R.id.spCardType);
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
		mPaymentLeft = intent.getFloatExtra("paymentLeft", 0);
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
			months[i] = String.format("%02d", i + 1);
		}
		SpinnerAdapter adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, months);
		mSpExpMonth.setAdapter(adapter);
		Calendar c = Calendar.getInstance(Locale.getDefault());
		mSpExpMonth.setSelection(c.get(Calendar.MONTH));
	}
	
	private void init(){
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		
		displayTotalPrice();
		loadCreditCardType();
		loadBankName();
		createSpinnerMonth();
		createSpinnerYear();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(mPaymentLeft));
	}
	
	private boolean checkCardNoSeq(){
		boolean already = false;
		if(!mTxtCardNoSeq1.equals("") && !mTxtCardNoSeq2.equals("") && 
				!mTxtCardNoSeq3.equals("") && !mTxtCardNoSeq4.equals("")){
			if(mTxtCardNoSeq1.getText().toString().length() < 4){
				already = false;
				mTxtCardNoSeq1.requestFocus();
			}
			else if(mTxtCardNoSeq2.getText().toString().length() < 4){
				already = false;
				mTxtCardNoSeq2.requestFocus();
			}
			else if(mTxtCardNoSeq3.getText().toString().length() < 4){
				already = false;
				mTxtCardNoSeq3.requestFocus();
			}
			else if(mTxtCardNoSeq4.getText().toString().length() < 4){
				already = false;
				mTxtCardNoSeq4.requestFocus();
			}
			else{
				already = true;
			}
		}else{
			already = false;
		}
		return already;
	}
	
	private void addPayment(){
		if (checkCardNoSeq()) {
			if (!mTxtCVV2.getText().toString().isEmpty()) {
				try {
					mTotalCreditPay = Float.parseFloat(mTxtTotalPay.getText()
							.toString());
				} catch (NumberFormatException e) {
					mTotalCreditPay = 0.0f;
					e.printStackTrace();
				}
				mTxtTotalPay.setText(MPOSApplication.getGlobalProperty()
						.currencyFormat(mTotalCreditPay));
				if (mTotalCreditPay > 0) {
					LayoutInflater inflater = (LayoutInflater) 
							this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View cardConfirmView = inflater.inflate(R.layout.confirm_credit_pay_layout, null);
					((EditText) cardConfirmView.findViewById(R.id.txtTotalPay))
						.setText(mTxtTotalPay.getText().toString());
					((EditText) cardConfirmView.findViewById(R.id.txtCardNo))
						.setText(mTxtCardNoSeq1.getText().toString() + "-" +
								mTxtCardNoSeq2.getText().toString() + "-" +
								mTxtCardNoSeq3.getText().toString() + "-" +
								mTxtCardNoSeq4.getText().toString());
					((EditText) cardConfirmView.findViewById(R.id.txtCardType))
						.setText(mSpCardType.getItemAtPosition(mSpCardType.getSelectedItemPosition()).toString());
					((EditText) cardConfirmView.findViewById(R.id.txtBank))
						.setText(mSpBank.getItemAtPosition(mSpBank.getSelectedItemPosition()).toString());
					((EditText) cardConfirmView.findViewById(R.id.txtExpDate))
						.setText(mSpExpMonth.getItemAtPosition(mSpExpMonth.getSelectedItemPosition()).toString() + "/" +
								mSpExpYear.getItemAtPosition(mSpExpYear.getSelectedItemPosition()).toString());
					AlertDialog.Builder builder = new AlertDialog.Builder(CreditPayActivity.this);
					builder.setTitle(R.string.credit_pay);
					builder.setView(cardConfirmView);
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					builder.setNeutralButton(android.R.string.ok, null);
					
					final AlertDialog d = builder.create();
					d.show();
					
					Button btnConfirm = d.getButton(AlertDialog.BUTTON_NEUTRAL);
					btnConfirm.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							String cardNo = mTxtCardNoSeq1.getText().toString()
									+ mTxtCardNoSeq2.getText().toString()
									+ mTxtCardNoSeq3.getText().toString()
									+ mTxtCardNoSeq4.getText().toString();
							if (mPayment.addPaymentDetail(mTransactionId, mComputerId,
									PaymentActivity.PAY_TYPE_CREDIT, mTotalCreditPay,
									cardNo, mExpMonth, mExpYear, mBankId, mCardTypeId)) {
								d.dismiss();
								finish();
							}
						}
						
					});
				} else {
					mTxtTotalPay.requestFocus();
					new AlertDialog.Builder(CreditPayActivity.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.payment)
							.setMessage(R.string.enter_enough_money)
							.setNeutralButton(R.string.close,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).show();
				}
			} else {
				mTxtCVV2.requestFocus();
				new AlertDialog.Builder(CreditPayActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.payment)
				.setMessage(R.string.promp_cvv2)
				.setNeutralButton(R.string.close,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();
			}
		} else {
			new AlertDialog.Builder(CreditPayActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.payment)
			.setMessage(R.string.promp_card_no)
			.setNeutralButton(R.string.close,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {

						}
					}).show();
		}	
	}
	
	private void loadCreditCardType(){
		CreditCard credit = new CreditCard(MPOSApplication.getWriteDatabase());
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
		Bank bank = new Bank(MPOSApplication.getWriteDatabase());
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
		inflater.inflate(R.menu.activity_credit_pay, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
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
