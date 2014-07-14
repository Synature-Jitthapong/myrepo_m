package com.synature.mpos;

import android.content.Context;
import android.text.TextUtils;

import com.synature.util.ThaiLevelText;
import com.synature.util.ThaiLevelText.ThaiTextThreeLine;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Printer;

public abstract class WintecPrinter extends PrinterUtility{
	
	/**
	 * ISO8859-11 character
	 */
	public static final String ISO_8859_11 = "x-iso-8859-11";
	
	protected Context mContext;
	protected StringBuilder mBuilder;
	protected Printer mPrinter;
	
	public WintecPrinter(Context context){
		mContext = context;
		mPrinter = new Printer(Utils.getWintecPrinterDevPath(mContext), 
				ComIO.Baudrate.valueOf(Utils.getWintecPrinterBaudRate(mContext)));
		mPrinter.PRN_DisableChinese();
		mPrinter.PRN_SetCodePage(70);
		mBuilder = new StringBuilder();
	}

	protected void print(){
		String[] subElement = mBuilder.toString().split("\n");
    	for(String data : subElement){
//    		if(!data.contains("<b>")){
//	    		mPrinter.PRN_EnableBoldFont(0);
//    		}
//    		if(!data.contains("<u>")){
//	    		mPrinter.PRN_DisableFontUnderline();
//    		}
			if(data.contains("<c>")){
				data = adjustAlignCenter(data.replace("<c>", ""));
			}
			if(data.contains("<b>")){
				//mPrinter.PRN_EnableBoldFont(1);
				data = data.replace("<b>", "");
			}
			if(data.contains("<u>")){
				//mPrinter.PRN_EnableFontUnderline();
				data = data.replace("<u>", "");
			}

    		ThaiTextThreeLine supportThai = ThaiLevelText.parsingThaiLevel(data);
    		if(!TextUtils.isEmpty(supportThai.TextLine1))
    			mPrinter.PRN_Print(supportThai.TextLine1, ISO_8859_11);
    		mPrinter.PRN_Print(supportThai.TextLine2, ISO_8859_11);
    		if(!TextUtils.isEmpty(supportThai.TextLine3))
    			mPrinter.PRN_Print(supportThai.TextLine3, ISO_8859_11);
		}
    	mPrinter.PRN_PrintAndFeedLine(6);		
    	mPrinter.PRN_HalfCutPaper();
    	close();
	}
	
	private void close(){
		if(mPrinter != null)
			mPrinter.PRN_Close();
	}
	
	private static String adjustAlignCenter(String text){
		int rimSpace = (HORIZONTAL_MAX_SPACE - text.length()) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	public void prepareDataToPrint(int transactionId){};
	public void prepareDataToPrint(){};
}
