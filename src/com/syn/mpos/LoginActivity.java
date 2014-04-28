package com.syn.mpos;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.syn.mpos.R;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.Login;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.SessionDataSource;
import com.syn.mpos.database.Util;
import com.syn.pos.ShopData;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
	
	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;
	private int mStaffId;
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
		
		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
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
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		if(url.equals("")){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * Compare system date with session date
	 * if system date less than session date 
	 * this not allow to do anything and 
	 * force to date & time setting.
	 * @return
	 */
	private boolean checkSessionDate(){
		ComputerDataSource computer = new ComputerDataSource(mSqlite);
		SessionDataSource session = new SessionDataSource(mSqlite);
		int computerId= computer.getComputerProperty().getComputerID();
		int sessionId = session.getCurrentSession(computerId);
		if(sessionId != 0){
			String strSaleDate = session.getSessionDate(computerId);
			Calendar calendar = Calendar.getInstance(Locale.US);
			calendar.setTimeInMillis(Long.parseLong(strSaleDate));
			Date saleDate = calendar.getTime();
			calendar = Calendar.getInstance();
			if(saleDate.compareTo(calendar.getTime()) > 0){
				new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle(R.string.system_date)
				.setMessage(R.string.system_date_less)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setPositiveButton(R.string.date_time_setting, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(
								new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
					}
				}).show();
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
		switch(item.getItemId()){
		case R.id.itemSetting:
			intent = new Intent(LoginActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.itemUpdate:
			MPOSUtil.updateData(mSqlite, LoginActivity.this);
			return true;
		case R.id.itemAbout:
			intent = new Intent(LoginActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.itemClearSale:
			MPOSUtil.clearSale();
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}

	@Override
	protected void onResume() {
		checkSessionDate();
		init();
		mTxtUser.requestFocus();
		super.onResume();
	}
			
	private void gotoMainActivity(){
		mTxtUser.setText(null);
		mTxtPass.setText(null);
		SessionDataSource sess = new SessionDataSource(mSqlite);
		if(sess.getSessionEnddayDetail(String.valueOf(Util.getDate().getTimeInMillis())) > 0){
			new AlertDialog.Builder(this)
			.setTitle(R.string.endday)
			.setMessage(R.string.alredy_endday)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).show();
		}else{
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra("staffId", mStaffId);
			startActivity(intent);
		}
	}
	
	private void checkLogin(){
		String user = "";
		String pass = "";
	
		if(!mTxtUser.getText().toString().isEmpty()){
			user = mTxtUser.getText().toString();
			
			if(!mTxtPass.getText().toString().isEmpty()){
				pass = mTxtPass.getText().toString();
				Login login = new Login(mSqlite, user, pass);
				
				if(login.checkUser()){
					ShopData.Staff s = login.checkLogin();
					
					if(s != null){
						mStaffId = s.getStaffID();
						gotoMainActivity();
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
			checkLogin();
			break;
		}
	}
}
