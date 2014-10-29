package com.synature.mpos;

import java.io.File;
import java.util.Calendar;

import com.synature.mpos.SoftwareExpirationChecker.SoftwareExpirationCheckerListener;
import com.synature.mpos.database.ComputerDao;
import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.ShopDao;
import com.synature.mpos.database.SoftwareUpdateDao;
import com.synature.mpos.database.StaffsDao;
import com.synature.mpos.database.SyncHistoryDao;
import com.synature.mpos.database.UserVerification;
import com.synature.mpos.database.model.SoftwareUpdate;
import com.synature.pos.Staff;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
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
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LoginActivity extends Activity implements OnClickListener, 
	OnEditorActionListener, UserVerifyDialogFragment.OnCheckPermissionListener{
	
	/**
	 * Request code for set system date
	 */
	public static final int REQUEST_FOR_SETTING_DATE = 1;
	
	public static enum WhatToDo{
		VIEW_REPORT,
		UTILITY
	};

	private WhatToDo mWhatToDo;
	
	private int mStaffId;
	private int mStaffRoleId;
	
	private ShopDao mShop;
	private SessionDao mSession;
	private ComputerDao mComputer;
	private GlobalPropertyDao mFormat;
	private SyncHistoryDao mSync;
	
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

		mSession = new SessionDao(this);
		mShop = new ShopDao(this);
		mComputer = new ComputerDao(this);
		mFormat = new GlobalPropertyDao(this);
		mSync = new SyncHistoryDao(this);

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
				Calendar lastSessCal = Calendar.getInstance();
				lastSessCal.setTimeInMillis(Long.parseLong(mSession.getLastSessionDate()));
				Utils.endingMultipleDay(LoginActivity.this, mShop.getShopId(), 
						mComputer.getComputerId(), mStaffId, lastSessCal);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		UserVerifyDialogFragment userFragment = null;
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
		case R.id.itemPerformTest:
			PerformTest f = PerformTest.newInstance();
			f.show(getFragmentManager(), "PerformTest");
			return true;
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemUtils:
			mWhatToDo = WhatToDo.UTILITY;
			userFragment = UserVerifyDialogFragment.newInstance(0);
			userFragment.show(getFragmentManager(), UserVerifyDialogFragment.TAG);
			return true;
		case R.id.itemReport:
			mWhatToDo = WhatToDo.VIEW_REPORT;
			userFragment = UserVerifyDialogFragment.newInstance(0);
			userFragment.show(getFragmentManager(), UserVerifyDialogFragment.TAG);
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}

	private void createUtilsPopup(final View v){
		if(v != null){
			PopupMenu popup = new PopupMenu(this, v);
			popup.inflate(R.menu.action_utility);
			popup.setOnMenuItemClickListener(new OnMenuItemClickListener(){
	
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					Intent intent = null;
					switch(item.getItemId()){
					case R.id.itemBackup:
						Utils.backupDatabase(LoginActivity.this);
						return true;
					case R.id.itemRestore:
						RestoreDatabaseFragment restoreFragment = RestoreDatabaseFragment.newInstance();
						restoreFragment.show(getFragmentManager(), RestoreDatabaseFragment.TAG);
						return true;
					case R.id.itemSendEndday:
						intent = new Intent(LoginActivity.this, SendEnddayActivity.class);
						intent.putExtra("staffId", mStaffId);
						intent.putExtra("shopId", mShop.getShopId());
						intent.putExtra("computerId", mComputer.getComputerId());
						startActivity(intent);
						return true;
					case R.id.itemSendSale:
						intent = new Intent(LoginActivity.this, SendSaleActivity.class);
						intent.putExtra("staffId", mStaffId);
						intent.putExtra("shopId", mShop.getShopId());
						intent.putExtra("computerId", mComputer.getComputerId());
						startActivity(intent);
						return true;
					case R.id.itemResetEndday:
						ResetEnddayStateDialogFragment resetFragment = ResetEnddayStateDialogFragment.newInstance();
						resetFragment.show(getFragmentManager(), ResetEnddayStateDialogFragment.TAG);
						return true;
					case R.id.itemClearSale:
						ClearSaleDialogFragment clearSaleFragment = ClearSaleDialogFragment.newInstance();
						clearSaleFragment.show(getFragmentManager(), ClearSaleDialogFragment.TAG);
						return true;
					default :
						return false;
					}
				}
				
			});
			popup.show();
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
		checkSoftwareUpdate();
		displayWelcome();
		super.onResume();
	}
	
	private void checkSoftwareUpdate(){
		final SoftwareUpdateDao su = new SoftwareUpdateDao(this);
		final SoftwareUpdate update = su.getUpdateData();
		if(update != null){
			if(update.isDownloaded()){
				if(!update.isAlreadyUpdated()){
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(R.string.software_update);
					builder.setMessage(R.string.software_update_mesg);
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
							File apkFile = new File(download + File.separator + Utils.UPDATE_FILE_NAME);
						    Intent intent = new Intent(Intent.ACTION_VIEW);
						    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
						    startActivity(intent);
						}
					});
					AlertDialog d = builder.create();
					d.show();
				}
			}
		}
	}
	
	private void displayWelcome(){
		if(Utils.isEnableWintecCustomerDisplay(this)){
			WintecCustomerDisplay dsp = new WintecCustomerDisplay(this);
			dsp.displayWelcome();
		}
	}
	
	private void requestValidUrl(){
		new SoftwareRegister(this, new RegisterValidUrlListener()).execute(Utils.REGISTER_URL);
		//new DeviceChecker(LoginActivity.this, new DeviceCheckerListener()).execute(Utils.getFullUrl(LoginActivity.this));
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
		final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("staffRoleId", mStaffRoleId);
		SoftwareExpirationChecker swChecker = new SoftwareExpirationChecker(this, new SoftwareExpirationCheckerListener() {
			
			@Override
			public void onNotExpired() {
				startActivity(intent);
		        finish();	
			}
			
			@Override
			public void onExpire(final Calendar lockDate, final boolean isLocked) {
				String msg = getString(R.string.software_expired_msg);
				msg += " " + mFormat.dateFormat(lockDate.getTime());
				if(isLocked){
					msg = getString(R.string.software_locked);
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setTitle(R.string.software_expired);
				builder.setMessage(msg);
				builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(!isLocked){
							startActivity(intent);
					        finish();	
						}
					}
				});
				AlertDialog d = builder.create();
				d.show();
				
			}
		});
		swChecker.checkExpDate();
	}
	
	public void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!TextUtils.isEmpty(mTxtUser.getText())){
			user = mTxtUser.getText().toString();
			
			if(!TextUtils.isEmpty(mTxtPass.getText())){
				pass = mTxtPass.getText().toString();
				UserVerification login = new UserVerification(LoginActivity.this, user, pass);
				
				if(login.checkUser()){
					Staff s = login.checkLogin();
					if(s != null){
						mStaffId = s.getStaffID();
						mStaffRoleId = s.getStaffRoleID();
						mTxtUser.setError(null);
						mTxtPass.setError(null);
						mTxtUser.setText(null);
						mTxtPass.setText(null);
						if(checkSessionDate()){
							StaffsDao st = new StaffsDao(LoginActivity.this);
							if(st.checkAccessPOSPermission(s.getStaffRoleID())){
								gotoMainActivity();
							}else{
								new AlertDialog.Builder(LoginActivity.this)
								.setTitle(R.string.permission_required)
								.setMessage(R.string.not_have_permission_to_access_pos)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).show();
							}
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

	@Override
	public void onAllow(int staffId, int permissionId) {
		switch(mWhatToDo){
		case VIEW_REPORT:
			Intent intent = new Intent(this, SaleReportActivity.class);
			intent.putExtra("staffId", staffId);
			startActivity(intent);
			break;
		case UTILITY:
			createUtilsPopup(findViewById(R.id.itemUtils));
			break;
		}
	}
}
