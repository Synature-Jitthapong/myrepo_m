package com.synature.mpos;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CheckUpdateActivity extends Activity {

	private SoftwareRegister mRegister;
	
	private static int sProgress;
	
	private static Button sBtnCheckUpdate;
	private static Button sBtnInstall;
	private static ProgressBar sProgressBar;
	private static TextView sTvTitle;
	private static TextView sTvLastUpdate;
	private static TextView sTvPercent;
	private static TextView sTvCurrentVersion;
	
	private static class DownloadReceiver extends ResultReceiver{

		private static DownloadReceiver sInstance = null;
		
		public static synchronized DownloadReceiver getInstance(){
			if(sInstance == null){
				sInstance = new DownloadReceiver(new Handler());
			}
			return sInstance;
		}
		
		public DownloadReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case DownloadService.UPDATE_PROGRESS:
				sProgress = resultData.getInt("progress");
				sProgressBar.setProgress(sProgress);
				sTvPercent.setText(NumberFormat.getInstance().format(sProgress) + "%");
				sBtnCheckUpdate.setEnabled(false);
				break;
			case DownloadService.DOWNLOAD_COMPLETE:
				sTvTitle.setText(R.string.download_complete);
				sBtnCheckUpdate.setVisibility(View.GONE);
				sBtnInstall.setVisibility(View.VISIBLE);
				sProgress = 100;
				sProgressBar.setProgress(sProgress);
				sTvPercent.setText(NumberFormat.getInstance().format(sProgress) + "%");
				String fileName = resultData.getString("fileName");
				Calendar c = Calendar.getInstance();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MPOSApplication.getContext());
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SettingsActivity.KEY_PREF_LAST_UPDATE, String.valueOf(c.getTimeInMillis()));
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "1");
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, fileName);
				editor.commit();
				sTvLastUpdate.setText(MPOSApplication.getContext().getString(R.string.last_time_update_version) + ": " 
						+ java.text.DateFormat.getDateInstance().format(c.getTime()));
				break;
			case DownloadService.ERROR:
				sBtnCheckUpdate.setEnabled(true);
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_update);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		sProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		sBtnCheckUpdate = (Button) findViewById(R.id.button1);
		sBtnInstall = (Button) findViewById(R.id.button2);
		sTvTitle = (TextView) findViewById(R.id.textView1);
		sTvLastUpdate = (TextView) findViewById(R.id.textView2);
		sTvPercent = (TextView) findViewById(R.id.textView3);
		sTvCurrentVersion = (TextView) findViewById(R.id.textView4);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CheckUpdateActivity.this);
		String lastUpdate = sharedPref.getString(SettingsActivity.KEY_PREF_LAST_UPDATE, "");
		String fileDownloadStatus = sharedPref.getString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "0");
		if(!TextUtils.isEmpty(lastUpdate)){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(lastUpdate));
			sTvLastUpdate.setText(getString(R.string.last_time_update_version) + ": " 
					+ java.text.DateFormat.getDateInstance().format(c.getTime()));
		}
		sTvCurrentVersion.setText(getString(R.string.current_version) + " " + Utils.getSoftWareVersion(this));
		if(TextUtils.equals(fileDownloadStatus, "1")){
			sTvTitle.setText(R.string.download_complete);
			sBtnInstall.setVisibility(View.VISIBLE);
			sBtnCheckUpdate.setVisibility(View.GONE);
		}else{
			sBtnInstall.setVisibility(View.GONE);
			sBtnCheckUpdate.setVisibility(View.VISIBLE);
		}
		sProgressBar.setProgress(sProgress);
		sTvPercent.setText(NumberFormat.getInstance().format(sProgress) + "%");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class RegisterValidUrlListener implements
			SoftwareRegister.SoftwareRegisterListener, DialogInterface.OnClickListener {
		
		private ProgressDialog mProgressDialog;
		
		public RegisterValidUrlListener() {
			 mProgressDialog = new ProgressDialog(CheckUpdateActivity.this);
			 mProgressDialog.setCanceledOnTouchOutside(false);
			 mProgressDialog.setMessage(getString(R.string.checking_update));
			 mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), this);
		}

		@Override
		public void onPreExecute() {
			mProgressDialog.show();
		}

		@Override
		public void onProgressUpdate(int value) {
		}

		@Override
		public void onPostExecute() {
		}

		@Override
		public void onError(String msg) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			sBtnCheckUpdate.setEnabled(true);
		}

		@Override
		public void onCancelled(String msg) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			sBtnCheckUpdate.setEnabled(true);
		}

		@Override
		public void onPostExecute(MPOSSoftwareInfo info) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			if(info != null){
				String version = info.getSzSoftwareVersion();
				String fileUrl = info.getSzSoftwareDownloadUrl();
				if(!TextUtils.isEmpty(version) && !TextUtils.isEmpty(fileUrl)){
					if(!TextUtils.equals(version, Utils.getSoftWareVersion(CheckUpdateActivity.this))){
						sTvTitle.setText(R.string.downloading);
						Intent intent = new Intent(CheckUpdateActivity.this, DownloadService.class);
						intent.putExtra("fileUrl", fileUrl);
						intent.putExtra("receiver", DownloadReceiver.getInstance());
						startService(intent);
					}else{
						new AlertDialog.Builder(CheckUpdateActivity.this)
						.setMessage(R.string.software_up_to_date)
						.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}
				}
			}
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			mRegister.cancel(true);
		}
	}

	public void installClicked(final View v){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String fileName = sharedPref.getString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
		if(!TextUtils.isEmpty(fileName)){
			File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File apkFile = new File(download + File.separator + fileName);
		    Intent intent = new Intent(Intent.ACTION_VIEW);
		    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		    startActivity(intent);
		}
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
		editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "0");
		editor.commit();
		Utils.backupDatabase(this);
	}
	
	public void updateClicked(final View v){
		mRegister = new SoftwareRegister(this, new RegisterValidUrlListener());
		mRegister.execute(Utils.REGISTER_URL);
		sBtnCheckUpdate.setEnabled(false);
	}
}
