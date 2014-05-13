package com.syn.mpos;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.j1tth4.util.ImageLoader;
import com.syn.mpos.dao.GlobalPropertyDao;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.ProductsDao;
import com.syn.mpos.dao.ProductsDao.Product;
import com.syn.mpos.dao.TransactionDao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProductSetActivity extends Activity{

	private static Context sContext;
	private static ProductsDao sProduct;
	private static GlobalPropertyDao sGlobal;
	
	private static TransactionDao sTransaction;
	
	private int mTransactionId;
	private int mComputerId;
	private int mOrderDetailId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_product_set);
		
		sContext = ProductSetActivity.this;
		sProduct = new ProductsDao(getApplicationContext());
		sGlobal = new GlobalPropertyDao(getApplicationContext());
		sTransaction = new TransactionDao(getApplicationContext());
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		int productId = intent.getIntExtra("productId", 0);
		
		Product p = sProduct.getProduct(productId);
		mOrderDetailId = sTransaction.addOrderDetail(mTransactionId, mComputerId, productId, 
				p.getProductCode(), p.getProductName(), p.getProductTypeId(), 
				p.getVatType(), p.getVatRate(), 1, p.getProductPrice());
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, 
							PlaceholderFragment.newInsance(mTransactionId, mOrderDetailId, productId)).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_set, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			cancelOrder();
			finish();
			return true;
		case R.id.itemConfirm:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void cancelOrder(){
		sTransaction.deleteOrderDetail(mTransactionId);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment{
		
		private int mTransactionId;
		private int mOrderDetailId;
		private int mProductId;
		
		private List<ProductsDao.ProductComponent> mProductCompLst;
		private List<MPOSOrderTransaction.OrderSet> mOrderSetLst;
		private OrderSetAdapter mOrderSetAdapter;
		
		private ListView mLvOrderSet;
		private GridView mGvSetItem;
		private HorizontalScrollView mScroll;
		private LayoutInflater mInflater;
		
		public static PlaceholderFragment newInsance(int transactionId, int orderDetailId, int productId) {
			PlaceholderFragment f = new PlaceholderFragment();
			Bundle b = new Bundle();
			b.putInt("transactionId", transactionId);
			b.putInt("orderDetailId", orderDetailId);
			b.putInt("productId", productId);
			f.setArguments(b);
			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			mTransactionId = getArguments().getInt("transactionId");
			mOrderDetailId = getArguments().getInt("orderDetailId");
			mProductId = getArguments().getInt("productId");
			
			mProductCompLst = new ArrayList<ProductsDao.ProductComponent>();
			mOrderSetLst = new ArrayList<MPOSOrderTransaction.OrderSet>();
			mOrderSetAdapter = new OrderSetAdapter();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			createSetGroupButton();
			loadOrderSet();
		}

		private void updateBadge(int groupId, double requireAmount){
			double totalQty = sTransaction.getOrderSetTotalQty(
				mTransactionId, mOrderDetailId, groupId);
			final LinearLayout scrollContent = (LinearLayout) mScroll.findViewById(R.id.LinearLayout1);
			View groupBtn = scrollContent.findViewById(groupId);
			TextView tvBadge = (TextView) groupBtn.findViewById(R.id.textView1);
			tvBadge.setText(NumberFormat.getInstance().format(requireAmount - totalQty));
		}
		
		@SuppressLint("NewApi")
		private void createSetGroupButton(){
			List<ProductsDao.ProductComponentGroup> productCompGroupLst;
			productCompGroupLst = sProduct.listProductComponentGroup(mProductId);
			if(productCompGroupLst != null){
				final LinearLayout scrollContent = (LinearLayout) mScroll.findViewById(R.id.LinearLayout1);
				for(int i = 0; i < productCompGroupLst.size(); i++){
					final ProductsDao.ProductComponentGroup pCompGroup = productCompGroupLst.get(i);
					View setGroupView = mInflater.inflate(R.layout.set_group_button_layout, null);
					TextView tvGroupName = (TextView) setGroupView.findViewById(R.id.textView2);
					TextView tvBadge = (TextView) setGroupView.findViewById(R.id.textView1);
					tvGroupName.setText(pCompGroup.getGroupName());
					tvBadge.setText(NumberFormat.getInstance().format(pCompGroup.getRequireAmount()));
					if(pCompGroup.getRequireAmount() > 0)
						tvBadge.setVisibility(View.VISIBLE);
					else
						tvBadge.setVisibility(View.GONE);
					setGroupView.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							mProductCompLst = sProduct.listProductComponent(pCompGroup.getProductGroupId());
							
							// i use Products.ProductGroupId instead ProductComponent.PGroupId
							SetItemAdapter adapter = new SetItemAdapter(
									pCompGroup.getProductGroupId(), pCompGroup.getRequireAmount());
							mGvSetItem.setAdapter(adapter);

							v.setId(pCompGroup.getProductGroupId());
							v.setSelected(true);
							for(int j = 0; j < scrollContent.getChildCount(); j++){
								View child = scrollContent.getChildAt(j);
								if(child.getId() != pCompGroup.getProductGroupId()){
									child.setSelected(false);
								}
							}
						}
						
					});
					
					if(i == 0){
						try {
							setGroupView.callOnClick();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					scrollContent.addView(setGroupView, 
							new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
				}
			}
		}
		
		/**
		 * load order set
		 */
		private void loadOrderSet(){
			mOrderSetLst =
					sTransaction.listOrderSet(mTransactionId, mOrderDetailId); 
			mOrderSetAdapter.notifyDataSetChanged();
			mLvOrderSet.setSelection(mOrderSetAdapter.getCount() - 1);
			//mLvOrderSet.smoothScrollToPosition(mOrderSetAdapter.getCount());
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_product_set,
					container, false);
			mLvOrderSet = (ListView) rootView.findViewById(R.id.lvOrderSet);
			mGvSetItem = (GridView) rootView.findViewById(R.id.gvSetItem);
			mScroll = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView1);
			mLvOrderSet.setAdapter(mOrderSetAdapter);
			return rootView;
		}
		
		
		/**
		 * @author j1tth4
		 *
		 */
		private class OrderSetAdapter extends BaseAdapter{
			
			private LayoutInflater mInflater;
			
			public OrderSetAdapter(){
				mInflater = (LayoutInflater) 
						getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public int getCount() {
				return mOrderSetLst != null ? mOrderSetLst.size() : 0;
			}

			@Override
			public MPOSOrderTransaction.OrderSet getItem(int position) {
				return mOrderSetLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewSetGroupHolder holder = null;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.order_set_template, null);
					holder = new ViewSetGroupHolder();
					holder.setDetailContent = (LinearLayout) convertView.findViewById(R.id.setDetailContent);
					holder.tvSetGroupName = (TextView) convertView.findViewById(R.id.tvSetGroupName);
					holder.btnDel = (Button) convertView.findViewById(R.id.btnSetGroupDel);
					convertView.setTag(holder);
				}else{
					holder = (ViewSetGroupHolder) convertView.getTag();
				}
				
				final MPOSOrderTransaction.OrderSet set = mOrderSetLst.get(position);
				holder.tvSetGroupName.setText(set.getGroupName());
				
				if(set.mProductLst != null){
					holder.setDetailContent.removeAllViews();
					for(int i = 0 ; i < set.mProductLst.size(); i ++){
						final MPOSOrderTransaction.OrderSet.OrderSetDetail detail =
								set.mProductLst.get(i);
						View detailView = mInflater.inflate(R.layout.order_set_detail_template, null);
						TextView tvSetNo = (TextView) detailView.findViewById(R.id.tvSetNo);
						TextView tvSetName = (TextView) detailView.findViewById(R.id.tvSetName);
						EditText txtSetQty = (EditText) detailView.findViewById(R.id.txtSetQty);
						Button btnSetMinus = (Button) detailView.findViewById(R.id.btnSetMinus);
						Button btnSetPlus = (Button) detailView.findViewById(R.id.btnSetPlus);
						tvSetNo.setText(String.valueOf(i + 1) + ".");
						tvSetName.setText(detail.getProductName());
						txtSetQty.setText(sGlobal.qtyFormat(detail.getOrderSetQty()));
						btnSetMinus.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								double qty = detail.getOrderSetQty();
								if(qty > 0){
									if(--qty == 0){
										new AlertDialog.Builder(getActivity())
										.setTitle(R.string.delete)
										.setMessage(R.string.confirm_delete_item)
										.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
											}
										}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												sTransaction.deleteOrderSet(
														mTransactionId, mOrderDetailId, detail.getOrderSetId());
												set.mProductLst.remove(detail);
												updateBadge(set.getProductGroupId(), set.getRequireAmount());
												mOrderSetAdapter.notifyDataSetChanged();
											}
										}).show();
									}else{
										detail.setOrderSetQty(qty);
										sTransaction.updateOrderSet(mTransactionId, 
												mOrderDetailId, detail.getOrderSetId(), detail.getProductId(), qty);
										updateBadge(set.getProductGroupId(), set.getRequireAmount());
										mOrderSetAdapter.notifyDataSetChanged();
									}
								}
							}
							
						});
						btnSetPlus.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								double qty = detail.getOrderSetQty();
								if(set.getRequireAmount() > 0){
									// count total group qty from db
									double totalQty = sTransaction.getOrderSetTotalQty(
											mTransactionId, mOrderDetailId, set.getProductGroupId());
									if(totalQty < set.getRequireAmount()){
										detail.setOrderSetQty(++qty);
										sTransaction.updateOrderSet(mTransactionId, 
												mOrderDetailId, detail.getOrderSetId(), detail.getProductId(), qty);
										updateBadge(set.getProductGroupId(), set.getRequireAmount());
										mOrderSetAdapter.notifyDataSetChanged();
									}
								}else{
									detail.setOrderSetQty(++qty);
									sTransaction.updateOrderSet(mTransactionId, 
											mOrderDetailId, detail.getOrderSetId(), detail.getProductId(), qty);
									updateBadge(set.getProductGroupId(), set.getRequireAmount());
									mOrderSetAdapter.notifyDataSetChanged();
								}
							}
							
						});
						holder.btnDel.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								new AlertDialog.Builder(getActivity())
								.setTitle(R.string.delete)
								.setMessage(R.string.confirm_delete_all)
								.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										sTransaction.deleteOrderSet(mTransactionId, mOrderDetailId);
										loadOrderSet();
									}
									
								}).show();
							}
							
						});
						holder.setDetailContent.addView(detailView);
					}
				}
				
				return convertView;
			}
			
			private class ViewSetGroupHolder{
				LinearLayout setDetailContent;
				TextView tvSetGroupName;
				Button btnDel;
			}
		}
		
		/**
		 * @author j1tth4
		 * set menu item adapter
		 */
		public class SetItemAdapter extends BaseAdapter{
			
			private int mPcompGroupId;
			private double mRequireAmount;
			
			private ImageLoader mImgLoader;
			
			/**
			 * @param pcompGroupId
			 * @param requireAmount
			 */
			public SetItemAdapter(int pcompGroupId, double requireAmount){
				mPcompGroupId = pcompGroupId;
				mRequireAmount = requireAmount;
				
				mImgLoader = new ImageLoader(getActivity(), 0,
						MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.SMALL);
			}

			@Override
			public int getCount() {
				return mProductCompLst != null ? mProductCompLst.size() : 0;
			}

			@Override
			public ProductsDao.ProductComponent getItem(int position) {
				return mProductCompLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final MainActivity.MenuItemViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.menu_template, null);
					holder = new MainActivity.MenuItemViewHolder();
					holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
					holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
					holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
					convertView.setTag(holder);
				}else{
					holder = (MainActivity.MenuItemViewHolder) convertView.getTag();
				}
				
				final ProductsDao.ProductComponent pComp = mProductCompLst.get(position);
				holder.tvMenu.setText(pComp.getProductName());
				holder.tvPrice.setText(sGlobal.currencyFormat(pComp.getProductPrice()));

				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						try {
							mImgLoader.displayImage(MPOSApplication.getImageUrl(sContext.getApplicationContext()) + pComp.getImgUrl(), holder.imgMenu);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, 500);
				
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(mRequireAmount > 0){
							// count total group qty from db
							double totalQty = sTransaction.getOrderSetTotalQty(
									mTransactionId, mOrderDetailId, mPcompGroupId);
							if(totalQty < mRequireAmount){
								sTransaction.addOrderSet(mTransactionId, mOrderDetailId, pComp.getProductId(), 
										pComp.getProductName(), mPcompGroupId, mRequireAmount);
								updateBadge(mPcompGroupId, mRequireAmount);
							}
						}else{
							sTransaction.addOrderSet(mTransactionId, mOrderDetailId, pComp.getProductId(), 
									pComp.getProductName(), mPcompGroupId, mRequireAmount);
						}
						loadOrderSet();
					}
					
				});
				return convertView;
			}
			
		}
	}

}
