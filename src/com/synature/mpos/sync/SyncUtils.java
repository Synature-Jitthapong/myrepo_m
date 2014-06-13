package com.synature.mpos.sync;

import com.synature.mpos.account.Authenticator;
import com.synature.mpos.provider.MPOSProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

public class SyncUtils {
	public static final long MILLISECONDS_PER_SECOND = 1000L;
	public static final long SECONDS_PER_MINUTE = 6L;
	public static final long SYNC_INTERVAL = SECONDS_PER_MINUTE *
			MILLISECONDS_PER_SECOND;
	public static final String AUTHORITY = MPOSProvider.AUTHORITY;
	public static final String ACCOUNT_TYPE = "com.synature.mpos.account";
	
	public static void createSync(Context context){
		boolean newAccount = false;
		Account account = Authenticator.getAccount(ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager)
				context.getSystemService(Context.ACCOUNT_SERVICE);
		if(accountManager.addAccountExplicitly(account, null, null)){
			ContentResolver.setIsSyncable(account, AUTHORITY, 1);
			ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
			ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_INTERVAL);
			newAccount = true;
		}
		if(newAccount){
	        Bundle b = new Bundle();
	        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
	        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
	        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, AUTHORITY, b);
		}
	}
}
