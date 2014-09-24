package com.synature.mpos;

import com.synature.mpos.database.SoftwareInfoDao;
import com.synature.mpos.database.model.SoftwareInfo;
import com.synature.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{
	public static final String TAG = BootUpReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
			SoftwareInfoDao sw = new SoftwareInfoDao(context);
			SoftwareInfo info = sw.getSoftwareInfo();
			if(info != null){
				sw.setStatusAlreadyUpdated(info.getId(), 1);
				Logger.appendLog(context, Utils.LOG_PATH, Utils.LOG_FILE_NAME, "Success update apk infoId: " + info.getId());
			}
		}
		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(loginIntent);
	}
}