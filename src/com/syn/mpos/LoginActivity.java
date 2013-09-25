package com.syn.mpos;

import com.syn.mpos.R;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.Shop;
import com.syn.mpos.transaction.MPOSSession;
import com.syn.pos.Setting;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private Context mContext;
	private int mShopId;
	private int mComputerId;
	private int mSessionId;
	private Shop mShop;
	private MPOSSession mSession;
	private SharedPreferences mSharedPref;
	private Setting mSetting;
	
	private EditText mTxtUser;
	private EditText mTxtPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = LoginActivity.this;
		
		mTxtUser = (EditText) findViewById(R.id.editTextUserName);
		mTxtPass = (EditText) findViewById(R.id.editTextPassWord);
		mTxtUser.setSelectAllOnFocus(true);
		mTxtPass.setSelectAllOnFocus(true);
		
		mTxtUser.setText("admin");
		mTxtPass.setText("admin");
		
	}

	private void init(){
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mSetting = new Setting();
		
		mSetting.conn.setIpAddress(mSharedPref.getString("pref_ipaddress", ""));
		mSetting.conn.setServiceName(mSharedPref.getString("pref_webservice", ""));
		mSetting.conn.setFullUrl("http://" + mSetting.conn.getIpAddress() + "/" + mSetting.conn.getServiceName() + "/ws_mpos.asmx");
		mSetting.sync.setSyncWhenLogin(mSharedPref.getBoolean("pref_syncwhenlogin", false));
		
		mShop = new Shop(mContext);
		mShopId = mShop.getShopProperty().getShopID();
		mComputerId = mShop.getComputerProperty().getComputerID();
		
		mSession = new MPOSSession(mContext);
		
		if(mSetting.conn.getIpAddress().equals("") || 
				mSetting.conn.getServiceName().equals("")){
			Intent intent = new Intent(mContext, SettingsActivity.class);
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
			Intent intent = new Intent(mContext, SettingsActivity.class);
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
		if(mSetting.sync.isSyncWhenLogin()){
			MPOSService.sync(mSetting.conn, mContext, new IServiceStateListener(){
	
				@Override
				public void onProgress() {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onSuccess() {		
					checkLogin();
				}
	
				@Override
				public void onFail(String msg) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}else{
			checkLogin();
		}
		
	}
	
	private void gotoMainActivity(int staffId){
		Intent intent = new Intent(mContext, MainActivity.class);
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
				Login login = new Login(mContext, user, pass);
				
				if(login.checkUser()){
					ShopData.Staff s = login.checkLogin();
					
					if(s != null){
						mSessionId = mSession.getCurrentSession(mShopId, mComputerId);
						if(mSessionId == 0){
							mSessionId = mSession.addSession(mShopId, mComputerId, s.getStaffID(), 0);
						}
						
						gotoMainActivity(s.getStaffID());
					}else{
						Util.alert(mContext, android.R.drawable.ic_dialog_alert, 
								R.string.login, R.string.incorrect_password);
					}
				}else{
					Util.alert(mContext, android.R.drawable.ic_dialog_alert, 
							R.string.login, R.string.incorrect_user);
				}
			}else{
				Util.alert(mContext, android.R.drawable.ic_dialog_alert, 
						R.string.login, R.string.enter_password);
			}
		}else{
			Util.alert(mContext, android.R.drawable.ic_dialog_alert, 
					R.string.login, R.string.enter_username);
		}
	}
}
