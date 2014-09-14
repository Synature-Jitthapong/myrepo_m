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
	public int onStartCommand(Intent intent, int flags, int startId) {
		String fileUrl = intent.getStringExtra("fileUrl");
		String version = intent.getStringExtra("version");
		String dbVersion = intent.getStringExtra("dbVersion");
		try {
			URL url = new URL(fileUrl);
			URLConnection conn = url.openConnection();
			conn.connect();
			
			int fileLength = conn.getContentLength();
			
			InputStream input = new BufferedInputStream(conn.getInputStream());
			OutputStream output = new FileOutputStream(Utils.UPDATE_PATH);
			File sdPath = new File(Environment.getExternalStorageDirectory(), Utils.UPDATE_PATH);
			if(!sdPath.exists())
				sdPath.mkdirs();
			
			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while((count = input.read(data)) != -1){
				total += count;
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			SoftwareInfoDao sw = new SoftwareInfoDao(getApplicationContext());
			sw.logSoftwareInfo(version, dbVersion);
			
			stopSelf();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
