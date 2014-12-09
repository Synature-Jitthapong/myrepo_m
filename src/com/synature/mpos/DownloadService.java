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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;

public class DownloadService extends Service{

	public static final int ERROR = -1;
	public static final int UPDATE_PROGRESS = 1;
	public static final int DOWNLOAD_COMPLETE = 2;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
		final String fileUrl = Utils.checkProtocal(intent.getStringExtra("fileUrl"));
		new Thread(new Runnable(){

			@Override
			public void run() {
				String fileName = getFileNameFromUrl(fileUrl);
				InputStream input = null;
				OutputStream output = null;
				try {
					File sdPath = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					File apk = new File(sdPath + File.separator + fileName);

					URL url = new URL(fileUrl);
					URLConnection conn = url.openConnection();

					int downloaded = 0;
					int length = conn.getContentLength();
					if (apk.exists()) {
						if (apk.length() != length) {
							apk.delete();
						} else {
							Bundle resultData = new Bundle();
							resultData.putString("fileName", fileName);
							receiver.send(DOWNLOAD_COMPLETE, resultData);
							return;
						}
					}

					input = new BufferedInputStream(conn.getInputStream());
					output = new FileOutputStream(sdPath + File.separator
							+ fileName);

					byte data[] = new byte[1024];
					int count = 0;
					while ((count = input.read(data)) > 0) {
						output.write(data, 0, count);
						downloaded += count;
						if(length > 0){
							Bundle resultData = new Bundle();
							resultData.putInt("progress",
									(int) (downloaded * 100 / length));
							receiver.send(UPDATE_PROGRESS, resultData);
						}
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
					output.flush();
					Bundle resultData = new Bundle();
					resultData.putInt("progress", 100);
					receiver.send(UPDATE_PROGRESS, resultData);

					resultData = new Bundle();
					resultData.putString("fileName", fileName);
					receiver.send(DOWNLOAD_COMPLETE, resultData);
				} catch (MalformedURLException e) {
					Bundle resultData = new Bundle();
					resultData.putString("msg", e.getMessage());
					receiver.send(ERROR, resultData);
				} catch (IOException e) {
					Bundle resultData = new Bundle();
					resultData.putString("msg", e.getMessage());
					receiver.send(ERROR, resultData);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
					if (output != null) {
						try {
							output.close();
						} catch (IOException e) {
						}
					}
					stopSelf();
				}
			}
			
		}).start();
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
		// TODO Auto-generated method stub
		return null;
	}
}
