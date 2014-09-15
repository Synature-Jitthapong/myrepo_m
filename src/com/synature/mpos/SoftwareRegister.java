package com.synature.mpos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synature.mpos.database.SoftwareInfoDao;
import com.synature.mpos.database.model.SoftwareInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class SoftwareRegister extends MPOSServiceBase{

	public static final String REGIST_SERVICE_URL_METHOD = "WSmPOS_GetRegisterServiceUrl";
	
	public static final String SW_VERSION_PARAM = "szSwVersion";
	public static final String DB_VERSION_PARAM = "szDbVersion";
	
	private WebServiceWorkingListener mListener;
	
	public SoftwareRegister(Context context, WebServiceWorkingListener listener) {
		super(context, REGIST_SERVICE_URL_METHOD);
		mListener = listener;
		
//		mProperty = new PropertyInfo();
//		mProperty.setName(SW_VERSION_PARAM);
//		mProperty.setValue(Utils.getSoftWareVersion(mContext));
//		mProperty.setType(String.class);
//		mSoapRequest.addProperty(mProperty);
//		
//		mProperty = new PropertyInfo();
//		mProperty.setName(DB_VERSION_PARAM);
//		mProperty.setValue(Utils.DB_VERSION);
//		mProperty.setType(String.class);
//		mSoapRequest.addProperty(mProperty);
	}

	@Override
	protected void onPostExecute(String result) {
		Gson gson = new Gson();
		try {
			MPOSSoftwareInfo info = gson.fromJson(result, MPOSSoftwareInfo.class);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
			if(info != null){
				SoftwareInfoDao sw = new SoftwareInfoDao(mContext);
				if(!TextUtils.isEmpty(info.getSzSoftwareVersion())){
					sw.logSoftwareInfo(info.getSzSoftwareVersion(), String.valueOf(Utils.DB_VERSION));
					
					// compare version
					SoftwareInfo sf = sw.getSoftwareInfo();
					if(sf != null){
						if(!TextUtils.equals(sf.getVersion(), info.getSzSoftwareVersion())){
							Intent intent = new Intent(mContext, SoftwareUpdateService.class);
							intent.putExtra("fileUrl", info.getSzSoftwareDownloadUrl());
							intent.putExtra("version", info.getSzSoftwareVersion());
							intent.putExtra("dbVersion", Utils.DB_VERSION);
							mContext.startService(intent);
						}
					}
				}else{
					sw.logSoftwareInfo(Utils.getSoftWareVersion(mContext), String.valueOf(Utils.DB_VERSION));
				}
				if(!TextUtils.isEmpty(info.getSzRegisterServiceUrl())){
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(SettingsActivity.KEY_PREF_SERVER_URL, info.getSzRegisterServiceUrl());
					editor.commit();
					
					mListener.onPostExecute();
				}else{
					mListener.onError(mContext.getString(R.string.invalid_url));
				}
			}else{
				mListener.onError("No result from server");
			}
		} catch (JsonSyntaxException e1) {
			mListener.onError(result);
		}
	}

	@Override
	protected void onPreExecute() {
		mListener.onPreExecute();
	}

}