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
				if(cExp.compareTo(c) > 0){
					Calendar cLock = Calendar.getInstance();
					cLock.setTimeInMillis(Long.parseLong(info.getLockDate()));
					String msg = mContext.getString(R.string.software_expired_msg);
					if(cLock.compareTo(c) > 0){
						isLocked = true;
						msg += "\n" + mContext.getString(R.string.software_locked);
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.software_expired);
					builder.setMessage(msg);
					builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
					AlertDialog d = builder.create();
					d.show();
					
					mListener.onExpire(isLocked);
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
		void onExpire(boolean isLocked);
		void onNotExpired();
	}
}
