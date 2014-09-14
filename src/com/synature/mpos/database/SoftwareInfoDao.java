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
				SoftwareInfoTable.COLUMN_VERSION,
				SoftwareInfoTable.COLUMN_DB_VERSION,
				SoftwareInfoTable.COLUMN_LAST_UPDATE
				}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			sw = new SoftwareInfo();
			sw.setVersion(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_VERSION)));
			sw.setDbVersion(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_DB_VERSION)));
			sw.setLastUpdate(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_LAST_UPDATE)));
		}
		cursor.close();
		return sw;
	}
	
	public void logSoftwareInfo(String version, String dbVersion){
		getWritableDatabase().delete(SoftwareInfoTable.TABLE_SOFTWARE_INFO, null, null);
		ContentValues cv = new ContentValues();
		cv.put(SoftwareInfoTable.COLUMN_VERSION, version);
		cv.put(SoftwareInfoTable.COLUMN_DB_VERSION, dbVersion);
		cv.put(SoftwareInfoTable.COLUMN_LAST_UPDATE, Utils.getCalendar().getTimeInMillis());
		getWritableDatabase().insert(SoftwareInfoTable.TABLE_SOFTWARE_INFO, null, cv);
	}
}
