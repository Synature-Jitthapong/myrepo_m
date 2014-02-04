package com.syn.mpos;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.syn.mpos.R;
import com.syn.mpos.provider.Bank;
import com.syn.mpos.provider.CreditCard;
import com.syn.mpos.provider.PaymentDetail;
import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class CreditPayDialogBuilder extends AlertDialog.Builder {
	private int mTransactionId;
	private int mComputerId;
	private int mBankId;
	private int mCardTypeId;
	private int mExpYear;
	private int mExpMonth;
	
	private Context mContext;
	private double mPaymentLeft;
	private PaymentDetail mPayment;
	private List<BankName> mBankLst;
	private List<CreditCardType> mCreditCardLst;
	private double mTotalCreditPay;
	
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
	
	public CreditPayDialogBuilder(Context context, int transactionId, int computerId, 
			double paymentLeft) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.credit_pay_layout, null);
		setView(view);
		
		mTxtTotalPrice = (EditText) view.findViewById(R.id.txtCardTotalPrice);
		mTxtTotalPay = (EditText) view.findViewById(R.id.txtCardPayAmount);
		mTxtCardNoSeq1 = (EditText) view.findViewById(R.id.txtCardNoSeq1);
		mTxtCardNoSeq2 = (EditText) view.findViewById(R.id.txtCardNoSeq2);
		mTxtCardNoSeq3 = (EditText) view.findViewById(R.id.txtCardNoSeq3);
		mTxtCardNoSeq4 = (EditText) view.findViewById(R.id.txtCardNoSeq4);
		mTxtCVV2 = (EditText) view.findViewById(R.id.txtCvv2);
		
		mSpBank = (Spinner) view.findViewById(R.id.spBank);
		mSpCardType = (Spinner) view.findViewById(R.id.spCardType);
		mSpExpYear = (Spinner) view.findViewById(R.id.spExpYear);
		mSpExpMonth = (Spinner) view.findViewById(R.id.spExpMonth);
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
		mTransactionId = transactionId;
		mComputerId = computerId;
		mPaymentLeft = paymentLeft;
		init();
		
	}

	private void createSpinnerYear(){
		String[] years = new String[10];
		Calendar c = Calendar.getInstance(Locale.getDefault());
		for(int i = 0; i < years.length; i++){
			years[i] = String.valueOf(c.get(Calendar.YEAR) + i);
		}
		SpinnerAdapter adapter = new ArrayAdapter<String>(mContext, 
				android.R.layout.simple_spinner_dropdown_item, years);
		mSpExpYear.setAdapter(adapter);
	}
	
	private void createSpinnerMonth(){
		String[] months = new String[12];
		for(int i = 0; i < months.length; i++){
			months[i] = String.format("%02d", i + 1);
		}
		SpinnerAdapter adapter = new ArrayAdapter<String>(mContext, 
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

	public void addPayment(final OnCreditPayListener listener){
		if (checkCardNoSeq()) {
			if (!mTxtCVV2.getText().toString().isEmpty()) {
				try {
					mTotalCreditPay = Double.parseDouble(mTxtTotalPay.getText()
							.toString());
				} catch (NumberFormatException e) {
					mTotalCreditPay = 0.0f;
					e.printStackTrace();
				}
				mTxtTotalPay.setText(MPOSApplication.getGlobalProperty()
						.currencyFormat(mTotalCreditPay));
				if (mTotalCreditPay > 0) {
					LayoutInflater inflater = (LayoutInflater) 
							mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.credit_pay);
					builder.setView(cardConfirmView);
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onCancel();
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
							try {
								mPayment.addPaymentDetail(mTransactionId,
										mComputerId,
										PaymentDetail.PAY_TYPE_CREDIT,
										mTotalCreditPay, cardNo, mExpMonth,
										mExpYear, mBankId, mCardTypeId);
								d.dismiss();
								listener.onOk();
							} catch (SQLException e) {
								new AlertDialog.Builder(mContext)
								.setMessage(e.getMessage())
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										listener.onCancel();
									}
									
								}).show();
							}
							
						}
						
					});
				} else {
					listener.onRequiredSomeParam();
					
					mTxtTotalPay.requestFocus();
					new AlertDialog.Builder(mContext)
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
				listener.onRequiredSomeParam();
				
				mTxtCVV2.requestFocus();
				new AlertDialog.Builder(mContext)
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
			listener.onRequiredSomeParam();
			
			new AlertDialog.Builder(mContext)
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
				new ArrayAdapter<CreditCardType>(mContext, 
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
				new ArrayAdapter<BankName>(mContext, 
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
	
	public static interface OnCreditPayListener{
		void onOk();
		void onCancel();
		void onRequiredSomeParam();
	}
}
