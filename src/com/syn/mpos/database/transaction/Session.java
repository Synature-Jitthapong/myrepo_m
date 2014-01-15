package com.syn.mpos.database.transaction;

import java.util.Calendar;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Util;

import android.R.integer;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Session extends Transaction{
	
	public static final int NOT_ENDDAY_STATUS = 0;
	public static final int ALREADY_ENDDAY_STATUS = 1;
	
	public static final String TB_SESSION = "Session";
	public static final String TB_SESSION_DETAIL = "SessionEnddayDetail";
	public static final String COL_SESS_ID = "SessionId";
	public static final String COL_SESS_DATE = "SessionDate";
	public static final String COL_OPEN_DATE = "OpenDateTime";
	public static final String COL_CLOSE_DATE = "CloseDateTime";
	public static final String COL_OPEN_AMOUNT = "OpenAmount";
	public static final String COL_CLOSE_AMOUNT = "CloseAmount";
	public static final String COL_IS_ENDDAY = "IsEndday";
	public static final String COL_ENDDAY_DATE = "EnddayDateTime";
	public static final String COL_TOTAL_QTY_RECEIPT = "TotalQtyReceipt";
	public static final String COL_TOTAL_AMOUNT_RECEIPT = "TotalAmountReceipt";
	public static final String COL_IS_SEND_TO_HQ = "IsSendToHq";
	public static final String COL_SEND_TO_HQ_DATE = "SendToHqDateTime";
	
	public Session(SQLiteDatabase db) {
		super(db);
	}

	public boolean closeShift(int sessionId, int computerId, int closeStaffId,
			float closeAmount, boolean isEndday) {
		boolean isSuccess = false;
		isSuccess = closeSession(sessionId, computerId, closeStaffId,
				closeAmount, isEndday);
		return isSuccess;
	}

	public String getSessionDate(int computerId){
		String sessionDate = "";
		Cursor cursor = mSqlite.query(Session.TB_SESSION, 
				new String[]{COL_SESS_DATE}, 
				Computer.COL_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(computerId)}, null, null, null);
		
		if(cursor.moveToFirst()){
			sessionDate = cursor.getString(0);
		}
		cursor.close();
		return sessionDate;
	}
	
	public int deleteSession(int sessionId){
		return mSqlite.delete(TB_SESSION, COL_SESS_ID + "=?", 
				new String[]{String.valueOf(sessionId)});
	}
	
	public String getSessionDate(int sessionId, int computerId){
		String sessionDate = "";
		Cursor cursor = mSqlite.query(Session.TB_SESSION, 
				new String[]{COL_SESS_DATE}, 
				COL_SESS_ID + "=? AND " + 
				Computer.COL_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(sessionId), 
				String.valueOf(computerId)}, null, null, null);
		
		if(cursor.moveToFirst()){
			sessionDate = cursor.getString(0);
		}
		cursor.close();
		return sessionDate;
	}
	
	public int getMaxSessionId(int shopId, int computerId) {
		int sessionId = 0;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT MAX(" + COL_SESS_ID + ") " + 
				" FROM " + TB_SESSION + 
				" WHERE " + Shop.COL_SHOP_ID + "=" + shopId + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId + 1;
	}

	public int addSession(int shopId, int computerId, int openStaffId,
			float openAmount) throws SQLException{
		int sessionId = getMaxSessionId(shopId, computerId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_SESS_ID, sessionId);
		cv.put(Computer.COL_COMPUTER_ID, computerId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(COL_SESS_DATE, date.getTimeInMillis());
		cv.put(COL_OPEN_DATE, dateTime.getTimeInMillis());
		cv.put(COL_OPEN_STAFF, openStaffId);
		cv.put(COL_OPEN_AMOUNT, openAmount);
		cv.put(COL_IS_ENDDAY, 0);
		try {
			mSqlite.insertOrThrow(TB_SESSION, null, cv);
		} catch (Exception e) {
			sessionId = 0;
			e.printStackTrace();
		}
		return sessionId;
	}

	public boolean addSessionEnddayDetail(String sessionDate,
			float totalQtyReceipt, float totalAmountReceipt) throws SQLException {
		boolean isSuccess = false;
		Calendar dateTime = Util.getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_SESS_DATE, sessionDate);
		cv.put(COL_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(COL_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(COL_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);
		try {
			mSqlite.insertOrThrow(TB_SESSION_DETAIL, null, cv);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	public boolean closeSession(int sessionId, int computerId,
			int closeStaffId, float closeAmount, boolean isEndday){
		boolean isSuccess = false;
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(COL_CLOSE_STAFF, closeStaffId);
		cv.put(COL_CLOSE_DATE, dateTime.getTimeInMillis());
		cv.put(COL_CLOSE_AMOUNT, closeAmount);
		cv.put(COL_IS_ENDDAY, isEndday == true ? 1 : 0);
		int affectRow = mSqlite.update(TB_SESSION, cv, 
				COL_SESS_ID + "=? AND " + 
				Computer.COL_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(sessionId), 
				String.valueOf(computerId)});
		if(affectRow > 0)
			isSuccess = true;
		return isSuccess;
	}

	public int getSessionEnddayDetail(String sessionDate){
		int session = 0;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT COUNT(*) " +
				" FROM " + TB_SESSION_DETAIL +
				" WHERE " + COL_SESS_DATE + "=?", 
				new String[]{
				sessionDate});
		if(cursor.moveToFirst()){
			session = cursor.getInt(0);
		}
		cursor.close();
		return session;
	}
	
	public int getCurrentSession(int computerId, int staffId) {
		int sessionId = 0;
		Cursor cursor = mSqlite.query(TB_SESSION, 
				new String[]{COL_SESS_ID},  
				Computer.COL_COMPUTER_ID + " =? " +
				" AND " + COL_OPEN_STAFF + " =? " +
				" AND " + COL_SESS_DATE + " =? ",
				new String[]{
				String.valueOf(computerId),
				String.valueOf(staffId),
				String.valueOf(Util.getDate().getTimeInMillis())}, null, null, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId;
	}
}
