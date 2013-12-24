package com.syn.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.j1tth4.mobile.util.DotNetWebServiceTask;

import android.content.Context;

public class MPOSMainService extends DotNetWebServiceTask{
	// webservice method
	public static final String CHECK_DEVICE_METHOD = "WSmPOS_CheckAuthenShopDevice";
	public static final String LOAD_MENU_METHOD = "WSmPOS_JSON_LoadMenuDataV2";
	public static final String LOAD_PRODUCT_METHOD = "WSmPOS_JSON_LoadProductDataV2";
	public static final String LOAD_SHOP_METHOD = "WSmPOS_JSON_LoadShopData";
	public static final String SEND_SALE_TRANS_METHOD = "WSmPOS_JSON_SendSaleTransactionData";
	public static final String SEND_STOCK_METHOD = "WSmPOS_JSON_SendInventoryDocumentData";
	
	public MPOSMainService(Context c, String deviceCode, String method) {
		super(c, method);
		
		property = new PropertyInfo();
		property.setName("szDeviceCode");
		property.setValue(deviceCode);
		property.setType(String.class);
		soapRequest.addProperty(property);
	}

}
