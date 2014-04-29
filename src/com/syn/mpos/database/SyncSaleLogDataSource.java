package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.SyncSaleLogTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class SyncSaleLogDataSource extends MPOSDatabase{
	
	public static final int SYNC_FAIL = 0;
	public static final int SYNC_SUCCESS = 1;
	
	public SyncSaleLogDataSource(Context context) {
		super(context);
	}
	
	/**
	 * @return List<String>
	 */
	protected List<String> listSessionDate(){
		List<String> sessionDateLst = null;
		Cursor cursor = getReadableDatabase().query(
				SyncSaleLogTable.TABLE_NAME,
				new String[] { SessionTable.COLUMN_SESS_DATE },
				SyncSaleLogTable.COLUMN_SYNC_STATUS + "=?",
				new String[] { String.valueOf(SYNC_FAIL) }, null, null, null);
		if (cursor.moveToFirst()) {
			sessionDateLst = new ArrayList<String>();
			do {
				String sessionDate = cursor.getString(cursor
						.getColumnIndex(SessionTable.COLUMN_SESS_DATE));
				sessionDateLst.add(sessionDate);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return sessionDateLst;
	}
	
	/**
	 * @param sessionDate
	 * @return session date
	 */
	protected String getSyncSaleSessionDate(String sessionDate){
		String syncDate = "";
		Cursor cursor = getReadableDatabase().query(SyncSaleLogTable.TABLE_NAME, 
				new String[]{SessionTable.COLUMN_SESS_DATE}, 
				SessionTable.COLUMN_SESS_DATE + "=?", 
				new String[]{String.valueOf(sessionDate)}, null, null, null);
		if(cursor.moveToFirst()){
			syncDate = cursor.getString(0);
		}
		return syncDate;
	}
	
	/**
	 * @param sessionDate
	 * @param status
	 */
	protected void updateSyncSaleLog(String sessionDate, int status){
		ContentValues cv = new ContentValues();
		cv.put(SyncSaleLogTable.COLUMN_SYNC_STATUS, status);
		getWritableDatabase().update(SyncSaleLogTable.TABLE_NAME, cv, 
				SessionTable.COLUMN_SESS_DATE + "=?", 
				new String[]{sessionDate});
	}
	
	/**
	 * @param sessionDate
	 * @throws SQLException
	 */
	protected void addSyncSaleLog(String sessionDate) throws SQLException{
		if(!getSyncSaleSessionDate(sessionDate).equals(sessionDate)){
			ContentValues cv = new ContentValues();
			cv.put(SessionTable.COLUMN_SESS_DATE, sessionDate);
			cv.put(SyncSaleLogTable.COLUMN_SYNC_STATUS, 0);
			getWritableDatabase().insertOrThrow(SyncSaleLogTable.TABLE_NAME, null, cv);
		}
	}
}
