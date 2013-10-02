package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import com.syn.mpos.R;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.CreditCard;
import com.syn.mpos.transaction.MPOSPayment;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import com.syn.pos.OrderTransaction;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
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

public class CreditPayActivity extends Activity implements OnConfirmClickListener{
	private Context mContext; 
	private int mTransactionId;
	private int mComputerId;
	private int mBankId;
	private int mCardTypeId;
	private float mTotalPrice;
	private float mTotalPay;
	private Calendar mCalendar;
	private Formatter mFormat;
	private MPOSTransaction mTrans;
	private MPOSPayment mPayment;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	
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
		mContext = CreditPayActivity.this;
	
		mTxtTotalPrice = (EditText) findViewById(R.id.editTextCreditTotalPrice);
		mTxtTotalPay = (EditText) findViewById(R.id.editTextCreditPayAmount);
		mTxtCardNo = (EditText) findViewById(R.id.editTextCreditNo);
		mSpinnerBank = (Spinner) findViewById(R.id.spinnerBank);
		mSpinnerCardType = (Spinner) findViewById(R.id.spinnerCardType);
		mBtnExpire = (Button) findViewById(R.id.btnPopCardExp);

		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		if(mTransactionId != 0 && mComputerId != 0){
			init();
		}else{
			finish();
		}
	}

	private void init(){
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		mFormat = new Formatter(mContext);
		mTrans = new MPOSTransaction(mContext);
		mPayment = new MPOSPayment(mContext);
		
		loadTotalPrice();
		loadCreditCardType();
		loadBankName();
	}
	
	public void cardExpClicked(final View v){
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendar.setTimeInMillis(date);
				mBtnExpire.setText(mFormat.dateFormat(mCalendar.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
	}
	
	private void loadTotalPrice(){
		OrderTransaction.OrderDetail order = 
				mTrans.getSummary(mTransactionId, mComputerId);
		
		mTotalPrice = order.getTotalSalePrice() + order.getVat(); 
		
		displayTotalPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mFormat.currencyFormat(mTotalPrice));
	}
	
	private void addPayment(){
		String cardNo = mTxtCardNo.getText().toString();
		int expYear = mCalendar.get(Calendar.YEAR);
		int expMonth = mCalendar.get(Calendar.MONTH);
		
		try {
			mTotalPay = Float.parseFloat(mTxtTotalPay.getText().toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
			e.printStackTrace();
		}
		
		mTxtTotalPay.setText(mFormat.currencyFormat(mTotalPay));
		if(!cardNo.isEmpty() && mTotalPay > 0){
			if(mPayment.addPaymentDetail(mTransactionId, mComputerId, PaymentActivity.PAY_TYPE_CREDIT, 
						mTotalPay, cardNo, expMonth, expYear, mBankId, mCardTypeId)){
				finish();
			}
		}else{
			if(cardNo.isEmpty()){
				Util.alert(mContext, android.R.drawable.ic_dialog_alert, R.string.payment, R.string.promp_card_no);
			}else if(mTotalPay == 0){
				Util.alert(mContext, android.R.drawable.ic_dialog_alert, R.string.payment, R.string.enter_enough_money);
			}
		}
	}
	
	private void loadCreditCardType(){
		CreditCard credit = new CreditCard(mContext);
		mCreditCardLst = credit.listAllCreditCardType();
		
		ArrayAdapter<CreditCardType> adapter = 
				new ArrayAdapter<CreditCardType>(mContext, 
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
		Bank bank = new Bank(mContext);
		mBankLst = bank.listAllBank();
		
		ArrayAdapter<BankName> adapter = 
				new ArrayAdapter<BankName>(mContext, 
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

	@Override
	public void onSaveClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfirmClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCancelClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
