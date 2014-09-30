package com.synature.mpos;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.util.Log;

public class PointRedeemtion extends PointServiceBase{

	public static final String TAG = PointRedeemtion.class.getSimpleName();
	
	public static final String METHOD = "RedemptionItemsProcess";
	
	public PointRedeemtion(Context c, String merchantCode, String cardCode, String jsonRedeemItem, int requestId) {
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
		mProperty.setName(JSON_REDEEM_ITEMS_PARAM);
		mProperty.setValue(jsonRedeemItem);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		
		mProperty = new PropertyInfo();
		mProperty.setName(REQUEST_ID_PARAM);
		mProperty.setValue(requestId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Log.i(TAG, result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}
	
	public List<RedeemItem> toRedeemItemList(String json){
		List<RedeemItem> redeemItemLst = null;
		Gson gson = new Gson();
		Type type = new TypeToken<List<RedeemItem>>(){}.getType();
		redeemItemLst = gson.fromJson(json, type);
		return redeemItemLst;
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
