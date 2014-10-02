package com.synature.mpos;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
		Log.i(TAG, result);
		try {
			Result res = toResultObject(result);
			if(res != null){
				if(res.getiResultID() == RESPONSE_SUCCESS){
					mListener.onPost();
				}else{
					mListener.onError(TextUtils.isEmpty(res.getSzResultData()) ? res.getSzResultData() : result);
				}
			}
		} catch (Exception e) {
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
