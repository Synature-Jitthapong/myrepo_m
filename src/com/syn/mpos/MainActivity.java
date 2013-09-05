package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.R;
import com.syn.mpos.db.MPOSTransaction;
import com.syn.mpos.db.MenuDept;
import com.syn.mpos.db.MenuItem;
import com.syn.mpos.db.Shop;
import com.syn.mpos.model.MenuGroups;
import com.syn.mpos.model.OrderTransaction;
import com.syn.mpos.model.ShopData;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

public class MainActivity extends Activity implements POS {
	private static final String TAG = "MPOSMainActivity";
	private Shop shop;
	private ShopData.ShopProperty shopProp;
	private ShopData.ComputerProperty compProp;
	private Formatter format;
	private MPOSTransaction mposTrans;
	private List<OrderTransaction.OrderDetail> orderLst;
	private OrderListAdapter orderAdapter;
	private int transactionId;
	private Context context;
	
	private List<MenuGroups.MenuDept> menuDeptLst;
	private List<MenuGroups.MenuItem> menuLst;
	private MenuAdapter menuAdapter;
	
	private int menuDeptId = -1;
	
	private TableRow tbRowVat;
	private GridView menuGridView;
	private ListView orderListView;
	private TextView tvSubTotal;
	private TextView tvTotalPrice;
	private TextView tvVatExclude;
	private TextView tvDiscount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = MainActivity.this;
		
		orderListView = (ListView) findViewById(R.id.listViewOrder);
		menuGridView = (GridView) findViewById(R.id.gridViewMenu);
		tvTotalPrice = (TextView) findViewById(R.id.textViewTotalPrice);
		tvSubTotal = (TextView) findViewById(R.id.textViewSubTotal);
		tvVatExclude = (TextView) findViewById(R.id.textViewVatExclude);
		tvDiscount = (TextView) findViewById(R.id.textViewDiscount);
		tbRowVat = (TableRow) findViewById(R.id.tbRowVat);

