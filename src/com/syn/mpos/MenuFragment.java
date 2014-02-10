package com.syn.mpos;

import java.util.List;

import com.astuetz.PagerSlidingTabStrip;
import com.syn.mpos.provider.Products;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment{
	private List<Products.ProductDept> mProductDeptLst;
	private MenuItemPagerAdapter mPagerAdapter;

	private PagerSlidingTabStrip mTabs;
	private ViewPager mPager;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs);
		mPager = (ViewPager) getActivity().findViewById(R.id.pager);
		mPagerAdapter = new MenuItemPagerAdapter(getFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu_fragment, container, false);
	}

	private class MenuItemPagerAdapter extends FragmentPagerAdapter{
		
		public MenuItemPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mProductDeptLst.get(position).getProductDeptName();
		}
	
		@Override
		public Fragment getItem(int position) {
			int deptId = mProductDeptLst.get(position).getProductDeptId();
			return MenuPageFragment.newInstance(deptId);
		}
	
		@Override
		public int getCount() {
			return mProductDeptLst.size();
		}		
	}
}
