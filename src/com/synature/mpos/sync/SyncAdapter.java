package com.synature.mpos.sync;

import com.synature.mpos.MPOSApplication;
import com.synature.util.Logger;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class SyncAdapter extends AbstractThreadedSyncAdapter{

	public static final String TAG = SyncAdapter.class.getSimpleName();
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		// TODO Auto-generated method stub
		Logger.appendLog(getContext(), MPOSApplication.LOG_DIR, 
				MPOSApplication.LOG_FILE_NAME, "Sync");
	}

}
