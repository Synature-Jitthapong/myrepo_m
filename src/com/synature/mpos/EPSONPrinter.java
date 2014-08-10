package com.synature.mpos;

import android.content.Context;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;

public class EPSONPrinter extends PrinterUtility implements 
	BatteryStatusChangeEventListener, StatusChangeEventListener{
	
	protected Context mContext;
	protected Print mPrinter;
	protected Builder mBuilder;
	
	public EPSONPrinter(Context context){
		super(context);
		mContext = context;
		mPrinter = new Print(context.getApplicationContext());
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		
		try {
			mBuilder = new Builder(Utils.getEPSONModelName(mContext), Builder.MODEL_ANK, mContext);
			mBuilder.addTextSize(1, 1);
			if(Utils.getEPSONPrinterFont(mContext).equals("a")){
				mBuilder.addTextFont(Builder.FONT_A);
			}else if(Utils.getEPSONPrinterFont(mContext).equals("b")){
				mBuilder.addTextFont(Builder.FONT_B);
			}
			open();
		} catch (EposException e) {
			e.printStackTrace();
		}
	}
	
	protected void print(){
		try {
			mBuilder.addText(mTextToPrint.toString());
			// send mBuilder data
			int[] status = new int[1];
			int[] battery = new int[1];
			try {
				mBuilder.addFeedUnit(30);
				mBuilder.addCut(Builder.CUT_FEED);
				mPrinter.sendData(mBuilder, 10000, status, battery);
			} catch (EposException e) {
				e.printStackTrace();
			}
			if (mBuilder != null) {
				mBuilder.clearCommandBuffer();
			}
		} catch (EposException e) {
			e.printStackTrace();
		}
		close();
	}
	
	private void open(){
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, Utils.getPrinterIp(mContext), 0, 1000);
		} catch (EposException e) {
			e.printStackTrace();
		}	
	}

	private void close(){
		try {
			mPrinter.closePrinter();
			mPrinter = null;
		} catch (EposException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStatusChangeEvent(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
