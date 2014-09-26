package com.synature.mpos.database;

import com.synature.mpos.database.model.SoftwareUpdate;
import com.synature.mpos.database.table.SoftwareUpdateTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class SoftwareUpdateDao extends MPOSDatabase{

	public SoftwareUpdateDao(Context context) {
		super(context);
	}

	public SoftwareUpdate getUpdateData(){
		SoftwareUpdate update = null;
		Cursor cursor = getReadableDatabase().query(
				SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, 
				new String[]{
						SoftwareUpdateTable.COLUMN_IS_DOWNLOADED,
						SoftwareUpdateTable.COLUMN_IS_ALREADY_UPDATED
				}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			update = new SoftwareUpdate();
			update.setDownloaded(cursor.getInt(cursor.getColumnIndex(
					SoftwareUpdateTable.COLUMN_IS_DOWNLOADED)) == 0 ? false : true);
			update.setAlreadyUpdated(cursor.getInt(cursor.getColumnIndex(
					SoftwareUpdateTable.COLUMN_IS_ALREADY_UPDATED)) == 0 ? false : true);
		}
		cursor.close();
		return update;
	}
	
	public void setUpdateStatus(int status){
		ContentValues cv = new ContentValues();
		cv.put(SoftwareUpdateTable.COLUMN_IS_ALREADY_UPDATED, status);
		getWritableDatabase().update(SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, cv, null, null);
	}
	
	public void setDownloadStatus(int status){
		ContentValues cv = new ContentValues();
		cv.put(SoftwareUpdateTable.COLUMN_IS_DOWNLOADED, status);
		getWritableDatabase().update(SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, cv, null, null);
	}
	
	public void logSoftwareUpdate(String version){
		String currentVersion = getLogCurrentVersion();
		if(!TextUtils.isEmpty(currentVersion)){
			if(version.equals(currentVersion))
				return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.delete(SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, null, null);
		ContentValues cv = new ContentValues();
		cv.put(SoftwareUpdateTable.COLUMN_VERSION, version);
		cv.put(SoftwareUpdateTable.COLUMN_IS_DOWNLOADED, 0);
		cv.put(SoftwareUpdateTable.COLUMN_IS_ALREADY_UPDATED, 0);
		db.insert(SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, null, cv);
	}
	
	private String getLogCurrentVersion(){
		String currentVersion = null;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + SoftwareUpdateTable.COLUMN_VERSION
				+ " FROM " + SoftwareUpdateTable.TABLE_SOFTWARE_UPDATE, null);
		if(cursor.moveToFirst()){
			currentVersion = cursor.getString(0);
		}
		cursor.close();
		return currentVersion;
	}
}
