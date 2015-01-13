package com.synature.mpos;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

	private static final int AUTO_DOWNLOAD = 1;
	
	private static Context sContext;
	
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
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(sContext);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SettingsActivity.KEY_PREF_LAST_UPDATE, String.valueOf(c.getTimeInMillis()));
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "1");
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, fileName);
				editor.commit();
				sTvLastUpdate.setText(sContext.getString(R.string.last_time_update_version) + ": " 
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
		sContext = this;
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
		
		Intent intent = getIntent();
		if(intent.getIntExtra("auto_download", 0) == AUTO_DOWNLOAD){
			if(sProgress == 0)
				checkForUpdate();
		}
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
			sProgress = 100;
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
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class RegisterResultReceiver extends ResultReceiver{

		private ProgressDialog progressDialog;
		
		public RegisterResultReceiver(Handler handler) {
			super(handler);
			progressDialog = new ProgressDialog(CheckUpdateActivity.this);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(getString(R.string.checking_update));
			progressDialog.show();
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case MPOSServiceBase.RESULT_SUCCESS:
				if(progressDialog.isShowing())
					progressDialog.dismiss();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CheckUpdateActivity.this);
				String needToUpdate = sharedPref.getString(SettingsActivity.KEY_PREF_NEED_TO_UPDATE, "0");
				if(Integer.parseInt(needToUpdate) == 1){
					String fileUrl = sharedPref.getString(SettingsActivity.KEY_PREF_FILE_URL, "");
					Intent intent = new Intent(CheckUpdateActivity.this, DownloadService.class);
					intent.putExtra("fileUrl", fileUrl);
					intent.putExtra("receiver", DownloadReceiver.getInstance());
					startService(intent);
					sTvTitle.setText(R.string.downloading);
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
				break;
			case MPOSServiceBase.RESULT_ERROR:
				if(progressDialog.isShowing())
					progressDialog.dismiss();
				sBtnCheckUpdate.setEnabled(true);
				break;
			}
		}
	}

	public void installClicked(final View v){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String fileName = sharedPref.getString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
		if(!TextUtils.isEmpty(fileName)){
			File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File apkFile = new File(download + File.separator + fileName);
			if(apkFile.exists()){
			    Intent intent = new Intent(Intent.ACTION_VIEW);
			    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
			    startActivity(intent);
//				try {
//					Process su = Runtime.getRuntime().exec("su");
//					DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//
//					//outputStream.writeBytes("pm install " + apkFile.toString() + "\n");
//				    outputStream.writeBytes("screenrecord --time-limit 10 /sdcard/MyVideo.mp4\n");
//				    outputStream.writeBytes("exit\n");
//					outputStream.flush();
//					su.waitFor();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}else{
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
				editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "0");
				editor.putString(SettingsActivity.KEY_PREF_NEED_TO_UPDATE, "0");
				editor.putString(SettingsActivity.KEY_PREF_NEW_VERSION, "");
				editor.putString(SettingsActivity.KEY_PREF_FILE_URL, "");
				editor.commit();
			}
		}
		Utils.backupDatabase(this);
	}
	
	private void checkForUpdate(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CheckUpdateActivity.this);
		String needToUpdate = sharedPref.getString(SettingsActivity.KEY_PREF_NEED_TO_UPDATE, "0");
		String newVersion = sharedPref.getString(SettingsActivity.KEY_PREF_NEW_VERSION, "");
		if(Integer.parseInt(needToUpdate) == 1){
			String fileUrl = sharedPref.getString(SettingsActivity.KEY_PREF_FILE_URL, "");
			Intent intent = new Intent(CheckUpdateActivity.this, DownloadService.class);
			intent.putExtra("fileUrl", fileUrl);
			intent.putExtra("receiver", DownloadReceiver.getInstance());
			startService(intent);
			sTvTitle.setText(getString(R.string.downloading) + " " + newVersion);
		}else{
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new SoftwareRegister(this, new RegisterResultReceiver(new Handler())));
			executor.shutdown();
		}
		sBtnCheckUpdate.setEnabled(false);	
	}
	
	public void updateClicked(final View v){
		checkForUpdate();
	}
}
