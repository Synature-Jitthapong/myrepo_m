package com.synature.mpos.database;

import com.synature.mpos.Utils;
import com.synature.mpos.database.model.SoftwareInfo;
import com.synature.mpos.database.table.SoftwareInfoTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SoftwareInfoDao extends MPOSDatabase{

	public SoftwareInfoDao(Context context) {
		super(context);
	}

	public SoftwareInfo getSoftwareInfo(){
		SoftwareInfo sw = null;
		Cursor cursor = getReadableDatabase().query(SoftwareInfoTable.TABLE_SOFTWARE_INFO, 
				new String[]{
					SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID,
					SoftwareInfoTable.COLUMN_VERSION,
					SoftwareInfoTable.COLUMN_DB_VERSION,
					SoftwareInfoTable.COLUMN_LAST_UPDATE,
					SoftwareInfoTable.COLUMN_IS_DOWNLOADED,
					SoftwareInfoTable.COLUMN_IS_ALREADY_UPDATE
				}, null, null, null, null, SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID + " DESC ", "1");
		if(cursor.moveToFirst()){
			sw = new SoftwareInfo();
			sw.setId(cursor.getInt(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID)));
			sw.setVersion(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_VERSION)));
			sw.setDbVersion(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_DB_VERSION)));
			sw.setLastUpdate(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_LAST_UPDATE)));
			sw.setDownloaded(cursor.getInt(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_IS_DOWNLOADED)) == 1 ? 
					true : false);
			sw.setAlreadyUpdate(cursor.getInt(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_IS_ALREADY_UPDATE)) == 1 ? 
					true : false);
		}
		cursor.close();
		return sw;
	}
	
	public void setStatusAlreadyUpdated(int id, int status){
		ContentValues cv = new ContentValues();
		cv.put(SoftwareInfoTable.COLUMN_IS_ALREADY_UPDATE, status);
		cv.put(SoftwareInfoTable.COLUMN_LAST_UPDATE, Utils.getCalendar().getTimeInMillis());
		getWritableDatabase().update(SoftwareInfoTable.TABLE_SOFTWARE_INFO, 
				cv, SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID + "=?", 
				new String[]{
					String.valueOf(id)
				});
	}
	
	public void setStatusDownloaded(int id, int status){
		ContentValues cv = new ContentValues();
		cv.put(SoftwareInfoTable.COLUMN_IS_DOWNLOADED, status);
		getWritableDatabase().update(SoftwareInfoTable.TABLE_SOFTWARE_INFO, 
				cv, SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID + "=?", 
				new String[]{
					String.valueOf(id)
				});
	}
	
	public int logSoftwareInfo(String version, String dbVersion){
		int maxId = getMaxId();
		ContentValues cv = new ContentValues();
		cv.put(SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID, maxId);
		cv.put(SoftwareInfoTable.COLUMN_VERSION, version);
		cv.put(SoftwareInfoTable.COLUMN_DB_VERSION, dbVersion);
		getWritableDatabase().insert(SoftwareInfoTable.TABLE_SOFTWARE_INFO, null, cv);
		return maxId;
	}
	
	private int getMaxId(){
		int maxId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT MAX(" + SoftwareInfoTable.COLUMN_SOFTWARE_INFO_ID + ")"
				+ " FROM " + SoftwareInfoTable.TABLE_SOFTWARE_INFO, null);
		if(cursor.moveToFirst()){
			maxId = cursor.getInt(0);
		}
		cursor.close();
		return maxId + 1;
	}
}
