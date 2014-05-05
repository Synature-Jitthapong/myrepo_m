package com.syn.mpos.database;

import java.util.Calendar;

import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.SessionDetailTable;
import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.ShopTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class SessionDataSource extends MPOSDatabase{
	
	public static final int NOT_ENDDAY_STATUS = 0;
	public static final int ALREADY_ENDDAY_STATUS = 1;
	
	public SessionDataSource(Context context) {
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
		cv.put(SessionTable.COLUMN_CLOSE_DATE, Util.getDateTime().getTimeInMillis());
		
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
			getWritableDatabase().insertOrThrow(SessionTable.TABLE_SESSION, null, cv);
		} catch (Exception e) {
			sessionId = 0;
			e.printStackTrace();
		}
		return sessionId;
	}

	/**
	 * @param sessionDate
	 * @param totalQtyReceipt
	 * @param totalAmountReceipt
	 * @return the row ID of newly insert
	 * @throws SQLException
	 */
	public long addSessionEnddayDetail(String sessionDate,
			double totalQtyReceipt, double totalAmountReceipt) throws SQLException {
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(SessionTable.COLUMN_SESS_DATE, sessionDate);
		cv.put(SessionTable.COLUMN_ENDDAY_DATE, dateTime.getTimeInMillis());
		cv.put(SessionTable.COLUMN_TOTAL_QTY_RECEIPT, totalQtyReceipt);
		cv.put(SessionTable.COLUMN_TOTAL_AMOUNT_RECEIPT, totalAmountReceipt);
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
		Calendar dateTime = Util.getDateTime();
		ContentValues cv = new ContentValues();
		cv.put(OrderTransactionTable.COLUMN_CLOSE_STAFF, closeStaffId);
		cv.put(SessionTable.COLUMN_CLOSE_DATE, dateTime.getTimeInMillis());
		cv.put(SessionTable.COLUMN_CLOSE_AMOUNT, closeAmount);
		cv.put(SessionTable.COLUMN_IS_ENDDAY, isEndday == true ? 1 : 0);
		return getWritableDatabase().update(SessionTable.TABLE_SESSION, cv, 
				SessionTable.COLUMN_SESS_ID + "=? AND ", 
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
}
