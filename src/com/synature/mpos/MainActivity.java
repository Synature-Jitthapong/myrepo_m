package com.synature.mpos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.j1tth4.slidinglibs.SlidingTabLayout;
import com.synature.mpos.SaleService.LocalBinder;
import com.synature.mpos.common.MPOSFragmentActivityBase;
import com.synature.mpos.database.Computer;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.PaymentDetail;
import com.synature.mpos.database.PrintReceiptLog;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.UserVerification;
import com.synature.mpos.database.model.OrderComment;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.database.model.OrderSet.OrderSetDetail;
import com.synature.mpos.database.model.OrderTransaction;
import com.synature.mpos.database.model.Product;
import com.synature.mpos.database.model.ProductDept;
import com.synature.mpos.seconddisplay.SecondDisplayJSON;
import com.synature.pos.SecondDisplayProperty.clsSecDisplay_TransSummary;
import com.synature.util.ImageLoader;
import com.synature.util.Logger;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends MPOSFragmentActivityBase implements 
	MenuCommentFragment.OnCommentDismissListener, 
	ManageCashAmountFragment.OnManageCashAmountDismissListener{
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	/**
	 * send sale request code from payment activity
	 */
	public static final int PAYMENT_REQUEST = 1;
	
	/**
	 * food court payment request
	 */
	public static final int FOOD_COURT_PAYMENT_REQUEST = 2;
	
	/**
	 * Wintec customer display
	 */
	private WintecCustomerDisplay mDsp;
	
	/**
	 * Sale Service
	 */
	private SaleService mPartService;
	private boolean mBound = false;
	
	private Products mProducts;
	private Shop mShop;
	private Formater mFormat;
	
	private Session mSession;
	private Transaction mTrans;
	private Computer mComputer;
	
	private List<OrderDetail> mOrderDetailLst;
	private OrderDetailAdapter mOrderDetailAdapter;
	private List<ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPageAdapter;

	private ImageLoader mImageLoader;
	
	private int mSessionId;
	private int mTransactionId;
	private int mStaffId;
	private int mShopId;
	private int mComputerId;
	
	private ExpandableListView mLvOrderDetail;
	private EditText mTxtBarCode;
	private TableLayout mTbSummary;
	private ImageButton mBtnClearBarCode;
	
	private MenuItem mItemHoldBill;
	private MenuItem mItemSendSale;
	private MenuItem mItemSendData;
	private MenuItem mItemSendEndday;
	
	private SlidingTabLayout mTabs;
	private ViewPager mPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTxtBarCode = (EditText) findViewById(R.id.txtBarCode);
		mTbSummary = (TableLayout) findViewById(R.id.tbLayoutSummary);
		mLvOrderDetail = (ExpandableListView) findViewById(R.id.lvOrder);
		mTabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		mPager = (ViewPager) findViewById(R.id.pager);
		mBtnClearBarCode = (ImageButton) findViewById(R.id.imgBtnClearBarcode);
		
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mSession = new Session(this);
		mTrans = new Transaction(this);
		mProducts = new Products(this);
		mShop = new Shop(this);
		mComputer = new Computer(this);
		mFormat = new Formater(this);
		
		mShopId = mShop.getShopId();
		mComputerId = mComputer.getComputerId();
		
		/**
		 * Image Loader
		 */
		mImageLoader = new ImageLoader(this, 0,
					Utils.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
		 
		/**
		 * Wintec Customer Display
		 */
		mDsp = new WintecCustomerDisplay(getApplicationContext());

		//setupCustomSwLang();
		setupBarCodeEvent();
		setupMenuDeptPager();
	}
	
	/**
	 * @author j1tth4
	 */
	private class MasterLoaderListener implements WebServiceWorkingListener{

		private ProgressDialog mProgress;
		
		public MasterLoaderListener(){
			mProgress = new ProgressDialog(MainActivity.this);
			mProgress.setMessage(getString(R.string.load_master_progress));
			mProgress.setCancelable(false);
		}
		
		@Override
		public void onPreExecute() {
			mProgress.show();
		}

		@Override
		public void onPostExecute() {
			if(mProgress.isShowing())
				mProgress.dismiss();
			startActivity(getIntent());
			finish();
		}

		@Override
		public void onError(String msg) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new AlertDialog.Builder(MainActivity.this)
			.setMessage(msg)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}

		@Override
		public void onProgressUpdate(int value) {
		}
		
	}
	
	private void setupCustomSwLang(){
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		LayoutInflater inflater = getLayoutInflater();
		View swView = inflater.inflate(R.layout.sw_layout, null, false);
		final Switch swLang = (Switch) swView.findViewById(R.id.switch1);
		swLang.setText(getString(R.string.language));
		swLang.setTextOff(getString(R.string.lang_eng_short));
		swLang.setTextOn(getString(R.string.lang_thai_short));
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(swView);
		if(Utils.getLangCode(this).equals("th_TH"))
			swLang.setChecked(true);
		swLang.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(swLang.isChecked()){
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(SettingsActivity.KEY_PREF_LANGUAGE_LIST, "th_TH");
					editor.commit();
					Utils.switchLanguage(MainActivity.this, "th_TH");
				}else{
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(SettingsActivity.KEY_PREF_LANGUAGE_LIST, "en_US");
					editor.commit();
					Utils.switchLanguage(MainActivity.this, "en_US");
				}
				startActivity(getIntent());
				finish();
			}
			
		});
	}
	
	private void setupMenuDeptPager(){
		mProductDeptLst = mProducts.listProductDept();
		mPageAdapter = new MenuItemPagerAdapter(getSupportFragmentManager());
		//mPager.setOffscreenPageLimit(8);
		mPager.setAdapter(mPageAdapter);
		final int pageMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
						.getDisplayMetrics());
		mPager.setPageMargin(pageMargin);
		mTabs.setViewPager(mPager);
	}
	
	private void setupBarCodeEvent(){
		mTxtBarCode.setOnKeyListener(barCodeOnKeyListener);
		mBtnClearBarCode.setOnClickListener(clearBarCodeListener);
	}
	
	private OnClickListener clearBarCodeListener = new OnClickListener(){

		@Override
		public void onClick(View view) {
			mTxtBarCode.setText(null);
		}
		
	};
	
	private OnKeyListener barCodeOnKeyListener = new OnKeyListener(){

		@Override
		public boolean onKey(View view, int keyCode, KeyEvent event) {
			if(event.getAction() != KeyEvent.ACTION_DOWN)
				return true;
			
			if(keyCode == KeyEvent.KEYCODE_ENTER){
				String barCode = ((EditText) view).getText().toString();
				if(!barCode.isEmpty()){
					Product p = mProducts.getProduct(barCode);
					if(p != null){
						addOrder(p.getProductId(), p.getProductName(), 
								p.getProductTypeId(), p.getVatType(), p.getVatRate(), 
								1, p.getProductPrice());
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.search)
						.setMessage(R.string.not_found_item)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
					}
				}
				((EditText) view).setText(null);
			}
			return false;
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		mItemHoldBill = menu.findItem(R.id.itemHoldBill);
		mItemSendSale = menu.findItem(R.id.itemSendSale);
		mItemSendData = menu.findItem(R.id.itemSendData);
		mItemSendEndday = menu.findItem(R.id.itemSendEndday);
		
		countHoldOrder();
		countSaleDataNotSend();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
			case R.id.itemHoldBill:
				showHoldBill();
				return true;
			case R.id.itemSwUser:
				switchUser();
				return true;
			case R.id.itemLogout:
				logout();
				return true;
			case R.id.itemReport:
				intent = new Intent(this, SaleReportActivity.class);
				intent.putExtra("staffId", mStaffId);
				startActivity(intent);
				return true;
			case R.id.itemVoid:
				voidBill();
				return true;
			case R.id.itemEditCash:
				editCash();
				return true;
			case R.id.itemCloseShift:
				closeShift();
				return true;
			case R.id.itemEndday:
				endday();
				return true;
			case R.id.itemBackupDb:
				Utils.exportDatabase(this);
				return true;
			case R.id.itemSendEndday:
				intent = new Intent(this, SendEnddayActivity.class);
				intent.putExtra("staffId", mStaffId);
				intent.putExtra("shopId", mShopId);
				intent.putExtra("computerId", mComputerId);
				startActivity(intent);
				return true;
			case R.id.itemReprint:
				intent = new Intent(this, ReprintActivity.class);
				startActivity(intent);
				return true;
			case R.id.itemSendSale:
				intent = new Intent(this, SendSaleActivity.class);
				intent.putExtra("staffId", mStaffId);
				intent.putExtra("shopId", mShopId);
				intent.putExtra("computerId", mComputerId);
				startActivity(intent);
				return true;
			case R.id.itemSetting:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.itemUpdate:
				new MasterDataLoader(this, mShopId, new MasterLoaderListener()).execute(Utils.getFullUrl(this));
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, SaleService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBound){
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	@Override
	protected void onResume() {
		String curDateMillisec = String.valueOf(Utils.getDate().getTimeInMillis());
		// check current day is already end day ?
		if(!mSession.checkEndday(curDateMillisec)){
			/*
			 * If resume when system date > session date || 
			 * session date > system date. It means the system date
			 * is not valid.
			 * It will be return to LoginActivity for new initial
			 */
			String lastSessDate = mSession.getLastSessionDate();
			if(!lastSessDate.equals("")){
				Calendar sessCal = Calendar.getInstance();
				sessCal.setTimeInMillis(Long.parseLong(lastSessDate));
				if(Utils.getDate().getTime().compareTo(sessCal.getTime()) > 0 || 
					sessCal.getTime().compareTo(Utils.getDate().getTime()) > 0){
					// check last session is already end day ?
					if(!mSession.checkEndday(mSession.getLastSessionDate())){
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
						finish();
					}else{
						init();
					}
				}else{
					init();
				}
			}else{
				// not have any session
				init();
			}
		}else{
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
		}
		super.onResume();
	}

	/**
	 * summary transaction 
	 */
	public void summary(){
		mTbSummary.removeAllViews();
		
		mTrans.summary(mTransactionId);
		
		OrderDetail sumOrder = mTrans.getSummaryOrder(mTransactionId);
		
		mTbSummary.addView(createTableRowSummary(getString(R.string.sub_total), 
				mFormat.currencyFormat(sumOrder.getTotalRetailPrice()), 
				0, 0, 0, 0));
		
		if(sumOrder.getPriceDiscount() > 0){
			String discountText = sumOrder.getPromotionName().equals("") ? getString(R.string.discount) : sumOrder.getPromotionName();
			mTbSummary.addView(createTableRowSummary(discountText, 
					"-" + mFormat.currencyFormat(sumOrder.getPriceDiscount()), 
							0, 0, 0, 0));
		}
		if(sumOrder.getVatExclude() > 0){
			mTbSummary.addView(createTableRowSummary(getString(R.string.vat_exclude) +
					" " + NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%",
					mFormat.currencyFormat(sumOrder.getVatExclude()),
					0, 0, 0, 0));
		}
		mTbSummary.addView(createTableRowSummary(getString(R.string.total),
				mFormat.currencyFormat(sumOrder.getTotalSalePrice()),
				0, R.style.HeaderText, 0, getResources().getInteger(R.integer.large_text_size)));
		
		if(Utils.isEnableSecondDisplay(this)){
			List<clsSecDisplay_TransSummary> transSummLst = new ArrayList<clsSecDisplay_TransSummary>();
			clsSecDisplay_TransSummary transSumm = new clsSecDisplay_TransSummary();
			transSumm.szSumName = getString(R.string.sub_total); 
			transSumm.szSumAmount = mFormat.currencyFormat(sumOrder.getTotalRetailPrice());
			transSummLst.add(transSumm);
			if(sumOrder.getPriceDiscount() > 0){
				String discountText = sumOrder.getPromotionName().equals("") ? getString(R.string.discount) : sumOrder.getPromotionName();
				transSumm = new clsSecDisplay_TransSummary();
				transSumm.szSumName = discountText;
				transSumm.szSumAmount = "-" + mFormat.currencyFormat(sumOrder.getPriceDiscount());
				transSummLst.add(transSumm);
			}
			if(sumOrder.getVatExclude() > 0){
				transSumm = new clsSecDisplay_TransSummary();
				transSumm.szSumName = getString(R.string.vat_exclude) + 
						" " + NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				transSumm.szSumAmount = mFormat.currencyFormat(sumOrder.getVatExclude());
				transSummLst.add(transSumm);
			}
			transSumm = new clsSecDisplay_TransSummary();
			transSumm.szSumName = getString(R.string.total_qty); 
			transSumm.szSumAmount = mFormat.qtyFormat(sumOrder.getOrderQty());
			transSummLst.add(transSumm);
			
			transSumm = new clsSecDisplay_TransSummary();
			transSumm.szSumName = getString(R.string.total); 
			transSumm.szSumAmount = mFormat.currencyFormat(sumOrder.getTotalSalePrice());
			transSummLst.add(transSumm);
			secondDisplayItem(transSummLst, mFormat.currencyFormat(sumOrder.getTotalSalePrice()));
		}
		
		mDsp.setOrderTotalQty(mFormat.qtyFormat(sumOrder.getOrderQty()));
		mDsp.setOrderTotalPrice(mFormat.currencyFormat(sumOrder.getTotalRetailPrice()));
		if(Utils.isEnableWintecCustomerDisplay(this)){
			// display order if qty and retail price > 0
			if(sumOrder.getOrderQty() > 0 && sumOrder.getTotalRetailPrice() > 0){
				try {
					mDsp.displayOrder();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private TableRow createTableRowSummary(String label, String value,
			int labelAppear, int valAppear, float labelSize, float valSize){
		TextView tvLabel = new TextView(this);
		TextView tvValue = new TextView(this);
		tvLabel.setTextAppearance(this, android.R.style.TextAppearance_Holo_Medium);
		tvLabel.setAllCaps(true);
		tvValue.setTextAppearance(this, android.R.style.TextAppearance_Holo_Medium);
		if(labelAppear != 0)
			tvLabel.setTextAppearance(this, labelAppear);
		if(valAppear != 0)
			tvValue.setTextAppearance(this, valAppear);
		tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
				TableRow.LayoutParams.WRAP_CONTENT, 1f));
		tvValue.setGravity(Gravity.RIGHT);
		if(labelSize != 0)
			tvLabel.setTextSize(labelSize);
		if(valSize != 0)
			tvValue.setTextSize(valSize);
		tvLabel.setText(label);
		tvValue.setText(value);

		TableRow rowSummary = new TableRow(this);
		rowSummary.addView(tvLabel);
		rowSummary.addView(tvValue);
		return rowSummary;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == PAYMENT_REQUEST){
			if(resultCode == RESULT_OK){
				// request param from PaymentActivity for log to 
				// PrintReceiptLog
				double totalSalePrice = intent.getDoubleExtra("totalSalePrice", 0);
				double totalPaid = intent.getDoubleExtra("totalPaid", 0);
				double change = intent.getDoubleExtra("change", 0);
				int transactionId = intent.getIntExtra("transactionId", 0);
				int staffId = intent.getIntExtra("staffId", 0);
				afterPaid(transactionId, staffId, totalSalePrice, totalPaid, change);
			}
		}
	}
	
	private void afterPaid(int transactionId, int staffId, double totalSalePrice, 
			double totalPaid, double change){

		PrintReceiptLog printLog = 
				new PrintReceiptLog(MainActivity.this);
		int isCopy = 0;
		for(int i = 0; i < mComputer.getReceiptHasCopy(); i++){
			if(i > 0)
				isCopy = 1;
			printLog.insertLog(transactionId, staffId, isCopy);
		}
		new Thread(new PrintReceipt(getApplicationContext())).start();
		
		if(change > 0){
			LinearLayout changeView = new LinearLayout(MainActivity.this);
			TextView tvChange = new TextView(MainActivity.this);
			tvChange.setTextSize(getResources().getDimension(R.dimen.larger_text_size));
			tvChange.setGravity(Gravity.CENTER);
			tvChange.setText(mFormat.currencyFormat(change));
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 
					LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			changeView.addView(tvChange, params);
			
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.change)
			.setCancelable(false)
			.setView(changeView)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(Utils.isEnableSecondDisplay(MainActivity.this)){
						clearSecondDisplay();
					}
				}
			}).show();
			if(Utils.isEnableSecondDisplay(this)){
				secondDisplayChangePayment(mFormat.currencyFormat(totalSalePrice), 
						mFormat.currencyFormat(totalPaid), mFormat.currencyFormat(change));
				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						clearSecondDisplay();
					}
					
				}, 10000);
			}
		}
		
		// Wintec DSP
		if(Utils.isEnableWintecCustomerDisplay(this)){
			mDsp.displayTotalPay(mFormat.currencyFormat(totalPaid), 
					mFormat.currencyFormat(change));
			new Handler().postDelayed(
					new Runnable(){

						@Override
						public void run() {
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									mDsp.displayWelcome();
								}
								
							});
						}
			}, 10000);
		}
		
		// send sale data service
		List<OrderTransaction> transIdLst = mTrans.listTransactionNotSend();
		for(OrderTransaction trans : transIdLst){
			mPartService.sendSale(mShopId, trans.getSessionId(), trans.getTransactionId(), 
					trans.getComputerId(), mStaffId, mPartialSaleSenderListener);
		}
	}
	
	WebServiceWorkingListener mPartialSaleSenderListener = new WebServiceWorkingListener() {
		
		@Override
		public void onPreExecute() {
		}

		@Override
		public void onPostExecute() {
			countSaleDataNotSend();
			Utils.makeToask(MainActivity.this, MainActivity.this
					.getString(R.string.send_sale_data_success));
		}

		@Override
		public void onError(String msg) {
		}

		@Override
		public void onProgressUpdate(int value) {
		}
	};
	
	/**
	 * @param v
	 * Paid equal total price
	 */
	public void cashPaidClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			new AlertDialog.Builder(this)
			.setTitle(R.string.cash_paid)
			.setMessage(R.string.confirm_fast_payment)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OrderDetail sumOrder = mTrans.getSummaryOrder(mTransactionId);
					PaymentDetail payment = new PaymentDetail(MainActivity.this);
					double totalSalePrice = sumOrder.getTotalSalePrice();
					
					payment.addPaymentDetail(mTransactionId, mComputerId, PaymentDetail.PAY_TYPE_CASH, 
							totalSalePrice, totalSalePrice, "", 0, 0, 0, 0, "");
					
					// open cash drawer
					WintecCashDrawer drw = new WintecCashDrawer(getApplicationContext());
					drw.openCashDrawer();
					drw.close();
					
					mTrans.closeTransaction(mTransactionId, mStaffId, totalSalePrice, 
							mShop.getCompanyVatType(), mShop.getCompanyVatRate());
					afterPaid(mTransactionId, mStaffId, totalSalePrice, totalSalePrice, 0);
					
					init();
				}
			}).show();
		}
	}
	
	/**
	 * @param v
	 * Go to PaymentActivity
	 */
	public void paymentClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			// food court type
			if(mShop.getFastFoodType() == Shop.SHOP_TYPE_FOOD_COURT){
				Intent intent = new Intent(MainActivity.this, FoodCourtCardPayActivity.class);
				intent.putExtra("transactionId", mTransactionId);
				intent.putExtra("shopId", mShopId);
				intent.putExtra("computerId", mComputerId);
				intent.putExtra("staffId", mStaffId);
				startActivityForResult(intent, FOOD_COURT_PAYMENT_REQUEST);
			}else{
				Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
				intent.putExtra("transactionId", mTransactionId);
				intent.putExtra("computerId", mComputerId);
				intent.putExtra("staffId", mStaffId);
				startActivityForResult(intent, PAYMENT_REQUEST);
			}
		}
	}

	/**
	 * @param v
	 */
	public void promotionClicked(final View v) {
		if (mOrderDetailLst.size() > 0) {
			Intent intent = new Intent(MainActivity.this, PromotionActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			startActivity(intent);
		}
	}
	
	/**
	 * @param v
	 * Go to DiscountActivity
	 */
	public void discountClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			Intent intent = new Intent(MainActivity.this, DiscountActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	private class OrderDetailAdapter extends BaseExpandableListAdapter{

		private LayoutInflater mInflater;
		
		public OrderDetailAdapter(){
			mInflater = getLayoutInflater();
		}
		
		@Override
		public int getGroupCount() {
			return mOrderDetailLst.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mOrderDetailLst.get(groupPosition).getOrdSetDetailLst().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mOrderDetailLst.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mOrderDetailLst.get(groupPosition).getOrdSetDetailLst().get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			ViewHolder holder;		
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.order_list_template, parent, false);
				holder = new ViewHolder();
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.tvOrderNo);
				holder.tvOrderName = (CheckedTextView) convertView.findViewById(R.id.chkOrderName);
				holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.tvOrderPrice);
				holder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
				holder.tvOrderQty = (TextView) convertView.findViewById(R.id.tvOrderQty);
				holder.btnComment = (ImageButton) convertView.findViewById(R.id.btnComment);
				holder.btnSetMod = (ImageButton) convertView.findViewById(R.id.btnSetModify);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnOrderMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnOrderPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			final OrderDetail orderDetail = mOrderDetailLst.get(groupPosition);
			holder.tvOrderName.setChecked(orderDetail.isChecked());
			holder.tvOrderNo.setText(Integer.toString(groupPosition + 1) + ".");
			holder.tvOrderName.setText(orderDetail.getProductName());
			holder.tvOrderPrice.setText(mFormat.currencyFormat(orderDetail.getProductPrice()));
			holder.tvOrderQty.setText(mFormat.qtyFormat(orderDetail.getOrderQty()));
			holder.tvComment.setText(null);
			if(orderDetail.getOrderCommentLst() != null){
				holder.tvComment.setVisibility(View.VISIBLE);
				for(int i = 0; i < orderDetail.getOrderCommentLst().size(); i++){
					final OrderComment comment = orderDetail.getOrderCommentLst().get(i);
					holder.tvComment.append("-" + comment.getCommentName());
					if(comment.getCommentPrice() > 0){
						double commentQty = comment.getCommentQty();
						double commentPrice = comment.getCommentPrice();
						double commentTotalPrice = commentPrice * commentQty;
						holder.tvComment.append(" " + mFormat.qtyFormat(commentQty));
						holder.tvComment.append("x" + mFormat.currencyFormat(commentPrice));
						holder.tvComment.append("=" + mFormat.currencyFormat(commentTotalPrice));
					}
					holder.tvComment.append("\n");
				}
			}else{
				holder.tvComment.setVisibility(View.GONE);
			}
			if(orderDetail.getOrderComment() != null)
				holder.tvComment.append(orderDetail.getOrderComment());
			holder.tvOrderName.setOnClickListener(new OnOrderClickListener(orderDetail));
			holder.btnComment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					MenuCommentFragment commentDialog = 
							MenuCommentFragment.newInstance(groupPosition, mTransactionId, 
									mComputerId, orderDetail.getOrderDetailId(), 
									orderDetail.getVatType(), mProducts.getVatRate(orderDetail.getProductId()),
									orderDetail.getProductName(), orderDetail.getOrderComment());
					commentDialog.show(getFragmentManager(), "CommentDialog");
				}
				
			});
			holder.btnSetMod.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
					intent.putExtra("mode", ProductSetActivity.EDIT_MODE);
					intent.putExtra("transactionId", mTransactionId);
					intent.putExtra("computerId", mComputerId);
					intent.putExtra("orderDetailId", orderDetail.getOrderDetailId());
					intent.putExtra("productId", orderDetail.getProductId());
					startActivity(intent);
				}
				
			});
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public synchronized void onClick(View v) {
					double qty = orderDetail.getOrderQty();
					
					if(--qty > 0){
						orderDetail.setOrderQty(qty);
						updateOrder(orderDetail.getOrderDetailId(),
								qty, orderDetail.getProductPrice(), 
								orderDetail.getVatType(),
								mProducts.getVatRate(orderDetail.getProductId()),
								orderDetail.getProductName());
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.delete)
						.setMessage(R.string.confirm_delete_item)
						.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						})
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								deleteOrder(orderDetail.getOrderDetailId());
								loadOrder();
							}
						}).show();
					}
					mOrderDetailAdapter.notifyDataSetChanged();
				}
			});
			
			holder.btnPlus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getOrderQty();
					orderDetail.setOrderQty(++qty);
					updateOrder(orderDetail.getOrderDetailId(),
							qty, orderDetail.getProductPrice(), 
							orderDetail.getVatType(),
							mProducts.getVatRate(orderDetail.getProductId()),
							orderDetail.getProductName());
					
					mOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			
			if(orderDetail.getProductTypeId() == Products.SET_CAN_SELECT){
				holder.btnSetMod.setVisibility(View.VISIBLE);
				holder.btnComment.setVisibility(View.GONE);
			}else{
				holder.btnSetMod.setVisibility(View.GONE);
				holder.btnComment.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ChildViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.order_detail_set_item, parent, false);
				holder = new ChildViewHolder();
				holder.tvSetNo = (TextView) convertView.findViewById(R.id.tvSetNo);
				holder.tvSetName = (TextView) convertView.findViewById(R.id.tvSetName);
				holder.tvSetPrice = (TextView) convertView.findViewById(R.id.tvSetPrice);
				holder.tvSetQty = (TextView) convertView.findViewById(R.id.tvSetQty);
				holder.tvSetNo.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
				holder.tvSetName.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
				holder.tvSetPrice.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
				holder.tvSetQty.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
				convertView.setTag(holder);
			}else{
				holder = (ChildViewHolder) convertView.getTag();
			}
			final OrderSetDetail setDetail = mOrderDetailLst.get(groupPosition).getOrdSetDetailLst().get(childPosition);
			holder.tvSetNo.setText("-");
			holder.tvSetName.setText(setDetail.getProductName());
			holder.tvSetPrice.setText(setDetail.getProductPrice() > 0 ? mFormat.currencyFormat(setDetail.getProductPrice()) : null);
			holder.tvSetQty.setText(mFormat.qtyFormat(setDetail.getOrderSetQty()));
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void notifyDataSetChanged() {
			summary();
			countSelectedOrder();
			// scroll order list
			mLvOrderDetail.post(new Runnable(){

				@Override
				public void run() {
					mLvOrderDetail.setSelection(mOrderDetailAdapter.getGroupCount() - 1);
				}
				
			});
			super.notifyDataSetChanged();
		}
	}
	
	/**
	 * @author j1tth4
	 * OrderDetail selected event
	 */
	private class OnOrderClickListener implements OnClickListener{

		private OrderDetail mOrder;
		
		public OnOrderClickListener(OrderDetail order){
			mOrder = order;
		}
		
		@Override
		public void onClick(View v) {
			if(mOrder.isChecked()){
				mOrder.setChecked(false);
			}else{
				mOrder.setChecked(true);
			}
			mOrderDetailAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Count selected orderitem
	 */
	private void countSelectedOrder(){
		int totalSelected = 0;
		TextView tvOrderSelected = (TextView) findViewById(R.id.tvOrderSelected);
		for(OrderDetail order : mOrderDetailLst){
			if(order.isChecked()){
				totalSelected ++;
			}
		}
		if(totalSelected > 0){
			tvOrderSelected.setText(getString(R.string.item_selection) + "(" + totalSelected + ")");
		}else{
			tvOrderSelected.setText(null);
		}
	}
		
	private class ChildViewHolder {
		TextView tvSetNo;
		TextView tvSetName;
		TextView tvSetPrice;
		TextView tvSetQty;
	}
		
	private class ViewHolder{
		TextView tvOrderNo;
		CheckedTextView tvOrderName;
		TextView tvOrderPrice;
		TextView tvComment;
		TextView tvOrderQty;
		ImageButton btnSetMod;
		ImageButton btnComment;
		Button btnMinus;
		Button btnPlus;
	}
		
	private class HoldBillAdapter extends BaseAdapter{
		LayoutInflater inflater;
		List<OrderTransaction> transLst;
		Calendar c;
		
		public HoldBillAdapter(List<OrderTransaction> transLst){
			inflater = getLayoutInflater();
			this.transLst = transLst;
			c = Calendar.getInstance();
		}
		
		@Override
		public int getCount() {
			return transLst != null ? transLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return transLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderTransaction trans = transLst.get(position);

			convertView = inflater.inflate(R.layout.hold_bill_template, parent, false);
			TextView tvNo = (TextView) convertView.findViewById(R.id.tvNo);
			TextView tvOpenTime = (TextView) convertView.findViewById(R.id.tvOpenTime);
			TextView tvOpenStaff = (TextView) convertView.findViewById(R.id.tvOpenStaff);
			TextView tvRemark = (TextView) convertView.findViewById(R.id.tvRemark);

			c.setTimeInMillis(Long.parseLong(trans.getOpenTime()));
			tvNo.setText(Integer.toString(position + 1) + ".");
			tvOpenTime.setText(mFormat.dateTimeFormat(c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getTransactionNote());

			return convertView;
		}
	}
	
	/**
	 * @author j1tth4
	 * page
	 */
	private class MenuItemPagerAdapter extends FragmentPagerAdapter{
		
		public MenuItemPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mProductDeptLst.get(position).getProductDeptName();
		}
	
		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			int deptId = mProductDeptLst.get(position).getProductDeptId();
			return MenuPageFragment.newInstance(deptId);
		}
	
		@Override
		public int getCount() {
			return mProductDeptLst.size();
		}		
	}
	
	public static class MenuPageFragment extends android.support.v4.app.Fragment {
		
		private List<Product> mProductLst;
		private MenuItemAdapter mMenuItemAdapter;
		
		private int mDeptId;

		private GridView mGvItem;
		private LayoutInflater mInflater;
		
		public static MenuPageFragment newInstance(int deptId){
			MenuPageFragment f = new MenuPageFragment();
			Bundle b = new Bundle();
			b.putInt("deptId", deptId);
			f.setArguments(b);
			return f;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mDeptId = getArguments().getInt("deptId");
			mInflater = getActivity().getLayoutInflater();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			loadMenuItem();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			mGvItem = (GridView) inflater.inflate(R.layout.menu_grid_view, container, false);
			mGvItem.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					Product p = (Product) parent.getItemAtPosition(position);
					((MainActivity) getActivity()).onMenuClick(p.getProductId(),
							p.getProductName(), p.getProductTypeId(), 
							p.getVatType(), p.getVatRate(), p.getProductPrice());
				}
			});
			
			mGvItem.setOnItemLongClickListener(new OnItemLongClickListener(){
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long id) {
					Product p = (Product) parent.getItemAtPosition(position);
					ImageViewPinchZoom imgZoom = ImageViewPinchZoom.newInstance(p.getImgName(), p.getProductName(), 
							((MainActivity) getActivity()).mFormat.currencyFormat(p.getProductPrice()));
					imgZoom.show(getFragmentManager(), "MenuImage");
					return true;
				}
				
			});
			return mGvItem;
		}
		
		private void loadMenuItem(){
			mProductLst = ((MainActivity) getActivity()).mProducts.listProduct(mDeptId);
			mMenuItemAdapter = new MenuItemAdapter();
			mGvItem.setAdapter(mMenuItemAdapter);
		}

		/**
		 * @author j1tth4
		 * MenuItemAdapter
		 */
		private class MenuItemAdapter extends BaseAdapter{
		
			@Override
			public int getCount() {
				return mProductLst.size();
			}

			@Override
			public Product getItem(int position) {
				return mProductLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final Product p = mProductLst.get(position);
				final MenuItemViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.menu_template, parent, false);
					holder = new MenuItemViewHolder();
					holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
					holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
					holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
					convertView.setTag(holder);
				}else{
					holder = (MenuItemViewHolder) convertView.getTag();
				}
				
				holder.tvMenu.setText(p.getProductName());
				// open price fix price = -1
				if(p.getProductPrice() < 0){
					holder.tvPrice.setVisibility(View.INVISIBLE);
				}else{
					holder.tvPrice.setText(((MainActivity) 
							getActivity()).mFormat.currencyFormat(p.getProductPrice()));
				}
				if(p.getProductTypeId() == Products.SIZE){
					holder.tvPrice.setText("(size)");
				}

				if(Utils.isShowMenuImage(getActivity())){
					((MainActivity) getActivity()).mImageLoader.displayImage(
							Utils.getImageUrl(getActivity()) + 
							p.getImgName(), holder.imgMenu);
				}
				return convertView;
			}
		}
	}
	
	/**
	 * @author j1tth4
	 * ProductSizeAdapter
	 */
	private class ProductSizeAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		private List<Product> mProLst;
		
		public ProductSizeAdapter(List<Product> proLst){
			mInflater = getLayoutInflater();
			mProLst = proLst;
		}
		
		@Override
		public int getCount() {
			return mProLst != null ? mProLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mProLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItemViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.menu_template, parent, false);
				holder = new MenuItemViewHolder();
				holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
				holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
				holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
				convertView.setTag(holder);
			}else{
				holder = (MenuItemViewHolder) convertView.getTag();
			}
			Product p = mProLst.get(position);
			holder.tvMenu.setText(p.getProductName());
			if(p.getProductPrice() < 0)
				holder.tvPrice.setVisibility(View.INVISIBLE);
			else
				holder.tvPrice.setText(mFormat.currencyFormat(p.getProductPrice()));

			if(Utils.isShowMenuImage(MainActivity.this)){
				mImageLoader.displayImage(
						Utils.getImageUrl(MainActivity.this) + 
						p.getImgName(), holder.imgMenu);
			}
			return convertView;
		}
	}
	
	/**
	 * @param productId
	 * @param productCode
	 * @param productName
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param productPrice
	 */
	public void onMenuClick(int productId, String productName, 
			int productTypeId, int vatType, double vatRate, double productPrice) {
		if(productTypeId == Products.NORMAL_TYPE || 
				productTypeId == Products.SET){
			addOrder(productId, productName, productTypeId, 
					vatType, vatRate, 1, productPrice);
		}else if(productTypeId == Products.SIZE){
			productSizeDialog(productId, productName);
		}else if(productTypeId == Products.SET_CAN_SELECT){
			Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
			intent.putExtra("mode", ProductSetActivity.ADD_MODE);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputerId);
			intent.putExtra("productId", productId);
			intent.putExtra("setGroupName", productName);
			startActivity(intent);
		}
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnDelOrder:
			deleteSelectedOrder();
			break;
		case R.id.btnClearSelOrder:
			clearSelectedOrder();
			break;
		}
	}
	
	public void cancelOrderClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(android.R.string.cancel)
			.setMessage(R.string.confirm_cancel_order)
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				
				}
			})
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clearTransaction();
				}
			})
			.show();
		}
	}

	/**
	 * @param v
	 * Hold order click
	 */
	public void holdOrderClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			final EditText txtRemark = new EditText(MainActivity.this);
			txtRemark.setHint(R.string.remark);
			txtRemark.setTextSize(getResources().getInteger(R.integer.large_text_size));
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(R.string.hold);
			builder.setView(txtRemark);
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String note = txtRemark.getText().toString();
					mTrans.holdTransaction(mTransactionId, note);
					init();
				}
			});
			final AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 
					WindowManager.LayoutParams.WRAP_CONTENT);
			txtRemark.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			    @Override
			    public void onFocusChange(View v, boolean hasFocus) {
			        if (hasFocus) {
			            dialog.getWindow().setSoftInputMode(
			            		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			        }
			    }
			});
		}
	}

	public void switchUser() {
		LayoutInflater inflater = getLayoutInflater();
		View swUserView = inflater.inflate(R.layout.switch_user_popup, null);
		final EditText txtUser = (EditText) swUserView.findViewById(R.id.txtUser);
		final EditText txtPassword = (EditText) swUserView.findViewById(R.id.txtPassword);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.switch_user);
		builder.setView(swUserView);
		builder.setCancelable(false);
		builder.setNeutralButton(android.R.string.ok, null);
		
		final AlertDialog d = builder.create();	
		d.show();
		Button btnOk = d.getButton(AlertDialog.BUTTON_NEUTRAL);
		btnOk.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				String user = "";
				String pass = "";
			
				if(!txtUser.getText().toString().isEmpty()){
					user = txtUser.getText().toString();
					
					if(!txtPassword.getText().toString().isEmpty()){
						pass = txtPassword.getText().toString();
						UserVerification login = new UserVerification(MainActivity.this, user, pass);
						
						if(login.checkUser()){
							com.synature.pos.Staff s = login.checkLogin();
							if(s != null){
								mStaffId = s.getStaffID();
								init();
								d.dismiss();
							}else{
								new AlertDialog.Builder(MainActivity.this)
								.setTitle(R.string.login)
								.setMessage(R.string.incorrect_password)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										
									}
								})
								.show();
							}
						}else{
							new AlertDialog.Builder(MainActivity.this)
							.setTitle(R.string.login)
							.setMessage(R.string.incorrect_staff_code)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							})
							.show();
						}
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.login)
						.setMessage(R.string.enter_password)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						})
						.show();
					}
				}else{
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.login)
					.setMessage(R.string.enter_staff_code)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.show();
				}
			}
			
		});
	}

	/**
	 * Logout
	 */
	public void logout() {
		Staffs staff = new Staffs(this);
		com.synature.pos.Staff s = staff.getStaff(mStaffId);
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.logout)
		.setMessage(s.getStaffName() + "\n" + getString(R.string.confirm_logout))
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mTrans.cancelTransaction(mTransactionId);
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}
		})
		.show();
	}

	/**
	 * Update order list
	 * @param position
	 * @param orderDetailId
	 */
	private void updateOrderLst(int position, int orderDetailId){
		OrderDetail ord = mTrans.getOrder(mTransactionId, orderDetailId);
		if(ord != null){
			mOrderDetailLst.set(position, ord);
			mOrderDetailAdapter.notifyDataSetChanged();
			expandOrderLv(mOrderDetailAdapter.getGroupCount() - 1);
		}
	}
	
	/**
	 * Update order list
	 * @param ordId
	 */
	private void updateOrderLst(int ordId){
		OrderDetail ord = mTrans.getOrder(mTransactionId, ordId);
		if(ord != null){
			mOrderDetailLst.add(ord);
			mOrderDetailAdapter.notifyDataSetChanged();
			expandOrderLv(mOrderDetailAdapter.getGroupCount() - 1);
		}
	}
	
	/**
	 * load order
	 */
	private void loadOrder(){
		mOrderDetailLst = mTrans.listAllOrder(mTransactionId);
		if(mOrderDetailAdapter == null){
			mOrderDetailAdapter = new OrderDetailAdapter();
			mLvOrderDetail.setAdapter(mOrderDetailAdapter);
			mLvOrderDetail.setGroupIndicator(null);
		}
		mOrderDetailAdapter.notifyDataSetChanged();
		// expand all
		expandOrderLv(0);
	}
	
	private void expandOrderLv(int position){
		try {
			if(position == 0){
				for(int i = 0; i < mOrderDetailAdapter.getGroupCount(); i++){
					if(!mLvOrderDetail.isGroupExpanded(i))
						mLvOrderDetail.expandGroup(i);
				}
			}else{
				if(!mLvOrderDetail.isGroupExpanded(position))
					mLvOrderDetail.expandGroup(position);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void openTransaction(){
		openSession();	
		mTransactionId = mTrans.getCurrentTransactionId(mSessionId);
		if(mTransactionId == 0){
			mTransactionId = mTrans.openTransaction(mSession.getLastSessionDate(), 
					mShopId, mComputerId, mSessionId, mStaffId, mShop.getCompanyVatRate());
		}
	}

	private void openSession(){
		mSessionId = mSession.getCurrentSessionId(mStaffId); 
		if(mSessionId == 0){
			mSessionId = mSession.openSession(mShopId, mComputerId, mStaffId, 0);
			
			ManageCashAmountFragment mf = ManageCashAmountFragment
					.newInstance(getString(R.string.open_shift), 0,
							ManageCashAmountFragment.OPEN_SHIFT_MODE);
			mf.show(getSupportFragmentManager(), "ManageCashAmount");
		}
	}

	private void init(){
		openTransaction();
		// update when changed user
		mTrans.updateTransaction(mTransactionId, mStaffId);
		countHoldOrder();
		countSaleDataNotSend();
		loadOrder();
		
		// init second display
		if(Utils.isEnableSecondDisplay(this)){
			initSecondDisplay();
		}
	}
	
	private void showHoldBill() {
		final OrderTransaction holdTrans = new OrderTransaction();
		LayoutInflater inflater = getLayoutInflater();
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null, false);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<OrderTransaction> billLst = mTrans.listHoldOrder(mSession.getLastSessionDate());
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				holdTrans.setTransactionId(trans.getTransactionId());
			}
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.hold_bill);
		builder.setView(holdBillView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		
		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setLayout(690, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				if(mOrderDetailLst.size() > 0){
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.hold)
					.setMessage(R.string.hold_current_order)
					.setNeutralButton(R.string.close,
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
								}
	
							}).show();
				}else{
					if(holdTrans.getTransactionId() != 0){
						mTrans.prepareTransaction(holdTrans.getTransactionId());
						// Delete current transaction because not have any orders.
						mTrans.deleteTransaction(mTransactionId);
						init();
						dialog.dismiss();
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.hold_bill)
						.setMessage(R.string.select_item)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
					}
				}
			}
		});
	}

	/**
	 * cancel order transaction
	 */
	private void clearTransaction(){
		mTrans.cancelTransaction(mTransactionId);
		init();
	}

	/**
	 * void bill
	 */
	private void voidBill(){
		Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("shopId", mShopId);
		startActivity(intent);
	}

	/**
	 * edit cash in drawer
	 */
	private void editCash(){
		ManageCashAmountFragment mf = ManageCashAmountFragment
				.newInstance(getString(R.string.edit_open_shift), mSession.getOpenAmount(mSessionId), 
						ManageCashAmountFragment.EDIT_CASH_MODE);
		mf.show(getSupportFragmentManager(), "ManageCashAmount");
	}
	
	/**
	 * close shift
	 */
	private void closeShift(){
		if(mTrans.countOrderStatusNotSuccess(mSession.getLastSessionDate()) == 0){
			new AlertDialog.Builder(MainActivity.this)
			.setCancelable(false)
			.setTitle(R.string.close_shift)
			.setMessage(R.string.confirm_close_shift)
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ManageCashAmountFragment mf = ManageCashAmountFragment
							.newInstance(getString(R.string.close_shift), 0, 
									ManageCashAmountFragment.CLOSE_SHIFT_MODE);
					mf.show(getSupportFragmentManager(), "ManageCashAmount");
				}
			}).show();
		}else{
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.close_shift)
			.setMessage(R.string.clear_order_first)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
		}
	}

	/**
	 * endday
	 */
	private void endday(){
		if(mTrans.countOrderStatusNotSuccess(mSession.getLastSessionDate()) == 0){
			new AlertDialog.Builder(MainActivity.this)
			.setCancelable(false)
			.setTitle(R.string.endday)
			.setMessage(R.string.confirm_endday)
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ManageCashAmountFragment mf = ManageCashAmountFragment
							.newInstance(getString(R.string.close_shift), 0, 
									ManageCashAmountFragment.END_DAY_MODE);
					mf.show(getSupportFragmentManager(), "ManageCashAmount");
				}
			}).show();
		}else{
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.endday)
			.setMessage(R.string.clear_order_first)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
		}
	}

	/**
	 * clear selected order
	 */
	private void clearSelectedOrder(){
		final List<OrderDetail> selectedOrderLst = listSelectedOrder();
		if(selectedOrderLst.size() > 0){
			for(OrderDetail order : selectedOrderLst){
				if(order.isChecked())
					order.setChecked(false);
			}
			mOrderDetailAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * delete multiple selected order
	 */
	private void deleteSelectedOrder(){
		final List<OrderDetail> selectedOrderLst = listSelectedOrder();
		if (selectedOrderLst.size() > 0) {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.delete)
					.setMessage(
							this.getString(R.string.confirm_delete) + " ("
									+ selectedOrderLst.size() + ")")
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									for (OrderDetail order : selectedOrderLst) {
										deleteOrder(order.getOrderDetailId());
									}
									loadOrder();
								}
							}).show();
		}
	}

	/**
	 * get selected order
	 * @return
	 */
	private List<OrderDetail> listSelectedOrder(){
		List<OrderDetail> orderSelectedLst = new ArrayList<OrderDetail>();
		for(OrderDetail order : mOrderDetailLst){
			if(order.isChecked())
				orderSelectedLst.add(order);
		}
		return orderSelectedLst;
	}

	/**
	 * Delete Order
	 * @param orderDetailId
	 */
	private void deleteOrder(int orderDetailId){
		mTrans.deleteOrder(mTransactionId, orderDetailId);
		mOrderDetailAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Update Order
	 * @param orderDetailId
	 * @param qty
	 * @param price
	 * @param vatType
	 * @param vatRate
	 */
	private void updateOrder(int orderDetailId, double qty, 
			double price, int vatType, double vatRate, String productName){
		mTrans.updateOrderDetail(mTransactionId,
				orderDetailId, vatType, vatRate, qty, price);
		mDsp.setOrderName(productName);
		mDsp.setOrderQty(mFormat.qtyFormat(qty));
		mDsp.setOrderPrice(mFormat.currencyFormat(price));
	}
	
	/**
	 * Add Order
	 * @param productId
	 * @param productCode
	 * @param productName
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param qty
	 * @param price
	 */
	private void addOrder(final int productId, final String productName, 
			final int productTypeId, final int vatType, final double vatRate, final double qty, double price){
		if(price > -1){
			int ordId = mTrans.addOrderDetail(mTransactionId, mComputerId, 
					productId, productTypeId, vatType, vatRate, qty, price);
			updateOrderLst(ordId);
			mDsp.setOrderName(productName);
			mDsp.setOrderQty(mFormat.qtyFormat(qty));
			mDsp.setOrderPrice(mFormat.currencyFormat(price));
		}else{
			final EditText txtProductPrice = new EditText(this);
			txtProductPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
			txtProductPrice.setTextSize(getResources().getInteger(R.integer.large_text_size));
			txtProductPrice.setOnEditorActionListener(new OnEditorActionListener(){
		
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE){
						return true;
					}
					return false;
				}
				
			});
			new AlertDialog.Builder(this)
			.setTitle(R.string.enter_price)
			.setCancelable(false)
			.setView(txtProductPrice)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
		
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
				
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					double openPrice = 0.0f;
					try {
						openPrice = Utils.stringToDouble(txtProductPrice.getText().toString());
						int ordId = mTrans.addOrderDetail(mTransactionId, mComputerId, 
								productId, productTypeId, vatType, vatRate, qty, openPrice);

						mDsp.setOrderName(productName);
						mDsp.setOrderQty(mFormat.qtyFormat(qty));
						mDsp.setOrderPrice(mFormat.currencyFormat(openPrice));
						updateOrderLst(ordId);
					} catch (ParseException e) {
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.enter_price)
						.setMessage(R.string.enter_valid_numeric)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {	
							}
						})
						.show();
						e.printStackTrace();
					}
				}
			})
			.show();
		}
	}

	/**
	 * create product size dialog
	 * @param proId
	 */
	private void productSizeDialog(int proId, String productName){
		List<Product> pSizeLst = mProducts.listProductSize(proId);
		LayoutInflater inflater = getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View sizeView = inflater.inflate(R.layout.product_size, null, false);
		GridView gvMenuSize = (GridView) sizeView.findViewById(R.id.gvMenuSize);
		builder.setView(sizeView);
		builder.setTitle(productName);
		final AlertDialog dialog = builder.create();
		gvMenuSize.setAdapter(new ProductSizeAdapter(pSizeLst));
		gvMenuSize.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long arg3) {
				Product p = (Product) parent.getItemAtPosition(position);
				addOrder(p.getProductId(), p.getProductName(), 
						p.getProductTypeId(), p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
				dialog.dismiss();
			}
			
		});
		dialog.show();
	}

	/**
	 * count transaction that not send to server
	 */
	private void countSaleDataNotSend(){
		if(mItemSendData != null){
			int totalTrans = mTrans.countTransNotSend();
			int totalSess = mSession.countSessionEnddayNotSend();
			int totalData = totalTrans + totalSess;
			if(totalData > 0){
				mItemSendData.setTitle(getString(R.string.send_sale_data) + "(" + totalData + ")");
			}else{
				mItemSendData.setTitle(getString(R.string.send_sale_data));
			}
			if(totalTrans > 0){
				mItemSendSale.setTitle(getString(R.string.send_sale_data) + "(" + totalTrans + ")");
			}else{
				mItemSendSale.setTitle(getString(R.string.send_sale_data));
			}
			if(totalSess > 0){
				mItemSendEndday.setTitle(getString(R.string.send_endday_data) + "(" + totalSess + ")");
			}else{
				mItemSendEndday.setTitle(getString(R.string.send_endday_data));
			}
		}
	}

	/**
	 * count order that hold
	 */
	private void countHoldOrder(){
		if(mItemHoldBill != null){
			int totalHold = mTrans.countHoldOrder(mSession.getLastSessionDate());
			if(totalHold > 0){
				mItemHoldBill.setTitle(getString(R.string.hold_bill) + "(" + totalHold + ")");
			}else{
				mItemHoldBill.setTitle(getString(R.string.hold_bill));
			}
		}
	}
	
	/**
	 * after MenuCommentFragment dismiss
	 * updateOrderDetailLst
	 */
	@Override
	public void onDismiss(int position, int orderDetailId) {
		updateOrderLst(position, orderDetailId);
	}
	
	private void secondDisplayChangePayment(String grandTotal, String totalPay, String change){
		final String paymentJson = SecondDisplayJSON.genChangePayment(grandTotal, totalPay, change);
		Logger.appendLog(this, Utils.LOG_PATH, Utils.LOG_FILE_NAME, paymentJson);
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					InetAddress iNetAddr = InetAddress.getByName(Utils.getSecondDisplayIp(MainActivity.this));
					Socket socket = new Socket(iNetAddr, Utils.getSecondDisplayPort(MainActivity.this));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//					while(reader.readLine() != null){
//					}
					writer.println(paymentJson);
					writer.flush();
				} catch (UnknownHostException e) {
					Log.d(TAG, e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
			}
			
		}).start();
	}
	
	private void secondDisplayItem(List<clsSecDisplay_TransSummary> transSummLst, String grandTotal){
		final String itemJson = SecondDisplayJSON.genDisplayItem(mFormat, mOrderDetailLst, 
				transSummLst, grandTotal);
		Logger.appendLog(this, Utils.LOG_PATH, Utils.LOG_FILE_NAME, itemJson);
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					InetAddress iNetAddr = InetAddress.getByName(Utils.getSecondDisplayIp(MainActivity.this));
					Socket socket = new Socket(iNetAddr, Utils.getSecondDisplayPort(MainActivity.this));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					writer.println(itemJson);
					writer.flush();
//					while(reader.readLine() != null){
//					}
				} catch (UnknownHostException e) {
					Log.d(TAG, e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
			}
			
		}).start();
	}
	
	private void initSecondDisplay(){
		Staffs s = new Staffs(this);
		final String initJson = SecondDisplayJSON.genInitDisplay(mShop.getShopName(), 
				s.getStaff(mStaffId).getStaffName());
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					InetAddress iNetAddr = InetAddress.getByName(Utils.getSecondDisplayIp(MainActivity.this));
					Socket socket = new Socket(iNetAddr, Utils.getSecondDisplayPort(MainActivity.this));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//					while(reader.readLine() != null){
//					}
					writer.println(initJson);
					writer.flush();
				} catch (UnknownHostException e) {
					Log.d(TAG, e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
			}
			
		}).start();
	}
	
	private void clearSecondDisplay(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					InetAddress iNetAddr = InetAddress.getByName(Utils.getSecondDisplayIp(MainActivity.this));
					Socket socket = new Socket(iNetAddr, Utils.getSecondDisplayPort(MainActivity.this));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//					while(reader.readLine() != null){
//					}
					writer.println(SecondDisplayJSON.genClearDisplay());
					writer.flush();
				} catch (UnknownHostException e) {
					Log.d(TAG, e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
			}
			
		}).start();
	}

	/**
	 * PartialSaleService Connection
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mPartService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
		
	};

	@Override
	public void onOpenShift(double cashAmount) {
		mSession.updateOpenAmount(mSessionId, cashAmount);
	}

	@Override
	public void onCloseShift(double cashAmount) {
		mSession.closeSession(mSessionId, mStaffId, cashAmount, false);

		// send sale data service
		mPartService.sendSale(mShopId, mSessionId, mTransactionId, 
				mComputerId, mStaffId, mPartialSaleSenderListener);

		// print close shift
		new PrintReport(MainActivity.this, 
			PrintReport.WhatPrint.SUMMARY_SALE, mSessionId, mStaffId).run();
		
		startActivity(new Intent(MainActivity.this, LoginActivity.class));
		finish();
	}

	@Override
	public void onEditCashAmount(double cashAmount) {
		mSession.updateOpenAmount(mSessionId, cashAmount);
	}

	@Override
	public void onEndday(double cashAmount) {
		boolean endday = Utils.endday(MainActivity.this, mShopId, 
				mComputerId, mSessionId, mStaffId, cashAmount, true);
		if(endday){
			int totalSess = mSession.countSession(mSession.getLastSessionDate());
			if(totalSess > 1){
				new PrintReport(MainActivity.this, 
					PrintReport.WhatPrint.SUMMARY_SALE, mSessionId, mStaffId).run();
			}
			// if parse sessionId = 0 will be print all summary in day
			new PrintReport(MainActivity.this, 
				PrintReport.WhatPrint.SUMMARY_SALE, 0, mStaffId).run();

			// backup the database
			Utils.exportDatabase(this);
			sendEnddayData();
		}
	}
	
	private void sendEnddayData(){
		final ProgressDialog progress = new ProgressDialog(MainActivity.this);
		progress.setTitle(getString(R.string.endday_success));
		progress.setCancelable(false);
		mPartService.sendEnddaySale(mShopId, mComputerId, 
				mStaffId, new WebServiceWorkingListener(){

					@Override
					public void onPreExecute() {
						progress.setMessage(getString(R.string.send_endday_data_progress));
						progress.show();
					}

					@Override
					public void onPostExecute() {
						if(progress.isShowing())
							progress.dismiss();

						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.endday)
						.setMessage(R.string.send_endday_data_success)
						.setCancelable(false)
						.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//Utils.shutdown();
								finish();
							}
						})
						.show();
					}

					@Override
					public void onError(String msg) {
						if(progress.isShowing())
							progress.dismiss();
						new AlertDialog.Builder(MainActivity.this)
							.setTitle(R.string.endday)
							.setMessage(R.string.cannot_send_endday_data_on_this_time)
							.setCancelable(false)
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									finish();
								}
							})
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									sendEnddayData();
								}
							}).show();
					}

					@Override
					public void onProgressUpdate(int value) {
					}
		});
	}
}
