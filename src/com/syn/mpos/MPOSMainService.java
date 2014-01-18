package com.syn.mpos;

import org.ksoap2.serialization.PropertyInfo;
import com.j1tth4.mobile.util.DotNetWebServiceTask;

public class MPOSMainService extends DotNetWebServiceTask{
	// webservice method
	public static final String CHECK_DEVICE_METHOD = "WSmPOS_CheckAuthenShopDevice";
	public static final String LOAD_MENU_METHOD = "WSmPOS_JSON_LoadMenuDataV2";
	public static final String LOAD_PRODUCT_METHOD = "WSmPOS_JSON_LoadProductDataV2";
	public static final String LOAD_SHOP_METHOD = "WSmPOS_JSON_LoadShopData";
	public static final String SEND_SALE_TRANS_METHOD = "WSmPOS_JSON_SendSaleAllTransactionDataWithEndDay";
	public static final String SEND_PARTIAL_SALE_TRANS_METHOD = "WSmPOS_JSON_SendSalePartialTransactionData";
	public static final String SEND_STOCK_METHOD = "WSmPOS_JSON_SendInventoryDocumentData";
	
	public static final String SHOP_ID_PARAM = "iShopID";
	public static final String COMPUTER_ID_PARAM = "iComputerID";
	public static final String STAFF_ID_PARAM = "iStaffID";
	public static final String DEVICE_CODE_PARAM = "szDeviceCode";
	public static final String JSON_SALE_PARAM = "szJsonSaleTransData";
	
	public MPOSMainService(String method) {
		super(MPOSApplication.getContext(), method);
		
		property = new PropertyInfo();
		property.setName(DEVICE_CODE_PARAM);
		property.setValue(MPOSApplication.getDeviceCode());
		property.setType(String.class);
		soapRequest.addProperty(property);
	}

}
