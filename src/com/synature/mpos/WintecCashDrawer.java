package com.synature.mpos;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Drw;

public class WintecCashDrawer{
	
	public static final String DEV_PATH = "/dev/ttySAC1";
	public static final ComIO.Baudrate BAUD_RATE = ComIO.Baudrate.valueOf("BAUD_38400");
	
	private Drw mDrw;
	
	public WintecCashDrawer(){
		mDrw = new Drw(DEV_PATH, BAUD_RATE);
	}
	
	public void openCashDrawer(){
		mDrw.DRW_Open();
	}
	
	public void close(){
		mDrw.DRW_Close();
	}
}
