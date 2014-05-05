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
import com.syn.mpos.MPOSUtil.LoadSaleTransactionListener;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.MPOSOrderTransaction;
import com.syn.mpos.database.OrdersDataSource;
import com.syn.mpos.database.PrintReceiptLogDataSource;
import com.syn.mpos.database.ProductsDataSource;
import com.syn.mpos.database.SaleTransactionDataSource.POSData_SaleTransaction;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.ShopDataSource;
import com.syn.mpos.database.StaffDataSource;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	private static ProductsDataSource sProducts;
	private static ShopDataSource sShop;
	private static GlobalPropertyDataSource sGlobal;
	
	private SessionDataSource mSession;
	private OrdersDataSource mOrders;
	private ComputerDataSource mComputer;
	
	private List<MPOSOrderTransaction.MPOSOrderDetail> mOrderDetailLst;
	private OrderDetailAdapter mOrderDetailAdapter;
	private List<ProductsDataSource.ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPageAdapter;
	
	private int mSessionId;
	private int mTransactionId;
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
	
			mSession = new SessionDataSource(getApplicationContext());
			mOrders = new OrdersDataSource(getApplicationContext());
			sProducts = new ProductsDataSource(getApplicationContext());
			sShop = new ShopDataSource(getApplicationContext());
			mComputer = new ComputerDataSource(getApplicationContext());
			sGlobal = new GlobalPropertyDataSource(getApplicationContext());
			
			mProductDeptLst = sProducts.listProductDept();
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
			mOrderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
			mOrderDetailAdapter = new OrderDetailAdapter();
			mLvOrderDetail.setAdapter(mOrderDetailAdapter);
			
			mBtnDelSelOrder.setOnClickListener(this);
			mBtnClearSelOrder.setOnClickListener(this);
			mLvOrderDetail.setOnItemClickListener(this);
			mTxtBarCode.setOnKeyListener(this);
		}
	}

	private int openSession(){
		mSessionId = mSession.getCurrentSessionId(mStaffId); 
		if(mSessionId == 0){
			mSessionId = mSession.openSession(sShop.getShopId(), 
					mComputer.getComputerId(), mStaffId, 0);
		}
		return mSessionId;
	}
	
	private void openTransaction(){
		mTransactionId = mOrders.getCurrTransactionId(mSession.getSessionDate());
		if(mTransactionId == 0){
			mTransactionId = mOrders.openTransaction(sShop.getShopId(), mComputer.getComputerId(),
					openSession(), mStaffId, sShop.getCompanyVatRate());
		}
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
				double change = intent.getDoubleExtra("change", 0);
				printReceipt();
				sendSale();
				
				if(change > 0){
					LayoutInflater inflater = (LayoutInflater) 
							MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
					tvChange.setText(sGlobal.currencyFormat(change));
					
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
			showHoldBill();
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
			startActivity(intent);
			return true;
		case R.id.itemSendSale:
			intent = new Intent(MainActivity.this, SyncSaleActivity.class);
			intent.putExtra("staffId", mStaffId);
			intent.putExtra("shopId", sShop.getShopId());
			intent.putExtra("computerId", mComputer.getComputerId());
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
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputer.getComputerId());
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
		public MPOSOrderTransaction.MPOSOrderDetail getItem(int position) {
			return mOrderDetailLst.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final MPOSOrderTransaction.MPOSOrderDetail orderDetail = mOrderDetailLst.get(position);
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
			holder.tvOrderPrice.setText(sGlobal.currencyFormat(orderDetail.getPricePerUnit()));
			holder.txtOrderAmount.setText(sGlobal.qtyFormat(orderDetail.getQty()));
	
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						mOrders.updateOrderDetail(mTransactionId,
								orderDetail.getOrderDetailId(), 
								orderDetail.getVatType(),
								sProducts.getVatRate(orderDetail.getProductId()), 
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
								mOrders.deleteOrderDetail(mTransactionId, 
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
					mOrders.updateOrderDetail(mTransactionId,
							orderDetail.getOrderDetailId(),
							orderDetail.getVatType(),
							sProducts.getVatRate(orderDetail.getProductId()), 
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
			tvOpenTime.setText(sGlobal.dateTimeFormat(c.getTime()));
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
	public static class MenuItemViewHolder{
		ImageView imgMenu;
		TextView tvMenu;
		TextView tvPrice;
	}
	
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
			mProductLst = sProducts.listProduct(mDeptId);
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
							sGlobal.currencyFormat(p.getProductPrice()));
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
					holder.tvPrice.setText(sGlobal.currencyFormat(p.getProductPrice()));

				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						try {
							mImgLoader.displayImage(MPOSApplication.getImageUrl(getActivity()) + 
									p.getImgUrl(), holder.imgMenu);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, 500);
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
			holder.tvProductPrice.setText(sGlobal.currencyFormat(p.getProductPrice()));
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
				mOrders.holdTransaction(mTransactionId, note);
				
				openTransaction();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	public void showHoldBill() {
		final MPOSOrderTransaction holdTrans = new MPOSOrderTransaction();
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<MPOSOrderTransaction> billLst = 
				mOrders.listHoldOrder(mSession.getSessionDate());
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				MPOSOrderTransaction trans = (MPOSOrderTransaction) parent.getItemAtPosition(position);
				if (mOrderDetailLst.size() == 0) {
					holdTrans.setTransactionId(trans.getTransactionId());
				}
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
		
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setLayout(690, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
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
					/* hold current transaction
					 * and prepare selected transaction
					 */
					mOrders.holdTransaction(mTransactionId, "");
					mOrders.prepareTransaction(holdTrans.getTransactionId());
					openTransaction();
				}
			}
		});
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
								openSession();
								mOrders.updateTransaction(mTransactionId, mStaffId);
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
		StaffDataSource staff = new StaffDataSource(getApplicationContext());
		ShopData.Staff s = staff.getStaff(mStaffId);
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
			MPOSOrderTransaction.MPOSOrderDetail order = 
				(MPOSOrderTransaction.MPOSOrderDetail) parent.getItemAtPosition(position);
			
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
				ProductsDataSource.Product p = sProducts.getProduct(barCode);
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
		mOrderDetailLst = mOrders.listAllOrder(mTransactionId);
		mOrderDetailAdapter.notifyDataSetChanged();
		if(mOrderDetailAdapter.getCount() > 1)
			mLvOrderDetail.setSelection(mOrderDetailAdapter.getCount() - 1);
	}

	/**
	 * summary transaction 
	 */
	public void summary(){
		mOrders.updateTransactionVat(mTransactionId);
		MPOSOrderTransaction trans = mOrders.getTransaction(mTransactionId);
		MPOSOrderTransaction.MPOSOrderDetail summOrder = 
				mOrders.getSummaryOrder(mTransactionId);
		if(summOrder.getPriceDiscount() > 0)
			mTbRowDiscount.setVisibility(View.VISIBLE);
		else
			mTbRowDiscount.setVisibility(View.GONE);
		
		if(summOrder.getVatExclude() > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		mTvVatExclude.setText(sGlobal.currencyFormat(summOrder.getVatExclude()));
		mTvSubTotal.setText(sGlobal.currencyFormat(summOrder.getTotalRetailPrice()));
		mTvDiscount.setText("-" + sGlobal.currencyFormat(summOrder.getPriceDiscount()));
		mTvTotalPrice.setText(sGlobal.currencyFormat(trans.getTransactionVatable()));
	}

	/**
	 * cancel order transaction
	 */
	private void clearTransaction(){
		mOrders.cancelTransaction(mTransactionId);
		openTransaction();
	}

	/**
	 * void bill
	 */
	private void voidBill(){
		Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("shopId", sShop.getShopId());
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
					MPOSUtil.doEndday(MainActivity.this, sShop.getShopId(), 
							mComputer.getComputerId(), mSessionId, mStaffId, 0, true, progressListener);
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
		for(MPOSOrderTransaction.MPOSOrderDetail order : listSelectedOrder()){
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
		final List<MPOSOrderTransaction.MPOSOrderDetail> selectedOrderLst = listSelectedOrder();
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
				for(MPOSOrderTransaction.MPOSOrderDetail order : selectedOrderLst){
						mOrders.deleteOrderDetail(mTransactionId, order.getOrderDetailId());
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
	 * @param productId
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param qty
	 * @param price
	 */
	private void addOrder(int productId, int productTypeId, int vatType, 
			double vatRate, double qty, double price){
		mOrders.addOrderDetail(mTransactionId, mComputer.getComputerId(), 
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
		List<ProductsDataSource.Product> pSizeLst = sProducts.listProductSize(proId);
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
			int total = mOrders.countTransNotSend();
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
			int totalHold = mOrders.countHoldOrder(mSession.getSessionDate());
			if(totalHold > 0){
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill) + "(" + totalHold + ")");
			}else{
				mItemHoldBill.setTitle(this.getString(R.string.hold_bill));
			}
		}
	}

	private void printReceipt(){
		PrintReceiptLogDataSource printLog = 
				new PrintReceiptLogDataSource(getApplicationContext());
		printLog.insertLog(mTransactionId, mStaffId);
		
		new PrintReceipt(getApplicationContext(), new PrintReceipt.PrintStatusListener() {
			
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
		MPOSUtil.doSendSale(MainActivity.this, sShop.getShopId(), 
				mComputer.getComputerId(), mStaffId, new ProgressListener(){

					@Override
					public void onPre() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onPost() {
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
