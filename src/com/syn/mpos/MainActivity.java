package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.R;
import com.syn.mpos.database.MenuDept;
import com.syn.mpos.database.MenuItem;
import com.syn.mpos.database.Shop;
import com.syn.mpos.transaction.MPOSPayment;
import com.syn.mpos.transaction.MPOSSession;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.MenuGroups;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Setting;
import com.syn.pos.ShopData.ComputerProperty;
import com.syn.pos.ShopData.ShopProperty;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

public class MainActivity extends Activity implements OnMPOSFunctionClickListener {
	//private static final String TAG = "MPOSMainActivity";
	private Shop mShop;
	private Formatter mFormat;
	private MPOSSession mSession;
	private MPOSTransaction mTrans;
	private MPOSPayment mPayment;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private OrderListAdapter mOrderAdapter;
	private int mShopId;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private int mSessionId;
	private Context mContext;
	private Setting mSetting;
	private SharedPreferences mSharedPref;
	
	private List<MenuGroups.MenuDept> mMenuDeptLst;
	private List<MenuGroups.MenuItem> mMenuItemLst;
	private MenuAdapter mMenuItemAdapter;
	private int mMenuDeptId = -1;
	
	private TableRow mTbRowVat;
	private GridView mMenuGridView;
	private ListView mOrderListView;
	private TextView mTvSubTotal;
	private TextView mTvVatExclude;
	private TextView mTvDiscount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = MainActivity.this;
		
		mOrderListView = (ListView) findViewById(R.id.listViewOrder);
		mMenuGridView = (GridView) findViewById(R.id.gridViewMenu);
		mTvSubTotal = (TextView) findViewById(R.id.textViewSubTotal);
		mTvVatExclude = (TextView) findViewById(R.id.textViewVatExclude);
		mTvDiscount = (TextView) findViewById(R.id.textViewDiscount);
		mTbRowVat = (TableRow) findViewById(R.id.tbRowVat);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mSessionId = intent.getIntExtra("sessionId", 0);
		
