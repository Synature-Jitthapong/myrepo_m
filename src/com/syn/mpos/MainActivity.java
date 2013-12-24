package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.syn.mpos.R;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.Products;
import com.syn.pos.OrderTransaction;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends FragmentActivity implements MenuPageFragment.OnMenuItemClick{
	public static final int SYNC_REQUEST_CODE = 1;
	
	private int mTransactionId;
	private int mComputerId;
	private int mSessionId;
	private int mStaffId;
	private int mShopId;
	private List<Products.ProductDept> mProductDeptLst;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private List<OrderTransaction.OrderDetail> mOrderSelLst;
	private OrderListAdapter mOrderAdapter;
	private PagerSlidingTabStrip mTabs;
	private ViewPager mPager;
	private MenuItemPagerAdapter mPageAdapter;
	private TableRow mTbRowVat;
	private ListView mOrderListView;
	private TextView mTvSubTotal;
	private TextView mTvVatExclude;
	private TextView mTvDiscount;
	private TextView mTvTotalPrice;
	private Button mBtnDiscount;
	private Button mBtnCash;
	private Button mBtnHold;
	private MenuItem mItemHoldBill;
	private LinearLayout mLayoutOrderCtrl;
	private ImageButton mBtnDelSelOrder;
	private ImageButton mBtnClearSelOrder;
	private TextView mTvOrderSelected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLayoutOrderCtrl = (LinearLayout) findViewById(R.id.layoutOrderCtrl);
		mBtnDelSelOrder = (ImageButton) findViewById(R.id.btnDelOrder);
		mBtnClearSelOrder = (ImageButton) findViewById(R.id.btnClearSelOrder);
		mTvOrderSelected = (TextView) findViewById(R.id.tvOrderSelected);
		mTvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
		mOrderListView = (ListView) findViewById(R.id.listViewOrder);
		mTvSubTotal = (TextView) findViewById(R.id.textViewSubTotal);
		mTvVatExclude = (TextView) findViewById(R.id.textViewVatExclude);
		mTvDiscount = (TextView) findViewById(R.id.textViewDiscount);
		mTbRowVat = (TableRow) findViewById(R.id.tbRowVat);
		mBtnDiscount = (Button) findViewById(R.id.buttonDiscount);
		mBtnCash = (Button) findViewById(R.id.buttonCash);
		mBtnHold = (Button) findViewById(R.id.buttonHold);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mSessionId = intent.getIntExtra("sessionId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mPager = (ViewPager) findViewById(R.id.pager);
		setupMenuItemPager();
	}
	
	public void init(){
		mTransactionId = GlobalVar.sTransaction.getCurrTransaction(mComputerId);
		if(mTransactionId == 0)
			mTransactionId = GlobalVar.sTransaction.openTransaction(mComputerId, mShopId, mSessionId, mStaffId);
		
		setupOrderListView();
		countHoldOrder();
		loadOrder();
	}
	
	private void setupMenuItemPager(){
		mProductDeptLst = GlobalVar.sProduct.listProductDept();
		if (mProductDeptLst.size() > 0) {
			mPageAdapter = new MenuItemPagerAdapter(getSupportFragmentManager());
			mPager.setAdapter(mPageAdapter);
			
			final int pageMargin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
							.getDisplayMetrics());
			mPager.setPageMargin(pageMargin);
			mTabs.setViewPager(mPager);
			mTabs.setIndicatorColor(0xFF1D78B2);
		}else{
	
		}
	}
	
	private void setupOrderListView(){
		mOrderSelLst = new ArrayList<OrderTransaction.OrderDetail>();
		
		if(mOrderSelLst.size() == 0){
			mLayoutOrderCtrl.setVisibility(View.GONE);
		}
		
		mOrderListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				OrderTransaction.OrderDetail order = 
						(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);

				if(mOrderSelLst.size() > 0){
					if (mOrderSelLst.contains(order)){ 
						mOrderSelLst.remove(order);
						mOrderLst.get(position).setChecked(false);
					}
					else{ 
						mOrderSelLst.add(order);
						mOrderLst.get(position).setChecked(true);
					}
				}else{
					mOrderSelLst.add(order);
					mOrderLst.get(position).setChecked(true);
				}
				
				if(mOrderSelLst.size() > 0){
					if(mLayoutOrderCtrl.getVisibility() == View.GONE) 
						mLayoutOrderCtrl.setVisibility(View.VISIBLE);
				}
				else if(mOrderSelLst.size() == 0){
					mLayoutOrderCtrl.setVisibility(View.GONE);
				}
				
				mTvOrderSelected.setText("selected(" + mOrderSelLst.size() + ")");
				mOrderAdapter.notifyDataSetChanged();
			}
		});
		
		mBtnDelSelOrder.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				TextView tvMsg = new TextView(MainActivity.this);
				tvMsg.setText(R.string.confirm_delete);
				tvMsg.append(" (" + mOrderSelLst.size() + ") ?");
				
				new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.delete)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(tvMsg.getText())
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (OrderTransaction.OrderDetail order : mOrderSelLst) {
							GlobalVar.sTransaction.deleteOrderDetail(mTransactionId, mComputerId, order.getOrderDetailId());
						}
						loadOrder();
						mOrderSelLst = new ArrayList<OrderTransaction.OrderDetail>();
						mLayoutOrderCtrl.setVisibility(View.GONE);
					}
				}).show();
			}
			
		});
		
		mBtnClearSelOrder.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				for(int i = 0; i < mOrderLst.size(); i++){
					mOrderLst.get(i).setChecked(false);
				}
				mOrderSelLst = new ArrayList<OrderTransaction.OrderDetail>();
				mLayoutOrderCtrl.setVisibility(View.GONE);
				mOrderAdapter.notifyDataSetChanged();
			}
			
		});
	}
	
	private void loadOrder(){
		mOrderLst = GlobalVar.sTransaction.listAllOrder(mTransactionId, mComputerId);
		mOrderAdapter = new OrderListAdapter();
		mOrderListView.setAdapter(mOrderAdapter);
		mOrderAdapter.notifyDataSetChanged();
		mOrderListView.setSelection(mOrderAdapter.getCount());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	public void summary(){
		float subTotal = GlobalVar.sTransaction.getTotalRetailPrice(mTransactionId, mComputerId, false);
		float totalVatExclude = GlobalVar.sTransaction.getTotalVatExclude(mTransactionId, mComputerId, false);
		float totalDiscount = GlobalVar.sTransaction.getPriceDiscount(mTransactionId, mComputerId, false);
		float totalPrice = GlobalVar.sTransaction.getTotalSalePrice(mTransactionId, mComputerId, false);
		
		GlobalVar.sTransaction.updateTransactionVat(mTransactionId, mComputerId, totalPrice, 
				totalVatExclude, GlobalVar.sShop.getShopProperty().getCompanyVat());
		
		if(totalVatExclude > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		
		mTvVatExclude.setText(MPOSApplication.sGlobalVar.currencyFormat(totalVatExclude));
		mTvSubTotal.setText(MPOSApplication.sGlobalVar.currencyFormat(subTotal));
		mTvDiscount.setText(MPOSApplication.sGlobalVar.currencyFormat(totalDiscount));
		mTvTotalPrice.setText(MPOSApplication.sGlobalVar.currencyFormat(totalPrice + totalVatExclude));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mItemHoldBill = menu.findItem(R.id.itemHoldBill);
		
		countHoldOrder();
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
		case R.id.itemDirectReceive:
			intent = new Intent(MainActivity.this, DirectReceiveActivity.class);
			intent.putExtra("shopId", mShopId);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
			return true;
		case R.id.itemStockCount:
			intent = new Intent(MainActivity.this, StockCountActivity.class);
			intent.putExtra("shopId", mShopId);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
			return true;
		case R.id.itemStockCard:
			intent = new Intent(MainActivity.this, StockCardActivity.class);
			intent.putExtra("shopId", mShopId);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
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
		case R.id.itemSetting:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode){
		case SYNC_REQUEST_CODE:
			setupMenuItemPager();
			break;
		}
	}

	void sync(){
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		intent.putExtra("settingPosition", 1);
		this.startActivityForResult(intent, SYNC_REQUEST_CODE);
	}

	private void countHoldOrder(){
		int totalHold = GlobalVar.sTransaction.countHoldOrder(mComputerId);
	
		if(totalHold > 0){
			TextView tv = new TextView(MainActivity.this);
			tv.setText(R.string.hold_bill);
			tv.append("(" + totalHold + ")");
			try {
				mItemHoldBill.setTitle(tv.getText().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void paymentClicked(final View v){
		Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		intent.putExtra("staffId", mStaffId);
		startActivity(intent);
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

	private class OrderListAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return mOrderLst != null ? mOrderLst.size() : 0;
		}
	
		@Override
		public OrderTransaction.OrderDetail getItem(int position) {
			return mOrderLst.get(position);
		}
	
		@Override
		public long getItemId(int position) {
			return position;
		}
	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final OrderTransaction.OrderDetail orderDetail = 
					mOrderLst.get(position);
	
			LayoutInflater inflater = (LayoutInflater) 
				MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.order_list_template, null);
			CheckBox chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
			TextView tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
			TextView tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
			final EditText txtOrderAmount = (EditText) convertView.findViewById(R.id.editTextOrderAmount);
			TextView tvOrderPrice = (TextView) convertView.findViewById(R.id.textViewOrderPrice);
			Button btnMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
			Button btnPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
		
			chk.setChecked(orderDetail.isChecked());
			tvOrderNo.setText(Integer.toString(position + 1) + ".");
			tvOrderName.setText(orderDetail.getProductName());
			txtOrderAmount.setText(MPOSApplication.sGlobalVar.qtyFormat(orderDetail.getQty()));
			tvOrderPrice.setText(MPOSApplication.sGlobalVar.currencyFormat(orderDetail.getPricePerUnit()));
			
			if(orderDetail.isChecked())
				chk.setVisibility(View.VISIBLE);
			else
				chk.setVisibility(View.GONE);
			
			btnMinus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					float qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						
						GlobalVar.sTransaction.updateOrderDetail(mTransactionId, mComputerId, 
								orderDetail.getOrderDetailId(), 
								GlobalVar.sProduct.getVatRate(orderDetail.getProductId()), 
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
								GlobalVar.sTransaction.deleteOrderDetail(mTransactionId, mComputerId, 
										orderDetail.getOrderDetailId());
								mOrderLst.remove(position);
								mOrderAdapter.notifyDataSetChanged();
							}
						}).show();
					}
					
					mOrderAdapter.notifyDataSetChanged();
				}
				
			});
			
			btnPlus.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					float qty = orderDetail.getQty();
					orderDetail.setQty(++qty);
					
					GlobalVar.sTransaction.updateOrderDetail(mTransactionId, mComputerId, 
							orderDetail.getOrderDetailId(), 
							GlobalVar.sProduct.getVatRate(orderDetail.getProductId()), 
							qty, orderDetail.getPricePerUnit());
					
					mOrderAdapter.notifyDataSetChanged();
				}
				
			});
			
			return convertView;
		}
		
		@Override
		public void notifyDataSetChanged() {
			if(mOrderLst.size() == 0){
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

			c.setTimeInMillis(trans.getOpenTime());
			tvNo.setText(Integer.toString(position + 1) + ".");
			tvOpenTime.setText(MPOSApplication.sGlobalVar.dateTimeFormat(c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getRemark());

			return convertView;
		}
	}
	
	public class MenuItemPagerAdapter extends FragmentStatePagerAdapter{
	
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
		if(GlobalVar.sTransaction.deleteOrderDetail(mTransactionId, mComputerId)){
			GlobalVar.sTransaction.deleteTransaction(mTransactionId, mComputerId);
			GlobalVar.sTransaction.cancelDiscount(mTransactionId, mComputerId);
			
			init();
		}
	}
	
	private void voidBill(){
		Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
		intent.putExtra("staffId", mStaffId);
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
				
			}
		}).show();
	}

	public void holdOrderClicked(final View v){
		LayoutInflater inflater = (LayoutInflater)
				MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View inputDialog = inflater.inflate(R.layout.input_text_dialog, null);
		final EditText txtRemark = (EditText) inputDialog.findViewById(R.id.editText1);
		txtRemark.setHint(R.string.remark);
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.hold)
		.setView(inputDialog)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String note = txtRemark.getText().toString();
				GlobalVar.sTransaction.holdTransaction(mTransactionId, mComputerId, note);
				
				init();
			}
		}).show();
	}

	public void holdBill() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<OrderTransaction> billLst = GlobalVar.sTransaction.listHoldOrder(mComputerId);
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				if (mOrderLst.size() == 0) {
					mTransactionId = trans.getTransactionId();
				}
			}
			
		});
		
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.hold_bill)
		.setView(holdBillView)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				init();
			}
		}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mOrderLst.size() > 0){
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
					GlobalVar.sTransaction.prepareTransaction(mTransactionId, mComputerId);
					loadOrder();
				}
			}
		}).show();
		
	}

	public void switchUser() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View swUserView = inflater.inflate(R.layout.switch_user_popup, null);
		final EditText txtUser = (EditText) swUserView.findViewById(R.id.txtUser);
		final EditText txtPassword = (EditText) swUserView.findViewById(R.id.txtPassword);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.switch_user);
		builder.setView(swUserView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String user = "";
				String pass = "";
			
				if(!txtUser.getText().toString().isEmpty()){
					user = txtUser.getText().toString();
					
					if(!txtPassword.getText().toString().isEmpty()){
						pass = txtPassword.getText().toString();
						Login login = new Login(MainActivity.this, user, pass);
						
						if(login.checkUser()){
							ShopData.Staff s = login.checkLogin();
							
							if(s != null){
								mStaffId = s.getStaffID();
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
		
		final AlertDialog d = builder.create();	
		d.show();
	}

	public void logout() {
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.logout)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(R.string.confirm_logout)
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

	@Override
	public void onClick(int productId, int productTypeId, int vatType, float vatRate, float productPrice) {
		int orderId = GlobalVar.sTransaction.addOrderDetail(mTransactionId, mComputerId, productId, 
				productTypeId, vatType, vatRate, 1, productPrice);
		
		mOrderLst.add(GlobalVar.sTransaction.getOrder(mTransactionId, mComputerId, orderId));
		mOrderAdapter.notifyDataSetChanged();
		mOrderListView.smoothScrollToPosition(mOrderLst.size());
	}
}
