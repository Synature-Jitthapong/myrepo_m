package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProductSetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 960;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
        getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_product_set);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, 
							PlaceholderFragment.newInsance(getIntent().getIntExtra("productId", 0))).commit();
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
		
		private Products mProduct;
		private int mProductId;
		private List<Products.ProductComponent> mProductCompLst;
		private OrderSetAdapter mOrderSetAdapter;
		private SetItemAdapter mSetItemAdapter;
		private ListView mLvOrderSet;
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
			mProduct = new Products(MPOSApplication.getWriteDatabase());
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

		private void createSetGroupButton(){
			List<Products.ProductComponentGroup> productCompGroupLst;
			productCompGroupLst = mProduct.listProductComponentGroup(mProductId);
			if(productCompGroupLst != null){
				LinearLayout scrollContent = (LinearLayout) mScroll.findViewById(R.id.LinearLayout1);
				for(final Products.ProductComponentGroup pCompGroup : productCompGroupLst){
					View setGroupView = mInflater.inflate(R.layout.set_group_button_layout, null);
					TextView tvGroupName = (TextView) setGroupView.findViewById(R.id.textView2);
					TextView tvBadge = (TextView) setGroupView.findViewById(R.id.textView1);
					tvGroupName.setText(pCompGroup.getGroupName());
					tvBadge.setText(String.valueOf(pCompGroup.getRequireAmount()));
					setGroupView.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							mProductCompLst = mProduct.listProductComponent(pCompGroup.getProductGroupId());
							mSetItemAdapter.notifyDataSetChanged();
						}
						
					});
					scrollContent.addView(setGroupView);
				}
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_product_set,
					container, false);
			mLvOrderSet = (ListView) rootView.findViewById(R.id.lvOrderSet);
			mGvSetItem = (GridView) rootView.findViewById(R.id.gvSetItem);
			mScroll = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView1);
			return rootView;
		}
		
		public class OrderSetAdapter extends BaseAdapter{
			
			public OrderSetAdapter(){
				
			}
			
			@Override
			public int getCount() {
				return 0;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
		public class SetItemAdapter extends BaseAdapter{
			
			private ImageLoader mImgLoader;
			
			public SetItemAdapter(){
				mImgLoader = new ImageLoader(getActivity(), 0,
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
				holder.tvPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(pComp.getFlexibleProductPrice()));

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
