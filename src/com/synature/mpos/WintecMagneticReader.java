package com.synature.mpos;

import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Msr;

public class WintecMagneticReader extends WintecUtils{

	private byte mEnterTrack1;
	private byte mEnterTrack2;
	private byte mEnterTrack3;
	
	private Msr mMsr;
	
	public WintecMagneticReader(){
		mMsr = new Msr(DEFAULT_DEV_PATH, 
				ComIO.Baudrate.valueOf(DEFAULT_BAUD_RATE));
	}
	
	public String getTrackData(){
		return mMsr.MSR_GetTrackData(mEnterTrack1, mEnterTrack2, mEnterTrack3);
	}
	
	public void close(){
		mMsr.MSR_Close();
	}
}
