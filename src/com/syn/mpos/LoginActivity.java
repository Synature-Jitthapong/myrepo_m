package com.syn.mpos;

import com.syn.mpos.R;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;
import com.syn.mpos.transaction.MPOSSession;
import com.syn.pos.ShopData;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private int mShopId;
	private int mComputerId;
	private int mSessionId;
	private Shop mShop;
	private MPOSSession mSession;
	private Setting mSetting;
	private Setting.Connection mConn;
	private String mDeviceCode;
	
	private EditText mTxtUser;
	private EditText mTxtPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mDeviceCode = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		
		mTxtUser = (EditText) findViewById(R.id.editTextUserName);
		mTxtPass = (EditText) findViewById(R.id.editTextPassWord);
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		
		mTxtUser.setText("1");
		mTxtPass.setText("1");
	}

	private void init(){
		mSetting = new Setting(this);
		mConn = mSetting.getConnection();
		
		mConn.setFullUrl(mConn.getProtocal() + mConn.getAddress() + 
				"/" + mConn.getBackoffice() + "/" + mConn.getService());
		
		mShop = new Shop(LoginActivity.this);
		mShopId = mShop.getShopProperty().getShopID();
		mComputerId = mShop.getComputerProperty().getComputerID();
		
		mSession = new MPOSSession(LoginActivity.this);
		
		if(mConn.getAddress() == null || 
				mConn.getBackoffice() == null){
			Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemSetting:
			Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}
	
	public void loginClicked(final View v){
		if(mShopId == 0){
			MPOSService mposService = new MPOSService(this, mConn);
			mposService.loadShopData(mDeviceCode, new MPOSService.OnServiceProcessListener() {
				
				@Override
				public void onSuccess() {
					checkLogin();
				}
				
				@Override
				public void onError(String mesg) {
					
				}
			});
		}else{
			checkLogin();
		}
	}
	
	private void gotoMainActivity(int staffId){
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("staffId", staffId);
		intent.putExtra("sessionId", mSessionId);
		startActivity(intent);
	}
	
	private void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!mTxtUser.getText().toString().isEmpty()){
			user = mTxtUser.getText().toString();
			
			if(!mTxtPass.getText().toString().isEmpty()){
				pass = mTxtPass.getText().toString();
				Login login = new Login(LoginActivity.this, user, pass);
				
				if(login.checkUser()){
					ShopData.Staff s = login.checkLogin();
					
					if(s != null){
						mSessionId = mSession.getCurrentSession(mShopId, mComputerId);
						if(mSessionId == 0){
							mSessionId = mSession.addSession(mShopId, mComputerId, s.getStaffID(), 0);
						}
						
						gotoMainActivity(s.getStaffID());
					}else{
						new AlertDialog.Builder(getApplicationContext())
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
					new AlertDialog.Builder(getApplicationContext())
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
				new AlertDialog.Builder(getApplicationContext())
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
			new AlertDialog.Builder(getApplicationContext())
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
}
