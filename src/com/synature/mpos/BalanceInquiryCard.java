package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.synature.util.Logger;

import android.content.Context;
import android.text.TextUtils;

public class BalanceInquiryCard extends PointServiceBase{

	public static final String METHOD = "GetBalanceInquiryCard";
	
	private GetBalanceListener mListener;
	
	public BalanceInquiryCard(Context c, String merchantCode, String cardCode, GetBalanceListener listener) {
		super(c, METHOD);
		
		mProperty = new PropertyInfo();
		mProperty.setName(MERCHANT_PARAM);
		mProperty.setValue(merchantCode);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
		
		mProperty = new PropertyInfo();
		mProperty.setName(CARD_CODE_PARAM);
		mProperty.setValue(cardCode);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);

		mProperty = new PropertyInfo();
		mProperty.setName(REQUEST_ID_PARAM);
		mProperty.setValue("1");
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
		
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			Result res = toResultObject(result);
			if(res != null){
				if(res.getiResultID() == RESPONSE_SUCCESS){
					String resultData = res.getSzResultData();
					if(!TextUtils.isEmpty(resultData)){
						MemberInfo member = toMemberInfoObject(resultData);
						mListener.onPost(member);
					}else{
						mListener.onError("No result");
					}
				}else{
					mListener.onError("Not found member");
				}
			}
		} catch (Exception e) {
			Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
					"Error GetBalanceInquiryCard: " + e.getLocalizedMessage() + "\n" + result);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mListener.onPre();
	}
	
	public static interface GetBalanceListener{
		void onPre();
		void onPost(MemberInfo member);
		void onError(String msg);
	}

}
