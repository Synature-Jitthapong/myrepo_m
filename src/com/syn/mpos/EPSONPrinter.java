package com.syn.mpos;

import android.content.Context;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;

public abstract class EPSONPrinter extends PrinterUtility implements 
	BatteryStatusChangeEventListener, StatusChangeEventListener{

	public static final String DEFAULT_MODEL_NAME = "TM-T81";
	public static final int DEFAULT_DEVICE_TYPE = Print.DEVTYPE_TCP;
	public static final int DEFAULT_LANG = Builder.MODEL_ANK;
	public static final int DEFAULT_INTERVAL = 1000;
	
	protected Context mContext;
	protected Print mPrinter;
	protected Builder mBuilder;
	
	private String mModelName = DEFAULT_MODEL_NAME;
	private String mPrinterIp;
	private int mLang = DEFAULT_LANG;
	private int mDeviceType = DEFAULT_DEVICE_TYPE;
	
	public EPSONPrinter(Context context, int deviceType, 
			String modelName, String printerIp, int lang){
		mContext = context;
		mPrinter = new Print(context.getApplicationContext());
		if(lang != 0)
			mLang = lang;
		if(deviceType != 0)
			mDeviceType = deviceType;
		mModelName = modelName;
		mPrinterIp = printerIp;
	}
	
	private void openPrinter() throws EposException{
		mPrinter.openPrinter(mDeviceType, mPrinterIp, 0, DEFAULT_INTERVAL);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		mPrinter.setStatusChangeEventCallback(this);
		mBuilder = new Builder(mModelName, mLang, mContext.getApplicationContext());
	}
	
	private void closePrinter() throws EposException{
		mPrinter.closePrinter();
		mPrinter = null;
	}

	protected void print() throws EposException{
		openPrinter();
		// send builder data
		int[] status = new int[1];
		int[] battery = new int[1];
		try {
			mPrinter.sendData(mBuilder, 10000, status, battery);
		} catch (EposException e) {
			e.printStackTrace();
		}
		if (mBuilder != null) {
			mBuilder.clearCommandBuffer();
		}
		closePrinter();
	}
	
	public abstract void prepareDataToPrint(int transactionId) throws EposException; 
}
