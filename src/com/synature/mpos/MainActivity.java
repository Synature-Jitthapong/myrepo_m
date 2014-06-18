package com.synature.mpos;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.j1tth4.slidinglibs.SlidingTabLayout;
import com.synature.exceptionhandler.ExceptionHandler;
import com.synature.mpos.provider.Computer;
import com.synature.mpos.provider.Formater;
import com.synature.mpos.provider.MPOSOrderTransaction;
import com.synature.mpos.provider.MenuComment;
import com.synature.mpos.provider.PrintReceiptLog;
import com.synature.mpos.provider.Products;
import com.synature.mpos.provider.Session;
import com.synature.mpos.provider.Shop;
import com.synature.mpos.provider.Staffs;
import com.synature.mpos.provider.Transaction;
import com.synature.mpos.provider.UserVerification;
import com.synature.mpos.provider.Util;
import com.synature.mpos.seconddisplay.ClientSocket;
import com.synature.mpos.seconddisplay.ISocketConnection;
import com.synature.pos.ShopData;
import com.synature.util.ImageLoader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends FragmentActivity 
	implements MenuCommentFragment.OnCommentDismissListener{
	
	// send sale request code from payment activity
	public static final int PAYMENT_REQUEST = 1;
	
	private WintecCustomerDisplay mDsp;
	
	private Thread mSockThread;
	private ISocketConnection mSockConn;
	
	private Products mProducts;
	private Shop mShop;
	private Formater mFormat;
	
	private Session mSession;
	private Transaction mTrans;
	private Computer mComputer;
	
	private List<MPOSOrderTransaction.MPOSOrderDetail> mOrderDetailLst;
	private OrderDetailAdapter mOrderDetailAdapter;
	private List<Products.ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPageAdapter;

	private ImageLoader mImageLoader;
	
	private ProgressDialog mProgress;
	
	private int mSessionId;
	private int mTransactionId;
	private int mStaffId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME));
		
		setContentView(R.layout.activity_main);
		
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mSession = new Session(getApplicationContext());
		mTrans = new Transaction(getApplicationContext());
		mProducts = new Products(getApplicationContext());
		mShop = new Shop(getApplicationContext());
		mComputer = new Computer(getApplicationContext());
		mFormat = new Formater(getApplicationContext());
		
		/**
		 * Image Loader
		 */
		mImageLoader = new ImageLoader(this, 0,
					MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
		 
		mDsp = new WintecCustomerDisplay(getApplicationContext());
		
		/**
		 * if enabled second display
		 */
		if(MPOSApplication.isEnableSecondDisplay(this)){
			mSockThread = new Thread(mSockRunnable);
			mSockThread.start();
		}
		
		/**
		 * For create pager by productDept
		 */
		mProductDeptLst = mProducts.listProductDept();
		mPageAdapter = new MenuItemPagerAdapter(getSupportFragmentManager());

		mProgress = new ProgressDialog(this);
		mProgress.setCancelable(false);
		mOrderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		mOrderDetailAdapter = new OrderDetailAdapter();

		if(savedInstanceState == null){
			getFragmentManager().beginTransaction().
				add(R.id.container, PlaceholderFragment.newInstance()).commit();
		}
	}
	
	@Override
	protected void onResume() {
		if(mSession.getCurrentSessionId() > 0){
			/*
			 * If resume when system date > session date || 
			 * session date > system date. It means the system date
			 * is not valid.
			 * It will be return to LoginActivity for new initial
			 */
			Calendar sessionCal = Calendar.getInstance();
			String sessionDate = mSession.getSessionDate();
			sessionCal.setTimeInMillis(Long.parseLong(sessionDate));
			if(Util.getDate().getTime().compareTo(sessionCal.getTime()) > 0 || 
					sessionCal.getTime().compareTo(Util.getDate().getTime()) > 0){
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}else{
				openTransaction();
			}
		}else{
			openTransaction();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		clearTransaction();
		stopSocketThread();
		super.onDestroy();
	}

	public static class PlaceholderFragment extends Fragment{

		private ExpandableListView mLvOrderDetail;
		private EditText mTxtBarCode;
		private TableLayout mTbSummary;
		private ImageButton mBtnClearBarCode;
		
		private MenuItem mItemHoldBill;
		private MenuItem mItemSendSale;
		
		private SlidingTabLayout mTabs;
		private ViewPager mPager;
		
		public static PlaceholderFragment newInstance(){
			PlaceholderFragment f = new PlaceholderFragment();
			return f;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			MainActivity activity = (MainActivity) getActivity();
			Intent intent = null;
			switch (item.getItemId()) {
			case R.id.itemHoldBill:
				activity.showHoldBill();
				return true;
			case R.id.itemSwUser:
				activity.switchUser();
				return true;
			case R.id.itemLogout:
				activity.logout();
				return true;
			case R.id.itemReport:
				intent = new Intent(getActivity(), SaleReportActivity.class);
				intent.putExtra("staffId", activity.mStaffId);
				startActivity(intent);
				return true;
			case R.id.itemVoid:
				activity.voidBill();
				return true;
			case R.id.itemCloseShift:
				activity.closeShift();
				return true;
			case R.id.itemEndday:
				activity.endday();
				return true;
			case R.id.itemReprint:
				intent = new Intent(getActivity(), ReprintActivity.class);
				startActivity(intent);
				return true;
			case R.id.itemSendSale:
				intent = new Intent(getActivity(), SyncSaleActivity.class);
				intent.putExtra("staffId", activity.mStaffId);
				intent.putExtra("shopId", activity.mShop.getShopId());
				intent.putExtra("computerId", activity.mComputer.getComputerId());
				startActivity(intent);
				return true;
			case R.id.itemSetting:
				intent = new Intent(getActivity(), SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.activity_main, menu);
			mItemHoldBill = menu.findItem(R.id.itemHoldBill);
			mItemSendSale = menu.findItem(R.id.itemSendSale);
			
			((MainActivity) getActivity()).countHoldOrder(getActivity());
			((MainActivity) getActivity()).countTransNotSend(getActivity());
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			mTxtBarCode = (EditText) view.findViewById(R.id.txtBarCode);
			mTbSummary = (TableLayout) view.findViewById(R.id.tbLayoutSummary);
			mLvOrderDetail = (ExpandableListView) view.findViewById(R.id.lvOrder);
			mTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
			mPager = (ViewPager) view.findViewById(R.id.pager);
			mBtnClearBarCode = (ImageButton) view.findViewById(R.id.imgBtnClearBarcode);

			final MainActivity activity = (MainActivity) getActivity();
			mPager.setAdapter(activity.mPageAdapter);
			final int pageMargin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
							.getDisplayMetrics());
			mPager.setPageMargin(pageMargin);
			mTabs.setViewPager(mPager);
			
			mLvOrderDetail.setAdapter(activity.mOrderDetailAdapter);
			mLvOrderDetail.setGroupIndicator(null);
			
			mTxtBarCode.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						String barCode = ((EditText) v).getText().toString();
						if(!barCode.isEmpty()){
							Products.Product p = activity.mProducts.getProduct(barCode);
							if(p != null){
								activity.addOrder(p.getProductId(), p.getProductName(), 
										p.getProductTypeId(), p.getVatType(), p.getVatRate(), 
										1, p.getProductPrice());
							}else{
								new AlertDialog.Builder(getActivity())
								.setTitle(R.string.search)
								.setMessage(R.string.not_found_item)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).show();
							}
						}
						((EditText) v).setText(null);
					}
					return false;
				}
			});
			mBtnClearBarCode.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mTxtBarCode.setText(null);
				}
				
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
		
		public void onClearBarCode(final View v){
			mTxtBarCode.setText(null);
		}
		
		/**
		 * summary transaction 
		 */
		public void summary(){
			MainActivity activity = (MainActivity) getActivity();
			mTbSummary.removeAllViews();
			
			activity.mTrans.summary(activity.mTransactionId);
			
			MPOSOrderTransaction.MPOSOrderDetail sumOrder = 
					activity.mTrans.getSummaryOrder(activity.mTransactionId);
			
			mTbSummary.addView(createTableRowSummary(getString(R.string.sub_total), 
					activity.mFormat.currencyFormat(sumOrder.getTotalRetailPrice()), 
					android.R.style.TextAppearance_Holo_Medium, 0));
			
			if(sumOrder.getPriceDiscount() > 0){
				mTbSummary.addView(createTableRowSummary(getString(R.string.discount), 
						"-" + activity.mFormat.currencyFormat(sumOrder.getPriceDiscount()), 
								android.R.style.TextAppearance_Holo_Medium, 0));
			}
			if(sumOrder.getVatExclude() > 0){
				mTbSummary.addView(createTableRowSummary(getString(R.string.vat_exclude) +
						" " + NumberFormat.getInstance().format(activity.mShop.getCompanyVatRate()) + "%",
						activity.mFormat.currencyFormat(sumOrder.getVatExclude()),
						android.R.style.TextAppearance_Holo_Medium, 0));
			}
			mTbSummary.addView(createTableRowSummary(getString(R.string.total),
					activity.mFormat.currencyFormat(sumOrder.getTotalSalePrice() + sumOrder.getVatExclude()),
					android.R.style.TextAppearance_Holo_Large, 32));
			
			// display summary to customer display
			if(sumOrder.getQty() > 0 && sumOrder.getTotalRetailPrice() > 0){
				activity.mDsp.setOrderTotalQty(activity.mFormat.qtyFormat(sumOrder.getQty()));
				activity.mDsp.setOrderTotalPrice(activity.mFormat.currencyFormat(sumOrder.getTotalRetailPrice()));
				try {
					activity.mDsp.displayOrder();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private TableRow createTableRowSummary(String label, String value,
				int textAppearance, float textSize){
			TextView tvLabel = new TextView(getActivity());
			TextView tvValue = new TextView(getActivity());
			tvLabel.setTextAppearance(getActivity(), textAppearance);
			tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
					TableRow.LayoutParams.WRAP_CONTENT, 1f));
			tvValue.setTextAppearance(getActivity(), textAppearance);
			tvValue.setGravity(Gravity.RIGHT);
			if(textSize != 0)
				tvValue.setTextSize(textSize);
			tvLabel.setText(label);
			tvValue.setText(value);

			TableRow rowSummary = new TableRow(getActivity());
			rowSummary.addView(tvLabel);
			rowSummary.addView(tvValue);
			return rowSummary;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == PAYMENT_REQUEST){
			if(resultCode == RESULT_OK){
				// request param from PaymentActivity for log to 
				// PrintReceiptLog
				double totalPaid = intent.getDoubleExtra("totalPaid", 0);
				double change = intent.getDoubleExtra("change", 0);
				int transactionId = intent.getIntExtra("transactionId", 0);
				int staffId = intent.getIntExtra("staffId", 0);
				afterPaid(transactionId, staffId, totalPaid, change);
			}
		}
	}

	private void afterPaid(int transactionId, int staffId, double totalPaid, 
			double change){
		
		PrintReceiptLog printLog = 
				new PrintReceiptLog(MainActivity.this);
		printLog.insertLog(transactionId, staffId);
		
		new PrintReceipt(MainActivity.this, new PrintReceipt.PrintStatusListener() {
			
			@Override
			public void onPrintSuccess() {
			}
			
			@Override
			public void onPrintFail(String msg) {
				MPOSUtil.makeToask(MainActivity.this, msg);
			}
			
			@Override
			public void onPrepare() {
			}
		}).execute();
		
		MPOSUtil.sendSale(MainActivity.this, mShop.getShopId(), 
				mComputer.getComputerId(), mStaffId, false, new ProgressListener(){

					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						countTransNotSend(MainActivity.this);
						MPOSUtil.makeToask(MainActivity.this, 
								MainActivity.this.getString(R.string.send_sale_data_success));
					}

					@Override
					public void onError(String msg) {
						MPOSUtil.makeToask(MainActivity.this, msg);
					}
		});

		mDsp.displayTotalPay(
				mFormat.currencyFormat(totalPaid), mFormat.currencyFormat(change));
		
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
		
		if(change > 0){
			LayoutInflater inflater = (LayoutInflater) 
					MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
			tvChange.setText(mFormat.currencyFormat(change));
			
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.change)
			.setCancelable(false)
			.setView(tvChange)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
		}	
	}
	
	/**
	 * @param v
	 * Go to PaymentActivity
	 */
	public void paymentClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputer.getComputerId());
			intent.putExtra("staffId", mStaffId);
			startActivityForResult(intent, PAYMENT_REQUEST);
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

		@Override
		public int getGroupCount() {
			return mOrderDetailLst.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mOrderDetailLst.get(groupPosition).getOrderSetDetailLst().size();
		}

		@Override
		public MPOSOrderTransaction.MPOSOrderDetail getGroup(int groupPosition) {
			return mOrderDetailLst.get(groupPosition);
		}

		@Override
		public MPOSOrderTransaction.OrderSet.OrderSetDetail getChild(int groupPosition, int childPosition) {
			return mOrderDetailLst.get(groupPosition).getOrderSetDetailLst().get(childPosition);
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
				LayoutInflater inflater = (LayoutInflater)
						MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.order_list_template, null);
				holder = new ViewHolder();
				holder.menuInfoContent = (LinearLayout) convertView.findViewById(R.id.menuInfoContent);
				holder.chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.tvOrderNo);
				holder.tvOrderName = (TextView) convertView.findViewById(R.id.tvOrderName);
				holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.tvOrderPrice);
				holder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
				holder.txtOrderAmount = (EditText) convertView.findViewById(R.id.txtOrderQty);
				holder.btnComment = (ImageButton) convertView.findViewById(R.id.btnComment);
				holder.btnSetMod = (ImageButton) convertView.findViewById(R.id.btnSetModify);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnOrderMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnOrderPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			final MPOSOrderTransaction.MPOSOrderDetail orderDetail = mOrderDetailLst.get(groupPosition);
			holder.chk.setChecked(orderDetail.isChecked());
			holder.tvOrderNo.setText(Integer.toString(groupPosition + 1) + ".");
			holder.tvOrderName.setText(orderDetail.getProductName());
			holder.tvOrderPrice.setText(mFormat.currencyFormat(orderDetail.getPricePerUnit()));
			holder.txtOrderAmount.setText(mFormat.qtyFormat(orderDetail.getQty()));
			holder.tvComment.setText(null);
			if(orderDetail.getOrderCommentLst() != null){
				for(int i = 0; i < orderDetail.getOrderCommentLst().size(); i++){
					final MenuComment.Comment comment = orderDetail.getOrderCommentLst().get(i);
					holder.tvComment.append("-" + comment.getCommentName());
					if(comment.getCommentPrice() > 0){
						holder.tvComment.append(" " + mFormat.qtyFormat(comment.getCommentQty()));
						holder.tvComment.append("@" + mFormat.currencyFormat(comment.getCommentPrice()));
					}
					holder.tvComment.append("\n");
				}
			}
			holder.menuInfoContent.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(orderDetail.isChecked()){
						orderDetail.setChecked(false);
					}else{
						orderDetail.setChecked(true);
					}
					mOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			holder.btnComment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					MenuCommentFragment commentDialog = 
							MenuCommentFragment.newInstance(groupPosition, mTransactionId, 
									orderDetail.getOrderDetailId(), orderDetail.getProductName());
					commentDialog.show(getFragmentManager(), "CommentDialog");
				}
				
			});
			holder.btnSetMod.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
					intent.putExtra("mode", ProductSetActivity.EDIT_MODE);
					intent.putExtra("transactionId", mTransactionId);
					intent.putExtra("computerId", mComputer.getComputerId());
					intent.putExtra("orderDetailId", orderDetail.getOrderDetailId());
					intent.putExtra("productId", orderDetail.getProductId());
					startActivity(intent);
				}
				
			});
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public synchronized void onClick(View v) {
					double qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						updateOrder(orderDetail.getOrderDetailId(),
								qty, orderDetail.getPricePerUnit(), 
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
					double qty = orderDetail.getQty();
					orderDetail.setQty(++qty);
					updateOrder(orderDetail.getOrderDetailId(),
							qty, orderDetail.getPricePerUnit(), 
							orderDetail.getVatType(),
							mProducts.getVatRate(orderDetail.getProductId()),
							orderDetail.getProductName());
					
					mOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			
			if(orderDetail.getProductTypeId() == Products.SET_TYPE_CAN_SELECT){
				holder.btnSetMod.setVisibility(View.VISIBLE);
				holder.btnComment.setVisibility(View.GONE);
			}else{
				holder.btnSetMod.setVisibility(View.GONE);
				holder.btnComment.setVisibility(View.VISIBLE);
			}
			if(orderDetail.isChecked()){
				holder.chk.setVisibility(View.VISIBLE);
			}else{
				holder.chk.setVisibility(View.GONE);
			}
			// expand if order has set detail
			if(orderDetail.getOrderSetDetailLst().size() > 0){
				if(!isExpanded){
					ExpandableListView lv = (ExpandableListView) parent;
					lv.expandGroup(groupPosition);
				}
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) 
						getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.order_detail_set_item, parent, false);
			}
			TextView tvSetNo = (TextView) convertView.findViewById(R.id.tvSetNo);
			TextView tvSetName = (TextView) convertView.findViewById(R.id.tvSetName);
			TextView txtSetQty = (EditText) convertView.findViewById(R.id.editText1);
			tvSetNo.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
			tvSetName.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
			txtSetQty.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Small);
			final MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail = 
						mOrderDetailLst.get(groupPosition).getOrderSetDetailLst().get(childPosition);
			tvSetNo.setText("-");
			tvSetName.setText(setDetail.getProductName());
			txtSetQty.setText(mFormat.qtyFormat(setDetail.getOrderSetQty()));
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void notifyDataSetChanged() {
			Fragment f = getFragmentManager().findFragmentById(R.id.container);
			if(f instanceof PlaceholderFragment){
				((PlaceholderFragment) f).summary();
			}
			super.notifyDataSetChanged();
		}
		
		private class ViewHolder{
			LinearLayout menuInfoContent;
			CheckBox chk;
			TextView tvOrderNo;
			TextView tvOrderName;
			TextView tvOrderPrice;
			TextView tvComment;
			EditText txtOrderAmount;
			ImageButton btnSetMod;
			ImageButton btnComment;
			Button btnMinus;
			Button btnPlus;
		}
	}
	
	private class HoldBillAdapter extends BaseAdapter{
		LayoutInflater inflater;
		List<MPOSOrderTransaction> transLst;
		Calendar c;
		
		public HoldBillAdapter(List<MPOSOrderTransaction> transLst){
			inflater = LayoutInflater.from(MainActivity.this);
			this.transLst = transLst;
			c = Calendar.getInstance();
		}
		
		@Override
		public int getCount() {
			return transLst != null ? transLst.size() : 0;
		}

		@Override
		public MPOSOrderTransaction getItem(int position) {
			return transLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MPOSOrderTransaction trans = transLst.get(position);

			convertView = inflater.inflate(R.layout.hold_bill_template, null);
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
		
		private List<Products.Product> mProductLst;
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
			mInflater =
					(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mMenuItemAdapter = new MenuItemAdapter();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			mGvItem = (GridView) inflater.inflate(R.layout.menu_grid_view, container, false);
			mGvItem.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					Products.Product p = 
							(Products.Product) parent.getItemAtPosition(position);
					((MainActivity) getActivity()).onMenuClick(p.getProductId(),
							p.getProductName(), p.getProductTypeId(), 
							p.getVatType(), p.getVatRate(), p.getProductPrice());
				}
			});
			
			mGvItem.setOnItemLongClickListener(new OnItemLongClickListener(){
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long id) {
					Products.Product p = (Products.Product) parent.getItemAtPosition(position);
					ImageViewPinchZoom imgZoom = ImageViewPinchZoom.newInstance(p.getImgUrl(), p.getProductName(), 
							((MainActivity) getActivity()).mFormat.currencyFormat(p.getProductPrice()));
					imgZoom.show(getFragmentManager(), "MenuImage");
					return true;
				}
				
			});
			new LoadMenuItemTask().execute();
			return mGvItem;
		}
		
		private class LoadMenuItemTask extends AsyncTask<Void, Void, Void>{

			@Override
			protected void onPreExecute() {
			}

			@Override
			protected void onPostExecute(Void result) {
				mGvItem.setAdapter(mMenuItemAdapter);
			}

			@Override
			protected Void doInBackground(Void... params) {
				mProductLst = ((MainActivity) getActivity()).mProducts.listProduct(mDeptId);
				return null;
			}
			
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
			public Products.Product getItem(int position) {
				return mProductLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final Products.Product p = mProductLst.get(position);
				final MenuItemViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.menu_template, null);
					holder = new MenuItemViewHolder();
					holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
					holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
					holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
					convertView.setTag(holder);
				}else{
					holder = (MenuItemViewHolder) convertView.getTag();
				}
				
				holder.tvMenu.setText(p.getProductName());
				if(p.getProductPrice() < 0)
					holder.tvPrice.setVisibility(View.INVISIBLE);
				else
					holder.tvPrice.setText(((MainActivity) 
							getActivity()).mFormat.currencyFormat(p.getProductPrice()));

				if(MPOSApplication.isShowMenuImage(getActivity())){
					((MainActivity) getActivity()).mImageLoader.displayImage(
							MPOSApplication.getImageUrl(getActivity()) + 
							p.getImgUrl(), holder.imgMenu);
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
		private List<Products.Product> mProLst;
		
		public ProductSizeAdapter(List<Products.Product> proLst){
			mInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mProLst = proLst;
		}
		
		@Override
		public int getCount() {
			return mProLst != null ? mProLst.size() : 0;
		}

		@Override
		public Products.Product getItem(int position) {
			return mProLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.product_size_template, null);
				holder = new ViewHolder();
				holder.tvProductName = (TextView) convertView.findViewById(R.id.tvProductName);
				holder.tvProductPrice = (TextView) convertView.findViewById(R.id.tvProductPrice);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			Products.Product p = mProLst.get(position);
			holder.tvProductName.setText(p.getProductName());
			holder.tvProductPrice.setText(mFormat.currencyFormat(p.getProductPrice()));
			return convertView;
		}

		class ViewHolder{
			public TextView tvProductName;
			public TextView tvProductPrice;
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
				productTypeId == Products.SET_TYPE){
			addOrder(productId, productName, productTypeId, 
					vatType, vatRate, 1, productPrice);
		}else if(productTypeId == Products.SIZE_TYPE){
			productSizeDialog(productId);
		}else if(productTypeId == Products.SET_TYPE_CAN_SELECT){
			Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
			intent.putExtra("mode", ProductSetActivity.ADD_MODE);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputer.getComputerId());
			intent.putExtra("productId", productId);
			startActivity(intent);
		}
		
		if(MPOSApplication.isEnableSecondDisplay(MainActivity.this)){
			// test send socket
			mSockConn.send("{\"iCommandTypeID\":1,\"szGrandTotalPrice\":\"0.00\",\"xTransaction\":{\"szTransName\":\"พีรพัฒน์ (เชอร์รี่) ทรงถาวรทวี\",\"iNoCustomer\":1,\"szCustName\":\"พีรพัฒน์ (เชอร์รี่) ทรงถาวรทวี\"},\"xListDetailItems\":[],\"xListTransSummarys\":[{\"szSumName\":\"Sub Total\",\"szSumAmount\":\"0.00\"},{\"szSumName\":\"Grand Total\",\"szSumAmount\":\"0.00\"},{\"szSumName\":\"Total Qty.\",\"szSumAmount\":\"0.00\"}]}");
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
	
	public void clearBillClicked(final View v){
		new AlertDialog.Builder(MainActivity.this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.cancel_bill)
		.setMessage(R.string.confirm_cancel_bill)
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

	/**
	 * @param v
	 * Hold order click
	 */
	public void holdOrderClicked(final View v){
		if(mOrderDetailLst.size() > 0){
			final EditText txtRemark = new EditText(MainActivity.this);
			txtRemark.setHint(R.string.remark);
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
					
					openTransaction();
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
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
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
						UserVerification login = new UserVerification(MainActivity.this.getApplicationContext(), user, pass);
						
						if(login.checkUser()){
							ShopData.Staff s = login.checkLogin();
							if(s != null){
								mStaffId = s.getStaffID();
								openSession();
								openTransaction();
								d.dismiss();
							}else{
								new AlertDialog.Builder(MainActivity.this)
								.setIcon(android.R.drawable.ic_dialog_alert)
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
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.login)
							.setMessage(R.string.incorrect_user)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							})
							.show();
						}
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
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
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.login)
					.setMessage(R.string.enter_username)
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
		Staffs staff = new Staffs(getApplicationContext());
		ShopData.Staff s = staff.getStaff(mStaffId);
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.logout)
		.setIcon(android.R.drawable.ic_dialog_info)
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
				finish();
			}
		})
		.show();
	}

	private void updateOrderDetailLst(int position, int orderDetailId){
		mOrderDetailLst.set(position, mTrans.getOrder(mTransactionId, orderDetailId));
		mOrderDetailAdapter.notifyDataSetChanged();
		Fragment f = getFragmentManager().findFragmentById(R.id.container);
		if(f instanceof PlaceholderFragment){
			((PlaceholderFragment) f).mLvOrderDetail.setSelectedGroup(position);
		}
	}
	
	/**
	 * load order
	 */
	private void loadOrder(){
		mOrderDetailLst = mTrans.listAllOrder(mTransactionId);
		mOrderDetailAdapter.notifyDataSetChanged();
		Fragment f = getFragmentManager().findFragmentById(R.id.container);
		if (f instanceof PlaceholderFragment) {
			((PlaceholderFragment) f).mLvOrderDetail
					.setSelectedGroup(mOrderDetailAdapter.getGroupCount());
		}
	}

	private void openTransaction(){
		openSession();	
		mTransactionId = mTrans.getCurrTransactionId(mSession.getSessionDate());
		if(mTransactionId == 0){
			mTransactionId = mTrans.openTransaction(mSession.getSessionDate(), 
					mShop.getShopId(), mComputer.getComputerId(),
					mSessionId, mStaffId, mShop.getCompanyVatRate());
		}
		// update when changed user
		mTrans.updateTransaction(mTransactionId, mStaffId);
		countHoldOrder(MainActivity.this);
		countTransNotSend(MainActivity.this);
		loadOrder();
	}

	private void openSession(){
		mSessionId = mSession.getCurrentSessionId(mStaffId); 
		if(mSessionId == 0){
			mSessionId = mSession.openSession(mShop.getShopId(), 
					mComputer.getComputerId(), mStaffId, 0);
		}
	}

	private void showHoldBill() {
		final MPOSOrderTransaction holdTrans = new MPOSOrderTransaction();
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<MPOSOrderTransaction> billLst = 
				mTrans.listHoldOrder(mSession.getSessionDate());
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				MPOSOrderTransaction trans = (MPOSOrderTransaction) parent.getItemAtPosition(position);
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
						openTransaction();
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
		openTransaction();
	}

	/**
	 * void bill
	 */
	private void voidBill(){
		Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("shopId", mShop.getShopId());
		startActivity(intent);
	}

	/**
	 * close shift
	 */
	private void closeShift(){
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
				mSession.closeSession(mSessionId, mStaffId, 0, false);
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}
		}).show();
	}

	/**
	 * endday
	 */
	private void endday(){
		if(mOrderDetailLst.size() == 0){
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
					mProgress.setTitle(MainActivity.this.getString(R.string.endday));
					mProgress.setMessage(MainActivity.this.getString(R.string.endday_progress));
					// execute print summary sale task
					new PrintReport(MainActivity.this, mStaffId, PrintReport.WhatPrint.SUMMARY_SALE).execute();
					MPOSUtil.endday(MainActivity.this, mShop.getShopId(), 
							mComputer.getComputerId(), mSessionId, mStaffId, 0, true,
							new ProgressListener() {
						
						@Override
						public void onPre() {
							mProgress.show();
						}

						@Override
						public void onPost() {
							if(mProgress.isShowing())
								mProgress.dismiss();
							
							new AlertDialog.Builder(MainActivity.this)
							.setCancelable(false)
							.setTitle(R.string.endday)
							.setMessage(R.string.endday_success)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							}).show();
						}

						@Override
						public void onError(String msg) {
							if(mProgress.isShowing())
								mProgress.dismiss();
							new AlertDialog.Builder(MainActivity.this)
							.setTitle(R.string.endday)
							.setMessage(R.string.endday_success)
							.setCancelable(false)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(mSession.checkEndday(mSession.getSessionDate()) > 0){
										finish();
									}
								}
							}).show();
						}
					});
				}
			}).show();
		}else{
			new AlertDialog.Builder(MainActivity.this)
			.setMessage(R.string.cannot_endday_have_order)
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
		final List<MPOSOrderTransaction.MPOSOrderDetail> selectedOrderLst = listSelectedOrder();
		if(selectedOrderLst.size() > 0){
			for(MPOSOrderTransaction.MPOSOrderDetail order : selectedOrderLst){
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
		final List<MPOSOrderTransaction.MPOSOrderDetail> selectedOrderLst = listSelectedOrder();
		if (selectedOrderLst.size() > 0) {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.delete)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(
							this.getString(R.string.confirm_delete) + " ("
									+ selectedOrderLst.size() + ") ?")
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
									for (MPOSOrderTransaction.MPOSOrderDetail order : selectedOrderLst) {
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
	private List<MPOSOrderTransaction.MPOSOrderDetail> listSelectedOrder(){
		List<MPOSOrderTransaction.MPOSOrderDetail> orderSelectedLst = 
				new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		for(MPOSOrderTransaction.MPOSOrderDetail order : mOrderDetailLst){
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
			mTrans.addOrderDetail(mTransactionId, mComputer.getComputerId(), 
					productId, productTypeId, vatType, vatRate, qty, price);
			mDsp.setOrderName(productName);
			mDsp.setOrderQty(mFormat.qtyFormat(qty));
			mDsp.setOrderPrice(mFormat.currencyFormat(price));
		}else{
			final EditText txtProductPrice = new EditText(this);
			txtProductPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
			txtProductPrice.setOnEditorActionListener(new OnEditorActionListener(){
		
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					// TODO Auto-generated method stub
					return false;
				}
				
			});
			new AlertDialog.Builder(this)
			.setTitle(R.string.enter_price)
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
						openPrice = MPOSUtil.stringToDouble(txtProductPrice.getText().toString());
						mTrans.addOrderDetail(mTransactionId, mComputer.getComputerId(), 
								productId, productTypeId, vatType, vatRate, qty, openPrice);

						mDsp.setOrderName(productName);
						mDsp.setOrderQty(mFormat.qtyFormat(qty));
						mDsp.setOrderPrice(mFormat.currencyFormat(openPrice));
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
		loadOrder();
	}

	/**
	 * create product size dialog
	 * @param proId
	 */
	private void productSizeDialog(int proId){
		List<Products.Product> pSizeLst = mProducts.listProductSize(proId);
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View sizeView = inflater.inflate(R.layout.product_size, null);
		ListView lvProSize = (ListView) sizeView.findViewById(R.id.lvProSize);
		builder.setView(sizeView);
		builder.setTitle(R.string.product_size);
		final AlertDialog dialog = builder.create();
		lvProSize.setAdapter(new ProductSizeAdapter(pSizeLst));
		lvProSize.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long arg3) {
				Products.Product p = (Products.Product) parent.getItemAtPosition(position);
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
	private void countTransNotSend(Context context){
		Fragment f = getFragmentManager().findFragmentById(R.id.container);
		if(f instanceof PlaceholderFragment){
			if(((PlaceholderFragment) f).mItemSendSale != null){
				int total = mTrans.countTransNotSend();
				if(total > 0){
					((PlaceholderFragment) f).mItemSendSale.setTitle(context.getString(R.string.send_sale_data) + "(" + total + ")");
				}else{
					((PlaceholderFragment) f).mItemSendSale.setTitle(context.getString(R.string.send_sale_data));
				}
			}
		}
	}

	/**
	 * count order that hold
	 */
	private void countHoldOrder(Context context){
		Fragment f = getFragmentManager().findFragmentById(R.id.container);
		if(f instanceof PlaceholderFragment){
			if(((PlaceholderFragment) f).mItemHoldBill != null){
				int totalHold = mTrans.countHoldOrder(mSession.getSessionDate());
				if(totalHold > 0){
					((PlaceholderFragment) f).mItemHoldBill.setTitle(context.getString(R.string.hold_bill) + "(" + totalHold + ")");
				}else{
					((PlaceholderFragment) f).mItemHoldBill.setTitle(context.getString(R.string.hold_bill));
				}
			}
		}
	}
	
	/**
	 * after MenuCommentFragment dismiss
	 * updateOrderDetailLst
	 */
	@Override
	public void onDismiss(int position, int orderDetailId) {
		updateOrderDetailLst(position, orderDetailId);
	}

	/**
	 * stop socket thread
	 */
	private void stopSocketThread(){
		if(mSockThread != null){
			mSockThread.interrupt();
			mSockThread = null;
		}
	}
	
	private Runnable mSockRunnable = new Runnable(){

		@Override
		public void run() {
			try {
				mSockConn = new ClientSocket("10.59.0.99", 6600);
				String msg;
				while((msg = mSockConn.receive()) != null){
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
}
