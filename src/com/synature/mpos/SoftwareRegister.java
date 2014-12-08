package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class SoftwareRegister extends MPOSServiceBase{

	public static final String TAG = SoftwareRegister.class.getSimpleName();
			
	public static final String REGIST_SERVICE_URL_METHOD = "WSmPOS_GetRegisterServiceUrl";
	
	public static final String SW_VERSION_PARAM = "szSwVersion";
	public static final String DB_VERSION_PARAM = "szDbVersion";
	
	private SoftwareRegisterListener mListener;
	
	public static interface SoftwareRegisterListener extends WebServiceWorkingListener{
		void onPostExecute(MPOSSoftwareInfo info);
	}
	
	public SoftwareRegister(Context context, SoftwareRegisterListener listener) {
		super(context, REGIST_SERVICE_URL_METHOD);
		mListener = listener;
		
		mProperty = new PropertyInfo();
		mProperty.setName(SW_VERSION_PARAM);
		mProperty.setValue(Utils.getSoftWareVersion(mContext));
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
		
		mProperty = new PropertyInfo();
		mProperty.setName(DB_VERSION_PARAM);
		mProperty.setValue(Utils.DB_VERSION);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
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
				mListener.onPostExecute(info);
			}else{
				mListener.onError(mContext.getString(R.string.device_not_register));
			}
		} catch (JsonSyntaxException e1) {
			mListener.onError(result);
		} catch(Exception e){
			mListener.onError(TextUtils.isEmpty(result) ? "Sorry unknown error." : result);
		}
	}

	@Override
	protected void onCancelled(String result) {
		mListener.onCancelled(result);
	}

	@Override
	protected void onPreExecute() {
		mListener.onPreExecute();
	}

}
