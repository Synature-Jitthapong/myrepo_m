package com.syn.mpos;

import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuPageFragment extends Fragment {
	private static MPOSTransaction mTrans;
	private static ImageLoader mImgLoader;
	private static OnMenuItemClick listener;
	private Products mProduct;
	private List<Products.Product> mProductLst;
	private MenuItemAdapter mAdapter;
	private int mTransactionId;
	private int mComputerId;
	private int mDeptId;
	
	public static MenuPageFragment newInstance(Context c, MPOSTransaction trans, 
			int transId, int compId, int deptId, OnMenuItemClick onItemClick){
		MenuPageFragment f = new MenuPageFragment();
		listener = onItemClick;
		mTrans = trans;
		mImgLoader = new ImageLoader(c, R.drawable.no_food, "mpos", 
				ImageLoader.IMAGE_SIZE.MEDIUM);
		Bundle b = new Bundle();
		b.putInt("transId", transId);
		b.putInt("compId", compId);
		b.putInt("deptId", deptId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTransactionId = getArguments().getInt("transId");
		mComputerId = getArguments().getInt("compId");
		mDeptId = getArguments().getInt("deptId");
		
		mProduct = new Products(getActivity());
		mProductLst = mProduct.listProduct(mDeptId);
		mAdapter = new MenuItemAdapter();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.menu_fragment, container, false);
		GridView gvItem = (GridView) v.findViewById(R.id.gridView1);
		gvItem.setAdapter(mAdapter);
		gvItem.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Products.Product p = 
						(Products.Product) parent.getItemAtPosition(position);
				int orderId = mTrans.addOrderDetail(mTransactionId, 
						mComputerId, p.getProductId(), p.getProductTypeId(), 
						p.getVatType(), 1, p.getProductPrice());
				
				listener.onClick(orderId);
			}
			
		});
		return v;
	}

	public static interface OnMenuItemClick{
		void onClick(int orderId);
	}
	
	private class MenuItemAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return mProductLst.size();
		}

		@Override
		public Products.Product getItem(int position) {
			return mProductLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Products.Product p = mProductLst.get(position);
			ViewHolder holder;
			
			if(convertView == null){
				LayoutInflater inflater = 
						(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.menu_template, null);
				holder = new ViewHolder();
				holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
				holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
				holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvMenu.setText(p.getProductName());
			holder.tvPrice.setText("" + p.getProductPrice());
			mImgLoader.displayImage(MainActivity.mSetting.getMenuImageUrl() + p.getPicName(), holder.imgMenu);
			return convertView;
		}
		
		
	}
	static class ViewHolder{
		ImageView imgMenu;
		TextView tvMenu;
		TextView tvPrice;
	}
}
