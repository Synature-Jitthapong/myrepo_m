package com.synature.mpos;

import android.content.Context;
import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Msr;

public class WintecMagneticReader{

	private byte mEnterTrack1;
	private byte mEnterTrack2;
	private byte mEnterTrack3;
	
	private Msr mMsr;
	
	public WintecMagneticReader(Context context){
		mMsr = new Msr(Utils.getWintecMsrDevPath(context), 
				ComIO.Baudrate.valueOf(Utils.getWintecMsrBaudRate(context)));
	}
	
	public String getTrackData(){
		return mMsr.MSR_GetTrackData(mEnterTrack1, mEnterTrack2, mEnterTrack3);
	}
	
	public void close(){
		if(mMsr != null)
			mMsr.MSR_Close();
	}

	public byte getTrack1() {
		return mEnterTrack1;
	}

	public byte getTrack2() {
		return mEnterTrack2;
	}

	public byte getTrack3() {
		return mEnterTrack3;
	}
}
