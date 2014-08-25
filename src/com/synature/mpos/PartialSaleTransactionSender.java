package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;

import com.synature.pos.WebServiceResult;

public class PartialSaleTransactionSender extends MPOSServiceBase{

	public static final String SEND_PARTIAL_SALE_TRANS_METHOD = "WSmPOS_JSON_SendSalePartialTransactionData";

	private WebServiceWorkingListener mListener;
	
	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param jsonSale
	 * @param listener
	 */
	public PartialSaleTransactionSender(Context context, int shopId, int computerId,
			int staffId, String jsonSale, WebServiceWorkingListener listener) {
		super(context, SEND_PARTIAL_SALE_TRANS_METHOD);
		mListener = listener;

		// shopId
		mProperty = new PropertyInfo();
		mProperty.setName(SHOP_ID_PARAM);
		mProperty.setValue(shopId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		// computerId
		mProperty = new PropertyInfo();
		mProperty.setName(COMPUTER_ID_PARAM);
		mProperty.setValue(computerId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		// staffId
		mProperty = new PropertyInfo();
		mProperty.setName(STAFF_ID_PARAM);
		mProperty.setValue(staffId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		// json sale
		mProperty = new PropertyInfo();
		mProperty.setName(JSON_SALE_PARAM);
		mProperty.setValue(jsonSale);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			WebServiceResult ws = (WebServiceResult) super.toServiceObject(result);
			if(ws.getiResultID() == WebServiceResult.SUCCESS_STATUS){
				mListener.onPostExecute();
			}else{
				mListener.onError(ws.getSzResultData().equals("") ? result :
					ws.getSzResultData());
			}
		} catch (Exception e) {
			mListener.onError(result);
			e.printStackTrace();
		}
	}

	@Override
	protected void onPreExecute() {
		mListener.onPreExecute();
	}
}