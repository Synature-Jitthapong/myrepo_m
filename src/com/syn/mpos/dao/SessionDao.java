package com.syn.mpos.dao;

import java.util.Calendar;

import com.syn.mpos.dao.ComputerDao.ComputerTable;
import com.syn.mpos.dao.ShopDao.ShopTable;
import com.syn.mpos.dao.TransactionDao.OrderTransactionTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SessionDao extends MPOSDatabase{
	
	public static final int NOT_ENDDAY_STATUS = 0;
	public static final int ALREADY_ENDDAY_STATUS = 1;
	
	public SessionDao(Context context) {
		super(context);
	}

	/**
	 * @param currentSaleDate
	 * @param closeStaffId
	 */
	public void autoEnddaySession(String currentSaleDate, int closeStaffId){
		ContentValues cv = new ContentValues();
		cv.put(SessionTable.COLUMN_IS_ENDDAY, ALREADY_ENDDAY_STATUS);
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionTable.COLUMN_CLOSE_DATE, Util.getCalendar().getTimeInMillis());
		
		getWritableDatabase().update(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, cv, 
				SessionTable.COLUMN_IS_ENDDAY + "=? " +
				" AND " + SessionTable.COLUMN_SESS_DATE + "<?", 
				new String[]{
				String.valueOf(NOT_ENDDAY_STATUS),
				currentSaleDate
		});
	}
	
	/**
	 * @param sessionId
	 * @param closeStaffId
	 * @param closeAmount
	 * @param isEndday
	 * @return row affected
	 */
	public int closeShift(int sessionId, int closeStaffId,
			double closeAmount, boolean isEndday) {
		return closeSession(sessionId, closeStaffId,
				closeAmount, isEndday);
	}

	/**
	 * @return session date
	 */
	public String getSessionDate(){
		String sessionDate = "";
		Cursor cursor = getReadableDatabase().query(SessionTable.TABLE_SESSION, 
				new String[]{SessionTable.COLUMN_SESS_DATE}, 
				null, null, null, null, SessionTable.COLUMN_SESS_DATE + " DESC ", "1");
		
		if(cursor.moveToFirst()){
			sessionDate = cursor.getString(0);
		}
		cursor.close();
		return sessionDate;
	}
	
	/**
	 * @param sessionId
	 * @return row affected
	 */
	public int deleteSession(int sessionId){
		return getWritableDatabase().delete(SessionTable.TABLE_SESSION, 
				SessionTable.COLUMN_SESS_ID + "=?", 
				new String[]{String.valueOf(sessionId)});
	}
	
	/**
	 * @return max sessionId
	 */
	public int getMaxSessionId() {
		int sessionId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + SessionTable.COLUMN_SESS_ID + ") " + 
				" FROM " + SessionTable.TABLE_SESSION, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId + 1;
	}

	/**
	 * @param shopId
	 * @param computerId
	 * @param openStaffId
	 * @param openAmount
	 * @return sessionId
	 */
	public int openSession(int shopId, int computerId, int openStaffId,
			double openAmount){
		int sessionId = getMaxSessionId();
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getCalendar();
		ContentValues cv = new ContentValues();
		cv.put(SessionTable.COLUMN_SESS_ID, sessionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ShopTable.COLUMN_SHOP_ID, shopId);
		cv.put(SessionTable.COLUMN_SESS_DATE, date.getTimeInMillis());
		cv.put(SessionTable.COLUMN_OPEN_DATE, dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, openStaffId);
		cv.put(SessionTable.COLUMN_OPEN_AMOUNT, openAmount);
		cv.put(SessionTable.COLUMN_IS_ENDDAY, 0);
		try {
			getWritableDatabase().insertOrThrow(SessionTable.TABLE_SESSION, null, cv);
		} catch (Exception e) {
			sessionId = 0;
			e.printStackTrace();
		}
		return sessionId;
	}

	/**
	 * Update set send status
	 * @param sessionDate
	 * @param status
	 * @return rows affected
	 */
	public int updateSessionEnddayDetail(String sessionDate, int status){
		ContentValues cv = new ContentValues();
		cv.put(BaseColumn.COLUMN_SEND_STATUS, status);
		return getWritableDatabase().update(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL,
				cv, SessionTable.COLUMN_SESS_DATE + "=?", new String[]{sessionDate});
	}
	
	/**
	 * @param sessionDate
	 * @param totalQtyReceipt
	 * @param totalAmountReceipt
	 * @return the row ID of newly insert
	 * @throws SQLException
	 */
	public long addSessionEnddayDetail(String sessionDate, double totalQtyReceipt, 
			double totalAmountReceipt) throws SQLException {
		Calendar dateTime = Util.getCalendar();
		ContentValues cv = new ContentValues();
		cv.put(SessionTable.COLUMN_SESS_DATE, sessionDate);
		cv.put(SessionDetailTable.COLUMN_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(SessionDetailTable.COLUMN_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);
		return getWritableDatabase().insertOrThrow(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, null, cv);
	}

	/**
	 * @param sessionId
	 * @param closeStaffId
	 * @param closeAmount
	 * @param isEndday
	 * @return row affected
	 */
	public int closeSession(int sessionId, int closeStaffId, 
			double closeAmount, boolean isEndday){
		Calendar dateTime = Util.getCalendar();
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionTable.COLUMN_CLOSE_DATE, dateTime.getTimeInMillis());
		cv.put(SessionTable.COLUMN_CLOSE_AMOUNT, closeAmount);
		cv.put(SessionTable.COLUMN_IS_ENDDAY, isEndday == true ? 1 : 0);
		return getWritableDatabase().update(SessionTable.TABLE_SESSION, cv, 
				SessionTable.COLUMN_SESS_ID + "=?", 
				new String[]{
					String.valueOf(sessionId)
				});
	}

	
	/**
	 * @param sessionDate
	 * @return number of session endday
	 */
	public int checkEndday(String sessionDate){
		int session = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(*) " +
				" FROM " + SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL +
				" WHERE " + SessionTable.COLUMN_SESS_DATE + "=?", 
				new String[]{
				sessionDate});
		if(cursor.moveToFirst()){
			session = cursor.getInt(0);
		}
		cursor.close();
		return session;
	}
	
	/**
	 * @param staffId
	 * @param saleDate
	 * @return sessionId
	 */
	public int getCurrentSession(int staffId, String saleDate) {
		int sessionId = 0;
		Cursor cursor = getReadableDatabase().query(
				SessionTable.TABLE_SESSION,
				new String[] { SessionTable.COLUMN_SESS_ID },
				OrderTransactionTable.COLUMN_OPEN_STAFF + " =? " + " AND "
						+ SessionTable.COLUMN_SESS_DATE + " =? " + " AND "
						+ SessionTable.COLUMN_IS_ENDDAY + " =? ",
				new String[] { String.valueOf(staffId), saleDate,
						String.valueOf(NOT_ENDDAY_STATUS) }, null, null, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId;
	}
	
	/**
	 * @return current sessionId
	 */
	public int getCurrentSessionId() {
		int sessionId = 0;
		Cursor cursor = getReadableDatabase().query(
				SessionTable.TABLE_SESSION,
				new String[] { SessionTable.COLUMN_SESS_ID },
				SessionTable.COLUMN_IS_ENDDAY + "=?",
				new String[] {
						String.valueOf(NOT_ENDDAY_STATUS) }, null, null, 
						SessionTable.COLUMN_SESS_ID + " DESC ", "1");
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId;
	}
	
	/**
	 * @param staffId
	 * @return current sessionId by staff
	 */
	public int getCurrentSessionId(int staffId) {
		int sessionId = 0;
		Cursor cursor = getReadableDatabase().query(
				SessionTable.TABLE_SESSION,
				new String[] { SessionTable.COLUMN_SESS_ID },
				OrderTransactionTable.COLUMN_OPEN_STAFF + "=?" + " AND "
						+ SessionTable.COLUMN_IS_ENDDAY + "=?",
				new String[] { String.valueOf(staffId),
						String.valueOf(NOT_ENDDAY_STATUS) }, null, null, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId;
	}
	
	public static  class SessionDetailTable {

		public static final String TABLE_SESSION_ENDDAY_DETAIL = "SessionEnddayDetail";
		public static final String COLUMN_ENDDAY_DATE = "endday_date_time";
		public static final String COLUMN_TOTAL_QTY_RECEIPT = "total_qty_receipt";
		public static final String COLUMN_TOTAL_AMOUNT_RECEIPT = "total_amount_receipt";

		private static final String SQL_CREATE = "CREATE TABLE "
				+ TABLE_SESSION_ENDDAY_DETAIL + " ( "
				+ SessionTable.COLUMN_SESS_DATE + " TEXT, " + COLUMN_ENDDAY_DATE
				+ " TEXT, " + COLUMN_TOTAL_QTY_RECEIPT + " INTEGER, "
				+ COLUMN_TOTAL_AMOUNT_RECEIPT + " REAL, "
				+ BaseColumn.COLUMN_SEND_STATUS + " INTEGER, " + "PRIMARY KEY ("
				+ SessionTable.COLUMN_SESS_DATE + "));";

		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

		}
	}
	
	public static class SessionTable{
		
		public static final String TABLE_SESSION = "Session";
		public static final String COLUMN_SESS_ID = "session_id";
		public static final String COLUMN_SESS_DATE = "session_date";
		public static final String COLUMN_OPEN_DATE = "open_date_time";
		public static final String COLUMN_CLOSE_DATE = "close_date_time";
		public static final String COLUMN_OPEN_AMOUNT = "open_amount";
		public static final String COLUMN_CLOSE_AMOUNT = "close_amount";
		public static final String COLUMN_IS_ENDDAY = "is_endday";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_SESSION + " ( " +
				COLUMN_SESS_ID + " INTEGER, " +
				ComputerTable.COLUMN_COMPUTER_ID + " INTEGER, " +
				ShopTable.COLUMN_SHOP_ID + " INTEGER, " +
				OrderTransactionTable.COLUMN_OPEN_STAFF + " INTEGER, " +
				OrderTransactionTable.COLUMN_CLOSE_STAFF + " INTEGER, " +
				COLUMN_SESS_DATE + " TEXT, " +
				COLUMN_OPEN_DATE + " TEXT, " +
				COLUMN_CLOSE_DATE + " TEXT, " +
				COLUMN_OPEN_AMOUNT + " REAL, " +
				COLUMN_CLOSE_AMOUNT + " REAL, " +
				COLUMN_IS_ENDDAY + " INTEGER, " +
				"PRIMARY KEY (" + COLUMN_SESS_ID + "));";
		
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}
		
		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}	
	}
}
