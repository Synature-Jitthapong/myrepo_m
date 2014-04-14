package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

public class MenuPageFragment extends Fragment {
	
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
		if((savedInstanceState != null) && savedInstanceState.containsKey("deptId")){
			mDeptId = savedInstanceState.getInt("deptId");
		}
		else{
			mDeptId = getArguments().getInt("deptId");
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnMenuItemClick){
			mCallback = (OnMenuItemClick) activity;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("deptId", mDeptId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final GridView gvItem = (GridView) inflater.inflate(R.layout.menu_grid_view, container, false);
		mProductLst = ((MainActivity) getActivity()).getProduct().listProduct(mDeptId);
		mAdapter = new MenuItemAdapter(getActivity(), 
				((MainActivity) getActivity()).getDatabase(), mProductLst);
		gvItem.setAdapter(mAdapter);
		gvItem.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Products.Product p = 
						(Products.Product) parent.getItemAtPosition(position);
				
				mCallback.onMenuClick(p.getProductId(), p.getProductTypeId(), 
						p.getVatType(), p.getVatRate(), p.getProductPrice());
			}
		});
		
		gvItem.setOnItemLongClickListener(new OnItemLongClickListener(){
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				Products.Product p = (Products.Product) parent.getItemAtPosition(position);
				ImageViewPinchZoom imgZoom = ImageViewPinchZoom.newInstance(p.getImgUrl(), p.getProductName(), 
						GlobalProperty.currencyFormat(
								((MainActivity) getActivity()).getDatabase(), p.getProductPrice()));
				imgZoom.show(getFragmentManager(), "MenuImage");
				return true;
			}
			
		});
		return gvItem;
	}

	public interface OnMenuItemClick{
		void onMenuClick(int productId, int productTypeId, int vatType, double vatRate, double productPrice);
	}
}
