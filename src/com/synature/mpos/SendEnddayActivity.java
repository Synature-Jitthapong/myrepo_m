package com.synature.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.SessionDao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
import android.widget.Toast;

public class SendEnddayActivity extends Activity {

	private GlobalPropertyDao mFormat;
	
	private int mStaffId;
	private int mShopId;
	private int mComputerId;
	
	private SessionDao mSession;
	private List<String> mSessLst;
	private EnddayListAdapter mEnddayAdapter;
	
	private ListView mLvEndday;
	
	private MenuItem mItemSend;
	
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

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		
		mFormat = new GlobalPropertyDao(this);
		mSession = new SessionDao(this);
		mSessLst = new ArrayList<String>();

		setupAdapter();
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
		mItemSend = menu.findItem(R.id.itemSendAll);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				setResult(RESULT_OK);
				finish();
			return true;
			case R.id.itemSendAll:
				sendEndday();
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
	
	private class EnddayReceiver extends ResultReceiver{

		private ProgressDialog progress;
		
		public EnddayReceiver(Handler handler) {
			super(handler);
			progress = new ProgressDialog(SendEnddayActivity.this);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(getString(R.string.please_wait));
			progress.show();
			mItemSend.setEnabled(false);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				progress.dismiss();
				setupAdapter();
				break;
			case MPOSServiceBase.RESULT_ERROR:
				progress.dismiss();
				mItemSend.setEnabled(true);
				Toast.makeText(SendEnddayActivity.this, resultData.getString("msg"), Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
	}
	
	private void sendEndday(){
		Intent intent = new Intent(this, SaleSenderService.class);
		intent.putExtra("what", SaleSenderService.SEND_ENDDAY);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("computerId", mComputerId);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("sendSaleReceiver", new EnddayReceiver(new Handler()));
		startService(intent);
	}
	
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
}
