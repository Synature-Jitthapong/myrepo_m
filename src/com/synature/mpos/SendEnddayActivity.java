package com.synature.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SendEnddayActivity extends Activity {

	private GlobalPropertyDao mFormat;
	
	private int mStaffId;
	private int mShopId;
	private int mComputerId;
	private boolean mAutoClose = false;
	
	private SessionDao mSession;
	private TransactionDao mTrans;
	private List<String> mSessLst;
	private EnddayListAdapter mEnddayAdapter;
	
	private ListView mLvEndday;
	private ProgressBar mProgress;
	
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
		mProgress = (ProgressBar) findViewById(R.id.progressBar1);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mAutoClose = intent.getBooleanExtra("autoClose", false);
		
		mFormat = new GlobalPropertyDao(this);
		mSession = new SessionDao(this);
		mTrans = new TransactionDao(this);
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
				//sendEndday();
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

		public EnddayReceiver(Handler handler) {
			super(handler);
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				break;
			case MPOSServiceBase.RESULT_ERROR:
				break;
			}
		}
		
	}
	
//	private void sendEndday(){
//		mChkNetworkProgress.setVisibility(View.GONE);
//		List<String> sessionLst = mSession.listSessionEnddayNotSend();
//		if(sessionLst.size() > 0){
//			ExecutorService executor = Executors.newFixedThreadPool(5);
//			JSONSaleGenerator jsonGenerator = new JSONSaleGenerator(this);
//			Iterator<String> it = sessionLst.iterator();
//			try {
//				while(it.hasNext()){
//					final String sessionDate = it.next();
//					String jsonEndday = jsonGenerator.generateEnddaySale(sessionDate);
//					executor.execute(new EndDaySaleSender(SendEnddayActivity.this, mShopId, mComputerId, mStaffId, jsonEndday,
//							new WebServiceWorkingListener(){
//
//								@Override
//								public void onPostExecute() {
//									setSendEnddayDataStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
//									mItemClose.setEnabled(true);
//									mItemSend.setVisible(true);
//									mItemProgress.setVisible(false);
//									
//									setupAdapter();
//									
//									if(mAutoClose){
//										new AlertDialog.Builder(SendEnddayActivity.this)
//										.setCancelable(false)
//										.setTitle(R.string.send_endday_data)
//										.setMessage(R.string.send_endday_data_success)
//										.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
//											
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												setResult(RESULT_OK);
//												finish();
//											}
//										})
//										.show();
//									}else{
//										Toast.makeText(SendEnddayActivity.this, 
//												getString(R.string.send_sale_data_success), Toast.LENGTH_SHORT);
//									}
//								}
//
//								@Override
//								public void onError(String msg) {
//									setSendEnddayDataStatus(sessionDate, MPOSDatabase.NOT_SEND);
//									mItemClose.setEnabled(true);
//									mItemSend.setVisible(true);
//									mItemProgress.setVisible(false);
//									Toast.makeText(SendEnddayActivity.this, msg, Toast.LENGTH_SHORT);
//								}
//						
//					}));
//				}
//			} finally {
//				executor.shutdown();
//			}
//		}
//	}
	
	private void setSendEnddayDataStatus(String sessionDate, int status){
		mSession.updateSessionEnddayDetail(sessionDate, status);
		mTrans.updateTransactionSendStatus(sessionDate, status);
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
