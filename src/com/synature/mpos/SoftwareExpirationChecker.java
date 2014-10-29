package com.synature.mpos;

import java.util.Calendar;

import com.synature.mpos.database.SoftwareInfoDao;
import com.synature.mpos.database.model.SoftwareInfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

public class SoftwareExpirationChecker{

	private Context mContext;
	private SoftwareExpirationCheckerListener mListener;
	
	public SoftwareExpirationChecker(Context context, SoftwareExpirationCheckerListener listener){
		mContext = context;
		mListener = listener;
	}
	
	public void checkExpDate(){
		SoftwareInfoDao sw = new SoftwareInfoDao(mContext);
		SoftwareInfo info = sw.getSoftwareInfo();
		if(info != null){
			if(!TextUtils.isEmpty(info.getExpDate())){
				Calendar c = Calendar.getInstance();
				Calendar cExp = Calendar.getInstance();
				cExp.setTimeInMillis(Long.parseLong(info.getExpDate()));
				boolean isLocked = false;
				if(c.compareTo(cExp) >= 0){
					Calendar cLock = Calendar.getInstance();
					cLock.setTimeInMillis(Long.parseLong(info.getLockDate()));
					if(c.compareTo(cLock) > 0){
						isLocked = true;
					}
					mListener.onExpire(cLock, isLocked);
				}else{
					mListener.onNotExpired();
				}
			}else{
				mListener.onNotExpired();
			}
		}else{
			mListener.onNotExpired();
		}
	}
	
	public static interface SoftwareExpirationCheckerListener{
		void onExpire(Calendar lockDate, boolean isLocked);
		void onNotExpired();
	}
}