		loadMenu();
	}
	
	private void loadMenu(){
		menuLst = new ArrayList<MenuGroups.MenuItem>();
		menuAdapter = new MenuAdapter();
		menuGridView.setAdapter(menuAdapter);

		createMenuDept();
	}
	
	@Override
	public void init(){
		shop = new Shop(context);
		format = new Formatter(context);
		mposTrans = new MPOSTransaction(context);
		shopProp = shop.getShopProperty();
		compProp = shop.getComputerProperty();
		
		transactionId = mposTrans.getCurrTransaction(compProp.getComputerID());
		if(transactionId == 0){
			transactionId = mposTrans.openTransaction(compProp.getComputerID(), 
					shopProp.getShopID(), 1, 1);
		}
		
		//Log.i(TAG, "transactionId= " + transactionId);
		
		orderLst = mposTrans.listAllOrders(transactionId, compProp.getComputerID());
		orderAdapter = new OrderListAdapter(context, format, orderLst, new ListButtonOnClickListener(){
			OrderTransaction.OrderDetail order;
			float qty;
			@Override
			public void onMinusClick(int position) {
				order = orderLst.get(position);
				qty = order.getQty();
				if(--qty > 0){
					order.setQty(qty);
					mposTrans.updateOrderDetail(transactionId, compProp.getComputerID(), 
							order.getOrderDetailId(), order.getVatType(), 
							order.getQty(), order.getPricePerUnit());
				}
				
				orderAdapter.notifyDataSetChanged();
			}

			@Override
			public void onPlusClick(int position) {
				order = orderLst.get(position);
				qty = order.getQty();
				order.setQty(++qty);
				mposTrans.updateOrderDetail(transactionId, compProp.getComputerID(), 
						order.getOrderDetailId(), order.getVatType(), 
						order.getQty(), order.getPricePerUnit());
				
				orderAdapter.notifyDataSetChanged();
			}
			
		}, new AdapterStateListener(){

			@Override
			public void onNotify() {
				summary();
			}
			
		});
		
		// set on order click
		orderListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position,
					long id) {
				final OrderTransaction.OrderDetail orderDetail = 
						(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);
				
				PopupMenu popup = new PopupMenu(context, v);
				popup.getMenuInflater().inflate(R.menu.order_function,
						popup.getMenu());

				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(android.view.MenuItem item) {
						switch(item.getItemId()){
						case R.id.itemOrderModify:
							
							return true;
						case R.id.itemOrderDelete:
							if(mposTrans.deleteOrderDetail(transactionId, compProp.getComputerID(), 
								orderDetail.getOrderDetailId())){
								orderLst.remove(position);
								orderAdapter.notifyDataSetChanged();
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
		orderListView.setAdapter(orderAdapter);
		orderListView.setSelection(orderAdapter.getCount());
	}
	
	public void reportClicked(final View v){
		PopupMenu popup = new PopupMenu(this, v);
		popup.getMenuInflater().inflate(R.menu.report_function,
				popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(android.view.MenuItem item) {
				Intent intent = new Intent(MainActivity.this, SaleReportActivity.class);
				
				switch(item.getItemId()){
				case R.id.itemSaleByProduct:
					intent.putExtra("mode", 1);
					startActivity(intent);
					return true;
				case R.id.itemSaleReport:
					intent.putExtra("mode", 2);
					startActivity(intent);
					return true;
				}
				return false;
			}
		});

		popup.show();
	}
	
	public void inventClicked(final View v){
		PopupMenu popup = new PopupMenu(this, v);
		popup.getMenuInflater().inflate(R.menu.inventory_function,
				popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(android.view.MenuItem item) {
				return false;
			}
		});

		popup.show();
	}
	
	public void holdBillClicked(final View v){
		LayoutInflater inflater = LayoutInflater.from(context);
		View holdBillView = inflater.inflate(R.layout.hold_bill_layout, null);
		TableLayout tbHold = (TableLayout) holdBillView.findViewById(R.id.tbHoldBill);
	
		final Dialog d = new Dialog(context);
		d.setTitle(R.string.hold_bill);
		d.setContentView(holdBillView);
		
		List<OrderTransaction> billLst = mposTrans.listHoldOrder(compProp.getComputerID());
		
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
			tvOpenTime.setText(format.dateTimeFormat(c.getTime()));
			tvOpenStaff.setText(trans.getStaffName());
			tvRemark.setText(trans.getRemark());
			
			
			template.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(mposTrans.prepareTransaction(trans.getTransactionId(), 
							trans.getComputerId())){
						orderLst = mposTrans.listAllOrders(trans.getTransactionId(), 
								trans.getComputerId());
						
						orderAdapter.notifyDataSetChanged();
						d.dismiss();
					}
				}
				
			});
			
			tbHold.addView(template);
		}
		d.show();
	}
	
	public void holdOrderClicked(final View v){
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		param.setMargins(8, 0, 8, 0);
		
		LinearLayout layout = new LinearLayout(context);
		final EditText txtHoldRemark = new EditText(context);
		txtHoldRemark.setGravity(Gravity.TOP);
		txtHoldRemark.setLayoutParams(param);
		layout.addView(txtHoldRemark);
		
		new AlertDialog.Builder(context)
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
				if(mposTrans.holdTransaction(transactionId, compProp.getComputerID(), 
						txtHoldRemark.getText().toString())){
					init();
				}
			}
		})
		.show();
	}
	
	public void paymentClicked(final View v){
		Intent intent = new Intent(context, PaymentActivity.class);
		intent.putExtra("transactionId", transactionId);
		intent.putExtra("computerId", compProp.getComputerID());
		intent.putExtra("staffId", 1);
		startActivity(intent);
	}
	
	public void discountClicked(final View v){
		Intent intent = new Intent(context, DiscountActivity.class);
		intent.putExtra("transactionId", transactionId);
		intent.putExtra("computerId", compProp.getComputerID());
		startActivity(intent);
	}
	
	public void logoutClicked(final View v){
		finish();
	}
	
	@Override
	protected void onResume() {
		init();
		super.onResume();
	}
	
	// menu catgory
	protected void createMenuDept(){
		MenuDept menuDept = new MenuDept(context);
		menuDeptLst = menuDept.listMenuDept();
		
		if(menuDeptLst.size() > 0){
			
			LayoutInflater inflater = 
					LayoutInflater.from(context);
			int i = 0;
			for(final MenuGroups.MenuDept md : menuDeptLst){
				final View v = inflater.inflate(R.layout.menu_catgory_tempate, null);
				final Button btnCat = (Button) v.findViewById(R.id.button1);
				btnCat.setId(md.getMenuDeptID());
				
				btnCat.setText(md.getMenuDeptName_0());
				
				btnCat.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// display menu
						btnCat.setSelected(true);
						if(menuDeptId != -1 && menuDeptId != v.getId()){
							Button lastBtn = (Button)findViewById(menuDeptId);
							lastBtn.setSelected(false);
						}
						// load menu
						MenuItem mi = new MenuItem(context);
						menuLst = mi.listMenuItem(md.getMenuDeptID(), 1);
						menuAdapter.notifyDataSetChanged();
						
						menuDeptId = v.getId();
					}
					
				});

				//add to viewgroup
				ViewGroup menuCatLayout = (ViewGroup) findViewById(R.id.MenuCatLayout);
				LayoutParams layoutParam = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
						1f);

				if(i == 0){
					btnCat.callOnClick();
					btnCat.setBackgroundResource(R.drawable.orange_button_left);
					
				}else if(i == menuDeptLst.size() - 1){
					btnCat.setBackgroundResource(R.drawable.orange_button_right);
				}else{
					btnCat.setBackgroundResource(R.drawable.orange_button_center);
				}
				
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
			inflater = LayoutInflater.from(context);
