package com.synature.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.synature.mpos.NetworkConnectionChecker.NetworkCheckerListener;
import com.synature.mpos.SaleService.LocalBinder;
import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.SessionDao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SendEnddayActivity extends Activity {

	private SaleService mPartService;
	private boolean mBound = false;
	
	private GlobalPropertyDao mFormat;
	
	private int mStaffId;
	private int mShopId;
	private int mComputerId;
	private boolean mAutoClose = false;
	
	private SessionDao mSession;
	private List<String> mSessLst;
	private EnddayListAdapter mEnddayAdapter;
	
	private ListView mLvEndday;
	private View mChkNetworkProgress;
	
	private MenuItem mItemClose;
	private MenuItem mItemSend;
	private MenuItem mItemProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 500;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	    setFinishOnTouchOutside(false);
		setContentView(R.layout.activity_send_endday);
		
		mLvEndday = (ListView) findViewById(R.id.lvEndday);
		mChkNetworkProgress = findViewById(R.id.check_network_progress);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mAutoClose = intent.getBooleanExtra("autoClose", false);
		
		mFormat = new GlobalPropertyDao(this);
		mSession = new SessionDao(this);
		mSessLst = new ArrayList<String>();

		setupAdapter();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, SaleService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBound){
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.send_endday, menu);
		mItemClose = menu.findItem(R.id.itemClose);
		mItemSend = menu.findItem(R.id.itemSendAll);
		mItemProgress = menu.findItem(R.id.itemProgress);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				setResult(RESULT_OK);
				finish();
			return true;
			case R.id.itemClose:
				setResult(RESULT_OK);
				finish();
				return true;
			case R.id.itemSendAll:
				new NetworkConnectionChecker(this, new NetworkCheckerListener() {
					
					@Override
					public void serverProblem(int code, String msg) {
						mItemClose.setEnabled(true);
						mItemSend.setVisible(true);
						mItemProgress.setVisible(false);
						mChkNetworkProgress.setVisibility(View.GONE);
						Utils.makeToask(SendEnddayActivity.this, msg);
					}
					
					@Override
					public void onLine() {
						mChkNetworkProgress.setVisibility(View.GONE);
						mPartService.sendAllEndday(mShopId, mComputerId, mStaffId, mSendListener);
					}
					
					@Override
					public void offLine(String msg) {
						mItemClose.setEnabled(true);
						mItemSend.setVisible(true);
						mItemProgress.setVisible(false);
						mChkNetworkProgress.setVisibility(View.GONE);
						Utils.makeToask(SendEnddayActivity.this, msg);
					}

					@Override
					public void onPre() {
						mItemClose.setEnabled(false);
						mItemSend.setVisible(false);
						mItemProgress.setVisible(true);
						mChkNetworkProgress.setVisibility(View.VISIBLE);
					}
				}).execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setupAdapter(){
		mSessLst = mSession.listSessionEnddayNotSend();
		if(mEnddayAdapter == null){
			mEnddayAdapter = new EnddayListAdapter();
			mLvEndday.setAdapter(mEnddayAdapter);
		}
		mEnddayAdapter.notifyDataSetChanged();
		mLvEndday.setSelection(mEnddayAdapter.getCount() - 1);
	}
	
	private WebServiceWorkingListener mSendListener = new WebServiceWorkingListener(){

		@Override
		public void onPreExecute() {
		}

		@Override
		public void onPostExecute() {
			mItemClose.setEnabled(true);
			mItemSend.setVisible(true);
			mItemProgress.setVisible(false);
			
			setupAdapter();
			
			if(mAutoClose){
				new AlertDialog.Builder(SendEnddayActivity.this)
				.setCancelable(false)
				.setTitle(R.string.send_endday_data)
				.setMessage(R.string.send_endday_data_success)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_OK);
						finish();
					}
				})
				.show();
			}else{
				Utils.makeToask(SendEnddayActivity.this, getString(R.string.send_sale_data_success));
			}
		}

		@Override
		public void onError(final String msg) {
			mItemClose.setEnabled(true);
			mItemSend.setVisible(true);
			mItemProgress.setVisible(false);
			Utils.makeToask(SendEnddayActivity.this, msg);
		}

		@Override
		public void onProgressUpdate(int value) {
		}
		
	};
	
	private class EnddayListAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater = getLayoutInflater();
		
		@Override
		public int getCount() {
			return mSessLst != null ? mSessLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mSessLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.endday_list_template, parent, false);
			}
			String sessionDate = mSessLst.get(position);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Long.parseLong(sessionDate));
			TextView tvSaleDate = (TextView) convertView.findViewById(R.id.tvSaleDate);
			tvSaleDate.setText(mFormat.dateFormat(cal.getTime()));
			return convertView;
		}
		
	}
	
	/**
	 * PartialSaleService Connection
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mPartService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
		
	};
}
