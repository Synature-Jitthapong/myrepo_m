package com.syn.mpos;

import java.text.ParseException;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.DspPos;

public class WintecCustomerDisplay extends WintecUtils{
	public static final int MAX_TEXT_LENGTH = 20;
	
	private DspPos mDsp;
	
	public WintecCustomerDisplay(){
		mDsp = new DspPos(DEFAULT_DEV_PATH, 
				ComIO.Baudrate.valueOf(DEFAULT_BAUD_RATE));
	}
	
	public void displayTotalPay(String textPay, String textChange, 
			String totalPay, String change){
		clearScreen();
		mDsp.DSP_Dispay(textPay + ": " + totalPay);
		try {
			if(MPOSUtil.stringToDouble(change) > 0){
				mDsp.DSP_MoveCursorDown();
				mDsp.DSP_MoveCursorEndLeft();
				mDsp.DSP_Dispay(textChange + ": " + change);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}
	
	public void displayTotalPrice(String totalText, String totalQty, 
			String totalPrice, String totalDiscountText, String discount){
		clearScreen();
		mDsp.DSP_Dispay(totalText + ": " + totalPrice);
		try {
			if(MPOSUtil.stringToDouble(discount) > 0){
				mDsp.DSP_MoveCursorDown();
				mDsp.DSP_MoveCursorEndLeft();
				mDsp.DSP_Dispay(totalDiscountText + ": " + discount);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}
	
	public void displayOrder(String itemName, String itemQty, String itemPrice){
		if(itemName.length() > MAX_TEXT_LENGTH){
			itemName = substring(itemName);
		}
		clearScreen();
		mDsp.DSP_Dispay(itemName);
		mDsp.DSP_MoveCursorDown();
		mDsp.DSP_MoveCursorEndLeft();
		mDsp.DSP_Dispay(itemQty + " x " + itemPrice);
		close();
	}
	
	public void displayWelcome(){
		clearScreen();
		mDsp.DSP_Dispay("Welcome to");
		mDsp.DSP_MoveCursorDown();
		mDsp.DSP_MoveCursorEndLeft();
		mDsp.DSP_Dispay("pRoMiSe System");
		close();
	}
	
	private String substring(String text){
		return text.substring(0, 17) + "...";
	}
	
	public void clearScreen(){
		mDsp.DSP_ClearScreen();
	}
	
	public void close(){
		mDsp.DSP_Close();
	}
}
