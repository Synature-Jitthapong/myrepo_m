package com.syn.mpos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class SyncActivity extends Activity implements OnItemClickListener{
	public static final int SYNC_PRODUCT = 0;
	public static final int SYNC_SALE = 1;
	
	private int mStaffId;
	private ListView mLvSync;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sync);
		
		mLvSync = (ListView) findViewById(R.id.lvSync);
		
		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		String syncArr[] = getResources().getStringArray(R.array.sync_array);
		mLvSync.setAdapter(new SyncListAdapter(syncArr));
		mLvSync.setOnItemClickListener(this);
	}

	private class SyncListAdapter extends BaseAdapter{
		
		private String[] mWhatSyncArr;
		private LayoutInflater mInflater;
		
		public SyncListAdapter(String[] whatSyncArr){
			mWhatSyncArr = whatSyncArr;
			mInflater = (LayoutInflater) 
					SyncActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mWhatSyncArr.length;
		}

		@Override
		public Object getItem(int position) {
			return mWhatSyncArr[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.sync_template, null);
			TextView syncName = (TextView) convertView.findViewById(R.id.tvSyncName);
			syncName.setText(mWhatSyncArr[position]);
			return convertView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		//final TextView tvSyncName = (TextView) v.findViewById(R.id.tvSyncName);
		final ProgressBar progress = (ProgressBar) v.findViewById(R.id.syncProgress);
		final ImageView imgSyncStatus = (ImageView) v.findViewById(R.id.imgSyncStatus);
		imgSyncStatus.setVisibility(View.GONE);
		progress.setVisibility(View.VISIBLE);
		if(id==SYNC_PRODUCT){
			MPOSService mposService = new MPOSService();
			mposService.loadProductData(new MPOSService.OnServiceProcessListener() {
				
				@Override
				public void onSuccess() {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onError(String msg) {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
					imgSyncStatus.setImageResource(R.drawable.ic_navigation_cancel_light);
				}
			});
		}else if(id==SYNC_SALE){
			MPOSUtil.doSendSale(mStaffId, new MPOSService.OnServiceProcessListener() {
				
				@Override
				public void onSuccess() {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onError(String msg) {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
					imgSyncStatus.setImageResource(R.drawable.ic_navigation_cancel_light);
				}
			});
		}
	}
}
