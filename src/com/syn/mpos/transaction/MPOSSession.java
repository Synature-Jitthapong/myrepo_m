package com.syn.mpos.transaction;

import java.util.Calendar;

import com.syn.pos.transaction.SessionCreation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MPOSSession extends MPOSTransaction implements SessionCreation {

	public MPOSSession(Context context) {
		super(context);
	}

	public boolean closeShift(int sessionId, int computerId, int closeStaffId,
			float closeAmount, int isEndday) {
		boolean isSuccess = false;
		isSuccess = closeSession(sessionId, computerId, closeStaffId, closeAmount, isEndday);
		return isSuccess;
	}
	
	@Override
	public int getMaxSessionId(int shopId, int computerId) {
		int sessionId = 0;
		String strSql = "SELECT MAX(session_id) " +
				" FROM session 	" +
				" WHERE shop_id=" + shopId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		mDbHelper.close();
		return sessionId + 1;
	}

	@Override
	public int addSession(int shopId, int computerId, int openStaffId, float openAmount) {
		int sessionId = getMaxSessionId(shopId, computerId);
		Calendar date = getDate();
		Calendar dateTime = getDateTime();
		
		ContentValues cv = new ContentValues();
		cv.put("session_id", sessionId);
		cv.put("computer_id", computerId);
		cv.put("shop_id", shopId);
		cv.put("session_date", date.getTimeInMillis());
		cv.put("open_date_time", dateTime.getTimeInMillis());
		cv.put("open_staff_id", openStaffId);
		cv.put("open_amount", openAmount);
		cv.put("is_endday", 0);
		
		mDbHelper.open();
		mDbHelper.insert("session", cv);
		mDbHelper.close();
		return sessionId;
	}

	@Override
	public boolean addSessionEnddayDetail(String sessionDate,
			float totalQtyReceipt, float totalAmountReceipt) {
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();
		
		ContentValues cv = new ContentValues();
		cv.put("session_date", sessionDate);
		cv.put("endday_date_time", dateTime.getTimeInMillis());
		cv.put("total_qty_receipt", totalQtyReceipt);
		cv.put("total_amount_receipt", totalAmountReceipt);
		
		mDbHelper.open();
		isSuccess = mDbHelper.insert("session_end_day", cv);
		mDbHelper.close();
		return isSuccess;
	}

	@Override
	public boolean closeSession(int sessionId, int computerId,
			int closeStaffId, float closeAmount, int isEnday) {
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();
		
		String strSql = "UPDATE session SET " +
				" close_staffId=" + closeStaffId + ", " +
				" close_date_time='" + dateTime.getTimeInMillis() + "', " +
				" close_amount=" + closeAmount + ", " +
				" is_end_day=" + isEnday +
				" WHERE session_id=" + sessionId +
				" AND computer_id=" + computerId;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}

	@Override
	public int getCurrentSession(int shopId, int computerId) {
		int sessionId = 0;
		String strSql = "SELECT session_id " +
				" FROM session " +
				" WHERE shop_id=" + shopId +
				" AND computer_id=" + computerId + 
				" AND is_endday=0";
		 
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			sessionId = cursor.getInt(0);
		}
		mDbHelper.close();
		return sessionId;
	}

}
