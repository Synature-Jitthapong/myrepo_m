package com.synature.mpos;

import java.util.Calendar;

import com.synature.mpos.database.Computer;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.SyncMasterLog;
import com.synature.mpos.database.UserVerification;
import com.synature.pos.ShopData;

import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LoginActivity extends Activity implements OnClickListener, OnEditorActionListener{
	
	/**
	 * Request code for set system date
	 */
	public static final int REQUEST_FOR_SETTING_DATE = 1;
	
	/**
	 * Request code for setting
	 */
	public static final int REQUEST_FOR_SETTING = 2;
	
	public static final int CLICK_TIMES_TO_SETTING = 5;
	
	private int mStaffId;
	
	private Shop mShop;
	private Session mSession;
	private Computer mComputer;
	private Formater mFormat;
	private SyncMasterLog mSync;
	
	private Button mBtnLogin;
	private EditText mTxtUser;
	private EditText mTxtPass;
	private TextView mTvShopName;
	private TextView mTvSaleDate;
	private TextView mTvLastSession;
	private TextView mTvDeviceCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mTxtUser = (EditText) findViewById(R.id.txtUser);
		mTxtPass = (EditText) findViewById(R.id.txtPass);
		mTvShopName = (TextView) findViewById(R.id.tvShopName);
		mTvSaleDate = (TextView) findViewById(R.id.tvSaleDate);
		mTvLastSession = (TextView) findViewById(R.id.tvLastSession);
		mTvDeviceCode = (TextView) findViewById(R.id.tvDeviceCode);
		
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		mBtnLogin.setOnClickListener(this);
		mTxtPass.setOnEditorActionListener(this);

		mTvDeviceCode.setText(Utils.getDeviceCode(this));

		mSession = new Session(this);
		mShop = new Shop(this);
		mComputer = new Computer(this);
		mFormat = new Formater(this);
		mSync = new SyncMasterLog(this);
		
		try {
			if(mShop.getShopName() != null)
				mTvShopName.setText(mShop.getShopName());
			
			if(mSession.getSessionDate() != null && 
					!mSession.getSessionDate().isEmpty())
				mTvLastSession.setText(mFormat.dateFormat(mSession.getSessionDate()));
			else
				mTvLastSession.setText("- ");

			mTvSaleDate.setText(mFormat.dateFormat(Utils.getDate().getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_FOR_SETTING_DATE){
			if(resultCode == RESULT_OK){
				gotoMainActivity();
			}
		}
		if(requestCode == REQUEST_FOR_SETTING){
			if(resultCode == SettingsActivity.UPDATE_NEW_DATA){
				if(!mSync.IsAlreadySync())
					updateData();
			}
			if(resultCode == SettingsActivity.REFRESH_PARENT_ACTIVITY){
				startActivity(getIntent());
				finish();
			}
		}
	}
	
	/**
	 * Compare system date with session date
	 * if system date less than session date 
	 * this not allow to do anything and 
	 * force to date & time setting.
	 */
	private void checkSessionDate(){
		if(mSession.getCurrentSessionId() > 0){
			final Calendar sessionDate = Calendar.getInstance();
			sessionDate.setTimeInMillis(Long.parseLong(mSession.getSessionDate()));
			/*
			 *  sessionDate > currentDate
			 *  mPOS will force to go to date & time Settings
			 *  for setting correct date.
			 */
			if(sessionDate.getTime().compareTo(Utils.getDate().getTime()) > 0){
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
			}
			
			/*
			 * Current date > Session date
			 * mPOS will force to end day.
			 */
			else if(Utils.getDate().getTime().compareTo(sessionDate.getTime()) > 0){
				// check last session has already enddday ?
				if(!mSession.checkEndday(mSession.getSessionDate())){
					Utils.endday(LoginActivity.this, mShop.getShopId(), 
							mComputer.getComputerId(), mSession.getCurrentSessionId(), 
							mStaffId, 0, true);
					gotoMainActivity();
					// force end previous sale date
//					new AlertDialog.Builder(this)
//					.setCancelable(false)
//					.setTitle(R.string.system_date)
//					.setMessage(R.string.system_date_more_than_session)
//					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//						}
//					})
//					.setPositiveButton(R.string.endday, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							boolean endday = Utils.endday(LoginActivity.this, mShop.getShopId(), 
//									mComputer.getComputerId(), mSession.getCurrentSessionId(), 
//									mStaffId, 0, true);
//							if(endday){
//								new AlertDialog.Builder(LoginActivity.this)
//								.setCancelable(false)
//								.setTitle(R.string.endday)
//								.setMessage(R.string.endday_success)
//								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
//									
//									@Override
//									public void onClick(DialogInterface dialog, int which) {
//										gotoMainActivity();
//									}
//								}).show();
//							}
//						}
//					}).show();
				}else{
					gotoMainActivity();
				}
			}else{
				gotoMainActivity();
			}
		}else{
			gotoMainActivity();
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
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch(item.getItemId()){
		case R.id.itemSetting:
			intent = new Intent(LoginActivity.this, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_FOR_SETTING);
			return true;
		case R.id.itemUpdate:
			updateData();
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
	
	private void updateData(){
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setTitle(R.string.update_data);
		// checking device
		MPOSWebServiceClient.authenDevice(this, new MPOSWebServiceClient.AuthenDeviceListener() {
			
			@Override
			public void onPre() {
				progress.setMessage(getString(R.string.check_device_progress));
				progress.show();
			}
			
			@Override
			public void onPost() {
			}
			
			@Override
			public void onError(String msg) {
				if(progress.isShowing())
					progress.dismiss();
				new AlertDialog.Builder(LoginActivity.this)
				.setTitle(R.string.update_data)
				.setMessage(msg)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
			
			@Override
			public void onPost(final int shopId) {
				// load shop data
				MPOSWebServiceClient.loadShopData(LoginActivity.this, shopId, new ProgressListener(){

					@Override
					public void onPre() {
						progress.setMessage(getString(R.string.update_shop_progress));
					}

					@Override
					public void onPost() {
						// load product datat
						MPOSWebServiceClient.loadProductData(LoginActivity.this, shopId, new ProgressListener(){

							@Override
							public void onPre() {
								progress.setMessage(getString(R.string.update_product_progress));
							}

							@Override
							public void onPost() {
								if(progress.isShowing())
									progress.dismiss();
								startActivity(new Intent(LoginActivity.this, LoginActivity.class));
								finish();
							}

							@Override
							public void onError(String msg) {
								if(progress.isShowing())
									progress.dismiss();
								new AlertDialog.Builder(LoginActivity.this)
								.setTitle(R.string.update_data)
								.setMessage(msg)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								})
								.show();
							}
							
						});
					}

					@Override
					public void onError(String msg) {
						if(progress.isShowing())
							progress.dismiss();
						new AlertDialog.Builder(LoginActivity.this)
						.setTitle(R.string.update_data)
						.setMessage(msg)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}
					
				});
			}
		});
	}
	
	@Override
	protected void onResume() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		if(url.isEmpty()){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_FOR_SETTING);
		}else{
			mTxtUser.requestFocus();
		}
		super.onResume();
	}
			
	private void gotoMainActivity(){
		mTxtUser.setText(null);
		mTxtPass.setText(null);
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
		}else{
			enddingMultipleDay();
			startEnddayService();
			final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra("staffId", mStaffId);
			if(!mSync.IsAlreadySync()){
				final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
				progress.setCancelable(false);
				progress.setTitle(R.string.update_data);
				// load shop data
				MPOSWebServiceClient.loadShopData(LoginActivity.this, mShop.getShopId(), new ProgressListener(){
	
					@Override
					public void onPre() {
						progress.setMessage(getString(R.string.update_shop_progress));
						progress.show();
					}
	
					@Override
					public void onPost() {
						// load product data
						MPOSWebServiceClient.loadProductData(LoginActivity.this, mShop.getShopId(), new ProgressListener(){
	
							@Override
							public void onPre() {
								progress.setMessage(getString(R.string.update_product_progress));
							}
	
							@Override
							public void onPost() {
								if(progress.isShowing())
									progress.dismiss();
								startActivity(intent);
								finish();
							}
	
							@Override
							public void onError(String msg) {
								if(progress.isShowing())
									progress.dismiss();
								startActivity(intent);
								finish();
							}
							
						});
					}
	
					@Override
					public void onError(String msg) {
						if(progress.isShowing())
							progress.dismiss();
						startActivity(intent);
						finish();
					}
					
				});
			}else{
				startActivity(intent);
				finish();
			}
		}
	}
	
	private void startEnddayService(){
		// start endday service for send endday sale
		Intent enddayIntent = new Intent(LoginActivity.this, EnddaySaleService.class);
		enddayIntent.putExtra("staffId", mStaffId);
		enddayIntent.putExtra("shopId", mShop.getShopId());
		enddayIntent.putExtra("computerId", mComputer.getComputerId());
		startService(enddayIntent);
	}
	
	private void enddingMultipleDay(){
		String sessionDate = mSession.getSessionDate();
		if(!sessionDate.equals("")){
			Calendar sessCal = Calendar.getInstance();
			sessCal.setTimeInMillis(Long.parseLong(sessionDate));
			if(Utils.getDiffDay(sessCal) > 0){
				Utils.endingMultipleDay(LoginActivity.this, mShop.getShopId(), 
						mComputer.getComputerId(), mStaffId, sessCal);
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
					ShopData.Staff s = login.checkLogin();
					if(s != null){
						mStaffId = s.getStaffID();
						checkSessionDate();
						mTxtUser.setError(null);
						mTxtPass.setError(null);
					}else{
						mTxtUser.setError(null);
						mTxtPass.setError(getString(R.string.incorrect_password));
					}
				}else{
					mTxtUser.setError(getString(R.string.incorrect_user));
					mTxtPass.setError(null);
				}
			}else{
				mTxtUser.setError(null);
				mTxtPass.setError(getString(R.string.enter_password));
			}
		}else{
			mTxtUser.setError(getString(R.string.enter_username));
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
