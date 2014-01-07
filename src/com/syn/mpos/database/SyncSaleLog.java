package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.transaction.Session;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class SyncSaleLog extends MPOSDatabase{
	public static final int SYNC_FAIL = 0;
	public static final int SYNC_SUCCESS = 1;
	
	public static final String TB_SYNC_SALE_LOG = "SyncSaleLog";
	public static final String COL_SYNC_STATUS = "SyncStatus";
	
	public SyncSaleLog(Context c) {
		super(c);
	}
	
	public List<String> listSessionDate(){
		List<String> sessionDateLst = null;
		
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT "
				+ " FROM " + TB_SYNC_SALE_LOG 
				+ " WHERE " + COL_SYNC_STATUS
				+ "=?", 
				new String[]{String.valueOf(SYNC_FAIL)});
		if(cursor.moveToFirst()){
			sessionDateLst = new ArrayList<String>();
			do{
				String sessionDate = cursor.getString(cursor.getColumnIndex(Session.COL_SESS_DATE));
				sessionDateLst.add(sessionDate);
			}while(cursor.moveToNext());
		}
		close();
		return sessionDateLst;
	}
	
	public void updateSyncSaleLog(String sessionDate, int status){
		open();
		ContentValues cv = new ContentValues();
		cv.put(COL_SYNC_STATUS, status);
		mSqlite.update(TB_SYNC_SALE_LOG, cv, 
				Session.COL_SESS_DATE + "=?", 
				new String[]{String.valueOf(sessionDate)});
		close();
	}
	
	public void addSyncSaleLog(String sessionDate) throws SQLException{
		open();
		mSqlite.delete(TB_SYNC_SALE_LOG, Session.COL_SESS_DATE + "=?", 
				new String[]{sessionDate});
		
		ContentValues cv = new ContentValues();
		cv.put(Session.COL_SESS_DATE, sessionDate);
		cv.put(COL_SYNC_STATUS, 0);
		mSqlite.insertOrThrow(TB_SYNC_SALE_LOG, null, cv);
		close();
	}
}
