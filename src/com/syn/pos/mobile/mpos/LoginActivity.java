package com.syn.pos.mobile.mpos;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	public void loginClicked(final View v){
		final Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
//		MPOSService.sync(this, new IServiceStateListener(){
//
//			@Override
//			public void onProgress() {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onSuccess() {
//				startActivity(intent);
//			}
//
//			@Override
//			public void onFail(String msg) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//		});
	}

}
