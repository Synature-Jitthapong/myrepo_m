package com.syn.mpos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class MenuPageFragment extends Fragment {
	private List<HashMap<String, Object>> mItem;
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
		mItem = new ArrayList<HashMap<String, Object>>();
		mAdapter = new MenuItemAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.menu_fragment, container, false);
		GridView gvItem = (GridView) v.findViewById(R.id.gridView1);
		gvItem.setAdapter(mAdapter);
		return v;
	}

	public static interface OnMenuItemClick{
		void onClick(int id, float unitPrice);
	}
	
	private class MenuItemAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
}
