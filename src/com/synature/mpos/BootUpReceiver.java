package com.synature.mpos;

import com.synature.mpos.database.SoftwareUpdateDao;
import com.synature.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{
	public static final String TAG = BootUpReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
			try {
				SoftwareUpdateDao update = new SoftwareUpdateDao(context);
				update.setUpdateStatus(1);
				Logger.appendLog(context, MPOSApplication.LOG_PATH, 
						MPOSApplication.LOG_FILE_NAME, "Success update apk");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(loginIntent);
	}
}