//			imgLoader = new ImageLoader(context, R.drawable.no_food, 
//					globalVar.MENU_IMAGE_CACHE_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
			
		}
		
		@Override
		public int getCount() {
			return menuLst.size();
		}

		@Override
		public MenuGroups.MenuItem getItem(int position) {
			return menuLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MenuGroups.MenuItem mi = menuLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.menu_template, null);
				holder = new ViewHolder();
				holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
				holder.tvMenuName = (TextView) convertView.findViewById(R.id.textViewMenuName);
				holder.tvMenuDeptName = (TextView) convertView.findViewById(R.id.textViewMenuDept);
				holder.tvMenuDescript = (TextView) convertView.findViewById(R.id.textViewMenuDescript);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			//imgLoader.displayImage(globalVar.getImageUrl() + mi.getImgUrl(), holder.imgMenu);
			holder.tvMenuName.setText(mi.getMenuName_0());
			
			convertView.setOnLongClickListener(new OnLongClickListener(){
				
				@Override
				public boolean onLongClick(View v) {
					final LayoutInflater inflater = LayoutInflater.from(context);
					View menuDetailView = inflater.inflate(R.layout.menu_detail_layout, null);
					ImageView imvMenuDetail = (ImageView) menuDetailView.findViewById(R.id.imageViewMenuDetail);
//					ImageLoader imgLoader2 = new ImageLoader(context, 
//							R.drawable.no_food, globalVar.MENU_IMAGE_CACHE_DIR, ImageLoader.IMAGE_SIZE.LARGE);
//					imgLoader2.displayImage(globalVar.getImageUrl() + mi.getImgUrl(), imvMenuDetail);
					
					final Dialog dialog = new Dialog(context);
					dialog.getWindow().setGravity(Gravity.LEFT); 
					dialog.setContentView(menuDetailView);
					dialog.setContentView(menuDetailView);
					dialog.show();
					return true;
				}
				
			});
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int orderDetailId = mposTrans.addOrderDetail(transactionId, 
							compProp.getComputerID(), mi.getProductID(), mi.getProductTypeID(), 
							mi.getVatType(), mi.getMenuName_0(), 1, 
							mi.getProductPricePerUnit());
					
					OrderTransaction.OrderDetail order = 
							mposTrans.getOrder(transactionId, compProp.getComputerID(), orderDetailId);
					orderLst.add(order);
					
					summary();
					
					orderAdapter.notifyDataSetChanged();
					orderListView.smoothScrollToPosition(orderAdapter.getCount());
				}
				
			});
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgMenu;
			TextView tvMenuName;
			TextView tvMenuDeptName;
			TextView tvMenuDescript;
		}
	}
	
	@Override
	public void summary(){
		OrderTransaction.OrderDetail orderDetail
			= mposTrans.getSummary(transactionId, compProp.getComputerID());
		
		float subTotal = orderDetail.getTotalRetailPrice();
		float vat = orderDetail.getVat();	// vat exclude
		float totalSalePrice = orderDetail.getTotalSalePrice() + vat;
		float totalDiscount = orderDetail.getPriceDiscount() + orderDetail.getMemberDiscount();
		
		// update trans vat
		mposTrans.updateTransactionVat(transactionId, compProp.getComputerID(), 
				totalSalePrice, vat);
		
		if(vat > 0)
			tbRowVat.setVisibility(View.VISIBLE);
		else
			tbRowVat.setVisibility(View.GONE);
		
		tvVatExclude.setText(format.currencyFormat(vat));
		tvSubTotal.setText(format.currencyFormat(subTotal));
		tvDiscount.setText(format.currencyFormat(totalDiscount));
		tvTotalPrice.setText(format.currencyFormat(totalSalePrice));
	}
	
	public void clearBillClicked(final View v){
		new AlertDialog.Builder(context)
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
				mposTrans.cancelTransaction(transactionId, compProp.getComputerID());

				init();
			}
		})
		.show();
	}

	@Override
	public void addOrder() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteOrder() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOrderQty() {
		// TODO Auto-generated method stub
		
	}
}
