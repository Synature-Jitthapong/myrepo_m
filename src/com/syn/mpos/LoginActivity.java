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
	private String deviceCode;
	
	private EditText mTxtUser;
	private EditText mTxtPass;
	private TextView mTvDeviceCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		deviceCode = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		
		mTxtUser = (EditText) findViewById(R.id.editTextUserName);
		mTxtPass = (EditText) findViewById(R.id.editTextPassWord);
		mTvDeviceCode = (TextView) findViewById(R.id.tvDeviceCode);
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		
		mTxtUser.setText("1");
		mTxtPass.setText("1");
		mTvDeviceCode.setText(deviceCode);
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
		
		if(mConn.getAddress().isEmpty() || 
				mConn.getBackoffice().isEmpty()){
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
//		if(mSetting.sync.isSyncWhenLogin()){
//			MPOSService.sync(mSetting.conn, LoginActivity.this, deviceCode, new IServiceStateListener(){
//	
//				@Override
//				public void onProgress() {
//					// TODO Auto-generated method stub
//					
//				}
//	
//				@Override
//				public void onSuccess() {		
//					checkLogin();
//				}
//	
//				@Override
//				public void onFail(String msg) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//			});
//		}else{
			checkLogin();
		//}
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
						Util.alert(LoginActivity.this, android.R.drawable.ic_dialog_alert, 
								R.string.login, R.string.incorrect_password);
					}
				}else{
					Util.alert(LoginActivity.this, android.R.drawable.ic_dialog_alert, 
							R.string.login, R.string.incorrect_user);
				}
			}else{
				Util.alert(LoginActivity.this, android.R.drawable.ic_dialog_alert, 
						R.string.login, R.string.enter_password);
			}
		}else{
			Util.alert(LoginActivity.this, android.R.drawable.ic_dialog_alert, 
					R.string.login, R.string.enter_username);
		}
	}
}
