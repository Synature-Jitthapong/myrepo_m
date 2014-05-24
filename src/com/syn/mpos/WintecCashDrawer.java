package com.syn.mpos;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Drw;

public class WintecCashDrawer extends WintecUtils{
	
	private Drw mDrw;
	
	public WintecCashDrawer(){
		mDrw = new Drw(WintecUtils.DEFAULT_DEV_PATH,
				ComIO.Baudrate.valueOf(WintecUtils.DEFAULT_BAUD_RATE));
	}
	
	public void openCashDrawer(){
		mDrw.DRW_Open();
	}
	
	public void close(){
		mDrw.DRW_Close();
	}
}