		loadMenu();
	}
	
	private void loadMenu(){
		mMenuItemLst = new ArrayList<MenuGroups.MenuItem>();
		mMenuItemAdapter = new MenuAdapter();
		mMenuGridView.setAdapter(mMenuItemAdapter);

		createMenuDept();
	}
	
	public void init(){
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mSetting = new Setting();
		mSetting.setMenuImageUrl("http://" + mSharedPref.getString("pref_ipaddress", "") + "/" + 
				mSharedPref.getString("pref_webservice", "") + "/Resources/Shop/MenuImage/");
		
		mShop = new Shop(mContext);
		mFormat = new Formatter(mContext);
		mTrans = new MPOSTransaction(mContext);
		mPayment = new MPOSPayment(mContext);
		mSession = new MPOSSession(mContext);
		
		ShopProperty shopProp = mShop.getShopProperty();
		ComputerProperty compProp = mShop.getComputerProperty();
		
		mShopId = mShop.getShopProperty().getShopID();
		mComputerId = compProp.getComputerID();
		mTransactionId = mTrans.getCurrTransaction(compProp.getComputerID());
		if(mTransactionId == 0){
			mTransactionId = mTrans.openTransaction(compProp.getComputerID(), 
					shopProp.getShopID(), mSessionId, mStaffId);
		}
		
		//Log.i(TAG, "transactionId= " + transactionId);
		
		mOrderLst = mTrans.listAllOrders(mTransactionId, mComputerId);
		mOrderAdapter = new OrderListAdapter(mContext, mFormat, mOrderLst, new ListButtonOnClickListener(){
			OrderTransaction.OrderDetail order;
			float qty;
			@Override
			public void onMinusClick(int position) {
				order = mOrderLst.get(position);
				qty = order.getQty();
				if(--qty > 0){
					order.setQty(qty);
					mTrans.updateOrderDetail(mTransactionId, mComputerId, 
							order.getOrderDetailId(), order.getVatType(), 
							order.getQty(), order.getPricePerUnit());
				}
				
				mOrderAdapter.notifyDataSetChanged();
			}

			@Override
			public void onPlusClick(int position) {
				order = mOrderLst.get(position);
				qty = order.getQty();
				order.setQty(++qty);
				mTrans.updateOrderDetail(mTransactionId, mComputerId, 
						order.getOrderDetailId(), order.getVatType(), 
						order.getQty(), order.getPricePerUnit());
				
				mOrderAdapter.notifyDataSetChanged();
			}
			
		}, new AdapterStateListener(){

			@Override
			public void onNotify() {
				summary();
			}
			
		});
		
		// set on order click
		mOrderListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position,
					long id) {
				final OrderTransaction.OrderDetail orderDetail = 
						(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);
				
				PopupMenu popup = new PopupMenu(mContext, v);
				popup.getMenuInflater().inflate(R.menu.action_order_function,
						popup.getMenu());

				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(android.view.MenuItem item) {
						switch(item.getItemId()){
						case R.id.itemOrderDelete:
							if(mTrans.deleteOrderDetail(mTransactionId, mComputerId, 
								orderDetail.getOrderDetailId())){
								mOrderLst.remove(position);
								mOrderAdapter.notifyDataSetChanged();
							}
							return true;
						}
						return false;
					}
				});

				popup.show();
			}
		});
		
		summary();
		mOrderListView.setAdapter(mOrderAdapter);
		mOrderListView.setSelection(mOrderAdapter.getCount());
	}
	
	public void paymentClicked(final View v){
		Intent intent = new Intent(mContext, PaymentActivity.class);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		intent.putExtra("staffId", mStaffId);
		startActivity(intent);
	}
	
	public void discountClicked(final View v){
		Intent intent = new Intent(mContext, DiscountActivity.class);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}
	
	// menu catgory
	protected void createMenuDept(){
		MenuDept menuDept = new MenuDept(mContext);
		mMenuDeptLst = menuDept.listMenuDept();
		
		if(mMenuDeptLst.size() > 0){
			
			LayoutInflater inflater = 
					LayoutInflater.from(mContext);
			int i = 0;
			for(final MenuGroups.MenuDept md : mMenuDeptLst){
				final View v = inflater.inflate(R.layout.menu_catgory_tempate, null);
				final Button btnCat = (Button) v.findViewById(R.id.button1);
				btnCat.setId(md.getMenuDeptID());
				
				btnCat.setText(md.getMenuDeptName_0());
				
				btnCat.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// display menu
						btnCat.setSelected(true);
						if(mMenuDeptId != -1 && mMenuDeptId != v.getId()){
							Button lastBtn = (Button)findViewById(mMenuDeptId);
							lastBtn.setSelected(false);
						}
						// load menu
						MenuItem mi = new MenuItem(mContext);
						mMenuItemLst = mi.listMenuItem(md.getMenuDeptID(), 1);
						mMenuItemAdapter.notifyDataSetChanged();
						
						mMenuDeptId = v.getId();
					}
					
				});

				//add to viewgroup
				ViewGroup menuCatLayout = (ViewGroup) findViewById(R.id.MenuCatLayout);
				LayoutParams layoutParam = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
						1f);

				if(i == 0)
					btnCat.callOnClick();
		
				menuCatLayout.addView(v, layoutParam);
				
				i++;
			}
		}
	}
	
	// menu item adapter
	private class MenuAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		private ImageLoader imgLoader;
		public MenuAdapter(){
			inflater = LayoutInflater.from(mContext);
			imgLoader = new ImageLoader(mContext, R.drawable.no_food, 
					"mpos_img");
			
		}
		
		@Override
		public int getCount() {
			return mMenuItemLst.size();
		}

		@Override
		public MenuGroups.MenuItem getItem(int position) {
			return mMenuItemLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MenuGroups.MenuItem mi = mMenuItemLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.menu_template, null);
				holder = new ViewHolder();
				holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
				holder.tvMenuName = (TextView) convertView.findViewById(R.id.textViewMenuName);
				holder.tvMenuPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			imgLoader.displayImage(mSetting.getMenuImageUrl() + mi.getMenuImageLink(), holder.imgMenu);
			holder.tvMenuName.setText(mi.getMenuName_0());
			
			if(mi.getProductPricePerUnit() >= 0)
				holder.tvMenuPrice.setText(mFormat.currencyFormat(mi.getProductPricePerUnit()));
			else
				holder.tvMenuPrice.setText("N/A");
			
			convertView.setOnLongClickListener(new OnLongClickListener(){
				
				@Override
				public boolean onLongClick(View v) {
					final LayoutInflater inflater = LayoutInflater.from(mContext);
					View menuDetailView = inflater.inflate(R.layout.menu_detail_layout, null);
					ImageView imvMenuDetail = (ImageView) menuDetailView.findViewById(R.id.imageViewMenuDetail);
					ImageLoader imgLoader2 = new ImageLoader(mContext, 
							R.drawable.no_food, "mpos_img", ImageLoader.IMAGE_SIZE.LARGE);
					imgLoader2.displayImage(mSetting.getMenuImageUrl() + mi.getMenuImageLink(), imvMenuDetail);
					
					final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
					dialog.getWindow().setGravity(Gravity.LEFT); 
					dialog.setContentView(menuDetailView);
					dialog.show();
					return true;
				}
				
			});
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int orderDetailId = mTrans.addOrderDetail(mTransactionId, 
							mComputerId, mi.getProductID(), mi.getProductTypeID(), 
							mi.getVatType(), mi.getMenuName_0(), 1, 
							mi.getProductPricePerUnit());
					
					OrderTransaction.OrderDetail order = 
							mTrans.getOrder(mTransactionId, mComputerId, orderDetailId);
					mOrderLst.add(order);
					
					summary();
					
					mOrderAdapter.notifyDataSetChanged();
					mOrderListView.smoothScrollToPosition(mOrderAdapter.getCount());
				}
				
			});
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgMenu;
			TextView tvMenuName;
			TextView tvMenuPrice;
		}
	}
	
	public void summary(){
		OrderTransaction.OrderDetail orderDetail
			= mTrans.getSummary(mTransactionId, mComputerId);
		
		float subTotal = orderDetail.getTotalRetailPrice();
		float vat = orderDetail.getVat();	// vat exclude
		float totalSalePrice = orderDetail.getTotalSalePrice() + vat;
		float totalDiscount = orderDetail.getPriceDiscount() + orderDetail.getMemberDiscount();
		
		// update trans vat
		mTrans.updateTransactionVat(mTransactionId, mComputerId, 
				totalSalePrice, vat);
		
		if(vat > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		
		mTvVatExclude.setText(mFormat.currencyFormat(vat));
		mTvSubTotal.setText(mFormat.currencyFormat(subTotal));
		mTvDiscount.setText(mFormat.currencyFormat(totalDiscount));
		setTitle(mFormat.currencyFormat(totalSalePrice));
	}
	
	public void clearBillClicked(final View v){
		new AlertDialog.Builder(mContext)
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
				mTrans.deleteTransaction(mTransactionId, mComputerId);
				mTrans.deleteAllOrderDetail(mTransactionId, mComputerId);
				mPayment.deleteAllPaymentDetail(mTransactionId, mComputerId);

				init();
			}
		})
		.show();
	}

	public void updateOrderQty() {
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		param.setMargins(8, 0, 8, 0);
		
		LinearLayout layout = new LinearLayout(mContext);
		final EditText txtHoldRemark = new EditText(mContext);
		txtHoldRemark.setGravity(Gravity.TOP);
		txtHoldRemark.setLayoutParams(param);
		layout.addView(txtHoldRemark);
		
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.hold_bill)
		.setView(layout)
		
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mTrans.holdTransaction(mTransactionId, mComputerId, 
						txtHoldRemark.getText().toString())){
					init();
				}
			}
		})
		.show();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_main_function, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.itemHoldBill:
			onHoldBillClick(item.getActionView());
			return true;
		case R.id.itemSwUser:
			onSwitchUserClick(item.getActionView());
			return true;
		case R.id.itemLogout:
			onLogoutClick(item.getActionView());
			return true;
		case R.id.itemReceive:
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
		case R.id.itemSaleReport:
			intent = new Intent(MainActivity.this, SaleReportActivity.class);
			intent.putExtra("mode", 1);
			startActivity(intent);
			return true;
		case R.id.itemSaleByProduct:
			intent = new Intent(MainActivity.this, SaleReportActivity.class);
			intent.putExtra("mode", 2);
			startActivity(intent);
			return true;
		case R.id.itemVoidBill:
			intent = new Intent(MainActivity.this, VoidBillActivity.class);
			intent.putExtra("shopId", mShopId);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
			return true;
		case R.id.itemCloseShift:
			mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 0);
			return true;
		case R.id.itemEndday:
			mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onHoldBillClick(View v) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		TableLayout tbHold = (TableLayout) holdBillView.findViewById(R.id.tbHoldBill);
	
		final Dialog d = new Dialog(mContext);
		d.setTitle(R.string.hold_bill);
		d.setContentView(holdBillView);
		
		List<OrderTransaction> billLst = mTrans.listHoldOrder(mComputerId);
		
		Calendar c = Calendar.getInstance();
	
		for(int i = 0; i < billLst.size(); i++){
			final OrderTransaction trans = billLst.get(i);
					
			View template = inflater.inflate(R.layout.hold_bill_template, null);
			TextView tvNo = (TextView) template.findViewById(R.id.tvNo);
			TextView tvOpenTime = (TextView) template.findViewById(R.id.tvOpenTime);
			TextView tvOpenStaff = (TextView) template.findViewById(R.id.tvOpenStaff);
			TextView tvRemark = (TextView) template.findViewById(R.id.tvRemark);
			
			tvNo.setText((i + 1) + ".");
			c.setTimeInMillis(trans.getOpenTime());
			tvOpenTime.setText(mFormat.dateTimeFormat(c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getRemark());
			
			
			template.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(mTrans.prepareTransaction(trans.getTransactionId(), 
							trans.getComputerId())){
						mOrderLst = mTrans.listAllOrders(trans.getTransactionId(), 
								trans.getComputerId());
						
						mOrderAdapter.notifyDataSetChanged();
						d.dismiss();
					}
				}
				
			});
			
			tbHold.addView(template);
		}
		d.show();
	}

	@Override
	public void onSwitchUserClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogoutClick(View v) {
		finish();
	}
	
	public void setMemberClicked(final View v){
		Intent intent = new Intent(mContext, AddMemberActivity.class);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("mode", "search");
		startActivity(intent);	
	}
	
	public void newMemberClicked(final View v){
		Intent intent = new Intent(mContext, AddMemberActivity.class);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("mode", "add");
		startActivity(intent);
	}
}
