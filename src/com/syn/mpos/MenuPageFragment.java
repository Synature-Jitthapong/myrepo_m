package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Products.Product;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
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
	private ImageLoader mImgLoader;
	private Products mProduct;
	private OnMenuItemClick mCallback;
	private List<Products.Product> mProductLst;
	private MenuItemAdapter mAdapter;
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
		
		mProduct = new Products(getActivity());
		mImgLoader = new ImageLoader(getActivity(), R.drawable.no_food,
				MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
		
		mProductLst = new ArrayList<Product>();
		mAdapter = new MenuItemAdapter();
		new ListProductTask().execute();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnMenuItemClick){
			mCallback = (OnMenuItemClick) activity;
		}
	}

	private class ListProductTask extends AsyncTask<Void, Void, List<Product>>{

		@Override
		protected void onPostExecute(List<Product> result) {
			mProductLst = result;
			mAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected List<Product> doInBackground(Void... params) {
			return mProduct.listProduct(mDeptId);
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
			
			LayoutInflater inflater = 
					(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(convertView == null){
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
			if(p.getProductPrice() < 0)
				holder.tvPrice.setVisibility(View.INVISIBLE);
			else
				holder.tvPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(p.getProductPrice()));
			
			mImgLoader.displayImage(MPOSApplication.getImageUrl() + p.getImgUrl(), holder.imgMenu);
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgMenu;
			TextView tvMenu;
			TextView tvPrice;
		}
	}
	
	
}
