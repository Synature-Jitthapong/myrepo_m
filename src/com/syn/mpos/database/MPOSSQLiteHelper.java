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
	private SQLiteDatabase db;
	private MPOSSQLiteDatabase sqlite;
	private MPOSLog mposLog;
	
	public MPOSSQLiteHelper(Context c){
		sqlite = new MPOSSQLiteDatabase(c);
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
