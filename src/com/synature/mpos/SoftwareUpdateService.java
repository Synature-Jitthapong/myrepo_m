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

import com.synature.mpos.database.SoftwareInfoDao;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

public class SoftwareUpdateService extends Service{
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, int startId) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				String fileUrl = intent.getStringExtra("fileUrl");
				int infoId = intent.getIntExtra("infoId", 0);
				try {
					URL url = new URL(fileUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					
					File sdPath = new File(Environment.getExternalStorageDirectory(), 
							Utils.UPDATE_PATH);
					if(!sdPath.exists())
						sdPath.mkdirs();
					
					InputStream input = new BufferedInputStream(conn.getInputStream());
					OutputStream output = new FileOutputStream(sdPath + File.separator + Utils.UPDATE_FILE_NAME);
					
					byte data[] = new byte[1024];
					int count;
					while((count = input.read(data)) != -1){
						output.write(data, 0, count);
					}
					output.flush();
					output.close();
					input.close();
					SoftwareInfoDao sw = new SoftwareInfoDao(getApplicationContext());
					sw.setStatusDownloaded(infoId, 1);
					stopSelf();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}}).start();
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
