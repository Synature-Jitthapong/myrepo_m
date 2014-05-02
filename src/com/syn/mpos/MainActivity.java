package com.syn.mpos;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.astuetz.PagerSlidingTabStrip;
import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.ImageLoader;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.MPOSUtil.LoadSaleTransaction;
import com.syn.mpos.MPOSUtil.LoadSaleTransactionForEndday;
import com.syn.mpos.MPOSUtil.LoadSaleTransactionListener;
import com.syn.mpos.MPOSWebServiceClient.SendSaleTransaction;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.MPOSOrderTransaction;
import com.syn.mpos.database.MPOSProduct;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.MPOSShop;
import com.syn.mpos.database.MPOSTransaction;
import com.syn.mpos.database.PrintReceiptLogDataSource;
import com.syn.mpos.database.ProductsDataSource;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.ShopDataSource;
import com.syn.mpos.database.StaffDataSource;
import com.syn.mpos.database.SyncSaleLogDataSource;
import com.syn.mpos.database.OrderTransactionDataSource;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.SaleTransactionDataSource.POSData_SaleTransaction;
import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends FragmentActivity implements
	OnItemClickListener, OnClickListener, OnKeyListener{
	
	public static final int TAB_UNDERLINE_COLOR = 0xFF1D78B2;
	
	// send sale request code from payment activity
	public static final int PAYMENT_REQUEST = 1;
	
	private MPOSShop mShop;
	private MPOSProduct mProducts;
	private MPOSTransaction mTransaction;
	
	private List<MPOSOrderTransaction.OrderDetail> mOrderDetailLst;
	private OrderDetailAdapter mOrderDetailAdapter;
	private List<ProductsDataSource.ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPageAdapter;
	
	private int mStaffId;
	
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
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		if(mStaffId == 0){
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
		}else{
			setContentView(R.layout.activity_main);
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
	
			mShop = new MPOSShop(getApplicationContext());
			mProducts = new MPOSProduct(getApplicationContext());
			mTransaction = new MPOSTransaction(getApplicationContext());
			
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
			mOrderDetailLst = new ArrayList<MPOSOrderTransaction.OrderDetail>();
			mOrderDetailAdapter = new OrderDetailAdapter();
			mLvOrderDetail.setAdapter(mOrderDetailAdapter);
			
			mBtnDelSelOrder.setOnClickListener(this);
			mBtnClearSelOrder.setOnClickListener(this);
			mLvOrderDetail.setOnItemClickListener(this);
			mTxtBarCode.setOnKeyListener(this);
		}
	}

	private void openTransaction(){
		mTransaction.openTransaction(mStaffId);
		countHoldOrder();
		countTransNotSend();
		loadOrder();
	}
	
	@Override
	protected void onResume() {
		openTransaction();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == PAYMENT_REQUEST){
			if(resultCode == RESULT_OK){
				int transactionId = intent.getIntExtra("transactionId", 0);
				int computerId = intent.getIntExtra("computerId", 0);
				double change = intent.getDoubleExtra("change", 0);
				printReceipt();
				sendSale();
				
				if(change > 0){
					LayoutInflater inflater = (LayoutInflater) 
							MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
					tvChange.setText(mShop.getGlobalProperty().currencyFormat(change));
					
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
			intent = new Intent(MainActivity.this, SaleReportActivity.class);
			startActivity(intent);
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
			intent.putExtra("shopId", mShop.getShopId());
			intent.putExtra("computerId", mShop.getComputerId());
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

	/**
	 * @param v
	 * Go to PaymentActivity
	 */
	public void paymentClicked(final View v){
		Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
		intent.putExtra("staffId", mStaffId);
		startActivityForResult(intent, PAYMENT_REQUEST);
	}

	/**
	 * @param v
	 * Go to DiscountActivity
	 */
	public void discountClicked(final View v){
		Intent intent = new Intent(MainActivity.this, DiscountActivity.class);
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
		public MPOSOrderTransaction.OrderDetail getItem(int position) {
			return mOrderDetailLst.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final MPOSOrderTransaction.OrderDetail orderDetail = mOrderDetailLst.get(position);
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
			holder.tvOrderPrice.setText(mShop.getGlobalProperty().currencyFormat(orderDetail.getPricePerUnit()));
			holder.txtOrderAmount.setText(mShop.getGlobalProperty().qtyFormat(orderDetail.getQty()));
	
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						mTransaction.updateOrder(
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
								mTransaction.deleteOrder(orderDetail.getOrderDetailId());
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
					mTransaction.updateOrder( 
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
			tvOpenTime.setText(mShop.getGlobalProperty().dateTimeFormat(c.getTime()));
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
		public Fragment getItem(int position) {
			int deptId = mProductDeptLst.get(position).getProductDeptId();
			return MenuPageFragment.newInstance(deptId);
		}
	
		@Override
		public int getCount() {
			return mProductDeptLst.size();
		}		
	}

	/**
	 * @author j1tth4
	 * menu pager fragment
	 */
	public static class MenuPageFragment extends Fragment {
		
		private List<ProductsDataSource.Product> mProductLst;
		private MenuItemAdapter mAdapter;
		private int mDeptId;
		
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
			if((savedInstanceState != null) && savedInstanceState.containsKey("deptId")){
				mDeptId = savedInstanceState.getInt("deptId");
			}
			else{
				mDeptId = getArguments().getInt("deptId");
			}
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("deptId", mDeptId);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			final GridView gvItem = (GridView) inflater.inflate(R.layout.menu_grid_view, container, false);
			mProductLst = ((MainActivity) getActivity()).getProduct().listProduct(mDeptId);
			mAdapter = new MenuItemAdapter();
			gvItem.setAdapter(mAdapter);
			gvItem.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					ProductsDataSource.Product p = 
							(ProductsDataSource.Product) parent.getItemAtPosition(position);
					
					((MainActivity) getActivity()).onMenuClick(p.getProductId(), p.getProductTypeId(), 
							p.getVatType(), p.getVatRate(), p.getProductPrice());
				}
			});
			
			gvItem.setOnItemLongClickListener(new OnItemLongClickListener(){
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long id) {
					ProductsDataSource.Product p = (ProductsDataSource.Product) parent.getItemAtPosition(position);
					ImageViewPinchZoom imgZoom = ImageViewPinchZoom.newInstance(p.getImgUrl(), p.getProductName(), 
							((MainActivity) getActivity()).getShop().getGlobalProperty().currencyFormat(p.getProductPrice()));
					imgZoom.show(getFragmentManager(), "MenuImage");
					return true;
				}
				
			});
			return gvItem;
		}
		
		/**
		 * @author j1tth4
		 * MenuItemAdapter
		 */
		private class MenuItemAdapter extends BaseAdapter{
			
			private ImageLoader mImgLoader;
			private LayoutInflater mInflater;
			
			public MenuItemAdapter(){
				mImgLoader = new ImageLoader(getActivity(), R.drawable.default_image,
						MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
				mInflater =
						(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public int getCount() {
				return mProductLst.size();
			}

			@Override
			public ProductsDataSource.Product getItem(int position) {
				return mProductLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final ProductsDataSource.Product p = mProductLst.get(position);
				final ViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.menu_template, null);
					holder = new ViewHolder();
					holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
					holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
					holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolder) convertView.getTag();
				}
				
				holder.tvMenu.setText(p.getProductName());
				if(p.getProductPrice() < 0)
					holder.tvPrice.setVisibility(View.INVISIBLE);
				else
					holder.tvPrice.setText(((MainActivity) 
							getActivity()).getShop().getGlobalProperty()
							.currencyFormat(p.getProductPrice()));

				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						try {
							mImgLoader.displayImage(MPOSApplication.getImageUrl() + 
									p.getImgUrl(), holder.imgMenu);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, 500);
				return convertView;
			}
			
			class ViewHolder{
				ImageView imgMenu;
				TextView tvMenu;
				TextView tvPrice;
			}
		}
	}
	
	/**
	 * @author j1tth4
	 * ProductSizeAdapter
	 */
	private class ProductSizeAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		private List<ProductsDataSource.Product> mProLst;
		
		public ProductSizeAdapter(List<ProductsDataSource.Product> proLst){
			mInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mProLst = proLst;
		}
		
		@Override
		public int getCount() {
			return mProLst != null ? mProLst.size() : 0;
		}

		@Override
		public ProductsDataSource.Product getItem(int position) {
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
			ProductsDataSource.Product p = mProLst.get(position);
			holder.tvProductName.setText(p.getProductName());
			holder.tvProductPrice.setText(mShop.getGlobalProperty().currencyFormat(p.getProductPrice()));
			return convertView;
		}

		class ViewHolder{
			public TextView tvProductName;
			public TextView tvProductPrice;
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

	/**
	 * @param v
	 * Hold order click
	 */
	public void holdOrderClicked(final View v){
		LayoutInflater inflater = (LayoutInflater)
				MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View inputLayout = inflater.inflate(R.layout.input_text_layout, null);
		final EditText txtRemark = (EditText) inputLayout.findViewById(R.id.editText1);
		txtRemark.setHint(R.string.remark);
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.hold);
		builder.setView(inputLayout);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String note = txtRemark.getText().toString();
				mTransaction.holdOrder(note);
				
				openTransaction();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	public void holdBill() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<MPOSOrderTransaction> billLst = 
				mTransaction.listHoldOrder(mTransaction.getCurrentSessionDate());
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				
				MPOSOrderTransaction trans = (MPOSOrderTransaction) parent.getItemAtPosition(position);
				if (mOrderDetailLst.size() == 0) {
					mTransaction.setTransactionId(trans.getTransactionId());
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
					mTransaction.prepareTransaction();
					openTransaction();
				}
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setLayout(690, 
				WindowManager.LayoutParams.WRAP_CONTENT);
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
						Login login = new Login(MainActivity.this.getApplicationContext(), user, pass);
						
						if(login.checkUser()){
							ShopData.Staff s = login.checkLogin();
							if(s != null){
								mStaffId = s.getStaffID();
								mTransaction.openSession(mShop.getShopId(), 
										mShop.getComputerId(), mStaffId, 0);
								mTransaction.updateTransaction(mStaffId);
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
		ShopData.Staff s = mShop.getStaff(mStaffId);
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.logout)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(s.getStaffName() + "\n" + this.getString(R.string.confirm_logout))
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
	
	/**
	 * on menu item click
	 * @param productId
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param productPrice
	 */
	public void onMenuClick(int productId, int productTypeId, int vatType, 
			double vatRate, double productPrice) {
		if(productTypeId == ProductsDataSource.NORMAL_TYPE || 
				productTypeId == ProductsDataSource.SET_TYPE){
			checkOpenPrice(productId, productTypeId, vatType, vatRate, 1, productPrice);
		}else if(productTypeId == ProductsDataSource.SIZE_TYPE){
			productSizeDialog(productId);
		}else if(productTypeId == ProductsDataSource.SET_TYPE_CAN_SELECT){
//			Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
//			intent.putExtra("transactionId", mTransactionId);
//			intent.putExtra("computerId", mComputerId);
//			intent.putExtra("productTypeId", productTypeId);
//			intent.putExtra("productId", productId);
//			intent.putExtra("vatType", vatType);
//			intent.putExtra("vatRate", vatRate);
//			intent.putExtra("productPrice", productPrice);
//			startActivity(intent);
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
			MPOSOrderTransaction.OrderDetail order = 
				(MPOSOrderTransaction.OrderDetail) parent.getItemAtPosition(position);
			
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
	
	/**
	 * txtBarCode onKey
	 */
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getAction() != KeyEvent.ACTION_DOWN)
			return true;
		
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			String barCode = mTxtBarCode.getText().toString();
			if(!barCode.equals("")){
				ProductsDataSource.Product p = mProducts.getProduct(barCode);
				if(p != null){
					checkOpenPrice(p.getProductId(), p.getProductTypeId(), 
							p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
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
	
	public void onClearBarCode(final View v){
		mTxtBarCode.setText(null);
	}
	
	/**
	 * load order
	 */
	private void loadOrder(){
		mOrderDetailLst = mTransaction.listAllOrder();
		mOrderDetailAdapter.notifyDataSetChanged();
		if(mOrderDetailAdapter.getCount() > 1)
			mLvOrderDetail.setSelection(mOrderDetailAdapter.getCount() - 1);
	}

	public MPOSShop getShop(){
		return mShop;
	}
	
	public MPOSProduct getProduct(){
		return mProducts;
	}
	
	/**
	 * summary transaction 
	 */
	public void summary(){
		mTransaction.updateTransactionVat();
		if(mTransaction.getTotalDiscount() > 0)
			mTbRowDiscount.setVisibility(View.VISIBLE);
		else
			mTbRowDiscount.setVisibility(View.GONE);
		
		if(mTransaction.getTotalVatExclude() > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		mTvVatExclude.setText(mShop.getGlobalProperty().currencyFormat(mTransaction.getTotalVatExclude()));
		mTvSubTotal.setText(mShop.getGlobalProperty().currencyFormat(mTransaction.getSubTotalPrice()));
		mTvDiscount.setText("-" + mShop.getGlobalProperty().currencyFormat(mTransaction.getTotalDiscount()));
		mTvTotalPrice.setText(mShop.getGlobalProperty().currencyFormat(mTransaction.getTransactionVatable()));
	}

	/**
	 * clear order transaction
	 */
	private void clearTransaction(){
		mTransaction.clearTransaction();
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

	/**
	 * endday
	 */
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
		for(MPOSOrderTransaction.OrderDetail order : listSelectedOrder()){
			if(order.isChecked())
				order.setChecked(false);
		}
		mOrderDetailAdapter.notifyDataSetChanged();
		mLayoutOrderCtrl.setVisibility(View.GONE);
	}

	/**
	 * delete multiple selected order
	 */
	private void deleteSelectedOrder(){
		final List<MPOSOrderTransaction.OrderDetail> selectedOrderLst = listSelectedOrder();
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
				for(MPOSOrderTransaction.OrderDetail order : selectedOrderLst){
						mTransaction.deleteOrder(order.getOrderDetailId());
				}
				loadOrder();
				mLayoutOrderCtrl.setVisibility(View.GONE);
			}
		}).show();
	}

	/**
	 * get selected order
	 * @return
	 */
	private List<MPOSOrderTransaction.OrderDetail> listSelectedOrder(){
		List<MPOSOrderTransaction.OrderDetail> orderSelectedLst = 
				new ArrayList<MPOSOrderTransaction.OrderDetail>();
		for(MPOSOrderTransaction.OrderDetail order : mOrderDetailLst){
			if(order.isChecked())
				orderSelectedLst.add(order);
		}
		return orderSelectedLst;
	}

	/**
	 * @param productId
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param qty
	 * @param price
	 */
	private void addOrder(int productId, int productTypeId, int vatType, 
			double vatRate, double qty, double price){
		mTransaction.addOrder(mShop.getComputerId(), 
				productId, productTypeId, vatType, vatRate, qty, price);
		loadOrder();
	}

	/**
	 * @param productId
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param qty
	 * @param price
	 */
	private void checkOpenPrice(int productId, int productTypeId, int vatType, 
			double vatRate, double qty, double price){
		if(price > -1){
			addOrder(productId, productTypeId, vatType, vatRate, 1, price);
		}else{
			openPriceDialog(productId, productTypeId, vatType, vatRate);
		}
	}

	/**
	 * create open price dialog
	 * @param productId
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 */
	private void openPriceDialog(final int productId, final int productTypeId, 
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
					addOrder(productId, productTypeId, vatType, vatRate, 1, productPrice);
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

	/**
	 * create product size dialog
	 * @param proId
	 */
	private void productSizeDialog(int proId){
		List<ProductsDataSource.Product> pSizeLst = mProducts.listProductSize(proId);
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
				ProductsDataSource.Product p = (ProductsDataSource.Product) parent.getItemAtPosition(position);
				addOrder(p.getProductId(), p.getProductTypeId(), p.getVatType(), 
						p.getVatRate(), 1, p.getProductPrice());
				dialog.dismiss();
			}
			
		});
		dialog.show();
	}

	/**
	 * count transaction that not send to server
	 */
	private void countTransNotSend(){
		if(mItemSendSale != null){
			int total = mTransaction.countTransNotSend();
			if(total > 0){
				mItemSendSale.setTitle(this.getString(R.string.send_sale_data) + "(" + total + ")");
			}else{
				mItemSendSale.setTitle(this.getString(R.string.send_sale_data));
			}
		}
	}

	/**
	 * count order that hold
	 */
	private void countHoldOrder(){
		if(mItemHoldBill != null){
			int totalHold = mTransaction.countHoldOrder();
			if(totalHold > 0){
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill) + "(" + totalHold + ")");
			}else{
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill));
			}
		}
	}

	private void printReceipt(){
		PrintReceiptLogDataSource printLog = 
				new PrintReceiptLogDataSource(MPOSApplication.getContext());
		printLog.insertLog(mTransaction.getTransactionId(), mStaffId);
		
		new PrintReceipt(new PrintReceipt.PrintStatusListener() {
			
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

	/**
	 * send sale data to server
	 */
	private void sendSale(){
		final LoadSaleTransactionListener loadSaleListener = new LoadSaleTransactionListener() {

			@Override
			public void onPre() {
				
			}

			@Override
			public void onPost(POSData_SaleTransaction saleTrans,
					final String sessionDate) {

				JSONUtil jsonUtil = new JSONUtil();
				Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
				final String jsonSale = jsonUtil.toJson(type, saleTrans);

				ProgressListener sendSaleListener = new ProgressListener() {
					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
						// do update transaction already send
						mTransaction.updateTransactionSendStatus();
						countTransNotSend();
						MPOSUtil.makeToask(MainActivity.this, 
								MainActivity.this.getString(R.string.send_sale_data_success));
					}

					@Override
					public void onError(String msg) {
						MPOSUtil.makeToask(MainActivity.this, msg);
					}
				};
				new MPOSWebServiceClient.SendPartialSaleTransaction(MPOSApplication.getContext(), 
						mStaffId, mShop.getShopId(), mShop.getComputerId(), 
						jsonSale, sendSaleListener).execute(MPOSApplication.getFullUrl());
			}

			@Override
			public void onError(String msg) {
				MPOSUtil.makeToask(MainActivity.this, msg);
			}

			@Override
			public void onPost() {
			}

		};
		new LoadSaleTransaction(MPOSApplication.getContext(),
				mTransaction.getTransaction().getSaleDate(), 
				mTransaction.getTransactionId(), loadSaleListener).execute();
	}
}
