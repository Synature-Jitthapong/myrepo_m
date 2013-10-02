package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.R;
import com.syn.mpos.database.MenuDept;
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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
	private TextView mTvTotalPrice;
	private Button mBtnDiscount;
	private Button mBtnCash;
	private Button mBtnHold;
	private MenuItem mItemHoldBill;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = MainActivity.this;
		
		mTvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
		mOrderListView = (ListView) findViewById(R.id.listViewOrder);
		mMenuGridView = (GridView) findViewById(R.id.gridViewMenu);
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
		countHoldOrder();
		loadOrder();
	}
	
	private void loadOrder(){
		mOrderLst = mTrans.listAllOrders(mTransactionId, mComputerId);
		mOrderAdapter = new OrderListAdapter();
		mOrderListView.setAdapter(mOrderAdapter);
		mOrderAdapter.notifyDataSetChanged();
		mOrderListView.setSelection(mOrderAdapter.getCount());
	}
	
	private class OrderListAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public OrderListAdapter (){
			inflater = LayoutInflater.from(mContext);
		}

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
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.order_list_template, null);
				holder = new ViewHolder();
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
				holder.tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
				holder.txtOrderAmount = (EditText) convertView.findViewById(R.id.editTextOrderAmount);
				holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.textViewOrderPrice);
				holder.btnMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvOrderNo.setText(Integer.toString(position + 1));
			holder.tvOrderName.setText(orderDetail.getProductName());
			holder.txtOrderAmount.setText(mFormat.qtyFormat(orderDetail.getQty()));
			holder.tvOrderPrice.setText(mFormat.currencyFormat(orderDetail.getPricePerUnit()));
			
			holder.btnMinus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					float qty = orderDetail.getQty();
					
					if(--qty > 0){
						orderDetail.setQty(qty);
						
						mTrans.updateOrderDetail(mTransactionId, mComputerId, 
								orderDetail.getOrderDetailId(), orderDetail.getVatType(), 
								qty, orderDetail.getPricePerUnit());
					}else{
						new AlertDialog.Builder(mContext)
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
								mTrans.deleteOrderDetail(mTransactionId, mComputerId, orderDetail.getOrderDetailId());
								mOrderLst.remove(position);
								mOrderAdapter.notifyDataSetChanged();
							}
						}).show();
					}
					
					mOrderAdapter.notifyDataSetChanged();
				}
				
			});
			
			holder.btnPlus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					float qty = orderDetail.getQty();
					orderDetail.setQty(++qty);
					
					mTrans.updateOrderDetail(mTransactionId, mComputerId, 
							orderDetail.getOrderDetailId(), orderDetail.getVatType(), 
							orderDetail.getQty(), orderDetail.getPricePerUnit());
					
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

		private class ViewHolder{
			TextView tvOrderNo;
			TextView tvOrderName;
			EditText txtOrderAmount;
			TextView tvOrderPrice;
			Button btnMinus;
			Button btnPlus;
		}
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
						com.syn.mpos.database.MenuItem mi = new com.syn.mpos.database.MenuItem(mContext);
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

		private void addOrder(int productId, int productType, int vatType,
				String menuName, float qty, float pricePerUnit){
			int orderDetailId = mTrans.addOrderDetail(mTransactionId, 
					mComputerId, productId, productType, 
					vatType, menuName, qty, pricePerUnit);
			
			OrderTransaction.OrderDetail order = 
					mTrans.getOrder(mTransactionId, mComputerId, orderDetailId);
			mOrderLst.add(order);

			mOrderAdapter.notifyDataSetChanged();
			mOrderListView.smoothScrollToPosition(mOrderAdapter.getCount());
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
					// open price
					if(mi.getProductPricePerUnit() == -1){
						final EditText txtPrice = new EditText(mContext);
						txtPrice.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | 
								InputType.TYPE_NUMBER_FLAG_SIGNED);
						new AlertDialog.Builder(mContext)
						.setTitle(mi.getMenuName_0())
						.setMessage(R.string.enter_price)
						.setView(txtPrice)
						.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String price = txtPrice.getText().toString();
								if(!price.isEmpty()){
									try {
										addOrder(mi.getProductID(), mi.getProductTypeID(),
												mi.getVatType(), mi.getMenuName_0(), 1,
												Float.parseFloat(price));
									} catch (NumberFormatException e) {
										new AlertDialog.Builder(mContext)
										.setTitle(R.string.error)
										.setMessage(R.string.enter_price)
										.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												hideKeyboard();
											}
										})
										.show();
									}
									hideKeyboard();
								}else{
									new AlertDialog.Builder(mContext)
									.setTitle(R.string.open_price)
									.setMessage(R.string.enter_price)
									.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											hideKeyboard();
										}
									})
									.show();
								}
							}
						})
						.show();
					}else{
						addOrder(mi.getProductID(), mi.getProductTypeID(),
								mi.getVatType(), mi.getMenuName_0(), 1,
								mi.getProductPricePerUnit());
					}
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
		mTvTotalPrice.setText(mFormat.currencyFormat(totalSalePrice));
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
		mItemHoldBill = menu.findItem(R.id.itemHoldBill);
		countHoldOrder();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Intent intent;
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
			new AlertDialog.Builder(mContext)
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
					if(mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 0)){
						finish();
					}
				}
			}).show();
			return true;
		case R.id.itemEndday:
			new AlertDialog.Builder(mContext)
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
					if(mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 1)){
						finish();
					}
				}
			}).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void holdOrderClicked(final View v){
		final EditText txtRemark = new EditText(mContext);
		txtRemark.setHint(R.string.remark);
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.hold)
		.setView(txtRemark)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				hideKeyboard();
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String remark = txtRemark.getText().toString();

				if(mTrans.holdTransaction(mTransactionId, mComputerId, remark)){	
					init();
				}
				
				hideKeyboard();
			}
		}).show();
	}
	
	private void countHoldOrder(){
		int totalHold = mTrans.countHoldOrder(mComputerId);

		if(totalHold > 0){
			TextView tv = new TextView(mContext);
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
	
	@Override
	public void onHoldBillClick(View v) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		ListView lvHoldBill = (ListView) holdBillView.findViewById(R.id.listView1);
		List<OrderTransaction> billLst = mTrans.listHoldOrder(mComputerId);
		HoldBillAdapter billAdapter = new HoldBillAdapter(billLst);
		lvHoldBill.setAdapter(billAdapter);
		lvHoldBill.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				
				OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
				if (mOrderLst.size() == 0) {
					mTransactionId = trans.getTransactionId();
					mComputerId = trans.getComputerId();
				}
			}
			
		});
		
		new AlertDialog.Builder(mContext)
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
					new AlertDialog.Builder(mContext)
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
					mTrans.prepareTransaction(mTransactionId, mComputerId);
					
					loadOrder();
				}
			}
		}).show();
		
	}

	private class HoldBillAdapter extends BaseAdapter{
		
		LayoutInflater inflater;
		List<OrderTransaction> transLst;
		Calendar c;
		
		public HoldBillAdapter(List<OrderTransaction> transLst){
			inflater = LayoutInflater.from(mContext);
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
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.hold_bill_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvOpenTime = (TextView) convertView.findViewById(R.id.tvOpenTime);
				holder.tvOpenStaff = (TextView) convertView.findViewById(R.id.tvOpenStaff);
				holder.tvRemark = (TextView) convertView.findViewById(R.id.tvRemark);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			c.setTimeInMillis(trans.getOpenTime());
			holder.tvNo.setText(Integer.toString(position + 1) + ".");
			holder.tvOpenTime.setText(mFormat.dateTimeFormat(c.getTime()));
			holder.tvOpenStaff.setText(trans.getStaffName());
			holder.tvRemark.setText(trans.getRemark());
			
			if(position % 2 == 0)
			{
				convertView.setBackgroundResource(R.color.smoke_white);
			}else{
				convertView.setBackgroundResource(R.color.light_gray);
			}
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvNo;
			TextView tvOpenTime;
			TextView tvOpenStaff;
			TextView tvRemark;
		}
	}
	
	@Override
	public void onSwitchUserClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogoutClick(View v) {
		new AlertDialog.Builder(mContext)
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
	
	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
