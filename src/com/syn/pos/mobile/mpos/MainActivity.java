package com.syn.pos.mobile.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.core.util.ImageLoader;
import com.syn.pos.mobile.model.MenuGroups;
import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.dao.MPOSTransaction;
import com.syn.pos.mobile.mpos.dao.MenuDept;
import com.syn.pos.mobile.mpos.dao.MenuItem;


import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

public class MainActivity extends Activity {
	private MPOSVar mposVar;
	private MPOSTransaction mposTrans;
	private OrderTransaction orderTrans;
	private OrderListAdapter orderAdapter;
	
	private double totalPrice = 0.0d;
	private int totalQty = 0;
	private List<MenuGroups.MenuDept> menuDeptLst;
	private List<MenuGroups.MenuItem> menuLst;
	private MenuAdapter menuAdapter;
	
	private int menuDeptId = -1;
	
	private GridView menuGridView;
	private ListView orderListView;
	private TextView tvTotalPrice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		orderListView = (ListView) findViewById(R.id.listViewOrder);
		menuGridView = (GridView) findViewById(R.id.gridViewMenu);
		tvTotalPrice = (TextView) findViewById(R.id.textViewTotalPrice);
		
	
		menuLst = new ArrayList<MenuGroups.MenuItem>();
		menuAdapter = new MenuAdapter();
		menuGridView.setAdapter(menuAdapter);
		
		createMenuDept();
	}
	
	private void init(){
		mposVar = new MPOSVar(MainActivity.this);
		mposTrans = new MPOSTransaction(MainActivity.this, mposVar);
		
		//mposTrans.getCurrTransaction(mposVar.com)
	}
	
	public void holdBillClicked(final View v){
		
	}
	
	public void holdOrderClicked(final View v){
		final Dialog dialog = 
				new Dialog(MainActivity.this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setGravity(Gravity.TOP);
		
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View holdView = inflater.inflate(R.layout.hold_order_layout, null);
		Button btnConfirm = (Button) holdView.findViewById(R.id.buttonConfirmHold);
		Button btnCancel = (Button) holdView.findViewById(R.id.buttonCancelHold);
		btnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
			
		});
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
			
		});
		
		dialog.setContentView(holdView);
		dialog.show();
	}
	
	public void logoutClicked(final View v){
		MainActivity.this.finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	// menu catgory
	protected void createMenuDept(){
		MenuDept menuDept = new MenuDept(MainActivity.this);
		menuDeptLst = menuDept.listMenuDept();
		
		if(menuDeptLst.size() > 0){
			
			LayoutInflater inflater = 
					LayoutInflater.from(MainActivity.this);
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
						MenuItem mi = new MenuItem(MainActivity.this);
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
	
	/*
	// order adapter
	private class OrderAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		public OrderAdapter(){
			inflater = LayoutInflater.from(MainActivity.this);
		}
		
		@Override
		public int getCount() {
			return ORDER_LST.size();
		}

		@Override
		public MenuDataItem getItem(int position) {
			return ORDER_LST.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final MenuDataItem mi = ORDER_LST.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.order_list_template, null);
				holder = new ViewHolder();
				holder.tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
				holder.tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
				holder.tvOrderQty = (TextView) convertView.findViewById(R.id.textViewOrderQty);
				holder.btnOrderMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
				holder.btnOrderPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvOrderNo.setText(globalVar.getQtyFormat().format(position + 1) + ".");
			holder.tvOrderName.setText(mi.getMenuName());
			holder.tvOrderQty.setText(globalVar.getQtyFormat().format(mi.getProductQty()));
			
			holder.btnOrderMinus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					double orderQty = mi.getProductQty();
					if(--orderQty >= 1){
						// update 
						TOTAL_QTY -= orderQty;
						TOTAL_PRICE -= mi.getPricePerUnit();
						updateTextPrice();
						
						mi.setProductQty(orderQty);
						holder.tvOrderQty.setText(globalVar.getQtyFormat().format(orderQty));
					}
					
				}
				
			});
			holder.btnOrderPlus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					double orderQty = mi.getProductQty();
					++orderQty;
					// update 
					TOTAL_QTY += orderQty;
					TOTAL_PRICE += mi.getPricePerUnit();
					updateTextPrice();
					
					mi.setProductQty(orderQty);
					
					holder.tvOrderQty.setText(globalVar.getQtyFormat().format(orderQty));
				}
				
			});
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvOrderNo;
			TextView tvOrderName;
			TextView tvOrderQty;
			Button btnOrderMinus;
			Button btnOrderPlus;
		}
		
	}
	*/
	
	
	// menu item adapter
	private class MenuAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		private ImageLoader imgLoader;
		public MenuAdapter(){
			inflater = LayoutInflater.from(MainActivity.this);
//			imgLoader = new ImageLoader(MainActivity.this, R.drawable.no_food, 
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
			holder.btnOrder.setText(Double.toString(mi.getProductPricePerUnit()));
			
			holder.imgMenu.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
					View menuDetailView = inflater.inflate(R.layout.menu_detail_layout, null);
					ImageView imvMenuDetail = (ImageView) menuDetailView.findViewById(R.id.imageViewMenuDetail);
//					ImageLoader imgLoader2 = new ImageLoader(MainActivity.this, 
//							R.drawable.no_food, globalVar.MENU_IMAGE_CACHE_DIR, ImageLoader.IMAGE_SIZE.LARGE);
//					imgLoader2.displayImage(globalVar.getImageUrl() + mi.getImgUrl(), imvMenuDetail);
					
					final Dialog dialog = new Dialog(MainActivity.this);
					dialog.getWindow().setGravity(Gravity.LEFT); 
					dialog.setContentView(menuDetailView);
					dialog.setContentView(menuDetailView);
					dialog.show();
				}
				
			});
			
			holder.btnOrder.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
//					MenuGroups.MenuDept en item = new MenuDataItem();
//					item.setProductID(mi.getProductID());
//					item.setMenuName(mi.getMenuName());
//					item.setProductQty(1);
//					item.setPricePerUnit(mi.getPricePerUnit());
//					
//					ORDER_LST.add(item);
//					TOTAL_QTY += item.getProductQty();
//					TOTAL_PRICE += item.getPricePerUnit() * item.getProductQty();
//					updateTextPrice();
//					
//					orderAdapter.notifyDataSetChanged();
//					orderListView.smoothScrollToPosition(ORDER_LST.size());
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
	
//	private class ResizeMenuImage extends ResizeImage{
//		public ResizeMenuImage(Context context, int theme, ImageView img) {
//			super(context, theme);
//	
//			img.setOnTouchListener(this);
//		}
//		
//	}
	
	
	private void updateTextPrice(){
		tvTotalPrice.setText("0");
	}
}
