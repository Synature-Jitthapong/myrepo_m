package com.syn.mpos;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.astuetz.PagerSlidingTabStrip;
import com.j1tth4.exceptionhandler.ExceptionHandler;
import com.j1tth4.util.ImageLoader;
import com.syn.mpos.dao.ComputerDao;
import com.syn.mpos.dao.GlobalPropertyDao;
import com.syn.mpos.dao.Login;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.PrintReceiptLogDao;
import com.syn.mpos.dao.ProductsDao;
import com.syn.mpos.dao.SessionDao;
import com.syn.mpos.dao.ShopDao;
import com.syn.mpos.dao.StaffDao;
import com.syn.mpos.dao.TransactionDao;
import com.syn.pos.ShopData;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends FragmentActivity{
	
	public static final int TAB_UNDERLINE_COLOR = 0xFF1D78B2;
	
	// send sale request code from payment activity
	public static final int PAYMENT_REQUEST = 1;
	
	private static ProductsDao sProducts;
	private static ShopDao sShop;
	private static GlobalPropertyDao sGlobal;
	
	private static SessionDao sSession;
	private static TransactionDao sTransaction;
	private static ComputerDao sComputer;
	
	private static List<MPOSOrderTransaction.MPOSOrderDetail> sOrderDetailLst;
	private static OrderDetailAdapter sOrderDetailAdapter;
	private static List<ProductsDao.ProductDept> sProductDeptLst;
	private static MenuItemPagerAdapter sPageAdapter;
	
	private static TableLayout sTbSummary;
	private static EditText sTxtBarCode;
	
	private static ProgressDialog mProgress;

	private static MenuItem mItemHoldBill;
	private static MenuItem mItemSendSale;
	
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
		
		sSession = new SessionDao(getApplicationContext());
		sTransaction = new TransactionDao(getApplicationContext());
		sProducts = new ProductsDao(getApplicationContext());
		sShop = new ShopDao(getApplicationContext());
		sComputer = new ComputerDao(getApplicationContext());
		sGlobal = new GlobalPropertyDao(getApplicationContext());
		
		/*
		 * For create pager by productDept
		 */
		sProductDeptLst = sProducts.listProductDept();
		sPageAdapter = new MenuItemPagerAdapter(getSupportFragmentManager());

		mProgress = new ProgressDialog(this);
		mProgress.setCancelable(false);
		sOrderDetailLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		sOrderDetailAdapter = new OrderDetailAdapter();

		if(savedInstanceState == null){
			getFragmentManager().beginTransaction().
				add(R.id.container, PlaceholderFragment.newInstance()).commit();
		}
	}
	
	@Override
	protected void onResume() {
		openTransaction();
		super.onResume();
	}

	public static class PlaceholderFragment extends Fragment{
		
		private PagerSlidingTabStrip mTabs;
		private ViewPager mPager;
		private ListView mLvOrderDetail;
		
		public static PlaceholderFragment newInstance(){
			PlaceholderFragment f = new PlaceholderFragment();
			return f;
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.activity_main, menu);
			mItemHoldBill = menu.findItem(R.id.itemHoldBill);
			mItemSendSale = menu.findItem(R.id.itemSendSale);
			
			countHoldOrder(getActivity());
			countTransNotSend(getActivity());
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			sTxtBarCode = (EditText) rootView.findViewById(R.id.txtBarCode);
			sTbSummary = (TableLayout) rootView.findViewById(R.id.tbLayoutSummary);
			mLvOrderDetail = (ListView) rootView.findViewById(R.id.lvOrder);
			mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
			mPager = (ViewPager) rootView.findViewById(R.id.pager);

			mPager.setAdapter(sPageAdapter);
			final int pageMargin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
							.getDisplayMetrics());
			mPager.setPageMargin(pageMargin);
			mTabs.setViewPager(mPager);
			mTabs.setIndicatorColor(TAB_UNDERLINE_COLOR);
			
			mLvOrderDetail.setAdapter(sOrderDetailAdapter);
			mLvOrderDetail.post(new Runnable(){

				@Override
				public void run() {
					if(sOrderDetailAdapter.getCount() > 1)
						mLvOrderDetail.setSelection(sOrderDetailAdapter.getCount() - 1);
				}
				
			});
			
			mLvOrderDetail.setOnItemClickListener(((MainActivity) getActivity()).onItemClick);
			sTxtBarCode.setOnKeyListener(((MainActivity) getActivity()).onKey);
			return rootView;
		}	
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == PAYMENT_REQUEST){
			if(resultCode == RESULT_OK){
				// request param from PaymentActivity for log to 
				// PrintReceiptLog
				double change = intent.getDoubleExtra("change", 0);
				int transactionId = intent.getIntExtra("transactionId", 0);
				int staffId = intent.getIntExtra("staffId", 0);
				printReceipt(transactionId, staffId);
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
			intent.putExtra("computerId", sComputer.getComputerId());
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
		if(sOrderDetailLst.size() > 0){
			Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", sComputer.getComputerId());
			intent.putExtra("staffId", mStaffId);
			startActivityForResult(intent, PAYMENT_REQUEST);
		}
	}

	/**
	 * @param v
	 * Go to DiscountActivity
	 */
	public void discountClicked(final View v){
		if(sOrderDetailLst.size() > 0){
			Intent intent = new Intent(MainActivity.this, DiscountActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			startActivity(intent);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else if(keyCode == KeyEvent.KEYCODE_HOME){
			return false;
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
			return sOrderDetailLst != null ? sOrderDetailLst.size() : 0;
		}
	
		@Override
		public MPOSOrderTransaction.MPOSOrderDetail getItem(int position) {
			return sOrderDetailLst.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final MPOSOrderTransaction.MPOSOrderDetail orderDetail = sOrderDetailLst.get(position);
			ViewHolder holder;		
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.order_list_template, null);
				holder.orderSetContent = (LinearLayout) convertView.findViewById(R.id.orderSetContent);
				holder.chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.tvOrderNo);
				holder.tvOrderName = (TextView) convertView.findViewById(R.id.tvOrderName);
				holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.tvOrderPrice);
				holder.txtOrderAmount = (EditText) convertView.findViewById(R.id.txtOrderQty);
				holder.btnSetMod = (Button) convertView.findViewById(R.id.btnSetModify);
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
	
			holder.orderSetContent.removeAllViews();
			if(orderDetail.getOrderSetDetailLst() != null){
				for(int i = 0; i < orderDetail.getOrderSetDetailLst().size(); i++){
					final MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail = 
							orderDetail.getOrderSetDetailLst().get(i);
					final View detailView = mInflater.inflate(R.layout.order_set_detail_template, null);
					TextView tvSetNo = (TextView) detailView.findViewById(R.id.tvSetNo);
					TextView tvSetName = (TextView) detailView.findViewById(R.id.tvSetName);
					EditText txtSetQty = (EditText) detailView.findViewById(R.id.txtSetQty);
					Button btnSetMinus = (Button) detailView.findViewById(R.id.btnSetMinus);
					Button btnSetPlus = (Button) detailView.findViewById(R.id.btnSetPlus);
					tvSetNo.setText("-");
					tvSetName.setText(setDetail.getProductName());
					txtSetQty.setText(sGlobal.qtyFormat(setDetail.getOrderSetQty()));
					btnSetMinus.setVisibility(View.GONE);
					btnSetPlus.setVisibility(View.GONE);
					holder.orderSetContent.addView(detailView);
				}
			}
			
			holder.btnSetMod.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
					intent.putExtra("mode", ProductSetActivity.EDIT_MODE);
					intent.putExtra("transactionId", mTransactionId);
					intent.putExtra("computerId", sComputer.getComputerId());
					intent.putExtra("orderDetailId", orderDetail.getOrderDetailId());
					intent.putExtra("productId", orderDetail.getProductId());
					startActivity(intent);
				}
				
			});
			
			holder.btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						sTransaction.updateOrderDetail(mTransactionId,
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
								sTransaction.cancelOrder(mTransactionId, 
										orderDetail.getOrderDetailId());
								sOrderDetailLst.remove(position);
								sOrderDetailAdapter.notifyDataSetChanged();
							}
						}).show();
					}
					
					sOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			
			holder.btnPlus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					double qty = orderDetail.getQty();
					orderDetail.setQty(++qty);
					sTransaction.updateOrderDetail(mTransactionId,
							orderDetail.getOrderDetailId(),
							orderDetail.getVatType(),
							sProducts.getVatRate(orderDetail.getProductId()), 
							qty, orderDetail.getPricePerUnit());
					
					sOrderDetailAdapter.notifyDataSetChanged();
				}
				
			});
			
			if(orderDetail.getProductTypeId() == ProductsDao.SET_TYPE_CAN_SELECT)
				holder.btnSetMod.setVisibility(View.VISIBLE);
			else
				holder.btnSetMod.setVisibility(View.GONE);
			if(orderDetail.isChecked())
				holder.chk.setVisibility(View.VISIBLE);
			else
				holder.chk.setVisibility(View.GONE);
			return convertView;
		}
		
		@Override
		public void notifyDataSetChanged() {
			summary();
			super.notifyDataSetChanged();
		}
		
		private class ViewHolder{
			LinearLayout orderSetContent;
			CheckBox chk;
			TextView tvOrderNo;
			TextView tvOrderName;
			TextView tvOrderPrice;
			EditText txtOrderAmount;
			Button btnSetMod;
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
			return sProductDeptLst.get(position).getProductDeptName();
		}
	
		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			int deptId = sProductDeptLst.get(position).getProductDeptId();
			return MenuPageFragment.newInstance(deptId);
		}
	
		@Override
		public int getCount() {
			return sProductDeptLst.size();
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
	
	public static class MenuPageFragment extends android.support.v4.app.Fragment {
		
		private List<ProductsDao.Product> mProductLst;
		private MenuItemAdapter mAdapter;
		private int mDeptId;

		private ImageLoader mImgLoader;
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
			if((savedInstanceState != null) && savedInstanceState.containsKey("deptId")){
				mDeptId = savedInstanceState.getInt("deptId");
			}
			else{
				mDeptId = getArguments().getInt("deptId");
			}
			mImgLoader = new ImageLoader(getActivity(), 0,
					MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.SMALL);
			mInflater =
					(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mAdapter = new MenuItemAdapter();
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
			gvItem.setAdapter(mAdapter);
			gvItem.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					ProductsDao.Product p = 
							(ProductsDao.Product) parent.getItemAtPosition(position);
					((MainActivity) getActivity()).onMenuClick(p.getProductId(), p.getProductCode(), 
							p.getProductName(), p.getProductTypeId(), 
							p.getVatType(), p.getVatRate(), p.getProductPrice());
				}
			});
			
			gvItem.setOnItemLongClickListener(new OnItemLongClickListener(){
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v,
						int position, long id) {
					ProductsDao.Product p = (ProductsDao.Product) parent.getItemAtPosition(position);
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
		
			@Override
			public int getCount() {
				return mProductLst.size();
			}

			@Override
			public ProductsDao.Product getItem(int position) {
				return mProductLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final ProductsDao.Product p = mProductLst.get(position);
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

//				new Handler().postDelayed(new Runnable(){
//
//					@Override
//					public void run() {
//						try {
							mImgLoader.displayImage(MPOSApplication.getImageUrl(getActivity()) + 
									p.getImgUrl(), holder.imgMenu);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					
//				}, 500);
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
		private List<ProductsDao.Product> mProLst;
		
		public ProductSizeAdapter(List<ProductsDao.Product> proLst){
			mInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mProLst = proLst;
		}
		
		@Override
		public int getCount() {
			return mProLst != null ? mProLst.size() : 0;
		}

		@Override
		public ProductsDao.Product getItem(int position) {
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
			ProductsDao.Product p = mProLst.get(position);
			holder.tvProductName.setText(p.getProductName());
			holder.tvProductPrice.setText(sGlobal.currencyFormat(p.getProductPrice()));
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
	public void onMenuClick(int productId, String productCode, String productName, 
			int productTypeId, int vatType, double vatRate, double productPrice) {
		if(productTypeId == ProductsDao.NORMAL_TYPE || 
				productTypeId == ProductsDao.SET_TYPE){
			addOrder(productId, productCode, productName, productTypeId, 
					vatType, vatRate, 1, productPrice);
		}else if(productTypeId == ProductsDao.SIZE_TYPE){
			productSizeDialog(productId);
		}else if(productTypeId == ProductsDao.SET_TYPE_CAN_SELECT){
			Intent intent = new Intent(MainActivity.this, ProductSetActivity.class);
			intent.putExtra("mode", ProductSetActivity.ADD_MODE);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", sComputer.getComputerId());
			intent.putExtra("productId", productId);
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

	public OnItemClickListener onItemClick = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch(parent.getId()){
			case R.id.lvOrder:
				MPOSOrderTransaction.MPOSOrderDetail order = 
					(MPOSOrderTransaction.MPOSOrderDetail) parent.getItemAtPosition(position);
				
				if(order.isChecked()){
					order.setChecked(false);
				}else{
					order.setChecked(true);
				}
				sOrderDetailAdapter.notifyDataSetChanged();
				break;
			}
		}
		
	};
	
	public OnKeyListener onKey = new OnKeyListener(){

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction() != KeyEvent.ACTION_DOWN)
				return true;
			
			if(keyCode == KeyEvent.KEYCODE_ENTER){
				String barCode = ((EditText) v).getText().toString();
				if(!barCode.equals("")){
					ProductsDao.Product p = sProducts.getProduct(barCode);
					if(p != null){
						addOrder(p.getProductId(), p.getProductCode(), p.getProductName(), 
								p.getProductTypeId(), p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
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
				((EditText) v).setText(null);
			}
			return false;
		}
		
	};
	
	public void onClearBarCode(final View v){
		sTxtBarCode.setText(null);
	}
	
	/**
	 * summary transaction 
	 */
	public void summary(){
		sTbSummary.removeAllViews();
		TextView tvLabel = new TextView(MainActivity.this);
		TextView tvValue = new TextView(MainActivity.this);
		tvLabel.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
		tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
				TableRow.LayoutParams.WRAP_CONTENT, 1f));
		tvValue.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
		tvValue.setGravity(Gravity.RIGHT);
		
		sTransaction.summary(mTransactionId);
		MPOSOrderTransaction.MPOSOrderDetail summOrder = 
				sTransaction.getSummaryOrder(mTransactionId);
		if(summOrder.getPriceDiscount() > 0){
			TableRow rowDiscount = new TableRow(MainActivity.this);
			tvLabel = new TextView(MainActivity.this);
			tvValue = new TextView(MainActivity.this);
			tvLabel.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
			tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
					TableRow.LayoutParams.WRAP_CONTENT, 1f));
			tvValue.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
			tvValue.setGravity(Gravity.RIGHT);
			tvLabel.setText(R.string.discount);
			tvValue.setText("-" + sGlobal.currencyFormat(summOrder.getPriceDiscount()));
			rowDiscount.addView(tvLabel);
			rowDiscount.addView(tvValue);
			sTbSummary.addView(rowDiscount);
		}
		if(summOrder.getVatExclude() > 0){
			TableRow rowExcludeVat = new TableRow(MainActivity.this);
			tvLabel = new TextView(MainActivity.this);
			tvValue = new TextView(MainActivity.this);
			tvLabel.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
			tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
					TableRow.LayoutParams.WRAP_CONTENT, 1f));
			tvValue.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
			tvValue.setGravity(Gravity.RIGHT);
			tvLabel.setText(R.string.tax_exclude);
			tvValue.setText(sGlobal.currencyFormat(summOrder.getVatExclude()));
			rowExcludeVat.addView(tvLabel);
			rowExcludeVat.addView(tvValue);
			sTbSummary.addView(rowExcludeVat);
		}
		TableRow rowSubTotal = new TableRow(MainActivity.this);
		tvLabel = new TextView(MainActivity.this);
		tvValue = new TextView(MainActivity.this);
		tvLabel.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
		tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
				TableRow.LayoutParams.WRAP_CONTENT, 1f));
		tvValue.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
		tvValue.setGravity(Gravity.RIGHT);
		tvLabel.setText(R.string.sub_total);
		tvValue.setText(sGlobal.currencyFormat(summOrder.getTotalRetailPrice()));
		rowSubTotal.addView(tvLabel);
		rowSubTotal.addView(tvValue);
		sTbSummary.addView(rowSubTotal);
		
		TableRow rowGrandTotal = new TableRow(MainActivity.this);
		tvLabel = new TextView(MainActivity.this);
		tvValue = new TextView(MainActivity.this);
		tvLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
				TableRow.LayoutParams.WRAP_CONTENT, 1f));
		tvLabel.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Large);
		tvValue.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Holo_Medium);
		tvValue.setGravity(Gravity.RIGHT);
		tvLabel.setText(R.string.total);
		MPOSOrderTransaction trans = sTransaction.getTransaction(mTransactionId);
		tvValue.setText(sGlobal.currencyFormat(trans.getTransactionVatable()));
		tvValue.setTextSize(32);
		rowGrandTotal.addView(tvLabel);
		rowGrandTotal.addView(tvValue);
		sTbSummary.addView(rowGrandTotal);
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
		if(sOrderDetailLst.size() > 0){
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
					sTransaction.holdTransaction(mTransactionId, note);
					
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
						Login login = new Login(MainActivity.this.getApplicationContext(), user, pass);
						
						if(login.checkUser()){
							ShopData.Staff s = login.checkLogin();
							if(s != null){
								mStaffId = s.getStaffID();
								openSession();
								sTransaction.updateTransaction(mTransactionId, mStaffId);
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
		StaffDao staff = new StaffDao(getApplicationContext());
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
	 * load order
	 */
	private void loadOrder(){
		sOrderDetailLst = sTransaction.listAllOrder(mTransactionId);
		sOrderDetailAdapter.notifyDataSetChanged();
	}

	private void openTransaction(){
		openSession();	
		mTransactionId = sTransaction.getCurrTransactionId(sSession.getSessionDate());
		if(mTransactionId == 0){
			mTransactionId = sTransaction.openTransaction(sSession.getSessionDate(), 
					sShop.getShopId(), sComputer.getComputerId(),
					mSessionId, mStaffId, sShop.getCompanyVatRate());
		}
		countHoldOrder(MainActivity.this);
		countTransNotSend(MainActivity.this);
		loadOrder();
	}

	private void openSession(){
		mSessionId = sSession.getCurrentSessionId(mStaffId); 
		if(mSessionId == 0){
			mSessionId = sSession.openSession(sShop.getShopId(), 
					sComputer.getComputerId(), mStaffId, 0);
		}
	}

	private void showHoldBill() {
		final MPOSOrderTransaction holdTrans = new MPOSOrderTransaction();
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<MPOSOrderTransaction> billLst = 
				sTransaction.listHoldOrder(sSession.getSessionDate());
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
				if(sOrderDetailLst.size() > 0){
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
					if(holdTrans.getTransactionId() != 0){
						sTransaction.prepareTransaction(holdTrans.getTransactionId());
						// Delete current transaction because not have any orders.
						sTransaction.deleteTransaction(mTransactionId);
						openTransaction();
						dialog.dismiss();
					}else{
						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.hold_bill)
						.setMessage(R.string.select_order_first)
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
		sTransaction.cancelTransaction(mTransactionId);
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
		if(sOrderDetailLst.size() == 0){
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
							sComputer.getComputerId(), mSessionId, mStaffId, 0, true, progressListener);
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
			sOrderDetailAdapter.notifyDataSetChanged();
			//mLayoutOrderCtrl.setVisibility(View.GONE);
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
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									for (MPOSOrderTransaction.MPOSOrderDetail order : selectedOrderLst) {
										sTransaction.cancelOrder(
												mTransactionId,
												order.getOrderDetailId());
									}
									loadOrder();
									//mLayoutOrderCtrl.setVisibility(View.GONE);
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
		for(MPOSOrderTransaction.MPOSOrderDetail order : sOrderDetailLst){
			if(order.isChecked())
				orderSelectedLst.add(order);
		}
		return orderSelectedLst;
	}

	/**
	 * @param productId
	 * @param productCode
	 * @param productName
	 * @param productTypeId
	 * @param vatType
	 * @param vatRate
	 * @param qty
	 * @param price
	 */
	private void addOrder(final int productId, final String productCode, final String productName, 
			final int productTypeId, final int vatType, final double vatRate, final double qty, double price){
		if(price > -1){
			sTransaction.addOrderDetail(mTransactionId, sComputer.getComputerId(), 
					productId, productCode, productName, productTypeId, vatType, vatRate, qty, price);
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
						sTransaction.addOrderDetail(mTransactionId, sComputer.getComputerId(), 
								productId, productCode, productName, productTypeId, vatType, vatRate, qty, openPrice);
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
		List<ProductsDao.Product> pSizeLst = sProducts.listProductSize(proId);
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
				ProductsDao.Product p = (ProductsDao.Product) parent.getItemAtPosition(position);
				addOrder(p.getProductId(), p.getProductCode(), p.getProductName(), 
						p.getProductTypeId(), p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
				dialog.dismiss();
			}
			
		});
		dialog.show();
	}

	/**
	 * count transaction that not send to server
	 */
	private static void countTransNotSend(Context context){
		if(mItemSendSale != null){
			int total = sTransaction.countTransNotSend();
			if(total > 0){
				mItemSendSale.setTitle(context.getString(R.string.send_sale_data) + "(" + total + ")");
			}else{
				mItemSendSale.setTitle(context.getString(R.string.send_sale_data));
			}
		}
	}

	/**
	 * count order that hold
	 */
	private static void countHoldOrder(Context context){
		if(mItemHoldBill != null){
			int totalHold = sTransaction.countHoldOrder(sSession.getSessionDate());
			if(totalHold > 0){
				mItemHoldBill.setTitle(context.getString(R.string.hold_bill) + "(" + totalHold + ")");
			}else{
				mItemHoldBill.setTitle(context.getString(R.string.hold_bill));
			}
		}
	}

	/**
	 * @param transactionId
	 * @param staffId
	 */
	private void printReceipt(int transactionId, int staffId){
		PrintReceiptLogDao printLog = 
				new PrintReceiptLogDao(getApplicationContext());
		printLog.insertLog(transactionId, staffId);
		
		new PrintReceipt(MainActivity.this, new PrintReceipt.PrintStatusListener() {
			
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
				sComputer.getComputerId(), mStaffId, new ProgressListener(){

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
	}
}
