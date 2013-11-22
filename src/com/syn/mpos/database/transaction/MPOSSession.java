package com.syn.mpos.database.transaction;

import java.util.Calendar;

import com.syn.mpos.database.Computer;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class MPOSSession extends MPOSTransaction{

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
	
	public MPOSSession(Context c) {
		super(c);
	}

	public boolean closeShift(int sessionId, int computerId, int closeStaffId,
			float closeAmount, int isEndday) {
		boolean isSuccess = false;
		isSuccess = closeSession(sessionId, computerId, closeStaffId,
				closeAmount, isEndday);
		return isSuccess;
	}

	public int getMaxSessionId(int shopId, int computerId) {
		int sessionId = 0;
		String strSql = "SELECT MAX(" + COL_SESS_ID + ") " + 
				" FROM " + TB_SESSION + 
				" WHERE " + Shop.COL_SHOP_ID + "=" + shopId + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId;

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery(strSql);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		mSqlite.close();
		return sessionId + 1;
	}

	public int addSession(int shopId, int computerId, int openStaffId,
			float openAmount) throws SQLException{
		int sessionId = getMaxSessionId(shopId, computerId);
		Calendar date = getDate();
		Calendar dateTime = getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_SESS_ID, sessionId);
		cv.put(Computer.COL_COMPUTER_ID, computerId);
		cv.put(Shop.COL_SHOP_ID, shopId);
		cv.put(COL_SESS_DATE, date.getTimeInMillis());
		cv.put(COL_OPEN_DATE, dateTime.getTimeInMillis());
		cv.put(COL_OPEN_STAFF, openStaffId);
		cv.put(COL_OPEN_AMOUNT, openAmount);
		cv.put(COL_IS_ENDDAY, 0);

		mSqlite.open();
		mSqlite.insert(TB_SESSION, cv);
		mSqlite.close();
		return sessionId;
	}

	public boolean addSessionEnddayDetail(String sessionDate,
			float totalQtyReceipt, float totalAmountReceipt) throws SQLException {
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();

		ContentValues cv = new ContentValues();
		cv.put(COL_SESS_DATE, sessionDate);
		cv.put(COL_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(COL_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(COL_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);

		mSqlite.open();
		isSuccess = mSqlite.insert(TB_SESSION_DETAIL, cv);
		mSqlite.close();
		return isSuccess;
	}

	public boolean closeSession(int sessionId, int computerId,
			int closeStaffId, float closeAmount, int isEnday) throws SQLException {
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();

		String strSql = "UPDATE " +TB_SESSION + 
				" SET " + COL_CLOSE_STAFF + "=" + closeStaffId + ", " + 
				COL_CLOSE_DATE  + "='" + dateTime.getTimeInMillis() + "', " + 
				COL_CLOSE_AMOUNT + "=" + closeAmount + ", " + 
				COL_IS_ENDDAY + "=" + isEnday +
				" WHERE " + COL_SESS_ID + "=" + sessionId + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId;

		mSqlite.open();
		isSuccess = mSqlite.execSQL(strSql);
		mSqlite.close();
		return isSuccess;
	}

	public int getCurrentSession(int shopId, int computerId) {
		int sessionId = 0;
		String strSql = "SELECT " + COL_SESS_ID + 
				" FROM " + TB_SESSION +
				" WHERE " + Shop.COL_SHOP_ID + "=" + shopId + 
				" AND " + Computer.COL_COMPUTER_ID + "=" + computerId +
				" AND " + COL_IS_ENDDAY + "=0";

		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery(strSql);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		mSqlite.close();
		return sessionId;
	}
}
