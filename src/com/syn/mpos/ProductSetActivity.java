package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.OrderSetDataSource;
import com.syn.mpos.database.OrderTransactionDataSource;
import com.syn.mpos.database.ProductsDataSource;
import com.syn.pos.OrderTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProductSetActivity extends Activity{

	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;
	
	private OrderTransactionDataSource mTransaction; 
	
	private int mTransactionId;
	private int mComputerId;
	private int mOrderDetailId;
	
	private ProductsDataSource mProduct;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    params.height= WindowManager.LayoutParams.WRAP_CONTENT;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
        getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_product_set);
		
		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		mTransaction = new OrderTransactionDataSource(mSqlite);
		mProduct = new ProductsDataSource(mSqlite);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		int productId = intent.getIntExtra("productId", 0);
		int productTypeId = intent.getIntExtra("productTypeId", 0);
		int vatType = intent.getIntExtra("vatType", 0);
		double vatRate = intent.getDoubleExtra("vatRate", 0);
		double productPrice = intent.getDoubleExtra("productPrice", 0);
		
		mOrderDetailId = mTransaction.addOrderDetail(mTransactionId, mComputerId, productId, 
				productTypeId, vatType, vatRate, 1, productPrice);
		
		if(mTransactionId == 0 || mOrderDetailId == 0 || productId == 0){
			new AlertDialog.Builder(this)
			.setTitle(R.string.error)
			.setMessage("transactionId=" + mTransactionId + 
					", orderDetailId=" + mOrderDetailId + 
					", productId=" + productId)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();
		}else{
			if (savedInstanceState == null) {
				getFragmentManager().beginTransaction()
						.add(R.id.container, 
								PlaceholderFragment.newInsance(mTransactionId, mOrderDetailId, productId)).commit();
			}
		}
	}

	public ProductsDataSource getProduct(){
		return mProduct;
	}
	
	public SQLiteDatabase getDatabase(){
		return mSqlite;
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
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment{
		
		private int mTransactionId;
		private int mOrderDetailId;
		private int mProductId;
		
		private List<ProductsDataSource.ProductComponent> mProductCompLst;
		
		private OrderSetDataSource mOrderSet;
		
		private ExpandableListView mLvOrderSet;
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
			
			mOrderSet = new OrderSetDataSource(((ProductSetActivity) getActivity()).getDatabase());
			
			mProductCompLst = new ArrayList<ProductsDataSource.ProductComponent>();
			
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			createSetGroupButton();
			loadOrderSet();
		}

		@SuppressLint("NewApi")
		private void createSetGroupButton(){
			List<ProductsDataSource.ProductComponentGroup> productCompGroupLst;
			productCompGroupLst = 
					((ProductSetActivity) getActivity()).getProduct().listProductComponentGroup(mProductId);
			if(productCompGroupLst != null){
				LinearLayout scrollContent = (LinearLayout) mScroll.findViewById(R.id.LinearLayout1);
				for(int i = 0; i < productCompGroupLst.size(); i++){
					final ProductsDataSource.ProductComponentGroup pCompGroup = productCompGroupLst.get(i);
					View setGroupView = mInflater.inflate(R.layout.set_group_button_layout, null);
					TextView tvGroupName = (TextView) setGroupView.findViewById(R.id.textView2);
					TextView tvBadge = (TextView) setGroupView.findViewById(R.id.textView1);
					tvGroupName.setText(pCompGroup.getGroupName());
					tvBadge.setText(String.valueOf(pCompGroup.getRequireAmount()));
					setGroupView.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							v.setSelected(true);
							mProductCompLst = 
									((ProductSetActivity) getActivity()).
										getProduct().listProductComponent(pCompGroup.getProductGroupId());
							
							// i use Products.ProductGroupId instead ProductComponent.PGroupId
							SetItemAdapter adapter = new SetItemAdapter(
									pCompGroup.getProductGroupId(), pCompGroup.getRequireAmount());
							mGvSetItem.setAdapter(adapter);
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
					scrollContent.addView(setGroupView);
				}
			}
		}
		
		/**
		 * load order set
		 */
		private void loadOrderSet(){
			List<ProductsDataSource.ProductSet> setLst = 
					mOrderSet.listOrderSet(mTransactionId, mOrderDetailId); 
			OrderSetAdapter adapter = new OrderSetAdapter(setLst);
			mLvOrderSet.setAdapter(adapter);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_product_set,
					container, false);
			mLvOrderSet = (ExpandableListView) rootView.findViewById(R.id.expandableListView1);
			mGvSetItem = (GridView) rootView.findViewById(R.id.gvSetItem);
			mScroll = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView1);
			return rootView;
		}
		
		public class OrderSetAdapter extends BaseExpandableListAdapter{
			
			private List<ProductsDataSource.ProductSet> mProductSetLst;
			
			private LayoutInflater mInflater;
			
			public OrderSetAdapter(List<ProductsDataSource.ProductSet> productSetLst){
				mProductSetLst = productSetLst;
				
				mInflater = (LayoutInflater) getActivity().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public int getGroupCount() {
				return mProductSetLst != null ? mProductSetLst.size() : 0;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				ProductsDataSource.ProductSet set = mProductSetLst.get(groupPosition);
				return set.mProductLst != null ? set.mProductLst.size() : 0;
			}

			@Override
			public ProductsDataSource.ProductSet getGroup(int groupPosition) {
				return mProductSetLst.get(groupPosition);
			}

			@Override
			public ProductsDataSource.ProductSet.ProductSetDetail getChild(int groupPosition, int childPosition) {
				ProductsDataSource.ProductSet set = mProductSetLst.get(groupPosition);
				return set.mProductLst.get(childPosition);
			}

			@Override
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				// TODO Auto-generated method stub
				return false;
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
				
				mImgLoader = new ImageLoader(getActivity(), R.drawable.default_image,
						MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
			}

			@Override
			public int getCount() {
				return mProductCompLst != null ? mProductCompLst.size() : 0;
			}

			@Override
			public ProductsDataSource.ProductComponent getItem(int position) {
				return mProductCompLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final MenuItemAdapter.ViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.menu_template, null);
					holder = new MenuItemAdapter.ViewHolder();
					holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
					holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
					holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
					convertView.setTag(holder);
				}else{
					holder = (MenuItemAdapter.ViewHolder) convertView.getTag();
				}
				
				final ProductsDataSource.ProductComponent pComp = mProductCompLst.get(position);
				holder.tvMenu.setText(pComp.getProductName());
				holder.tvPrice.setText(GlobalPropertyDataSource.currencyFormat(
						((ProductSetActivity) getActivity()).getDatabase(), pComp.getFlexibleProductPrice()));

				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						try {
							mImgLoader.displayImage(MPOSApplication.getImageUrl() + pComp.getImgUrl(), holder.imgMenu);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, 500);
				
				convertView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						mOrderSet.addOrderSet(mTransactionId, mOrderDetailId, pComp.getProductId(), 
								pComp.getProductName(), mPcompGroupId, mRequireAmount);
						
						loadOrderSet();
					}
					
				});
				return convertView;
			}
			
		}
	}

}
