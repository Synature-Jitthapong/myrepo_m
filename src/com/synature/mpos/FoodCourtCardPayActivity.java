package com.synature.mpos;

import com.synature.mpos.database.FormaterDao;
import com.synature.mpos.database.ShopDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.foodcourt.R;
import com.synature.pos.PrepaidCardInfo;
import com.synature.util.CreditCardParser;
import com.synature.util.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class FoodCourtCardPayActivity extends Activity implements Runnable{
	public static final int STATUS_READY_TO_USE = 1; 	//Ready to Use
	public static final int STATUS_INUSE = 2;			//In Use
	public static final int STATUS_BLACK_LIST = 3;		//BlackList
	public static final int STATUS_CANCEL = 4;			//Cancel
	public static final int STATUS_MISSING = 5;			//Missing
		
	/**
	 * Point
	 */
	public static final float POINT = 100000; 
	
	/*
	 * is magnatic read state
	 */
	private boolean mIsRead = false;
	
	/*
	 * Thread for run magnetic reader listener 
	 */
	private Thread mMsrThread;

	private WintecMagneticReader mMsrReader;
	
	private TransactionDao mTrans;
	private FormaterDao mFormat;
	
	private int mTransactionId;
	private int mShopId;
	private int mComputerId;
	private int mStaffId;
	private double mTotalSalePrice = 0.0d;
	private double mCardBalanceBefore = 0.0d;
	private double mCardBalance = 0.0d;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    params.height= getResources().getInteger(R.integer.activity_dialog_height);
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_food_court_card_pay);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		mTrans = new TransactionDao(this);
		mFormat = new FormaterDao(this);
		
		if(savedInstanceState == null){
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment(), "Placeholder").commit();
		}
	}

	public static void setPoint(Context context, float point){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		sharedPref.edit().putFloat("point", point).commit();	
	}
	
	public static float getPoint(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		float point = sharedPref.getFloat("point", 0);
		return point;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// start magnetic reader thread
		try {
			mMsrReader = new WintecMagneticReader(this);
			mMsrThread = new Thread(this);
			mIsRead = true;
			mMsrThread.start();
			Logger.appendLog(this, Utils.LOG_PATH, 
					Utils.LOG_FILE_NAME, "Start magnetic reader thread");
		} catch (Exception e) {
			Logger.appendLog(this, Utils.LOG_PATH, 
					Utils.LOG_FILE_NAME, 
					"Error start magnetic reader thread " + 
					e.getMessage());
		}
		//test();
	}

	@Override
	protected void onStop() {
		closeMsrThread();
		mIsRead = false;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
						Utils.LOG_PATH, Utils.LOG_FILE_NAME,
						"Content : " + content);
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							try {
								String[] track1 = content.split(":");
								String memberCode = track1[1].replace("?", "");
								String cardNo = "0001";//memberCode;
								PlaceholderFragment fragment = (PlaceholderFragment)
										getFragmentManager().findFragmentById(R.id.container);
								fragment.mTxtCardNo.setText(null);
								fragment.mTxtCardNo.setText(cardNo);
								fragment.loadCardInfo();
								
							} catch (Exception e) {
								Logger.appendLog(getApplicationContext(), 
										Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
										"Error parser card : " + e.getMessage());
							}
						}
						
					});
				}
			} catch (Exception e) {
				Logger.appendLog(getApplicationContext(), 
						Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
						" Error when read data from magnetic card : " + e.getMessage());
			}
		}
		mMsrReader.close();
	}
	
	public static class PayResultFragment extends Fragment{
		
		private FoodCourtCardPayActivity mHost;
		private double mBalance;
		private TextView mTvResult;
		private EditText mTxtCardBalance;
		
		public static PayResultFragment newInstance(double balance){
			PayResultFragment f = new PayResultFragment();
			Bundle b = new Bundle();
			b.putDouble("balance", balance);
			f.setArguments(b);
			return f;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mHost = (FoodCourtCardPayActivity) getActivity();
			mBalance = getArguments().getDouble("balance");
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.food_court_payment_result, container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			mTvResult = (TextView) view.findViewById(R.id.textView2);
			mTxtCardBalance = (EditText) view.findViewById(R.id.txtCardBalance);
			mTvResult.setText("Successfully.");
			mTxtCardBalance.setText(mHost.mFormat.currencyFormat(mBalance));
		}
	}
	
	public static class PlaceholderFragment extends Fragment{
		
		private FoodCourtCardPayActivity mHost;
		private MenuItem mItemConfirm;
		private EditText mTxtTotalPrice;
		private EditText mTxtCardNo;
		private EditText mTxtBalance;
		private ImageButton mBtnCheckCard;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
			mHost = (FoodCourtCardPayActivity) getActivity();
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.food_court_card_pay, menu);
			mItemConfirm = menu.findItem(R.id.itemConfirm);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()){
			case R.id.itemConfirm:
				confirm();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_food_court_card_pay, container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			mTxtTotalPrice = (EditText) view.findViewById(R.id.txtTotal);
			mTxtCardNo = (EditText) view.findViewById(R.id.txtCardNo);
			mTxtBalance = (EditText) view.findViewById(R.id.editText1);
			mBtnCheckCard = (ImageButton) view.findViewById(R.id.btnCheckCard);
			
			//mTxtBalance.setText(mHost.mFormat.currencyFormat(mHost.mCardBalance));
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
			summary();
		}
		
		private void loadCardInfo(){
			InputMethodManager imm = (InputMethodManager) 
					getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mTxtCardNo.getWindowToken(), 0);
			if(!TextUtils.isEmpty(mTxtCardNo.getText())){
				new FoodCourtBalanceOfCard(getActivity(), mHost.mShopId, 
						mHost.mComputerId, mHost.mStaffId, mTxtCardNo.getText().toString(), 
						mHost.mCardBalanceListener).execute(Utils.getFullUrl(getActivity()));
			}else{
				mTxtCardNo.requestFocus();
			}
		}
		
		private void confirm(){
			if(!TextUtils.isEmpty(mTxtCardNo.getText())){
				if(mHost.mCardBalance >= mHost.mTotalSalePrice){
					
					new FoodCourtCardPay(getActivity(), mHost.mShopId, 
							mHost.mComputerId, mHost.mStaffId, mTxtCardNo.getText().toString(),
							String.valueOf(mHost.mTotalSalePrice), 
							mHost.mCardPayListener).execute(Utils.getFullUrl(getActivity()));
				}else{
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.payment)
					.setMessage("Your point not enough!")
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
			OrderDetail summOrder = 
					mHost.mTrans.getSummaryOrder(mHost.mTransactionId);
			mHost.mTotalSalePrice = summOrder.getTotalSalePrice();
			mTxtTotalPrice.setText(mHost.mFormat.currencyFormat(mHost.mTotalSalePrice));		
		}
	}
	
	private void clearTextBox(){
		PlaceholderFragment fragment = (PlaceholderFragment)
				getFragmentManager().findFragmentById(R.id.container);
		fragment.mTxtCardNo.setText(null);
		fragment.mTxtBalance.setText(null);
	}
	
	/**
	 * listener when call service to pay
	 */
	private FoodCourtMainService.FoodCourtWebServiceListener mCardPayListener 
		= new FoodCourtMainService.FoodCourtWebServiceListener(){

		@Override
		public void onPre() {
			PlaceholderFragment fragment = (PlaceholderFragment)
					getFragmentManager().findFragmentById(R.id.container);
			fragment.mItemConfirm.setEnabled(false);
		}

		@Override
		public void onPost(PrepaidCardInfo cardInfo) {
			PlaceholderFragment placeholder = (PlaceholderFragment)
					getFragmentManager().findFragmentById(R.id.container);
			placeholder.mItemConfirm.setEnabled(true);
			if(cardInfo != null){
				mCardBalance = cardInfo.getfCurrentAmount();
				placeholder.mTxtBalance.setText(mFormat.currencyFormat(mCardBalance));
			
				PayResultFragment fragment = PayResultFragment.newInstance(mCardBalance);
				getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
				
				ShopDao shop = new ShopDao(FoodCourtCardPayActivity.this);
				mTrans.closeTransaction(mTransactionId, mStaffId, mTotalSalePrice, 
						shop.getCompanyVatType(), shop.getCompanyVatRate());
				new ReceiptPrint().run();
			}
		}

		@Override
		public void onError(String msg) {
			PlaceholderFragment fragment = (PlaceholderFragment)
					getFragmentManager().findFragmentById(R.id.container);
			clearTextBox();
			fragment.mItemConfirm.setEnabled(true);
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

			//ProgressDialog progress;
		
			@Override
			public void onPre() { 
//				progress = new ProgressDialog(FoodCourtCardPayActivity.this);
//				progress.setCancelable(false);
//				progress.setMessage("Please wait...");
//				progress.show();
				PlaceholderFragment fragment = (PlaceholderFragment)
						getFragmentManager().findFragmentById(R.id.container);
				fragment.mBtnCheckCard.setEnabled(false);
			}

			@Override
			public void onPost(PrepaidCardInfo cardInfo) {
//				if(progress.isShowing())
//					progress.dismiss();
				
				PlaceholderFragment fragment = (PlaceholderFragment)
						getFragmentManager().findFragmentById(R.id.container);
				fragment.mBtnCheckCard.setEnabled(true);
				if(cardInfo != null){
					mCardBalance = cardInfo.getfCurrentAmount();
					mCardBalanceBefore = mCardBalance;
					fragment.mTxtBalance.setText(mFormat.currencyFormat(mCardBalance));
					//fragment.mTxtMember.setText("สมชาย สายสมาน");
					if(mCardBalance < mTotalSalePrice){
						fragment.mTxtBalance.setTextColor(Color.RED);
					}else{
						fragment.mTxtBalance.setTextColor(Color.BLACK);
					}
				}
			}

			@Override
			public void onError(String msg) {
				PlaceholderFragment fragment = (PlaceholderFragment)
						getFragmentManager().findFragmentById(R.id.container);
				clearTextBox();
				fragment.mBtnCheckCard.setEnabled(true);
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
	
	private class ReceiptPrint implements Runnable{

		@Override
		public void run() {
			WintecPrinter print = new WintecPrinter(FoodCourtCardPayActivity.this);
			print.createTextForPrintFoodCourtReceipt(mTransactionId, mCardBalanceBefore, mCardBalance, false);
			print.print();
		}
		
	}
}
