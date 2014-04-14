package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.astuetz.PagerSlidingTabStrip;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.ComputerTable;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.OrderTransactionTable;
import com.syn.mpos.database.PrintReceiptLog;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Session;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.mpos.database.SyncSaleLog;
import com.syn.mpos.database.Transaction;
import com.syn.mpos.database.Util;
import com.syn.pos.OrderTransaction;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends FragmentActivity implements MenuPageFragment.OnMenuItemClick,
	OnItemClickListener, OnClickListener, OnKeyListener{
	
	public static final int TAB_UNDERLINE_COLOR = 0xFF1D78B2;
	
	// send sale request code from payment activity
	public static final int PAYMENT_REQUEST = 1;
	
	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;
	private Shop mShop;
	private Computer mComputer;
	private Products mProducts;
	private Session mSession;
	private Transaction mTransaction;
	
	private List<OrderTransaction.OrderDetail> mOrderDetailLst;
	private OrderDetailAdapter mOrderDetailAdapter;
	private List<Products.ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPageAdapter;
	
	private int mTransactionId;
	private int mComputerId;
	private int mSessionId;
	private int mStaffId;
	private int mShopId;
	
	private PagerSlidingTabStrip mTabs;
	private ViewPager mPager;
	private ListView mLvOrderDetail;
	private TableRow mTbRowVat;
	private TableRow mTbRowDiscount;
	private EditText mTxtBarCode;
	private TextView mTvSubTotal;
	private TextView mTvVatExclude;
	private TextView mTvDiscount;
	private TextView mTvTotalPrice;
	private Button mBtnDiscount;
	private Button mBtnCash;
	private Button mBtnHold;
	private MenuItem mItemHoldBill;
	private MenuItem mItemSendSale;
	private LinearLayout mLayoutOrderCtrl;
	private ImageButton mBtnDelSelOrder;
	private ImageButton mBtnClearSelOrder;
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		View barcodeView = getLayoutInflater().inflate(R.layout.barcode_layout, null);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(barcodeView);

		mTxtBarCode = (EditText) findViewById(R.id.txtBarCode);
		mLvOrderDetail = (ListView) findViewById(R.id.lvOrder);
		mLayoutOrderCtrl = (LinearLayout) findViewById(R.id.layoutOrderCtrl);
		mBtnDelSelOrder = (ImageButton) findViewById(R.id.btnDelOrder);
		mBtnClearSelOrder = (ImageButton) findViewById(R.id.btnClearSelOrder);
		mTvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
		mTvSubTotal = (TextView) findViewById(R.id.textViewSubTotal);
		mTvVatExclude = (TextView) findViewById(R.id.textViewVatExclude);
		mTvDiscount = (TextView) findViewById(R.id.textViewDiscount);
		mTbRowVat = (TableRow) findViewById(R.id.tbRowVat);
		mTbRowDiscount = (TableRow) findViewById(R.id.tbRowDiscount);
		mBtnDiscount = (Button) findViewById(R.id.buttonDiscount);
		mBtnCash = (Button) findViewById(R.id.buttonCash);
		mBtnHold = (Button) findViewById(R.id.buttonHold);
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mPager = (ViewPager) findViewById(R.id.pager);
		
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		mProducts = new Products(mSqlite);

		mProductDeptLst = mProducts.listProductDept();
		mPageAdapter = new MenuItemPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPageAdapter);
		
		final int pageMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
						.getDisplayMetrics());
		mPager.setPageMargin(pageMargin);
		mTabs.setViewPager(mPager);
		mTabs.setIndicatorColor(TAB_UNDERLINE_COLOR);
		
		mProgress = new ProgressDialog(this);
		mProgress.setCancelable(false);
		mOrderDetailLst = new ArrayList<OrderTransaction.OrderDetail>();
		mOrderDetailAdapter = new OrderDetailAdapter();
		mLvOrderDetail.setAdapter(mOrderDetailAdapter);
		
		mBtnDelSelOrder.setOnClickListener(this);
		mBtnClearSelOrder.setOnClickListener(this);
		mLvOrderDetail.setOnItemClickListener(this);
		mTxtBarCode.setOnKeyListener(this);
	}
	
	private void init(){
		if(!mSqlite.isOpen())
			mSqlite = mSqliteHelper.getWritableDatabase();
		
		mShop = new Shop(mSqlite);
		mComputer = new Computer(mSqlite);
		mSession = new Session(mSqlite);
		mTransaction = new Transaction(mSqlite);

		mShopId = mShop.getShopProperty().getShopID();
		mComputerId = mComputer.getComputerProperty().getComputerID();
		
		mTransactionId = mTransaction.getCurrTransaction(mComputerId);
		if(mTransactionId == 0){
			mSessionId = getCurrentSession();
			mTransactionId = mTransaction.openTransaction(mComputerId, mShopId, mSessionId, mStaffId);
			// add current date for LoadSaleTransaction read Sale Data and send to server
			SyncSaleLog syncLog = new SyncSaleLog(mSqlite);
			syncLog.addSyncSaleLog(String.valueOf(Util.getDate().getTimeInMillis()));
		}
		countHoldOrder();
		countTransNotSend();
		loadOrder();
	}
	
	private int getCurrentSession(){
		int sessionId = 0;
		sessionId = mSession.getCurrentSession(mComputerId, mStaffId);
		if(sessionId == 0){
			sessionId = mSession.addSession(mShopId, mComputerId, mStaffId, 0);
		}
		return sessionId;
	}

	private void loadOrder(){
		mOrderDetailLst = mTransaction.listAllOrder(mTransactionId, mComputerId);
		mOrderDetailAdapter.notifyDataSetChanged();
		if(mOrderDetailAdapter.getCount() > 1)
			mLvOrderDetail.setSelection(mOrderDetailAdapter.getCount() - 1);
	}
	
	public Products getProduct(){
		return mProducts;
	}
	
	public SQLiteDatabase getDatabase(){
		return mSqlite;
	}
	
	@Override
	protected void onPause() {
		mSqliteHelper.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	public void summary(){
		double subTotal = mTransaction.getTotalRetailPrice(mTransactionId, mComputerId);
		double totalVatExclude = mTransaction.getTotalVatExclude(mTransactionId, mComputerId);
		double totalDiscount = mTransaction.getPriceDiscount(mTransactionId, mComputerId);
		double totalSale = mTransaction.getTotalSalePrice(mTransactionId, mComputerId);
		
		mTransaction.updateTransactionVat(mTransactionId, mComputerId, totalSale);
		double vatable = mTransaction.getTransactionVatable(mTransactionId, mComputerId);
		
		if(totalDiscount > 0)
			mTbRowDiscount.setVisibility(View.VISIBLE);
		else
			mTbRowDiscount.setVisibility(View.GONE);
		
		if(totalVatExclude > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		
		mTvVatExclude.setText(GlobalProperty.currencyFormat(mSqlite, totalVatExclude));
		mTvSubTotal.setText(GlobalProperty.currencyFormat(mSqlite, subTotal));
		mTvDiscount.setText("-" + GlobalProperty.currencyFormat(mSqlite, totalDiscount));
		mTvTotalPrice.setText(GlobalProperty.currencyFormat(mSqlite, vatable));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == PAYMENT_REQUEST){
			if(resultCode == RESULT_OK){
				int transactionId = intent.getIntExtra("transactionId", 0);
				int computerId = intent.getIntExtra("computerId", 0);
				printReceipt(transactionId, computerId);
				sendSale();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mItemHoldBill = menu.findItem(R.id.itemHoldBill);
		mItemSendSale = menu.findItem(R.id.itemSendSale);
		
		countHoldOrder();
		countTransNotSend();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.itemHoldBill:
			holdBill();
			return true;
		case R.id.itemSwUser:
			switchUser();
			return true;
		case R.id.itemLogout:
			logout();
			return true;
		case R.id.itemReport:
			
			return true;
		case R.id.itemVoid:
			voidBill();
			return true;
		case R.id.itemCloseShift:
			closeShift();
			return true;
		case R.id.itemEndday:
			endday();
			return true;
		case R.id.itemReprint:
			intent = new Intent(MainActivity.this, ReprintActivity.class);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
			return true;
		case R.id.itemSendSale:
			intent = new Intent(MainActivity.this, SyncSaleActivity.class);
			intent.putExtra("staffId", mStaffId);
			intent.putExtra("shopId", mShopId);
			intent.putExtra("computerId", mComputerId);
			startActivity(intent);
			return true;
		case R.id.itemSetting:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void countTransNotSend(){
		if(mItemSendSale != null){
			Cursor cursor = mSqlite.rawQuery(
					"SELECT COUNT(" + OrderTransactionTable.COLUMN_TRANSACTION_ID + ") " +
					" FROM " + OrderTransactionTable.TABLE_NAME + 
					" WHERE " + OrderTransactionTable.COLUMN_STATUS_ID + "=? AND " + 
					MPOSDatabase.COLUMN_SEND_STATUS + "=?", 
					new String[]{
							String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
							String.valueOf(MPOSDatabase.NOT_SEND)
					});
			if(cursor.moveToFirst()){
				int totalTrans = cursor.getInt(0);
				if(totalTrans > 0){
					mItemSendSale.setTitle(this.getString(R.string.send_sale_data) + "(" + totalTrans + ")");
				}else{
					mItemSendSale.setTitle(this.getString(R.string.send_sale_data));
				}
			}
			cursor.close();
		}
	}
	
	private void countHoldOrder(){
		if(mItemHoldBill != null){
			int totalHold = mTransaction.countHoldOrder(mComputerId);
		
			if(totalHold > 0){
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill) + "(" + totalHold + ")");
			}else{
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill));
			}
		}
	}
	
	public void paymentClicked(final View v){
		Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		intent.putExtra("staffId", mStaffId);
		startActivityForResult(intent, PAYMENT_REQUEST);
	}

	public void discountClicked(final View v){
		Intent intent = new Intent(MainActivity.this, DiscountActivity.class);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	private class MenuItemPagerAdapter extends FragmentPagerAdapter{
		
		public MenuItemPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mProductDeptLst.get(position).getProductDeptName();
		}
	
		@Override
		public Fragment getItem(int position) {
			int deptId = mProductDeptLst.get(position).getProductDeptId();
			return MenuPageFragment.newInstance(deptId);
		}
	
		@Override
		public int getCount() {
			return mProductDeptLst.size();
		}		
	}

	private class OrderDetailAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		
		public OrderDetailAdapter(){
			mInflater = (LayoutInflater) 
					MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mOrderDetailLst != null ? mOrderDetailLst.size() : 0;
		}
	
		@Override
		public OrderTransaction.OrderDetail getItem(int position) {
			return mOrderDetailLst.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final OrderTransaction.OrderDetail orderDetail = mOrderDetailLst.get(position);
			ViewHolder holder;		
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.order_list_template, null);
				holder.chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.tvOrderNo);
				holder.tvOrderName = (TextView) convertView.findViewById(R.id.tvOrderName);
				holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.tvOrderPrice);
				holder.txtOrderAmount = (EditText) convertView.findViewById(R.id.txtOrderQty);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnOrderMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnOrderPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.chk.setChecked(orderDetail.isChecked());
			holder.tvOrderNo.setText(Integer.toString(position + 1) + ". ");
			holder.tvOrderName.setText(orderDetail.getProductName());
			holder.tvOrderPrice.setText(GlobalProperty.currencyFormat(mSqlite, orderDetail.getPricePerUnit()));
			holder.txtOrderAmount.setText(GlobalProperty.qtyFormat(mSqlite, orderDetail.getQty()));
	
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						mTransaction.updateOrderDetail(mTransactionId, mComputerId, 
								orderDetail.getOrderDetailId(), 
								orderDetail.getVatType(),
								mProducts.getVatRate(orderDetail.getProductId()), 
								qty, orderDetail.getPricePerUnit());
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.delete)
						.setMessage(R.string.confirm_delete_item)
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mTransaction.deleteOrderDetail(mTransactionId, mComputerId, 
										orderDetail.getOrderDetailId());
								mOrderDetailLst.remove(position);
								mOrderDetailAdapter.notifyDataSetChanged();
								mLayoutOrderCtrl.setVisibility(View.GONE);
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
					mTransaction.updateOrderDetail(mTransactionId, mComputerId, 
							orderDetail.getOrderDetailId(),
							orderDetail.getVatType(),
							mProducts.getVatRate(orderDetail.getProductId()), 
							qty, orderDetail.getPricePerUnit());
					
					mOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			
			if(orderDetail.isChecked())
				holder.chk.setVisibility(View.VISIBLE);
			else
				holder.chk.setVisibility(View.GONE);
			return convertView;
		}
		
		@Override
		public void notifyDataSetChanged() {
			if(mOrderDetailLst.size() == 0){
				mBtnDiscount.setEnabled(false);
				mBtnCash.setEnabled(false);
				mBtnHold.setEnabled(false);
			}else{
				mBtnDiscount.setEnabled(true);
				mBtnCash.setEnabled(true);
				mBtnHold.setEnabled(true);
			}
			summary();
			super.notifyDataSetChanged();
		}
		
		private class ViewHolder{
			CheckBox chk;
			TextView tvOrderNo;
			TextView tvOrderName;
			TextView tvOrderPrice;
			EditText txtOrderAmount;
			Button btnMinus;
			Button btnPlus;
		}
	}

	private class HoldBillAdapter extends BaseAdapter{
		LayoutInflater inflater;
		List<OrderTransaction> transLst;
		Calendar c;
		
		public HoldBillAdapter(List<OrderTransaction> transLst){
			inflater = LayoutInflater.from(MainActivity.this);
			this.transLst = transLst;
			c = Calendar.getInstance();
		}
		
		@Override
		public int getCount() {
			return transLst != null ? transLst.size() : 0;
		}

		@Override
		public OrderTransaction getItem(int position) {
			return transLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderTransaction trans = transLst.get(position);

			convertView = inflater.inflate(R.layout.hold_bill_template, null);
			TextView tvNo = (TextView) convertView.findViewById(R.id.tvNo);
			TextView tvOpenTime = (TextView) convertView.findViewById(R.id.tvOpenTime);
			TextView tvOpenStaff = (TextView) convertView.findViewById(R.id.tvOpenStaff);
			TextView tvRemark = (TextView) convertView.findViewById(R.id.tvRemark);

			c.setTimeInMillis(Long.parseLong(trans.getOpenTime()));
			tvNo.setText(Integer.toString(position + 1) + ".");
			tvOpenTime.setText(GlobalProperty.dateTimeFormat(mSqlite, c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getTransactionNote());

			return convertView;
		}
	}
	
	public void clearBillClicked(final View v){
		new AlertDialog.Builder(MainActivity.this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.clear_bill)
		.setMessage(R.string.confirm_clear_bill)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clearTransaction();
			}
		})
		.show();
	}

	public void clearTransaction(){
		mTransaction.deleteOrderDetail(mTransactionId, mComputerId);
		mTransaction.deleteTransaction(mTransactionId, mComputerId);
		mTransaction.cancelDiscount(mTransactionId, mComputerId);
		init();
	}
	
	private void voidBill(){
		Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("shopId", mShopId);
		startActivity(intent);
	}

	private void closeShift(){
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.close_shift)
		.setMessage(R.string.confirm_close_shift)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).show();
	}

	private void endday(){
		if(mOrderDetailLst.size() == 0){
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.endday)
			.setMessage(R.string.confirm_endday)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mProgress.setTitle(MainActivity.this.getString(R.string.endday));
					mProgress.setMessage(MainActivity.this.getString(R.string.endday_progress));
					ProgressListener progressListener = 
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
									.setMessage(msg)
									.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
										}
									}).show();
								}
							};
					MPOSUtil.doEndday(mSqlite, mShopId, mComputerId, mSessionId, 
							mStaffId, 0.0f, mShop.getCompanyVatRate(), true, progressListener);
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

	public void holdOrderClicked(final View v){
		LayoutInflater inflater = (LayoutInflater)
				MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View inputLayout = inflater.inflate(R.layout.input_text_layout, null);
		final EditText txtRemark = (EditText) inputLayout.findViewById(R.id.editText1);
		txtRemark.setHint(R.string.remark);
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.hold)
		.setView(inputLayout)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String note = txtRemark.getText().toString();
				mTransaction.holdTransaction(mTransactionId, mComputerId, note);
				
				init();
			}
		}).show();
	}

	public void holdBill() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<OrderTransaction> billLst = mTransaction.listHoldOrder(mComputerId);
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				if (mOrderDetailLst.size() == 0) {
					mTransactionId = trans.getTransactionId();
				}
			}
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.hold_bill)
		.setView(holdBillView)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mOrderDetailLst.size() > 0){
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.hold)
					.setMessage(R.string.hold_order)
					.setNeutralButton(R.string.close,
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
	
								}
	
							}).show();
				}else{
					// reset status 9 to status 1
					mTransaction.prepareTransaction(mTransactionId, mComputerId);
					init();
				}
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		dialog.show();
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
						Login login = new Login(mSqlite, user, pass);
						
						if(login.checkUser()){
							ShopData.Staff s = login.checkLogin();
							
							if(s != null){
								mStaffId = s.getStaffID();
								mSessionId = getCurrentSession();
								if(mSessionId == 0)
									mSessionId = mSession.addSession(mShopId, mComputerId, mStaffId, 0);
								
								ContentValues cv = new ContentValues();
								cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, mStaffId);
								mSqlite.update(OrderTransactionTable.TABLE_NAME, 
										cv, OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? AND " + 
										ComputerTable.COLUMN_COMPUTER_ID + "=?", 
										new String[]{
										String.valueOf(mTransactionId), 
										String.valueOf(mComputerId)
										});
								d.dismiss();
								init();
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

	public void logout() {
		Staff s = new Staff(mSqlite);

		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.logout)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(s.getStaff(mStaffId).getStaffName() + "\n" + this.getString(R.string.confirm_logout))
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		})
		.show();
	}

	private void appendOrderList(int orderId){
		if(orderId > 0){
			mOrderDetailLst.add(mTransaction.getOrder(mTransactionId, mComputerId, orderId));
			mOrderDetailAdapter.notifyDataSetChanged();
			mLvOrderDetail.setSelection(mOrderDetailAdapter.getCount() -1);
		}
	}
	
	@Override
	public void onMenuClick(int productId, int productTypeId, int vatType, double vatRate, double productPrice) {
		if(productTypeId == Products.NORMAL_TYPE || 
				productTypeId == Products.SET_TYPE){
			// fixes for open price
			if(productPrice > -1){
				int orderId = mTransaction.addOrderDetail(mTransactionId, mComputerId, productId, 
						productTypeId, vatType, vatRate, 1, productPrice);
				appendOrderList(orderId);
			}else{
				popupOpenPrice(productId, productTypeId, vatType, vatRate);
			}
		}else if(productTypeId == Products.SIZE_TYPE){
			popupProductSize(productId);
		}else if(productTypeId == Products.SET_TYPE_CAN_SELECT){
			Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
			intent.putExtra("productId", productId);
			startActivity(intent);
		}
	}

	@Override
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

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		switch(parent.getId()){
		case R.id.lvOrder:
			OrderTransaction.OrderDetail order = 
				(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);
			
			if(order.isChecked()){
				order.setChecked(false);
			}else{
				order.setChecked(true);
			}
			mOrderDetailAdapter.notifyDataSetChanged();
			
			if(listSelectedOrder().size() > 0){
				mLayoutOrderCtrl.setVisibility(View.VISIBLE);
			}
			else{
				mLayoutOrderCtrl.setVisibility(View.GONE);
			}
			break;
		}
	}

	private void clearSelectedOrder(){
		for(OrderTransaction.OrderDetail order : listSelectedOrder()){
			if(order.isChecked())
				order.setChecked(false);
		}
		mOrderDetailAdapter.notifyDataSetChanged();
		mLayoutOrderCtrl.setVisibility(View.GONE);
	}

	private void deleteSelectedOrder(){
		final List<OrderTransaction.OrderDetail> selectedOrderLst = listSelectedOrder();
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.delete)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setMessage(this.getString(R.string.confirm_delete) + " (" + selectedOrderLst.size() + ") ?")
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for(OrderTransaction.OrderDetail order : selectedOrderLst){
						mTransaction.deleteOrderDetail(mTransactionId, mComputerId, order.getOrderDetailId());
				}
				loadOrder();
				mLayoutOrderCtrl.setVisibility(View.GONE);
			}
		}).show();
	}

	private List<OrderTransaction.OrderDetail> listSelectedOrder(){
		List<OrderTransaction.OrderDetail> orderSelectedLst = 
				new ArrayList<OrderTransaction.OrderDetail>();
		for(OrderTransaction.OrderDetail order : mOrderDetailLst){
			if(order.isChecked())
				orderSelectedLst.add(order);
		}
		return orderSelectedLst;
	}

	// create popup openprice
	private void popupOpenPrice(final int productId, final int productTypeId, 
			final int vatType, final double vatRate){
		final EditText txtProductPrice = new EditText(this);
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
				// TODO Auto-generated method stub
				
			}
			
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				double productPrice = 0.0f;
				try {
					productPrice = Float.parseFloat(txtProductPrice.getText().toString());
					int orderId = mTransaction.addOrderDetail(mTransactionId, mComputerId, productId, 
							productTypeId, vatType, vatRate, 1, productPrice);
					appendOrderList(orderId);
				} catch (NumberFormatException e) {
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

	// create popup product size
	private void popupProductSize(int proId){
		List<Products.Product> pSizeLst = mProducts.listProductSize(proId);
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View sizeView = inflater.inflate(R.layout.product_size, null);
		ListView lvProSize = (ListView) sizeView.findViewById(R.id.lvProSize);
		builder.setView(sizeView);
		builder.setTitle(R.string.product_size);
		final AlertDialog dialog = builder.create();
		lvProSize.setAdapter(new ProductSizeAdapter(MainActivity.this, 
				mSqlite, pSizeLst));
		lvProSize.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long arg3) {
				Products.Product p = (Products.Product) parent.getItemAtPosition(position);
				int orderId = mTransaction.addOrderDetail(mTransactionId, mComputerId, p.getProductId(), 
						p.getProductTypeId(), p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
				appendOrderList(orderId);
				dialog.dismiss();
			}
			
		});
		dialog.show();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getAction() != KeyEvent.ACTION_DOWN)
			return true;
		
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			String barCode = mTxtBarCode.getText().toString();
			if(!barCode.equals("")){
				Products.Product p = mProducts.getProduct(barCode);
				if(p != null){
					int orderId = mTransaction.addOrderDetail(mTransactionId, mComputerId, p.getProductId(), 
							p.getProductTypeId(), p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
					appendOrderList(orderId);
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
			mTxtBarCode.setText(null);
		}
		return false;
	}
	
	private void printReceipt(int transactionId, int computerId){
		PrintReceiptLog printLog = new PrintReceiptLog(mSqlite);
		printLog.insertLog(transactionId, computerId, mStaffId);
		
		new PrintReceipt(MainActivity.this, mSqlite, mStaffId, new PrintReceipt.PrintStatusListener() {
			
			@Override
			public void onPrintSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPrintFail(String msg) {
				MPOSUtil.makeToask(MainActivity.this, msg);
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				
			}
		}).execute();
	}
	
	private void sendSale(){
		MPOSUtil.doSendSale(mSqlite, mShopId, mComputerId, mStaffId, 
				mShop.getCompanyVatRate(), new ProgressListener(){

				@Override
				public void onPre() {
				}

				@Override
				public void onPost() {
					countTransNotSend();
					MPOSUtil.makeToask(MainActivity.this, 
							MainActivity.this.getString(R.string.send_sale_data_success));
				}

				@Override
				public void onError(String msg) {
					MPOSUtil.makeToask(MainActivity.this, msg);
				}
				
			});
	}
}
