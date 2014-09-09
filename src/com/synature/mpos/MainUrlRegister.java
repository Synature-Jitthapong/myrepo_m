package com.synature.mpos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class MainUrlRegister extends MPOSServiceBase{

	public static final String REGIST_SERVICE_URL_METHOD = "WSmPOS_GetRegisterServiceUrl";
	
	private WebServiceWorkingListener mListener;
	
	public MainUrlRegister(Context context, WebServiceWorkingListener listener) {
		super(context, REGIST_SERVICE_URL_METHOD);
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		Gson gson = new Gson();
		try {
			MPOSSoftwareInfo info = gson.fromJson(result, MPOSSoftwareInfo.class);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
			if(!TextUtils.isEmpty(info.getSzRegisterServiceUrl())){
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SettingsActivity.KEY_PREF_SERVER_URL, info.getSzRegisterServiceUrl());
				editor.commit();
				mListener.onPostExecute();
			}else{
				mListener.onError(mContext.getString(R.string.invalid_url));
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
