package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.R;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.MenuDept;
import com.syn.mpos.database.Shop;
import com.syn.mpos.transaction.MPOSPayment;
import com.syn.mpos.transaction.MPOSSession;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.MenuGroups;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Setting;
import com.syn.pos.ShopData;
import com.syn.pos.ShopData.ComputerProperty;
import com.syn.pos.ShopData.ShopProperty;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
	private List<OrderTransaction.OrderDetail> mOrderSelLst;
	private OrderListAdapter mOrderAdapter;
	private int mShopId;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private int mSessionId;
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
	private RelativeLayout mLayoutOrderCtrl;
	private ImageButton mBtnDelSelOrder;
	private ImageButton mBtnClearSelOrder;
	private TextView mTvOrderSelected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		mLayoutOrderCtrl = (RelativeLayout) findViewById(R.id.layoutOrderCtrl);
		mBtnDelSelOrder = (ImageButton) findViewById(R.id.btnDelOrder);
		mBtnClearSelOrder = (ImageButton) findViewById(R.id.btnClearSelOrder);
		mTvOrderSelected = (TextView) findViewById(R.id.tvOrderSelected);
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
		if(mStaffId == 0 || mSessionId == 0){
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.error)
			.setMessage("staffId=" + mStaffId + ", sessionId=" + mSessionId)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
		}
		
		createMenu();
	}
	
	public void init(){
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mSetting = new Setting();
		mSetting.setMenuImageUrl("http://" + mSharedPref.getString("pref_ipaddress", "") + "/" + 
				mSharedPref.getString("pref_webservice", "") + "/Resources/Shop/MenuImage/");
		
		mShop = new Shop(MainActivity.this);
		mFormat = new Formatter(MainActivity.this);
		mTrans = new MPOSTransaction(MainActivity.this);
		mPayment = new MPOSPayment(MainActivity.this);
		mSession = new MPOSSession(MainActivity.this);
		
		ShopProperty shopProp = mShop.getShopProperty();
		ComputerProperty compProp = mShop.getComputerProperty();
		
		mShopId = mShop.getShopProperty().getShopID();
		mComputerId = compProp.getComputerID();
		mTransactionId = mTrans.getCurrTransaction(compProp.getComputerID());
		if(mTransactionId == 0){
			mTransactionId = mTrans.openTransaction(compProp.getComputerID(), 
					shopProp.getShopID(), mSessionId, mStaffId);
		}
	
		registerOrderListEvent();
		countHoldOrder();
		loadOrder();
	}

	private void createMenu(){
		mMenuItemLst = new ArrayList<MenuGroups.MenuItem>();
		mMenuItemAdapter = new MenuAdapter();
		mMenuGridView.setAdapter(mMenuItemAdapter);

		createMenuDept();
	}
	
	private void registerOrderListEvent(){
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
							mTrans.deleteOrderDetail(
									mTransactionId,
									mComputerId,
									order.getOrderDetailId());
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
		mOrderLst = mTrans.listAllOrders(mTransactionId, mComputerId);
		mOrderAdapter = new OrderListAdapter();
		mOrderListView.setAdapter(mOrderAdapter);
		mOrderAdapter.notifyDataSetChanged();
		mOrderListView.setSelection(mOrderAdapter.getCount());
	}
	
	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	// menu catgory
	private void createMenuDept(){
		MenuDept menuDept = new MenuDept(MainActivity.this);
		mMenuDeptLst = menuDept.listMenuDept();
		
		if(mMenuDeptLst.size() > 0){
			
			LayoutInflater inflater = 
					LayoutInflater.from(MainActivity.this);
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
						com.syn.mpos.database.MenuItem mi = new com.syn.mpos.database.MenuItem(MainActivity.this);
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
				mTrans.deleteTransaction(mTransactionId, mComputerId);
				mTrans.deleteOrderDetail(mTransactionId, mComputerId);
				mPayment.deleteAllPaymentDetail(mTransactionId, mComputerId);
	
				init();
			}
		})
		.show();
	}

	//	public void updateOrderQty() {
	//		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	//		param.setMargins(8, 0, 8, 0);
	//		
	//		LinearLayout layout = new LinearLayout(MainActivity.this);
	//		final EditText txtHoldRemark = new EditText(MainActivity.this);
	//		txtHoldRemark.setGravity(Gravity.TOP);
	//		txtHoldRemark.setLayoutParams(param);
	//		layout.addView(txtHoldRemark);
	//		
	//		new AlertDialog.Builder(MainActivity.this)
	//		.setTitle(R.string.hold_bill)
	//		.setView(layout)
	//		
	//		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	//			
	//			@Override
	//			public void onClick(DialogInterface dialog, int which) {
	//				
	//			}
	//		})
	//		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	//			
	//			@Override
	//			public void onClick(DialogInterface dialog, int which) {
	//				if(mTrans.holdTransaction(mTransactionId, mComputerId, 
	//						txtHoldRemark.getText().toString())){
	//					init();
	//				}
	//			}
	//		})
	//		.show();
	//	}
	
	//	private void getStaffInfo(){
	//		Shop p = new Shop(MainActivity.this);
	//		ShopData.Staff s = p.getStaff(mStaffId);
	//		if(s != null){
	//			TextView tvLogout = new TextView(MainActivity.this);
	//			tvLogout.setText(R.string.logout);
	//			tvLogout.append("\n" + s.getStaffName());
	//			///mItemLogout.setTitle(tvLogout.getText());
	//		}
	//	}
		
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
		case R.id.itemSaleByBill:
			intent = new Intent(MainActivity.this, SaleReportActivity.class);
			intent.putExtra("mode", 1);
			startActivity(intent);
			return true;
		case R.id.itemSaleByProduct:
			intent = new Intent(MainActivity.this, SaleReportActivity.class);
			intent.putExtra("mode", 2);
			startActivity(intent);
			return true;
		case R.id.itemUtility:
			popupUtility();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void popupUtility(){
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View utilView = inflater.inflate(R.layout.utility_popup, null);
		Button btnVoid = (Button) utilView.findViewById(R.id.btnVoidBill);
		Button btnCloseShift =  (Button) utilView.findViewById(R.id.btnCloseShift);
		Button btnEndday =  (Button) utilView.findViewById(R.id.btnEndday);
		Button btnSync = (Button) utilView.findViewById(R.id.btnSync);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.utility);
		builder.setView(utilView);
		builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		final AlertDialog d = builder.create();	
		d.show();
		
		btnVoid.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, VoidBillActivity.class);
				intent.putExtra("shopId", mShopId);
				intent.putExtra("staffId", mStaffId);
				startActivity(intent);
				d.dismiss();
			}
			
		});
		
		btnCloseShift.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
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
						if(mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 0)){
							finish();
						}
					}
				}).show();
				d.dismiss();
			}
			
		});
		
		btnEndday.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
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
						if(mSession.closeShift(mSessionId, mComputerId, mStaffId, 0, 1)){
							finish();
						}
					}
				}).show();
				d.dismiss();
			}
			
		});
		
		btnSync.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
			
		});
	}

	private void countHoldOrder(){
		int totalHold = mTrans.countHoldOrder(mComputerId);
	
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

	public void holdOrderClicked(final View v){
		final EditText txtRemark = new EditText(MainActivity.this);
		txtRemark.setHint(R.string.remark);
		new AlertDialog.Builder(MainActivity.this)
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

	@Override
	public void onHoldBillClick(View v) {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
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
					mTrans.prepareTransaction(mTransactionId, mComputerId);
					
					loadOrder();
				}
			}
		}).show();
		
	}

	public void paymentClicked(final View v){
		Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
		intent.putExtra("shopId", mShopId);
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
	public void onSwitchUserClick(View v) {
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
								Util.alert(MainActivity.this, android.R.drawable.ic_dialog_alert, 
										R.string.login, R.string.incorrect_password);
							}
						}else{
							Util.alert(MainActivity.this, android.R.drawable.ic_dialog_alert, 
									R.string.login, R.string.incorrect_user);
						}
					}else{
						Util.alert(MainActivity.this, android.R.drawable.ic_dialog_alert, 
								R.string.login, R.string.enter_password);
					}
				}else{
					Util.alert(MainActivity.this, android.R.drawable.ic_dialog_alert, 
							R.string.login, R.string.enter_username);
				}
			}
		});
		
		final AlertDialog d = builder.create();	
		d.show();
	}

	@Override
	public void onLogoutClick(View v) {
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
	
	public void setMemberClicked(final View v){
		Intent intent = new Intent(MainActivity.this, AddMemberActivity.class);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("mode", "search");
		startActivity(intent);	
	}
	
	public void newMemberClicked(final View v){
		Intent intent = new Intent(MainActivity.this, AddMemberActivity.class);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("mode", "add");
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

	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private class OrderListAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public OrderListAdapter (){
			inflater = LayoutInflater.from(MainActivity.this);
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
	
			convertView = inflater.inflate(R.layout.order_list_template, null);
			CheckBox chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
			TextView tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
			TextView tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
			EditText txtOrderAmount = (EditText) convertView.findViewById(R.id.editTextOrderAmount);
			TextView tvOrderPrice = (TextView) convertView.findViewById(R.id.textViewOrderPrice);
			Button btnMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
			Button btnPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
		
			chk.setChecked(orderDetail.isChecked());
			tvOrderNo.setText(Integer.toString(position + 1) + ".");
			tvOrderName.setText(orderDetail.getProductName());
			txtOrderAmount.setText(mFormat.qtyFormat(orderDetail.getQty()));
			tvOrderPrice.setText(mFormat.currencyFormat(orderDetail.getPricePerUnit()));
			
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
						
						mTrans.updateOrderDetail(mTransactionId, mComputerId, 
								orderDetail.getOrderDetailId(), orderDetail.getVatType(), 
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
								mTrans.deleteOrderDetail(mTransactionId, mComputerId, orderDetail.getOrderDetailId());
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
	}

	// menu item adapter
	private class MenuAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		private ImageLoader imgLoader;
		public MenuAdapter(){
			inflater = LayoutInflater.from(MainActivity.this);
			imgLoader = new ImageLoader(MainActivity.this, R.drawable.no_food, 
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
					final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
					View menuDetailView = inflater.inflate(R.layout.menu_detail_layout, null);
					ImageView imvMenuDetail = (ImageView) menuDetailView.findViewById(R.id.imageViewMenuDetail);
					ImageLoader imgLoader2 = new ImageLoader(MainActivity.this, 
							R.drawable.no_food, "mpos_img", ImageLoader.IMAGE_SIZE.LARGE);
					imgLoader2.displayImage(mSetting.getMenuImageUrl() + mi.getMenuImageLink(), imvMenuDetail);
	
					int x = (int)v.getX();
					int y = (int)v.getY();
					PopupWindow popup = new PopupWindow(MainActivity.this);
					popup.setContentView(menuDetailView);
					popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
					popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
					popup.setFocusable(true);
					popup.showAtLocation(v, Gravity.CENTER, x, y);
					
					return true;
				}
				
			});
			
			convertView.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					// open price
					if(mi.getProductPricePerUnit() == -1){
						final EditText txtPrice = new EditText(MainActivity.this);
						txtPrice.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | 
								InputType.TYPE_NUMBER_FLAG_SIGNED);
						new AlertDialog.Builder(MainActivity.this)
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
										new AlertDialog.Builder(MainActivity.this)
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
									new AlertDialog.Builder(MainActivity.this)
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
			tvOpenTime.setText(mFormat.dateTimeFormat(c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getRemark());

			return convertView;
		}
	}
}
