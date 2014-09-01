package com.synature.mpos;

import java.util.Calendar;

import com.synature.mpos.common.MPOSActivityBase;
import com.synature.mpos.database.Computer;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.SyncHistory;
import com.synature.mpos.database.UserVerification;
import com.synature.pos.Staff;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LoginActivity extends MPOSActivityBase implements OnClickListener, OnEditorActionListener{
	
	/**
	 * Request code for set system date
	 */
	public static final int REQUEST_FOR_SETTING_DATE = 1;
	
	public static final int CLICK_TIMES_TO_SETTING = 5;
	
	private int mStaffId;
	
	private Shop mShop;
	private Session mSession;
	private Computer mComputer;
	private Formater mFormat;
	private SyncHistory mSync;
	
	private Button mBtnLogin;
	private EditText mTxtUser;
	private EditText mTxtPass;
	private TextView mTvDeviceCode;
	private TextView mTvLastSyncTime;
	private TextView mTvVersion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mTxtUser = (EditText) findViewById(R.id.txtUser);
		mTxtPass = (EditText) findViewById(R.id.txtPass);
		mTvDeviceCode = (TextView) findViewById(R.id.tvDeviceCode);
		mTvLastSyncTime = (TextView) findViewById(R.id.tvLastSyncTime);
		mTvVersion = (TextView) findViewById(R.id.tvVersion);
		
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		mBtnLogin.setOnClickListener(this);
		mTxtPass.setOnEditorActionListener(this);

		mTvDeviceCode.setText(Utils.getDeviceCode(this));
		mTvVersion.setText(getString(R.string.version) + " " + Utils.getSoftWareVersion(this));

		mSession = new Session(this);
		mShop = new Shop(this);
		mComputer = new Computer(this);
		mFormat = new Formater(this);
		mSync = new SyncHistory(this);

		try {
			if(mShop.getShopName() != null){
				setTitle(mShop.getShopName());
				getActionBar().setSubtitle(mComputer.getComputerProperty().getComputerName());
			}
			mTvLastSyncTime.setText(getString(R.string.last_update) + " " + mFormat.dateTimeFormat(mSync.getLastSyncTime()));
		} catch (Exception e) {
			// mFormat may be null if first initial
			e.printStackTrace();
		}
		
		// sync new master data every day
		if(isAlreadySetUrl()){
			if(!mSync.IsAlreadySync())
				requestValidUrl();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_FOR_SETTING_DATE){
			if(resultCode == RESULT_OK){
				gotoMainActivity();
			}
		}
	}
	
	/**
	 * @author j1tth4
	 * Listener for reference to checking device state
	 */
	private class DeviceCheckerListener implements DeviceChecker.AuthenDeviceListener{

		private ProgressDialog mProgress;
		
		public DeviceCheckerListener(){
			mProgress = new ProgressDialog(LoginActivity.this);
			mProgress.setCancelable(false);
			mProgress.setMessage(getString(R.string.loading));
		}
		
		@Override
		public void onPreExecute() {
			mProgress.show();
		}

		@Override
		public void onPostExecute() {
		}

		@Override
		public void onError(String msg) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new AlertDialog.Builder(LoginActivity.this)
			.setMessage(msg)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}

		@Override
		public void onPostExecute(int shopId) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new MasterDataLoader(LoginActivity.this, shopId, new MasterLoaderListener()).execute(Utils.getFullUrl(LoginActivity.this));
		}

		@Override
		public void onProgressUpdate(int value) {
		}
		
	}
	
	/**
	 * @author j1tth4
	 * Listener for 
	 */
	private class MasterLoaderListener implements WebServiceWorkingListener{

		private ProgressDialog mProgress;
		
		public MasterLoaderListener(){
			mProgress = new ProgressDialog(LoginActivity.this);
			mProgress.setMessage(getString(R.string.load_master_progress));
//			mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//			mProgress.setIndeterminate(false);
//			mProgress.setMax(100);
			mProgress.setCancelable(false);
		}
		
		@Override
		public void onPreExecute() {
			mProgress.show();
		}

		@Override
		public void onPostExecute() {
			if(mProgress.isShowing())
				mProgress.dismiss();
			startActivity(new Intent(LoginActivity.this, LoginActivity.class));
			finish();
		}

		@Override
		public void onError(String msg) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new AlertDialog.Builder(LoginActivity.this)
			.setMessage(msg)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}

		@Override
		public void onProgressUpdate(int value) {
			//mProgress.setProgress(value);
		}
		
	}
	
	/**
	 * @return true if not have the session that is not end
	 */
	private boolean checkSessionDate(){
		// check if have session
		if(mSession.getLastSessionId() > 0){
			// get last session date
			final Calendar lastSessDate = Calendar.getInstance();
			lastSessDate.setTimeInMillis(Long.parseLong(mSession.getLastSessionDate()));
			/*
			 *  sessionDate > currentDate
			 *  mPOS will force to go to date & time Settings
			 *  for setting correct date.
			 */
			if(lastSessDate.getTime().compareTo(Utils.getDate().getTime()) > 0){
				new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle(R.string.system_date)
				.setMessage(R.string.system_date_less)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(R.string.date_time_setting, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivityForResult(
								new Intent(android.provider.Settings.ACTION_DATE_SETTINGS),
								REQUEST_FOR_SETTING_DATE);
					}
				}).show();
				return false;
			}else if(Utils.getDate().getTime().compareTo(lastSessDate.getTime()) > 0){
				/*
				 * Current date > Session date
				 * mPOS will force to end previous day.
				 */
				if(!mSession.checkEndday(mSession.getLastSessionDate())){
					Utils.endday(LoginActivity.this, mShop.getShopId(), 
							mComputer.getComputerId(), mSession.getLastSessionId(), 
							mStaffId, 0, true);
				}
			}else{
				if(mSession.checkEndday(String.valueOf(Utils.getDate().getTimeInMillis()))){
					String enddayMsg = getString(R.string.sale_date) 
							+ " " + mFormat.dateFormat(Utils.getDate().getTime()) 
							+ " " + getString(R.string.alredy_endday);
					new AlertDialog.Builder(this)
					.setCancelable(false)
					.setTitle(R.string.endday)
					.setMessage(enddayMsg)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
					return false;
				}
			}
		}
		return true;
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
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch(item.getItemId()){
		case R.id.itemSetting:
			intent = new Intent(LoginActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.itemUpdate:
			requestValidUrl();
			return true;
		case R.id.itemAbout:
			intent = new Intent(LoginActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.itemExit:
			exit();
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}

	private void exit(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.exit)
		.setMessage(R.string.confirm_exit)
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).show();
	}
	
	@Override
	protected void onResume() {
		if(!isAlreadySetUrl()){
			requestValidUrl();
		}else{
			mTxtUser.requestFocus();
		}
		super.onResume();
	}
			
	private void requestValidUrl(){
		new MainUrlRegister(this, new RegisterValidUrlListener()).execute(Utils.MAIN_URL);
	}
	
	private class RegisterValidUrlListener implements WebServiceWorkingListener{

		private ProgressDialog mProgress;
		
		public RegisterValidUrlListener(){
			mProgress = new ProgressDialog(LoginActivity.this);
			mProgress.setCancelable(false);
			mProgress.setMessage(getString(R.string.loading));
		}
		
		@Override
		public void onPreExecute() {
			mProgress.show();
		}

		@Override
		public void onProgressUpdate(int value) {
		}

		@Override
		public void onPostExecute() {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new DeviceChecker(LoginActivity.this, new DeviceCheckerListener()).execute(Utils.getFullUrl(LoginActivity.this));
		}

		@Override
		public void onError(String msg) {
			if(mProgress.isShowing())
				mProgress.dismiss();
			new AlertDialog.Builder(LoginActivity.this)
			.setCancelable(false)
			.setMessage(msg)
			.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					requestValidUrl();
				}
			})
			.show();
		}
	}
	
	private boolean isAlreadySetUrl(){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		if(url.isEmpty()){
			return false;
		}	
		return true;
	}
	
	private void gotoMainActivity(){
		enddingMultipleDay();
		startEnddayService();
		final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("staffId", mStaffId);
		startActivity(intent);
        finish();
	}
	
	/**
	 * Start endday service for send endday sale
	 */
	private void startEnddayService(){
		Intent enddayIntent = new Intent(LoginActivity.this, EnddaySaleService.class);
		enddayIntent.putExtra("staffId", mStaffId);
		enddayIntent.putExtra("shopId", mShop.getShopId());
		enddayIntent.putExtra("computerId", mComputer.getComputerId());
		startService(enddayIntent);
	}
	
	/**
	 * ending multiple day
	 */
	private void enddingMultipleDay(){
		String lastSessDate = mSession.getLastSessionDate();
		if(!lastSessDate.equals("")){
			Calendar lastSessCal = Calendar.getInstance();
			lastSessCal.setTimeInMillis(Long.parseLong(lastSessDate));
			if(Utils.getDiffDay(lastSessCal) > 0){
				Utils.endingMultipleDay(LoginActivity.this, mShop.getShopId(), 
						mComputer.getComputerId(), mStaffId, lastSessCal);
			}
		}
	}
	
	public void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!TextUtils.isEmpty(mTxtUser.getText())){
			user = mTxtUser.getText().toString();
			
			if(!TextUtils.isEmpty(mTxtPass.getText())){
				pass = mTxtPass.getText().toString();
				UserVerification login = new UserVerification(getApplicationContext(), user, pass);
				
				if(login.checkUser()){
					Staff s = login.checkLogin();
					if(s != null){
						mStaffId = s.getStaffID();
						mTxtUser.setError(null);
						mTxtPass.setError(null);
						mTxtUser.setText(null);
						mTxtPass.setText(null);
						if(checkSessionDate()){
							gotoMainActivity();
						}
					}else{
						mTxtUser.setError(null);
						mTxtPass.setError(getString(R.string.incorrect_password));
					}
				}else{
					mTxtUser.setError(getString(R.string.incorrect_staff_code));
					mTxtPass.setError(null);
				}
			}else{
				mTxtUser.setError(null);
				mTxtPass.setError(getString(R.string.enter_password));
			}
		}else{
			mTxtUser.setError(getString(R.string.enter_staff_code));
			mTxtPass.setError(null);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE){
			checkLogin();
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.btnLogin:
				checkLogin();
				break;
		}
	}
}
