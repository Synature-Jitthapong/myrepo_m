package com.synature.mpos.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.synature.mpos.database.model.SoftwareInfo;
import com.synature.mpos.database.table.SoftwareInfoTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

public class SoftwareInfoDao extends MPOSDatabase{

	public SoftwareInfoDao(Context context) {
		super(context);
	}

	public SoftwareInfo getSoftwareInfo(){
		SoftwareInfo sw = null;
		Cursor cursor = getReadableDatabase().query(SoftwareInfoTable.TABLE_SOFTWARE_INFO, 
				new String[]{
					SoftwareInfoTable.COLUMN_EXP_DATE,
					SoftwareInfoTable.COLUMN_LOCK_DATE
				}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			sw = new SoftwareInfo();
			sw.setExpDate(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_EXP_DATE)));
			sw.setLockDate(cursor.getString(cursor.getColumnIndex(SoftwareInfoTable.COLUMN_LOCK_DATE)));
		}
		cursor.close();
		return sw;
	}
	
	public void logSoftwareInfo(String expDate, String lockDate){
		getWritableDatabase().delete(SoftwareInfoTable.TABLE_SOFTWARE_INFO, null, null);
		ContentValues cv = new ContentValues();
		if(!TextUtils.isEmpty(expDate)){
			try {
				Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expDate);
				cv.put(SoftwareInfoTable.COLUMN_EXP_DATE, d.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!TextUtils.isEmpty(lockDate)){
			try {
				Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(lockDate);
				cv.put(SoftwareInfoTable.COLUMN_LOCK_DATE, d.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(cv.size() > 0){
			getWritableDatabase().insert(SoftwareInfoTable.TABLE_SOFTWARE_INFO, null, cv);
		}
	}
}
