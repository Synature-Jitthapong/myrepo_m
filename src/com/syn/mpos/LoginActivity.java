package com.syn.mpos;

import com.syn.mpos.R;
import com.syn.mpos.database.Login;
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
	private Context context;
	private SharedPreferences sharedPref;
	private Setting setting;
	
	private EditText txtUser;
	private EditText txtPass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		context = LoginActivity.this;
		
		txtUser = (EditText) findViewById(R.id.editTextUserName);
		txtPass = (EditText) findViewById(R.id.editTextPassWord);
		txtUser.setSelectAllOnFocus(true);
		txtPass.setSelectAllOnFocus(true);
		
		txtUser.setText("admin");
		txtPass.setText("admin");
		
	}

	private void init(){
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		setting = new Setting();
		
		setting.conn.setIpAddress(sharedPref.getString("pref_ipaddress", ""));
		setting.conn.setServiceName(sharedPref.getString("pref_webservice", ""));
		setting.conn.setFullUrl("http://" + setting.conn.getIpAddress() + "/" + setting.conn.getServiceName() + "/ws_mpos.asmx");
		setting.sync.setSyncWhenLogin(sharedPref.getBoolean("pref_syncwhenlogin", false));
		
		if(setting.conn.getIpAddress().equals("") || 
				setting.conn.getServiceName().equals("")){
			Intent intent = new Intent(context, SettingsActivity.class);
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
			Intent intent = new Intent(context, SettingsActivity.class);
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
		if(setting.sync.isSyncWhenLogin()){
			MPOSService.sync(setting.conn, context, new IServiceStateListener(){
	
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
	
	private void gotoMainActivity(int staffId, int sessionId){
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("staffId", staffId);
		intent.putExtra("sessionId", sessionId);
		startActivity(intent);
	}
	
	private void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!txtUser.getText().toString().isEmpty()){
			user = txtUser.getText().toString();
			
			if(!txtPass.getText().toString().isEmpty()){
				pass = txtPass.getText().toString();
				Login login = new Login(context, user, pass);
				
				if(login.checkUser()){
					ShopData.Staff s = login.checkLogin();
					
					if(s != null){
						gotoMainActivity(s.getStaffID(), 1);
					}else{
						Util.alert(context, android.R.drawable.ic_dialog_alert, 
								R.string.login, R.string.incorrect_password);
					}
				}else{
					Util.alert(context, android.R.drawable.ic_dialog_alert, 
							R.string.login, R.string.incorrect_user);
				}
			}else{
				Util.alert(context, android.R.drawable.ic_dialog_alert, 
						R.string.login, R.string.enter_password);
			}
		}else{
			Util.alert(context, android.R.drawable.ic_dialog_alert, 
					R.string.login, R.string.enter_username);
		}
	}
}
