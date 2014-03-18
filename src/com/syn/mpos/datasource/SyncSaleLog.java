package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.datasource.Session.SessionEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SyncSaleLog extends MPOSDatabase{
	public static final int SYNC_FAIL = 0;
	public static final int SYNC_SUCCESS = 1;
	
	public SyncSaleLog(SQLiteDatabase db) {
		super(db);
	}
	
	public List<String> listSessionDate(){
		List<String> sessionDateLst = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * "
				+ " FROM " + SyncSaleLogEntry.TABLE_SYNC_SALE_LOG 
				+ " WHERE " + SyncSaleLogEntry.COLUMN_SYNC_STATUS
				+ "=?", 
				new String[]{String.valueOf(SYNC_FAIL)});
		if(cursor.moveToFirst()){
			sessionDateLst = new ArrayList<String>();
			do{
				String sessionDate = cursor.getString(cursor.getColumnIndex(SessionEntry.COLUMN_SESS_DATE));
				sessionDateLst.add(sessionDate);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return sessionDateLst;
	}
	
	public String getSyncSaleSessionDate(String sessionDate){
		String syncDate = "";
		Cursor cursor = mSqlite.query(SyncSaleLogEntry.TABLE_SYNC_SALE_LOG, 
				new String[]{SessionEntry.COLUMN_SESS_DATE}, 
				SessionEntry.COLUMN_SESS_DATE + "=?", 
				new String[]{String.valueOf(sessionDate)}, null, null, null);
		if(cursor.moveToFirst()){
			syncDate = cursor.getString(0);
		}
		return syncDate;
	}
	
	public void updateSyncSaleLog(String sessionDate, int status){
		ContentValues cv = new ContentValues();
		cv.put(SyncSaleLogEntry.COLUMN_SYNC_STATUS, status);
		mSqlite.update(SyncSaleLogEntry.TABLE_SYNC_SALE_LOG, cv, 
				SessionEntry.COLUMN_SESS_DATE + "=?", 
				new String[]{sessionDate});
	}
	
	public void addSyncSaleLog(String sessionDate) throws SQLException{
		if(!getSyncSaleSessionDate(sessionDate).equals(sessionDate)){
			ContentValues cv = new ContentValues();
			cv.put(SessionEntry.COLUMN_SESS_DATE, sessionDate);
			cv.put(SyncSaleLogEntry.COLUMN_SYNC_STATUS, 0);
			mSqlite.insertOrThrow(SyncSaleLogEntry.TABLE_SYNC_SALE_LOG, null, cv);
		}
	}
	
	public static abstract class SyncSaleLogEntry{
		public static final String TABLE_SYNC_SALE_LOG = "SyncSaleLog";
		public static final String COLUMN_SYNC_STATUS = "sync_status";
	}
}
