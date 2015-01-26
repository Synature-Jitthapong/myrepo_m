package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.synature.util.Logger;

import android.content.Context;
import android.text.TextUtils;

public class PointRedeemtion extends PointServiceBase{

	public static final String TAG = PointRedeemtion.class.getSimpleName();
	
	public static final String METHOD = "RedemptionItemsProcess";
	
	private RedeemtionListener mListener;
	
	public PointRedeemtion(Context c, String merchantCode, String cardCode, 
			String jsonRedeemItem, int requestId, RedeemtionListener listener) {
		super(c, METHOD);
		
		mProperty = new PropertyInfo();
		mProperty.setName(MERCHANT_PARAM);
		mProperty.setValue(merchantCode);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
		
		mProperty = new PropertyInfo();
		mProperty.setName(CARD_TAG_CODE_PARAM);
		mProperty.setValue(cardCode);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);

		mProperty = new PropertyInfo();
		mProperty.setName(JSON_REDEEM_ITEMS_PARAM);
		mProperty.setValue(jsonRedeemItem);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		
		mProperty = new PropertyInfo();
		mProperty.setName(REQUEST_ID_PARAM);
		mProperty.setValue(requestId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		
		mListener = listener;
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			Result res = toResultObject(result);
			if(res.getiResultID() == RESPONSE_SUCCESS){
				mListener.onPost();
			}else{
				mListener.onError(TextUtils.isEmpty(res.getSzResultData()) ? res.getSzResultData() : result);
			}
		} catch (Exception e) {
			Logger.appendLog(mContext, MPOSApplication.LOG_PATH, 
					MPOSApplication.LOG_FILE_NAME, 
					"Error RedemptionItemsProcess: " + e.getLocalizedMessage() + "\n" + result);
			mListener.onError(result);
		}
	}

	@Override
	protected void onPreExecute() {
		mListener.onPre();
	}
	
	public static interface RedeemtionListener{
		void onPre();
		void onPost();
		void onError(String msg);
	}
	
	public static class RedeemItem{
		private int iProductID;
		private String szItemName;
		private int iItemQty;
		private int iItemPoint;
		public int getiProductID() {
			return iProductID;
		}
		public void setiProductID(int iProductID) {
			this.iProductID = iProductID;
		}
		public String getSzItemName() {
			return szItemName;
		}
		public void setSzItemName(String szItemName) {
			this.szItemName = szItemName;
		}
		public int getiItemQty() {
			return iItemQty;
		}
		public void setiItemQty(int iItemQty) {
			this.iItemQty = iItemQty;
		}
		public int getiItemPoint() {
			return iItemPoint;
		}
		public void setiItemPoint(int iItemPoint) {
			this.iItemPoint = iItemPoint;
		}
	}
}
