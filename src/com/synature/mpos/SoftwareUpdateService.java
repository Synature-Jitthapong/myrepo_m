package com.synature.mpos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.synature.util.Logger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class SoftwareUpdateService extends Service{
	
	private int mDownloaded;
	private NotificationManager mNotify;
	private NotificationCompat.Builder mBuilder;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDownloaded = 0;
		mNotify = (NotificationManager) 
				getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
		mBuilder.setContentTitle("mPOS Update");
		mBuilder.setContentText("Download in progress");
	    mBuilder.setSmallIcon(R.drawable.ic_launcher);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, int startId) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				String fileUrl = intent.getStringExtra("fileUrl");
				Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Start download apk...");
				
				InputStream input = null;
				OutputStream output = null;
				try {
					String fileName = getFileNameFromUrl(fileUrl);
					File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					File apk = new File(sdPath + File.separator + fileName);
					
					URL url = new URL(fileUrl);
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("Range", "bytes=" + mDownloaded + "-");
					conn.connect();
					input = new BufferedInputStream(conn.getInputStream());
					output = mDownloaded == 0 ? 
							new FileOutputStream(sdPath + File.separator + fileName) : 
								new FileOutputStream(sdPath + File.separator + fileName, true);

					byte data[] = new byte[1024];
					int length = conn.getContentLength();
					int count = 0;
					while((count = input.read(data)) > 0){
						output.write(data, 0, count);
						mDownloaded += count;
						mBuilder.setProgress(100, (int)(mDownloaded * 100 / length), false);
						mNotify.notify(0, mBuilder.build());
					}
					output.flush();
					
					mBuilder.setContentText("Download complete");
					mBuilder.setProgress(0, 0, false);
					
					Intent intent = new Intent();
				    intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
				    PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				    
				    mBuilder.setContentIntent(pending);
					mNotify.notify(0, mBuilder.build());
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
							Utils.LOG_FILE_NAME, "Successfully download apk...");
				} catch (MalformedURLException e) {
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
							Utils.LOG_FILE_NAME, "Error download apk: " + e.getLocalizedMessage());
				} catch (IOException e) {
					mBuilder.setContentText(e.getMessage());
					mNotify.notify(0, mBuilder.build());
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, 
							Utils.LOG_FILE_NAME, "Error download apk: " + e.getLocalizedMessage());
				}finally{
					if(input != null){
						try {
							input.close();
						} catch (IOException e) {}
					}
					if(output != null){
						try {
							output.close();
						} catch (IOException e) {}
					}
					stopSelf();
				}
			}}).start();
		return START_NOT_STICKY;
	}

	private String getFileNameFromUrl(String url){
		String fileName = null;
		String[] segment = url.split("/");
		fileName = segment[segment.length - 1];
		return fileName;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
