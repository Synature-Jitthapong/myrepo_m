package com.syn.mpos.data;

import com.j1tth4.mobile.sqlite.SqliteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MPOSSqliteHelper implements SqliteHelper {
	
	private SQLiteDatabase db;
	private MPOSSqliteDatabase sqlite;
	private MPOSLog mposLog;
	
	public MPOSSqliteHelper(Context c){
		sqlite = new MPOSSqliteDatabase(c);
		mposLog = new MPOSLog(c);
	}
	
	@Override
	public void open() {
		db = sqlite.getWritableDatabase();
	}

	@Override
	public void close() {
		sqlite.close();
	}

	@Override
	public boolean insert(String table, ContentValues cv){
		boolean isSucc = false;
		
		try {
			db.insertOrThrow(table, null, cv);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(e.getMessage());
			e.printStackTrace();
		}
		return isSucc;
	}

	@Override
	public Cursor rawQuery(String sqlQuery){
		Cursor cursor = db.rawQuery(sqlQuery, null);
		return cursor;
	}

	@Override
	public boolean execSQL(String sqlExec){
		boolean isSucc = false;
		try {
			db.execSQL(sqlExec);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(sqlExec + " " + e.getMessage());
			e.printStackTrace();
		}
		return isSucc;
	}

}
