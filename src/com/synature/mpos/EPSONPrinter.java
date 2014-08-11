package com.synature.mpos;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.text.TextUtils;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.synature.util.LevelTextPrint;
import com.synature.util.LevelTextPrint.ThreeLevelPrint;

public class EPSONPrinter extends PrinterBase implements 
	BatteryStatusChangeEventListener, StatusChangeEventListener{
	
	public static final int PRINT_NORMAL = 0;
	public static final int PRINT_THAI_LEVEL = 1;
	public static final int PRINT_LAO_LEVEL = 2;
	
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
		int[] status = new int[1];
		int[] battery = new int[1];
		try {
			createBuilderCommand();
			mBuilder.addFeedUnit(30);
			mBuilder.addCut(Builder.CUT_FEED);
			mPrinter.sendData(mBuilder, 10000, status, battery);
		} catch (EposException e) {
			e.printStackTrace();
		}
		if (mBuilder != null) {
			mBuilder.clearCommandBuffer();
		}
		close();
	}
	
	private void createBuilderCommand() throws EposException{
		for(String data : getSubElement()){
			data = data.replace("\n", "");
			try {
				if(data.contains("<c>")){
					data = data.replace("<c>", "");
					mBuilder.addTextAlign(Builder.ALIGN_CENTER);
				}
				ThreeLevelPrint level = LevelTextPrint.parsingLaoLevel(data);
				if(!TextUtils.isEmpty(level.getLine1())){
					mBuilder.addCommand((level.getLine1() + "\n").getBytes("x-iso-8859-11"));
				}
				if(!TextUtils.isEmpty(level.getLine2())){
					mBuilder.addCommand((level.getLine2() + "\n").getBytes("x-iso-8859-11"));
				}
				if(!TextUtils.isEmpty(level.getLine3())){
					mBuilder.addCommand((level.getLine3() + "\n").getBytes("x-iso-8859-11"));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createBuilder() throws EposException{
		for(String data : getSubElement()){
			if(data.contains("<c>")){
				data = data.replace("<c>", "");
				mBuilder.addTextAlign(Builder.ALIGN_CENTER);
			}
			mBuilder.addText(data);
		}
	}
	
	private String[] getSubElement(){
		return mTextToPrint.toString().split("\n");
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
