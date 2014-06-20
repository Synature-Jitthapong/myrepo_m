package com.synature.mpos;

import com.synature.exceptionhandler.ExceptionHandler;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.MPOSOrderTransaction;
import com.synature.mpos.database.Transaction;
import com.synature.pos.PrepaidCardInfo;
import com.synature.util.CreditCardParser;
import com.synature.util.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class FoodCourtCardPayActivity extends Activity implements Runnable{
	public static final int STATUS_READY_TO_USE = 1; 	//Ready to Use – พร้อมเติมเงินใช้งาน
	public static final int STATUS_INUSE = 2;			//In Use – มีการใช้งานอยู่
	public static final int STATUS_BLACK_LIST = 3;		//BlackList – เป็นบัตรที่ติด BlackList
	public static final int STATUS_CANCEL = 4;			//Cancel – บัตรยกเลิก ไม่สามารถใช้งานได้แล้ว
	public static final int STATUS_MISSING = 5;			//Missing – บัตรหาย

	/*
	 * is magnatic read state
	 */
	private boolean mIsRead = false;
	
	/*
	 * Thread for run magnetic reader listener 
	 */
	private Thread mMsrThread;

	private WintecMagneticReader mMsrReader;
	
	private Transaction mTrans;
	private Formater mFormat;
	
	private int mTransactionId;
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	private double mTotalSalePrice = 0.0d;
	private double mCardBalance = 0.0d;
	
	private MenuItem mItemConfirm;
	private EditText mTxtTotalPrice;
	private EditText mTxtCardNo;
	private EditText mTxtBalance;
	private ImageButton mBtnCheckCard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME));
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    params.height= WindowManager.LayoutParams.WRAP_CONTENT;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_food_court_card_pay);
		
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotal);
		mTxtCardNo = (EditText) findViewById(R.id.txtCardNo);
		mTxtBalance = (EditText) findViewById(R.id.txtBalance);
		mBtnCheckCard = (ImageButton) findViewById(R.id.btnCheckCard);
		mTxtCardNo.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() != KeyEvent.ACTION_DOWN)
					return true;
				
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					loadCardInfo();
				}
				return false;
			}
		});
		
		mBtnCheckCard.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				loadCardInfo();
			}
			
		});
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		mTrans = new Transaction(this);
		mFormat = new Formater(this);
		mTxtBalance.setText(mFormat.currencyFormat(mCardBalance));
		summary();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// start magnetic reader thread
		try {
			mMsrReader = new WintecMagneticReader();
			mMsrThread = new Thread(this);
			mMsrThread.start();
			mIsRead = true;
			Logger.appendLog(this, MPOSApplication.LOG_DIR, 
					MPOSApplication.LOG_FILE_NAME, "Start magnetic reader thread");
		} catch (Exception e) {
			Logger.appendLog(this, MPOSApplication.LOG_DIR, 
					MPOSApplication.LOG_FILE_NAME, 
					"Error start magnetic reader thread " + 
					e.getMessage());
		}
		//test();
	}

	@Override
	protected void onStop() {
		closeMsrThread();
		mIsRead = false;
		mMsrReader.close();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.food_court_card_pay, menu);
		mItemConfirm = menu.findItem(R.id.itemConfirm);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				finish();
				return true;
			case R.id.itemConfirm:
				confirm();
				return true;
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	private void loadCardInfo(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mTxtCardNo.getWindowToken(), 0);
		if(!TextUtils.isEmpty(mTxtCardNo.getText())){
			new FoodCourtBalanceOfCard(this, mShopId, mComputerId,
					mStaffId, mTxtCardNo.getText().toString(), 
					mCardBalanceListener).execute(MPOSApplication.getFullUrl(this));
		}else{
			mTxtCardNo.requestFocus();
		}
	}
	
	private void confirm(){
		if(!TextUtils.isEmpty(mTxtCardNo.getText())){
			if(mCardBalance >= mTotalSalePrice){
				new FoodCourtCardPay(this, mShopId, mComputerId,
						mStaffId, mTxtCardNo.getText().toString(),
						mFormat.currencyFormat(mTotalSalePrice), mCardPayListener).execute(MPOSApplication.getFullUrl(this));
			}else{
				new AlertDialog.Builder(FoodCourtCardPayActivity.this)
				.setTitle(R.string.payment)
				.setMessage("Your balance not enough!")
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
		}else{
			mTxtCardNo.requestFocus();
		}
	}
	
	private void summary(){ 
		MPOSOrderTransaction.MPOSOrderDetail summOrder = 
				mTrans.getSummaryOrder(mTransactionId);
		mTotalSalePrice = summOrder.getTotalSalePrice() + summOrder.getVatExclude();
		mTxtTotalPrice.setText(mFormat.currencyFormat(mTotalSalePrice));		
	}

	/*
	 * Close magnetic reader thread
	 */
	private synchronized void closeMsrThread(){
		if(mMsrThread != null){
			mMsrThread.interrupt();
			mMsrThread = null;
		}
	}
	
	@Override
	public void run() {
		while(mIsRead){
			try {
				final String content = mMsrReader.getTrackData();
				if(content.length() > 0){
					Logger.appendLog(getApplicationContext(), 
						MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME,
						"Content : " + content);
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							try {
								CreditCardParser parser = new CreditCardParser();
								if(parser.parser(content)){
									String cardNo = parser.getCardNo();
									mTxtCardNo.setText(null);
									mTxtCardNo.setText(cardNo);
									loadCardInfo();
								}
							} catch (Exception e) {
								Logger.appendLog(getApplicationContext(), 
										MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME, 
										"Error parser card : " + e.getMessage());
							}
						}
						
					});
				}
			} catch (Exception e) {
				Logger.appendLog(getApplicationContext(), 
						MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME, 
						" Error when read data from magnetic card : " + e.getMessage());
			}
		}
	}
	
	private void clearTextBox(){
		mTxtCardNo.setText(null);
		mTxtBalance.setText(null);
	}
	
	/**
	 * listener when call service to pay
	 */
	private FoodCourtMainService.FoodCourtWebServiceListener mCardPayListener 
		= new FoodCourtMainService.FoodCourtWebServiceListener(){

		@Override
		public void onPre() {
			mItemConfirm.setEnabled(false);
		}

		@Override
		public void onPost(PrepaidCardInfo cardInfo) {
			mItemConfirm.setEnabled(true);
			if(cardInfo != null){
				mCardBalance = cardInfo.getfCurrentAmount();
				mTxtBalance.setText(mFormat.currencyFormat(mCardBalance));
				
				LayoutInflater inflater = (LayoutInflater) 
						getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.food_court_payment_result, null);
				((TextView) view.findViewById(R.id.textView2)).setText("Payment Success.");
				((EditText) view.findViewById(R.id.txtCardBalance)).setText(mTxtBalance.getText().toString());
				new AlertDialog.Builder(FoodCourtCardPayActivity.this)
				.setTitle(R.string.payment)
				.setView(view)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
		}

		@Override
		public void onError(String msg) {
			clearTextBox();
			mItemConfirm.setEnabled(true);
			new AlertDialog.Builder(FoodCourtCardPayActivity.this)
			.setTitle(R.string.payment)
			.setMessage(msg)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
		}
	
	};

	/**
	 * listener when check balance
	 */
	private FoodCourtMainService.FoodCourtWebServiceListener mCardBalanceListener 
		= new FoodCourtMainService.FoodCourtWebServiceListener(){

			@Override
			public void onPre() {
				mBtnCheckCard.setEnabled(false);
			}

			@Override
			public void onPost(PrepaidCardInfo cardInfo) {
				mBtnCheckCard.setEnabled(true);
				if(cardInfo != null){
					mCardBalance = cardInfo.getfCurrentAmount();
					mTxtBalance.setText(mFormat.currencyFormat(mCardBalance));
					if(mCardBalance < mTotalSalePrice){
						mTxtBalance.setTextColor(Color.RED);
					}else{
						mTxtBalance.setTextColor(Color.BLACK);
					}
				}
			}

			@Override
			public void onError(String msg) {
				clearTextBox();
				mBtnCheckCard.setEnabled(true);
				new AlertDialog.Builder(FoodCourtCardPayActivity.this)
				.setTitle(R.string.payment)
				.setMessage(msg)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
		
	};
}
