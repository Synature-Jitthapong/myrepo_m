package com.syn.mpos.database;

import android.content.Context;

/**
 * @author j1tth4
 * Manage session
 */
public class MPOSSession {
	
	/*
	 * Session data source
	 */
	private SessionDataSource mSession;

	public MPOSSession(Context context){
		mSession = new SessionDataSource(context);
	}
	
	/**
	 * @param shopId
	 * @param computerId
	 * @param openStaffId
	 * @param openAmount
	 * @return sessionId
	 */
	public int openSession(int shopId, int computerId, 
			int openStaffId, double openAmount){
		int sessionId = 0;
		if(getCurrentSession() == 0){
			sessionId = mSession.addSession(shopId, computerId, 
					openStaffId, openAmount);
		}
		return sessionId;
	}
	
	/**
	 * @param sessionId
	 * @return session date
	 */
	public String getCurrentSessionDate(int sessionId){
		return mSession.getSessionDate(sessionId);
	}
	
	/**
	 * @return session date string long millisecond pattern
	 */
	public String getLastSessionDate(){
		return mSession.getSessionDate();
	}
	
	/**
	 * @return sessionId
	 * 0 if not have session
	 */
	public int getCurrentSession(){
		return mSession.getCurrentSession();
	}
}
