package com.syn.mpos.database;

import com.j1tth4.mobile.sqlite.SQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSSQLiteHelper implements SQLiteHelper{
	public static String mErrMsg;
	private SQLiteDatabase mDb;
	private MPOSSQLiteDatabase mSqlite;
	private MPOSLog mposLog;
	
	public MPOSSQLiteHelper(Context c){
		mSqlite = new MPOSSQLiteDatabase(c);
		mposLog = new MPOSLog(c);
	}
	
	@Override
	public void open() {
		mDb = mSqlite.getWritableDatabase();
	}
	
	@Override
	public void close() {
		mSqlite.close();
	}

	@Override
	public boolean insert(String table, ContentValues cv){
		boolean isSucc = false;
		
		try {
			mDb.insertOrThrow(table, null, cv);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(e.getMessage());
			mErrMsg = e.getMessage();
		}
		return isSucc;
	}

	@Override
	public Cursor rawQuery(String sqlQuery){
		Cursor cursor = mDb.rawQuery(sqlQuery, null);
		return cursor;
	}

	@Override
	public boolean execSQL(String sqlExec){
		boolean isSucc = false;
		try {
			mDb.execSQL(sqlExec);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(sqlExec + " " + e.getMessage());
			mErrMsg = e.getMessage();
		}
		return isSucc;
	}

}
