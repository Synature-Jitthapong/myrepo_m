package com.syn.mpos;

import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;
import android.app.Activity;
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
	private OnMenuItemClick mCallback;
	private Products mProduct;
	private List<Products.Product> mProductLst;
	private MenuItemAdapter mAdapter;
	private ImageLoader mImgLoader;
	private int mDeptId;
	
	public static MenuPageFragment newInstance(int deptId){
		MenuPageFragment f = new MenuPageFragment();
		Bundle b = new Bundle();
		b.putInt("deptId", deptId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDeptId = getArguments().getInt("deptId");

		mImgLoader = new ImageLoader(getActivity(), R.drawable.no_food,
				"mpos_img");
		
		mProduct = new Products(getActivity());
		mProductLst = mProduct.listProduct(mDeptId);
		mAdapter = new MenuItemAdapter();
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnMenuItemClick){
			mCallback = (OnMenuItemClick) activity;
		}
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
				
				mCallback.onClick(p.getProductId(), p.getProductTypeId(), 
						p.getVatType(), p.getVatRate(), p.getProductPrice());
			}
		});
		return v;
	}

	public interface OnMenuItemClick{
		void onClick(int productId, int productTypeId, int vatType, float vatRate, float productPrice);
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
			holder.tvPrice.setText(MainActivity.mFormat.currencyFormat(p.getProductPrice()));
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
