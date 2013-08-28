package com.syn.mpos;

import java.util.ArrayList;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

public class MainActivity extends Activity {
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
	private TextView tvTransVat;
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
		tvTransVat = (TextView) findViewById(R.id.textViewTransVat);
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
	
	private void init(){
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
				qty = order.getProductAmount();
				if(--qty > 0){
					order.setProductAmount(qty);
					mposTrans.updateOrderDetail(transactionId, compProp.getComputerID(), 
							order.getOrderDetailId(), order.getVatType(), 
							order.getProductAmount(), order.getProductPrice());
				}
				
				orderAdapter.notifyDataSetChanged();
			}

			@Override
			public void onPlusClick(int position) {
				order = orderLst.get(position);
				qty = order.getProductAmount();
				order.setProductAmount(++qty);
				mposTrans.updateOrderDetail(transactionId, compProp.getComputerID(), 
						order.getOrderDetailId(), order.getVatType(), 
						order.getProductAmount(), order.getProductPrice());
				
				orderAdapter.notifyDataSetChanged();
			}
			
		}, new AdapterStateListener(){

			@Override
			public void onNotify() {
				updateTotalPrice();
			}
			
		});
		
		updateTotalPrice();
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
		
	}
	
	public void holdOrderClicked(final View v){
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		param.setMargins(8, 0, 8, 0);
		
		LinearLayout layout = new LinearLayout(context);
		EditText txtHoldRemark = new EditText(context);
		txtHoldRemark.setLines(2);
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
				
			}
		})
		.show();
	}
	
	public void paymentClicked(final View v){
		Intent intent = new Intent(context, PaymentActivity.class);
		intent.putExtra("transactionId", transactionId);
		intent.putExtra("computerId", compProp.getComputerID());
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
				holder.btnOrder = (Button) convertView.findViewById(R.id.buttonOrder);
				holder.tvMenuDescript = (TextView) convertView.findViewById(R.id.textViewMenuDescript);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			//imgLoader.displayImage(globalVar.getImageUrl() + mi.getImgUrl(), holder.imgMenu);
			holder.tvMenuName.setText(mi.getMenuName_0());
			holder.btnOrder.setText(format.currencyFormat(mi.getProductPricePerUnit()));
			
			holder.imgMenu.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
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
				}
				
			});
			
			holder.btnOrder.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int orderDetailId = mposTrans.addOrderDetail(transactionId, 
							compProp.getComputerID(), mi.getProductID(), mi.getProductTypeID(), 
							mi.getVatType(), 0, mi.getMenuName_0(), 1, 
							mi.getProductPricePerUnit());
					
					OrderTransaction.OrderDetail order = 
							mposTrans.getOrder(transactionId, compProp.getComputerID(), orderDetailId);
					orderLst.add(order);
					
					updateTotalPrice();
					
					orderAdapter.notifyDataSetChanged();
					orderListView.smoothScrollToPosition(orderAdapter.getCount());
				}
				
			});
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgMenu;
			TextView tvMenuName;
			Button btnOrder;
			TextView tvMenuDeptName;
			TextView tvMenuDescript;
		}
	}
	
	private void updateTotalPrice(){
		OrderTransaction.OrderDetail orderDetail
			= mposTrans.getSummary(transactionId);
		
		if(orderDetail.getVatExclude() > 0)
			tbRowVat.setVisibility(View.VISIBLE);
		else
			tbRowVat.setVisibility(View.GONE);
		
		tvTransVat.setText(format.currencyFormat(orderDetail.getVatExclude()));
		tvSubTotal.setText(format.currencyFormat(orderDetail.getProductPrice()));
		tvDiscount.setText(format.currencyFormat(orderDetail.getEachProductDiscount()));
		tvTotalPrice.setText(format.currencyFormat(orderDetail.getTotalPrice()));
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
}
