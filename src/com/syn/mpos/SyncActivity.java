package com.syn.mpos;
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
import android.app.AlertDialog;
import android.content.Context;

public class SyncActivity extends AlertDialog.Builder implements OnItemClickListener{
	public static final int SYNC_PRODUCT = 0;
	public static final int SYNC_SALE = 1;
	
	private Context mContext;
	private int mStaffId;
	private ListView mLvSync;
	
	public SyncActivity(Context context, int staffId) {
		super(context);
		mContext = context;
		LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View syncView = inflater.inflate(R.layout.activity_sync, null);
		mLvSync = (ListView) syncView.findViewById(R.id.lvSync);
	
		String syncArr[] = context.getResources().getStringArray(R.array.update_array);
		mLvSync.setAdapter(new SyncListAdapter(syncArr));
		mLvSync.setOnItemClickListener(this);
		setView(syncView);
	}

	private class SyncListAdapter extends BaseAdapter{
		
		private String[] mWhatSyncArr;
		private LayoutInflater mInflater;
		
		public SyncListAdapter(String[] whatSyncArr){
			mWhatSyncArr = whatSyncArr;
			mInflater = (LayoutInflater) 
					mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		
		if(id==SYNC_PRODUCT){
			MPOSService mposService = new MPOSService();
			ProgressListener loadProductListener = new ProgressListener() {

				@Override
				public void onPre() {
					imgSyncStatus.setVisibility(View.GONE);
					progress.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPost() {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
				}

				@Override
				public void onError(String msg) {
					progress.setVisibility(View.INVISIBLE);
					imgSyncStatus.setVisibility(View.VISIBLE);
					imgSyncStatus
							.setImageResource(R.drawable.ic_navigation_cancel_light);
				}
			};
			mposService.loadProductData(loadProductListener);
			
		}else if(id==SYNC_SALE){
			ProgressListener sendSaleListener =
					new ProgressListener(){

						@Override
						public void onPre() {
							imgSyncStatus.setVisibility(View.GONE);
							progress.setVisibility(View.VISIBLE);
						}

						@Override
						public void onPost() {
							progress.setVisibility(View.INVISIBLE);
							imgSyncStatus.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError(String msg) {
							progress.setVisibility(View.INVISIBLE);
							imgSyncStatus.setVisibility(View.VISIBLE);
							imgSyncStatus.setImageResource(R.drawable.ic_navigation_cancel_light);
						}};
					
			MPOSUtil.doSendSale(mStaffId, sendSaleListener);
		}
	}
}
