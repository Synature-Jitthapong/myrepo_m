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

import com.synature.mpos.database.SoftwareUpdateDao;
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
	
	public static boolean sIsRunning = false;

	private NotificationManager mNotify;
	private NotificationCompat.Builder mBuilder;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mNotify = (NotificationManager) 
				getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
		mBuilder.setContentTitle("mPOS Update");
		mBuilder.setContentText("Download in progress");
	    mBuilder.setSmallIcon(R.drawable.ic_launcher);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, int startId) {
		sIsRunning = true;
		new Thread(new Runnable(){

			@Override
			public void run() {
				String fileUrl = intent.getStringExtra("fileUrl");
				Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Start download apk...");
				try {
					URL url = new URL(fileUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					
					File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					File apk = new File(sdPath + File.separator + Utils.UPDATE_FILE_NAME);
					if(!sdPath.exists())
						sdPath.mkdirs();
					if(apk.exists())
						apk.delete();
					
					InputStream input = new BufferedInputStream(conn.getInputStream());
					OutputStream output = new FileOutputStream(sdPath + File.separator + Utils.UPDATE_FILE_NAME);

					byte data[] = new byte[2048];
					int length = conn.getContentLength();
					int total = 0;
					int count = 0;
					while((count = input.read(data)) > 0){
						output.write(data, 0, count);
						total += count;
						mBuilder.setProgress(100, (int)(total * 100 / length), false);
						mNotify.notify(0, mBuilder.build());
					}
					output.flush();
					output.close();
					input.close();
					SoftwareUpdateDao su = new SoftwareUpdateDao(getApplicationContext());
					su.setDownloadStatus(1);
					mBuilder.setContentText("Download complete");
					mBuilder.setProgress(0, 0, false);
					Intent intent = new Intent();
				    intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
				    PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				    mBuilder.setContentIntent(pending);
					mNotify.notify(0, mBuilder.build());
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Successfully download apk...");
				} catch (MalformedURLException e) {
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Error download apk: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					mBuilder.setContentText(e.getMessage());
					mNotify.notify(0, mBuilder.build());
					Logger.appendLog(getApplicationContext(), Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Error download apk: " + e.getLocalizedMessage());
					e.printStackTrace();
				}finally{
					stopSelf();
				}
			}}).start();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		sIsRunning = false;
		super.onDestroy();
	}

}
