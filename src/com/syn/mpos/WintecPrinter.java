package com.syn.mpos;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Printer;

public abstract class WintecPrinter extends PrinterUtility{
	
	/**
	 * fixes device path for wintec printer
	 */
	public static final String DEV_PATH = "/dev/ttySAC1";
	
	/**
	 * baud rate for this printer is BAUD_38400
	 */
	public static final ComIO.Baudrate BAUD_RATE = ComIO.Baudrate.valueOf("BAUD_38400");
	
	protected StringBuilder mBuilder;
	protected Printer mPrinter;
	
	public WintecPrinter(){
		mPrinter = new Printer(DEV_PATH, BAUD_RATE);
		mBuilder = new StringBuilder();
	}

	protected void print(){
		String[] subElement = mBuilder.toString().split("\n");
    	for(String data : subElement){
    		mPrinter.PRN_EnableBoldFont(0);
    		mPrinter.PRN_DisableFontUnderline();
			if(data.contains("<c>")){
				data = adjustAlignCenter(data.replace("<c>", ""));
			}
			if(data.contains("<b>")){
				mPrinter.PRN_EnableBoldFont(1);
				data = data.replace("<b>", "");
			}
			if(data.contains("<u>")){
				mPrinter.PRN_EnableFontUnderline();
				data = data.replace("<u>", "");
			}
    		mPrinter.PRN_Print(data);
		}
    	mPrinter.PRN_PrintAndFeedLine(6);		
    	mPrinter.PRN_HalfCutPaper();	
    	mPrinter.PRN_Close();
	}
	
	private static String adjustAlignCenter(String text){
		int maxSpace = 45;
		int rimSpace = (maxSpace - text.length()) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	public abstract void prepareDataToPrint();
}
