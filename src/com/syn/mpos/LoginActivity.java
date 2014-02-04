package com.syn.mpos;

import com.syn.mpos.R;
import com.syn.mpos.provider.Login;
import com.syn.mpos.provider.Session;
import com.syn.mpos.provider.Util;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends Activity implements OnClickListener {
	private int mShopId;
	private int mComputerId;
	private int mSessionId;
	private int mStaffId;
	private Session mSession;
	private Button mBtnLogin;
	private EditText mTxtUser;
	private EditText mTxtPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mBtnLogin = (Button) findViewById(R.id.buttonLogin);
		mTxtUser = (EditText) findViewById(R.id.txtUser);
		mTxtPass = (EditText) findViewById(R.id.txtPass);
		
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		mBtnLogin.setOnClickListener(this);
		
		mTxtUser.setText("1");
		mTxtPass.setText("1");
		mTxtPass.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					checkLogin();
					return true;
				}
				return false;
			}
			
		});
	}

	private void init(){
		mSession = new Session(MPOSApplication.getWriteDatabase());
		mShopId = MPOSApplication.getShopId();
		mComputerId = MPOSApplication.getComputerId();
		
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL,
				"");
		if(url.equals("")){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
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
			updateData();
			return true;
		case R.id.itemAbout:
			intent = new Intent(LoginActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!mBtnLogin.isEnabled())
			mBtnLogin.setEnabled(true);
		init();
	}
			
	private void updateData(){
		final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
		final MPOSService mposService = new MPOSService();
		mposService.loadShopData(new ProgressListener(){

			@Override
			public void onPre() {
				progress.setMessage(LoginActivity.this.getString(R.string.sync_shop_progress));
				progress.show();
			}

			@Override
			public void onPost() {
				mposService.loadProductData(new ProgressListener(){

					@Override
					public void onPre() {
						progress.setMessage(LoginActivity.this.getString(R.string.sync_product_progress));
					}

					@Override
					public void onPost() {
						if(progress.isShowing())
							progress.dismiss();

						new AlertDialog.Builder(LoginActivity.this)
						.setMessage(R.string.update_data_success)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mShopId = MPOSApplication.getShopId();
								mComputerId = MPOSApplication.getComputerId();
							}
						})
						.show();
					}

					@Override
					public void onError(String msg) {
						if(progress.isShowing())
							progress.dismiss();
						
						new AlertDialog.Builder(LoginActivity.this)
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
	
	private void gotoMainActivity(){
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("staffId", mStaffId);
		intent.putExtra("sessionId", mSessionId);
		intent.putExtra("shopId", mShopId);
		intent.putExtra("computerId", mComputerId);
		startActivity(intent);
	}
	
	private void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!mTxtUser.getText().toString().isEmpty()){
			user = mTxtUser.getText().toString();
			
			if(!mTxtPass.getText().toString().isEmpty()){
				pass = mTxtPass.getText().toString();
				Login login = new Login(MPOSApplication.getWriteDatabase(), user, pass);
				
				if(login.checkUser()){
					ShopData.Staff s = login.checkLogin();
					MPOSService mposService = new MPOSService();
					if(s != null){
						mStaffId = s.getStaffID();			
						mSessionId = mSession.getCurrentSession(mComputerId, mStaffId);
						if(mSessionId == 0){
							// auto endday if have session less than now
							mSession.autoEnddaySession(String.valueOf(Util.getDate().getTimeInMillis()), mStaffId);
							mSessionId = mSession.addSession(mShopId, mComputerId, mStaffId, 0);
						}

						// check endday
						String sessionDate = mSession.getSessionDate(mComputerId);
						// count check session endday detail
						if(mSession.getSessionEnddayDetail(sessionDate) == 0){
							final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
							progress.setMessage(LoginActivity.this.getString(R.string.sync_product_progress));
							mposService.loadProductData(new ProgressListener() {
								@Override
								public void onError(String mesg) {
									if(progress.isShowing())
										progress.dismiss();
									mBtnLogin.setEnabled(true);
									new AlertDialog.Builder(LoginActivity.this)
											.setMessage(mesg)
											.setNeutralButton(R.string.close,
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(DialogInterface dialog,
																int which) {
															gotoMainActivity();
														}
													}).show();
								}

								@Override
								public void onPre() {
									progress.show();
								}

								@Override
								public void onPost() {
									if(progress.isShowing())
										progress.dismiss();
									gotoMainActivity();
								}
							});
						}else{
							mSession.deleteSession(mSessionId);
							// alert endday
							new AlertDialog.Builder(LoginActivity.this)
							.setTitle(R.string.endday)
							.setMessage(LoginActivity.this.getString(R.string.sale_date) + " " +
									MPOSApplication.getGlobalProperty().dateFormat(Util.getDate().getTime()) + "\n" +
									LoginActivity.this.getString(R.string.alredy_endday))
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							.show();
						}
					}else{
						new AlertDialog.Builder(LoginActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.login)
						.setMessage(R.string.incorrect_password)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						})
						.show();
					}
				}else{
					new AlertDialog.Builder(LoginActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.login)
					.setMessage(R.string.incorrect_user)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.show();
				}
			}else{
				new AlertDialog.Builder(LoginActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.login)
				.setMessage(R.string.enter_password)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.show();
			}
		}else{
			new AlertDialog.Builder(LoginActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.login)
			.setMessage(R.string.enter_username)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
			.show();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.buttonLogin:
			mBtnLogin.setEnabled(false);
			if(mShopId == 0){
				// sync shop
				final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
				progress.setMessage(LoginActivity.this.getString(R.string.sync_shop_progress));
				final MPOSService mposService = new MPOSService();
				mposService.loadShopData(new ProgressListener() {
	
					@Override
					public void onError(String mesg) {
						if(progress.isShowing())
							progress.dismiss();
						mBtnLogin.setEnabled(true);
						new AlertDialog.Builder(LoginActivity.this)
						.setMessage(mesg)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}

					@Override
					public void onPre() {
						progress.show();
					}

					@Override
					public void onPost() {
						if(progress.isShowing())
							progress.dismiss();
						mShopId = MPOSApplication.getShopId();
						mComputerId = MPOSApplication.getComputerId();
						
						checkLogin();
					}
				});
			}else{
				checkLogin();
			}
			break;
		}
	}
}
