package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Products;
import com.syn.pos.OrderTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
	
	private Cursor mGroupCursor;
	private Cursor mDetailCursor;
	
	private Products mProduct;
	
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
		
		mProduct = new Products(mSqlite);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, 
							PlaceholderFragment.newInsance(getIntent().getIntExtra("productId", 0))).commit();
		}
	}

	public Products getProduct(){
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
	public static class PlaceholderFragment extends Fragment {
		
		private int mProductId;
		private List<Products.ProductComponent> mProductCompLst;
		private OrderSetAdapter mOrderSetAdapter;
		private SetItemAdapter mSetItemAdapter;
		private ExpandableListView mLvOrderSet;
		private GridView mGvSetItem;
		private HorizontalScrollView mScroll;
		private LayoutInflater mInflater;
		
		public static PlaceholderFragment newInsance(int productId) {
			PlaceholderFragment f = new PlaceholderFragment();
			Bundle b = new Bundle();
			b.putInt("productId", productId);
			f.setArguments(b);
			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mProductId = getArguments().getInt("productId");
			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mProductCompLst = new ArrayList<Products.ProductComponent>();
			mOrderSetAdapter = new OrderSetAdapter();
			mSetItemAdapter = new SetItemAdapter();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			mLvOrderSet.setAdapter(mOrderSetAdapter);
			mGvSetItem.setAdapter(mSetItemAdapter);
			createSetGroupButton();
		}

		public void onMenuSetClick(OrderTransaction.OrderDetail order){
			
		}
		
		@SuppressLint("NewApi")
		private void createSetGroupButton(){
			List<Products.ProductComponentGroup> productCompGroupLst;
			productCompGroupLst = 
					((ProductSetActivity) getActivity()).getProduct().listProductComponentGroup(mProductId);
			if(productCompGroupLst != null){
				LinearLayout scrollContent = (LinearLayout) mScroll.findViewById(R.id.LinearLayout1);
				for(int i = 0; i < productCompGroupLst.size(); i++){
					final Products.ProductComponentGroup pCompGroup = productCompGroupLst.get(i);
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
							mSetItemAdapter.notifyDataSetChanged();
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

			@Override
			public int getGroupCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getGroup(int groupPosition) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return 0;
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
		
		public class SetItemAdapter extends BaseAdapter{
			
			private ImageLoader mImgLoader;
			
			public SetItemAdapter(){
				mImgLoader = new ImageLoader(getActivity(), R.drawable.default_image,
						MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
			}
			
			@Override
			public int getCount() {
				return mProductCompLst != null ? mProductCompLst.size() : 0;
			}

			@Override
			public Products.ProductComponent getItem(int position) {
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
				
				final Products.ProductComponent pComp = mProductCompLst.get(position);
				holder.tvMenu.setText(pComp.getProductName());
				holder.tvPrice.setText(GlobalProperty.currencyFormat(
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
				return convertView;
			}
			
		}
	}

}
