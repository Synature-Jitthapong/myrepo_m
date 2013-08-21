package com.syn.mpos;

import android.content.Context;

import com.syn.mpos.db.MPOSTransaction;

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
