package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class SyncSaleLog extends MPOSDatabase{
	public static final int SYNC_FAIL = 0;
	public static final int SYNC_SUCCESS = 1;

	public static final String TABLE_SYNC_SALE_LOG = "SyncSaleLog";
	public static final String COLUMN_SYNC_STATUS = "sync_status";
	
	public SyncSaleLog(Context c) {
		super(c);
	}
	
	public List<String> listSessionDate(){
		List<String> sessionDateLst = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * "
				+ " FROM " + TABLE_SYNC_SALE_LOG 
				+ " WHERE " + COLUMN_SYNC_STATUS
				+ "=?", 
				new String[]{String.valueOf(SYNC_FAIL)});
		if(cursor.moveToFirst()){
			sessionDateLst = new ArrayList<String>();
			do{
				String sessionDate = cursor.getString(cursor.getColumnIndex(Session.COLUMN_SESS_DATE));
				sessionDateLst.add(sessionDate);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return sessionDateLst;
	}
	
	public String getSyncSaleSessionDate(String sessionDate){
		String syncDate = "";
		Cursor cursor = mSqlite.query(TABLE_SYNC_SALE_LOG, 
				new String[]{Session.COLUMN_SESS_DATE}, 
				Session.COLUMN_SESS_DATE + "=?", 
				new String[]{String.valueOf(sessionDate)}, null, null, null);
		if(cursor.moveToFirst()){
			syncDate = cursor.getString(0);
		}
		return syncDate;
	}
	
	public void updateSyncSaleLog(String sessionDate, int status){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SYNC_STATUS, status);
		mSqlite.update(TABLE_SYNC_SALE_LOG, cv, 
				Session.COLUMN_SESS_DATE + "=?", 
				new String[]{sessionDate});
	}
	
	public void addSyncSaleLog(String sessionDate) throws SQLException{
		if(!getSyncSaleSessionDate(sessionDate).equals(sessionDate)){
			ContentValues cv = new ContentValues();
			cv.put(Session.COLUMN_SESS_DATE, sessionDate);
			cv.put(COLUMN_SYNC_STATUS, 0);
			mSqlite.insertOrThrow(TABLE_SYNC_SALE_LOG, null, cv);
		}
	}
}
