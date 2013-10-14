package com.syn.mpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j1tth4.mobile.sqlite.SQLiteHelper;

public class MPOSSettingHelper implements SQLiteHelper{
	public static String mErrMsg;
	private SQLiteDatabase mSqlite;
	private MPOSSettingDatabase mDbSetting;
	private MPOSLog mposLog;
	
	public MPOSSettingHelper(Context c){
		mDbSetting = new MPOSSettingDatabase(c);
		mposLog = new MPOSLog(c);
	}
	
	@Override
	public void open() {
		mSqlite = mDbSetting.getWritableDatabase();
	}

	@Override
	public void close() {
		mDbSetting.close();
	}

	@Override
	public boolean insert(String table, ContentValues cv) {
		boolean isSuccess = false;
		try {
			mSqlite.insertOrThrow(table, null, cv);
		} catch (SQLException e) {
			mposLog.appendLog(e.getMessage());
			mErrMsg = e.getMessage();
			isSuccess = false;
		}
		return isSuccess;
	}

	@Override
	public Cursor rawQuery(String strQuery) {
		return mSqlite.rawQuery(strQuery, null);
	}

	@Override
	public boolean execSQL(String sqlExec) {
		boolean isSuccess = false;
		try {
			mSqlite.execSQL(sqlExec);
		} catch (SQLException e) {
			mposLog.appendLog(e.getMessage());
			mErrMsg = e.getMessage();
			isSuccess = false;
		}
		return isSuccess;
	}

}
