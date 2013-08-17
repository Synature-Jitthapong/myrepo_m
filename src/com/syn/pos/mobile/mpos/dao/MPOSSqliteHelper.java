package com.syn.pos.mobile.mpos.dao;

import com.j1tth4.mobile.sqlite.ISqliteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class MPOSSqliteHelper implements ISqliteHelper {
	
	private MPOSSqliteDatabase sqlite;
	private MPOSLog mposLog;
	
	public MPOSSqliteHelper(Context c){
		sqlite = new MPOSSqliteDatabase(c);
		mposLog = new MPOSLog(c);
	}
	
	@Override
	public void open() {
		sqlite.openDataBase();
	}

	@Override
	public void close() {
		sqlite.closeDataBase();
	}

	@Override
	public boolean insert(String table, ContentValues cv){
		boolean isSucc = false;
		
		try {
			sqlite.db.insertOrThrow(table, null, cv);
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
		Cursor cursor = sqlite.db.rawQuery(sqlQuery, null);
		return cursor;
	}

	@Override
	public boolean execSQL(String sqlExec){
		boolean isSucc = false;
		try {
			sqlite.db.execSQL(sqlExec);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(sqlExec + " " + e.getMessage());
			e.printStackTrace();
		}
		return isSucc;
	}

}
