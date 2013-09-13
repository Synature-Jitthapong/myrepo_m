package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.db.Bank;
import com.syn.mpos.db.CreditCard;
import com.syn.mpos.db.MPOSOrder;
import com.syn.mpos.db.MPOSPayment;
import com.syn.mpos.db.MPOSTransaction;
import com.syn.mpos.model.BankName;
import com.syn.mpos.model.CreditCardType;
import com.syn.mpos.model.OrderTransaction;
import com.syn.pos.Order;
import com.syn.pos.Payment;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CreditPayActivity extends Activity{
	private Context mContext; 
	private int mTransactionId;
	private int mComputerId;
	private int mBankId;
	private int mCardTypeId;
	private float mTotalPrice;
	private float mTotalPay;
	private Calendar mCalendar;
	private Formatter mFormat;
	private Order mOrder;
	private Payment mPayment;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	
	private StringBuilder mStrTotalPaid;
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
		mOrder = new MPOSOrder(mContext);
		mPayment = new MPOSPayment(mContext);
		mStrTotalPaid = new StringBuilder();
		
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
				mOrder.getSummary(mTransactionId, mComputerId);
		
		mTotalPrice = order.getTotalSalePrice(); 
		
		displayTotalPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mFormat.currencyFormat(mTotalPrice));
		
		displayTotalPaid();
	}

	private void displayTotalPaid(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPaid.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
			e.printStackTrace();
		}
		
		mTxtTotalPay.setText(mFormat.currencyFormat(mTotalPay));
	}
	
	private void addPayment(){
		String cardNo = mTxtCardNo.getText().toString();
		int expYear = mCalendar.get(Calendar.YEAR);
		int expMonth = mCalendar.get(Calendar.MONTH);
		
		if(!cardNo.isEmpty()){
			if(mPayment.addPaymentDetail(mTransactionId, mComputerId, PaymentActivity.PAY_TYPE_CREDIT, 
						mTotalPay, cardNo, expMonth, expYear, mBankId, mCardTypeId)){
				finish();
			}
		}else{
			Util.alert(mContext, android.R.drawable.ic_dialog_alert, R.string.payment, R.string.promp_card_no);
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
	
	public void creditPayClicked(final View v){
		switch (v.getId()) {
		case R.id.btnCredit0:
			mStrTotalPaid.append("0");
			break;
		case R.id.btnCredit1:
			mStrTotalPaid.append("1");
			break;
		case R.id.btnCredit2:
			mStrTotalPaid.append("2");
			break;
		case R.id.btnCredit3:
			mStrTotalPaid.append("3");
			break;
		case R.id.btnCredit4:
			mStrTotalPaid.append("4");
			break;
		case R.id.btnCredit5:
			mStrTotalPaid.append("5");
			break;
		case R.id.btnCredit6:
			mStrTotalPaid.append("6");
			break;
		case R.id.btnCredit7:
			mStrTotalPaid.append("7");
			break;
		case R.id.btnCredit8:
			mStrTotalPaid.append("8");
			break;
		case R.id.btnCredit9:
			mStrTotalPaid.append("9");
			break;
		case R.id.btnCreditDel:
			try {
				mStrTotalPaid.deleteCharAt(mStrTotalPaid.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btnCreditDot:
			mStrTotalPaid.append(".");
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
