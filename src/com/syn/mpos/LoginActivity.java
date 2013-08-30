package com.syn.mpos;

import com.syn.mpos.R;
import com.syn.mpos.db.Configuration;
import com.syn.mpos.model.Setting;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class LoginActivity extends Activity {
	private Context context;
	private Configuration config;
	private Setting.ConnectionSetting connSetting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		context = LoginActivity.this;
		
		init();
	}

	private void init(){
		config = new Configuration(context);
		connSetting = config.getConnectionSetting();
	}
	
	public void settingClicked(final View v){
		Intent intent = new Intent(context, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void loginClicked(final View v){
		
		final Intent intent = new Intent(context, MainActivity.class);
		//startActivity(intent);
		
		MPOSService.sync(connSetting, context, new IServiceStateListener(){

			@Override
			public void onProgress() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess() {
				startActivity(intent);
			}

			@Override
			public void onFail(String msg) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

}
