package com.syn.mpos;

import org.ksoap2.serialization.PropertyInfo;
import com.j1tth4.mobile.util.DotNetWebServiceTask;
import android.content.Context;

public class MPOSMainService extends DotNetWebServiceTask{

	public MPOSMainService(Context c, String deviceCode, String method) {
		super(c, method);
		
		property = new PropertyInfo();
		property.setName("szDeviceCode");
		property.setValue(deviceCode);
		property.setType(String.class);
		soapRequest.addProperty(property);
	}

}
