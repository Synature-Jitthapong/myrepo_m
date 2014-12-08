package com.synature.mpos;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

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
	private Button mBtnCheckUpdate;
	private ProgressBar mProgressBar;
	private TextView mTvTitle;
	private TextView mTvLastUpdate;
	private TextView mTvPercent;
	
	private class DownloadReceiver extends ResultReceiver{

		public DownloadReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case DownloadService.UPDATE_PROGRESS:
				int progress = resultData.getInt("progress");
				mProgressBar.setProgress(progress);
				mTvPercent.setText(NumberFormat.getInstance().format(progress) + "%");
				break;
			case DownloadService.DOWNLOAD_COMPLETE:
				mTvTitle.setText(R.string.download_complete);
				mBtnCheckUpdate.setEnabled(true);
				mProgressBar.setProgress(100);
				mTvPercent.setText(NumberFormat.getInstance().format(100) + "%");
				String fileName = resultData.getString("fileName");
				startInstallationActivity(fileName);
				
				Calendar c = Calendar.getInstance();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CheckUpdateActivity.this);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SettingsActivity.KEY_PREF_LAST_UPDATE, String.valueOf(c.getTimeInMillis()));
				editor.commit();
				mTvLastUpdate.setText(getString(R.string.last_time_update_version) + ": " 
						+ java.text.DateFormat.getDateInstance().format(c.getTime()));
				break;
			}
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_update);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mBtnCheckUpdate = (Button) findViewById(R.id.button1);
		mTvTitle = (TextView) findViewById(R.id.textView1);
		mTvLastUpdate = (TextView) findViewById(R.id.textView2);
		mTvPercent = (TextView) findViewById(R.id.textView3);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CheckUpdateActivity.this);
		String lastUpdate = sharedPref.getString(SettingsActivity.KEY_PREF_LAST_UPDATE, "");
		if(!TextUtils.isEmpty(lastUpdate)){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(lastUpdate));
			mTvLastUpdate.setText(getString(R.string.last_time_update_version) + ": " 
					+ java.text.DateFormat.getDateInstance().format(c.getTime()));
		}
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
		}

		@Override
		public void onCancelled(String msg) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
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
						mTvTitle.setText(R.string.downloading);
						Intent intent = new Intent(CheckUpdateActivity.this, DownloadService.class);
						intent.putExtra("fileUrl", fileUrl);
						intent.putExtra("receiver", new DownloadReceiver(new Handler()));
						startService(intent);
					}
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

		@Override
		public void onClick(DialogInterface dialog, int which) {
			mRegister.cancel(true);
		}
	}
	
	private void startInstallationActivity(String fileName){
		File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File apkFile = new File(download + File.separator + fileName);
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
	    startActivity(intent);
	}

	public void updateClicked(final View v){
		mRegister = new SoftwareRegister(this, new RegisterValidUrlListener());
		mRegister.execute(Utils.REGISTER_URL);
		mBtnCheckUpdate.setEnabled(false);
	}
}
