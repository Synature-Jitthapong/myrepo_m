package com.syn.mpos.database;

import java.util.Calendar;

import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.SessionDetailTable;
import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.ShopTable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SessionDataSource extends MPOSDatabase{
	
	public static final int NOT_ENDDAY_STATUS = 0;
	public static final int ALREADY_ENDDAY_STATUS = 1;
	
	public SessionDataSource(SQLiteDatabase db) {
		super(db);
	}

	public void autoEnddaySession(String currentSaleDate, int closeStaffId){
		ContentValues cv = new ContentValues();
		cv.put(SessionTable.COLUMN_IS_ENDDAY, ALREADY_ENDDAY_STATUS);
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionTable.COLUMN_CLOSE_DATE, Util.getDateTime().getTimeInMillis());
		
		mSqlite.update(SessionDetailTable.TABLE_NAME, cv, 
				SessionTable.COLUMN_IS_ENDDAY + "=? " +
				" AND " + SessionTable.COLUMN_SESS_DATE + "<?", 
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
		Cursor cursor = mSqlite.query(SessionTable.TABLE_NAME, 
				new String[]{SessionTable.COLUMN_SESS_DATE}, 
				ComputerTable.COLUMN_COMPUTER_ID + "=?", 
				new String[]{String.valueOf(computerId)}, 
				null, null, SessionTable.COLUMN_SESS_DATE + " DESC ", "1");
		
		if(cursor.moveToFirst()){
			sessionDate = cursor.getString(0);
		}
		cursor.close();
		return sessionDate;
	}
	
	public int deleteSession(int sessionId){
		return mSqlite.delete(SessionTable.TABLE_NAME, SessionTable.COLUMN_SESS_ID + "=?", 
				new String[]{String.valueOf(sessionId)});
	}
	
	public String getSessionDate(int sessionId, int computerId){
		String sessionDate = "";
		Cursor cursor = mSqlite.query(SessionTable.TABLE_NAME, 
				new String[]{SessionTable.COLUMN_SESS_DATE}, 
				SessionTable.COLUMN_SESS_ID + "=? AND " + 
				ComputerTable.COLUMN_COMPUTER_ID + "=?", 
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
				" SELECT MAX(" + SessionTable.COLUMN_SESS_ID + ") " + 
				" FROM " + SessionTable.TABLE_NAME + 
				" WHERE " + ShopTable.COLUMN_SHOP_ID + "=" + shopId + 
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=" + computerId, null);
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
		cv.put(SessionTable.COLUMN_SESS_ID, sessionId);
		cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
		cv.put(ShopTable.COLUMN_SHOP_ID, shopId);
		cv.put(SessionTable.COLUMN_SESS_DATE, date.getTimeInMillis());
		cv.put(SessionTable.COLUMN_OPEN_DATE, dateTime.getTimeInMillis());
		cv.put(OrderTransactionTable.COLUMN_OPEN_STAFF, openStaffId);
		cv.put(SessionTable.COLUMN_OPEN_AMOUNT, openAmount);
		cv.put(SessionTable.COLUMN_IS_ENDDAY, 0);
		try {
			mSqlite.insertOrThrow(SessionTable.TABLE_NAME, null, cv);
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
		cv.put(SessionTable.COLUMN_SESS_DATE, sessionDate);
		cv.put(SessionTable.COLUMN_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(SessionTable.COLUMN_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(SessionTable.COLUMN_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);
		return mSqlite.insertOrThrow(SessionDetailTable.TABLE_NAME, null, cv);
	}

	public int closeSession(int sessionId, int computerId,
			int closeStaffId, double closeAmount, boolean isEndday){
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionTable.COLUMN_CLOSE_DATE, dateTime.getTimeInMillis());
		cv.put(SessionTable.COLUMN_CLOSE_AMOUNT, closeAmount);
		cv.put(SessionTable.COLUMN_IS_ENDDAY, isEndday == true ? 1 : 0);
		return mSqlite.update(SessionTable.TABLE_NAME, cv, 
				SessionTable.COLUMN_SESS_ID + "=? AND " + 
				ComputerTable.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(sessionId), 
					String.valueOf(computerId)
				});
	}

	public int getSessionEnddayDetail(String sessionDate){
		int session = 0;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT COUNT(*) " +
				" FROM " + SessionDetailTable.TABLE_NAME +
				" WHERE " + SessionTable.COLUMN_SESS_DATE + "=?", 
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
		Cursor cursor = mSqlite.query(SessionTable.TABLE_NAME, 
				new String[]{SessionTable.COLUMN_SESS_ID},  
				ComputerTable.COLUMN_COMPUTER_ID + " =? " +
				" AND " + OrderTransactionTable.COLUMN_OPEN_STAFF + " =? " +
				" AND " + SessionTable.COLUMN_SESS_DATE + " =? " +
				" AND " + SessionTable.COLUMN_IS_ENDDAY + " =? ",
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
}
