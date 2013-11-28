package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import com.syn.mpos.R;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.CreditCard;
import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CreditPayActivity extends Activity {
	private int mBankId;
	private int mCardTypeId;
	
	private Calendar mCalendar;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	private float mTotalCreditPay;
	
	private EditText mTxtTotalPrice;
	private EditText mTxtTotalPay;
	private EditText mTxtCardNo;
	private Spinner mSpinnerBank;
	private Spinner mSpinnerCardType;
	private Button mBtnExpire;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_pay);
	
		mTxtTotalPrice = (EditText) findViewById(R.id.editTextCreditTotalPrice);
		mTxtTotalPay = (EditText) findViewById(R.id.editTextCreditPayAmount);
		mTxtCardNo = (EditText) findViewById(R.id.editTextCreditNo);
		mSpinnerBank = (Spinner) findViewById(R.id.spinnerBank);
		mSpinnerCardType = (Spinner) findViewById(R.id.spinnerCardType);
		mBtnExpire = (Button) findViewById(R.id.btnPopCardExp);
		mTxtTotalPay.setSelectAllOnFocus(true);
		
		init();
	}

	private void init(){
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		displayTotalPrice();
		loadCreditCardType();
		loadBankName();
	}
	
	public void cardExpClicked(final View v){
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendar.setTimeInMillis(date);
				mBtnExpire.setText(MainActivity.mFormat.dateFormat(mCalendar.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(MainActivity.mFormat.currencyFormat(PaymentActivity.mTotalSalePrice));
	}
	
	private void addPayment(){
		String cardNo = mTxtCardNo.getText().toString();
		int expYear = mCalendar.get(Calendar.YEAR);
		int expMonth = mCalendar.get(Calendar.MONTH);
		
		try {
			mTotalCreditPay = Float.parseFloat(mTxtTotalPay.getText().toString());
		} catch (NumberFormatException e) {
			mTotalCreditPay = 0.0f;
			e.printStackTrace();
		}
		
		mTxtTotalPay.setText(MainActivity.mFormat.currencyFormat(mTotalCreditPay));
		if(!cardNo.isEmpty() && mTotalCreditPay > 0){
			if(PaymentActivity.mPayment.addPaymentDetail(MPOSTransaction.mTransactionId, 
					MPOSTransaction.mComputerId, PaymentActivity.PAY_TYPE_CREDIT, 
						mTotalCreditPay, cardNo, expMonth, expYear, mBankId, mCardTypeId)){
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
		mSpinnerCardType.setAdapter(adapter);
		mSpinnerCardType.setOnItemSelectedListener(new OnItemSelectedListener(){

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
		mSpinnerBank.setAdapter(adapter);
		mSpinnerBank.setOnItemSelectedListener(new OnItemSelectedListener(){

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
