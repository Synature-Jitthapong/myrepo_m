package com.syn.mpos.datasource;

import java.util.Calendar;

import com.syn.mpos.datasource.Computer.ComputerEntry;
import com.syn.mpos.datasource.Shop.ShopEntry;
import com.syn.mpos.datasource.Transaction.TransactionEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Session extends MPOSDatabase{
	
	public static final int NOT_ENDDAY_STATUS = 0;
	public static final int ALREADY_ENDDAY_STATUS = 1;
	
	public Session(SQLiteDatabase db) {
		super(db);
	}

	public void autoEnddaySession(String currentSaleDate, int closeStaffId){
		ContentValues cv = new ContentValues();
		cv.put(SessionEntry.COLUMN_IS_ENDDAY, ALREADY_ENDDAY_STATUS);
		cv.put(TransactionEntry.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionEntry.COLUMN_CLOSE_DATE, Util.getDateTime().getTimeInMillis());
		
		mSqlite.update(SessionEntry.TABLE_SESSION, cv, 
				SessionEntry.COLUMN_IS_ENDDAY + "=? " +
				" AND " + SessionEntry.COLUMN_SESS_DATE + "<?", 
				new String[]{
				String.valueOf(NOT_ENDDAY_STATUS),
				currentSaleDate
		});
	}
	
	public int closeShift(int sessionId, int computerId, int closeStaffId,
			double closeAmount, boolean isEndday) {
		return closeSession(sessionId, computerId, closeStaffId,
				closeAmount, isEndday);
	}

	public String getSessionDate(int computerId){
		String sessionDate = "";
		Cursor cursor = mSqlite.query(SessionEntry.TABLE_SESSION, 
				new String[]{SessionEntry.COLUMN_SESS_DATE}, 
				ComputerEntry.COLUMN_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(computerId)}, 
				null, null, SessionEntry.COLUMN_SESS_DATE + " DESC ", "1");
		
		if(cursor.moveToFirst()){
			sessionDate = cursor.getString(0);
		}
		cursor.close();
		return sessionDate;
	}
	
	public int deleteSession(int sessionId){
		return mSqlite.delete(SessionEntry.TABLE_SESSION, SessionEntry.COLUMN_SESS_ID + "=?", 
				new String[]{String.valueOf(sessionId)});
	}
	
	public String getSessionDate(int sessionId, int computerId){
		String sessionDate = "";
		Cursor cursor = mSqlite.query(SessionEntry.TABLE_SESSION, 
				new String[]{SessionEntry.COLUMN_SESS_DATE}, 
				SessionEntry.COLUMN_SESS_ID + "=? AND " + 
				ComputerEntry.COLUMN_COMPUTER_ID + "=?", 
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
				" SELECT MAX(" + SessionEntry.COLUMN_SESS_ID + ") " + 
				" FROM " + SessionEntry.TABLE_SESSION + 
				" WHERE " + ShopEntry.COLUMN_SHOP_ID + "=" + shopId + 
				" AND " + ComputerEntry.COLUMN_COMPUTER_ID + "=" + computerId, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId + 1;
	}

	public int addSession(int shopId, int computerId, int openStaffId,
			double openAmount){
		int sessionId = getMaxSessionId(shopId, computerId);
		Calendar date = Util.getDate();
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(SessionEntry.COLUMN_SESS_ID, sessionId);
		cv.put(ComputerEntry.COLUMN_COMPUTER_ID, computerId);
		cv.put(ShopEntry.COLUMN_SHOP_ID, shopId);
		cv.put(SessionEntry.COLUMN_SESS_DATE, date.getTimeInMillis());
		cv.put(SessionEntry.COLUMN_OPEN_DATE, dateTime.getTimeInMillis());
		cv.put(TransactionEntry.COLUMN_OPEN_STAFF, openStaffId);
		cv.put(SessionEntry.COLUMN_OPEN_AMOUNT, openAmount);
		cv.put(SessionEntry.COLUMN_IS_ENDDAY, 0);
		try {
			mSqlite.insertOrThrow(SessionEntry.TABLE_SESSION, null, cv);
		} catch (Exception e) {
			sessionId = 0;
			e.printStackTrace();
		}
		return sessionId;
	}

	public long addSessionEnddayDetail(String sessionDate,
			double totalQtyReceipt, double totalAmountReceipt) throws SQLException {
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(SessionEntry.COLUMN_SESS_DATE, sessionDate);
		cv.put(SessionEntry.COLUMN_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(SessionEntry.COLUMN_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(SessionEntry.COLUMN_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);
		return mSqlite.insertOrThrow(SessionEntry.TABLE_SESSION_DETAIL, null, cv);
	}

	public int closeSession(int sessionId, int computerId,
			int closeStaffId, double closeAmount, boolean isEndday){
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(TransactionEntry.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionEntry.COLUMN_CLOSE_DATE, dateTime.getTimeInMillis());
		cv.put(SessionEntry.COLUMN_CLOSE_AMOUNT, closeAmount);
		cv.put(SessionEntry.COLUMN_IS_ENDDAY, isEndday == true ? 1 : 0);
		return mSqlite.update(SessionEntry.TABLE_SESSION, cv, 
				SessionEntry.COLUMN_SESS_ID + "=? AND " + 
				ComputerEntry.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(sessionId), 
					String.valueOf(computerId)
				});
	}

	public int getSessionEnddayDetail(String sessionDate){
		int session = 0;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT COUNT(*) " +
				" FROM " + SessionEntry.TABLE_SESSION_DETAIL +
				" WHERE " + SessionEntry.COLUMN_SESS_DATE + "=?", 
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
		Cursor cursor = mSqlite.query(SessionEntry.TABLE_SESSION, 
				new String[]{SessionEntry.COLUMN_SESS_ID},  
				ComputerEntry.COLUMN_COMPUTER_ID + " =? " +
				" AND " + TransactionEntry.COLUMN_OPEN_STAFF + " =? " +
				" AND " + SessionEntry.COLUMN_SESS_DATE + " =? " +
				" AND " + SessionEntry.COLUMN_IS_ENDDAY + " =? ",
				new String[]{
				String.valueOf(computerId),
				String.valueOf(staffId),
				String.valueOf(Util.getDate().getTimeInMillis()),
				String.valueOf(NOT_ENDDAY_STATUS)}, null, null, null);
		if (cursor.moveToFirst()) {
			sessionId = cursor.getInt(0);
		}
		cursor.close();
		return sessionId;
	}
	
	public static abstract class SessionEntry{
		public static final String TABLE_SESSION = "Session";
		public static final String TABLE_SESSION_DETAIL = "SessionEnddayDetail";
		public static final String COLUMN_SESS_ID = "session_id";
		public static final String COLUMN_SESS_DATE = "session_date";
		public static final String COLUMN_OPEN_DATE = "open_date_time";
		public static final String COLUMN_CLOSE_DATE = "close_date_time";
		public static final String COLUMN_OPEN_AMOUNT = "open_amount";
		public static final String COLUMN_CLOSE_AMOUNT = "close_amount";
		public static final String COLUMN_IS_ENDDAY = "is_endday";
		public static final String COLUMN_ENDDAY_DATE = "endday_date_time";
		public static final String COLUMN_TOTAL_QTY_RECEIPT = "total_qty_receipt";
		public static final String COLUMN_TOTAL_AMOUNT_RECEIPT = "total_amount_receipt";
		public static final String COLUMN_IS_SEND_TO_HQ = "is_send_to_hq";
		public static final String COLUMN_SEND_TO_HQ_DATE = "send_to_hq_date_time";
	}
}
