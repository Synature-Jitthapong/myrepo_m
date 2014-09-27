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
				if(cExp.compareTo(c) > 0){
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("Expire");
					builder.setMessage("Software expired");
					builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
					AlertDialog d = builder.create();
					d.show();
					mListener.onExpire("Expire");
				}
			}
		}
	}
	
	public static interface SoftwareExpirationCheckerListener{
		void onExpire(String msg);
	}
}
