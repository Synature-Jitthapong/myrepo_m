package com.syn.pos.mobile.mpos;

import android.content.Context;

import com.syn.pos.mobile.mpos.dao.MPOSTransaction;

public abstract class MPOS {
	private MPOSTransaction mposTrans;
	
	public MPOS(Context c){
		
	}
	
	public void onInit(MPOSTransaction mposTrans){
		
	}
	
	public long getCurrentTransaction(int computerId){
		return computerId;
		
	}
}