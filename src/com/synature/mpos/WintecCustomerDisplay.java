package com.synature.mpos;

import java.text.ParseException;

import android.content.Context;
import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.DspPos;

public class WintecCustomerDisplay{
	public static final int MAX_TEXT_LENGTH = 20;
	public static final int LIMIT_LENGTH = 10;
	
	private Context mContext;
	
	private DspPos mDsp;
	
	private String orderName;
	private String orderQty;
	private String orderPrice;
	private String orderTotalQty;
	private String orderTotalPrice;
	
	public WintecCustomerDisplay(Context context){
		mDsp = new DspPos(Utils.getWintecDspPath(context), 
				ComIO.Baudrate.valueOf(Utils.getWintecDspBaudRate(context)));
		mContext = context;
	}
	
	public void displayTotalPoint(String totalPoint, String currentPoint){
		clearScreen();
		mDsp.DSP_Dispay("Total");
		mDsp.DSP_MoveCursor(1, MAX_TEXT_LENGTH - totalPoint.length());
		mDsp.DSP_Dispay(totalPoint);
		mDsp.DSP_MoveCursorDown();
		mDsp.DSP_MoveCursorEndLeft();
		mDsp.DSP_Dispay("Curr. Point");
		mDsp.DSP_MoveCursor(2, MAX_TEXT_LENGTH - currentPoint.length());
		mDsp.DSP_Dispay(currentPoint);
	}
	
	public void displayOrder() throws Exception{
		if(orderName.length() > LIMIT_LENGTH){
			orderName = "";//limitString(orderName);
		}
		clearScreen();
		String combindText = orderQty + "@" + orderPrice;
		String combindTotalText = orderTotalQty + "@" + orderTotalPrice;
		mDsp.DSP_Dispay(orderName);
		mDsp.DSP_MoveCursor(1, MAX_TEXT_LENGTH - combindText.length());
		mDsp.DSP_Dispay(combindText);
		mDsp.DSP_MoveCursorDown();
		mDsp.DSP_MoveCursorEndLeft();
		mDsp.DSP_Dispay("Total");
		mDsp.DSP_MoveCursor(2, MAX_TEXT_LENGTH - combindTotalText.length());
		mDsp.DSP_Dispay(combindTotalText);
	}
	
	public void displayWelcome(){
		clearScreen();
		String line1 = Utils.getWintecDspTextLine1(mContext);
		String line2 = Utils.getWintecDspTextLine2(mContext);
		mDsp.DSP_Dispay(line1);
		mDsp.DSP_MoveCursorDown();
		mDsp.DSP_MoveCursorEndLeft();
		mDsp.DSP_Dispay(line2);
	}
	
	private String limitString(String text){
		return text.substring(0, LIMIT_LENGTH);
	}
	
	public void clearScreen(){
		mDsp.DSP_ClearScreen();
	}
	
	public void close(){
		if(mDsp != null)
			mDsp.DSP_Close();
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public String getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(String orderQty) {
		this.orderQty = orderQty;
	}

	public String getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(String orderPrice) {
		this.orderPrice = orderPrice;
	}

	public String getOrderTotalQty() {
		return orderTotalQty;
	}

	public void setOrderTotalQty(String orderTotalQty) {
		this.orderTotalQty = orderTotalQty;
	}

	public String getOrderTotalPrice() {
		return orderTotalPrice;
	}

	public void setOrderTotalPrice(String orderTotalPrice) {
		this.orderTotalPrice = orderTotalPrice;
	}
}
