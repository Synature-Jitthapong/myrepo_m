package com.syn.mpos.database;

import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.j1tth4.mobile.sqlite.SqliteExternalDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSSQLiteHelper extends SqliteExternalDatabase implements SQLiteHelper{
	public static final String DB_DIR = "MPOSDB";
	public static final String DB_NAME = "mpos.db";
	public static String mErrMsg;
	private MPOSLog mposLog;
	
	public MPOSSQLiteHelper(Context context) {
		super(context, DB_DIR, DB_NAME);
	}
	
	@Override
	public void open() {
		openDataBase();
	}
	
	@Override
	public void close() {
		closeDataBase();
	}

	@Override
	public boolean insert(String table, ContentValues cv) throws SQLException{
		boolean isSucc = false;
		
		try {
			db.insertOrThrow(table, null, cv);
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
		Cursor cursor = db.rawQuery(sqlQuery, null);
		return cursor;
	}

	@Override
	public boolean execSQL(String sqlExec) throws SQLException{
		boolean isSucc = false;
		try {
			db.execSQL(sqlExec);
			isSucc = true;
		} catch (SQLException e) {
			isSucc = false;
			mposLog.appendLog(sqlExec + " " + e.getMessage());
			mErrMsg = e.getMessage();
		}
		return isSucc;
	}

}
