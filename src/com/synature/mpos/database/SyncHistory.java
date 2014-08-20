package com.synature.mpos.database;

import com.synature.mpos.Utils;
import com.synature.mpos.database.table.SyncHistoryTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SyncHistory extends MPOSDatabase{
	
	public static final int SYNC_STATUS_SUCCESS = 1;
	
	public static final int SYNC_STATUS_FAIL = 0;
	
	public SyncHistory(Context context) {
		super(context);
	}

	/**
	 * Get last success sync time
	 * @return time in millisecond
	 */
	public String getLastSyncTime(){
		String time = "";
		Cursor cursor = getReadableDatabase().query(
				SyncHistoryTable.TABLE_SYNC_HISTORY, 
				new String[]{
					SyncHistoryTable.COLUMN_SYNC_TIME	
				}, SyncHistoryTable.COLUMN_SYNC_STATUS + "=?", 
				new String[]{
					String.valueOf(SYNC_STATUS_SUCCESS)
				}, null, null, SyncHistoryTable.COLUMN_SYNC_TIME + " desc ", "1");
		if(cursor.moveToFirst()){
			time = cursor.getString(0);
		}
		cursor.close();
		return time;
	}
	
	/**
	 * @return true if all sync_status = 1
	 */
	public boolean IsAlreadySync(){
		boolean isSync = true;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + SyncHistoryTable.COLUMN_SYNC_ID + ","
				+ SyncHistoryTable.COLUMN_SYNC_STATUS
				+ " FROM " + SyncHistoryTable.TABLE_SYNC_HISTORY
				+ " WHERE " + SyncHistoryTable.COLUMN_SYNC_DATE + "=?",
				new String[]{
						String.valueOf(Utils.getDate().getTimeInMillis())
				});
		if(cursor.moveToFirst()){
			if(cursor.getInt(cursor.getColumnIndex(
					SyncHistoryTable.COLUMN_SYNC_STATUS)) == SYNC_STATUS_FAIL)
				isSync = false;
		}else{
			isSync = false;
		}
		cursor.close();
		return isSync;
	}
	
	public void insertSyncLog(int status){
		ContentValues cv = new ContentValues();
		cv.put(SyncHistoryTable.COLUMN_SYNC_STATUS, status);
		cv.put(SyncHistoryTable.COLUMN_SYNC_DATE, Utils.getDate().getTimeInMillis());
		cv.put(SyncHistoryTable.COLUMN_SYNC_TIME, Utils.getCalendar().getTimeInMillis());
		getWritableDatabase().insert(SyncHistoryTable.TABLE_SYNC_HISTORY, null, cv);
	}
	
	public void deleteSyncLog(){
		getWritableDatabase().delete(SyncHistoryTable.TABLE_SYNC_HISTORY, null, null);
	}
}
