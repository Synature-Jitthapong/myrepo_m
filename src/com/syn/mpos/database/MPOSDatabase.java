package com.syn.mpos.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class MPOSDatabase {
	protected MPOSSQLiteHelper mSqliteHelper;
	protected SQLiteDatabase mSqlite;
	
	public MPOSDatabase(Context c){
		mSqliteHelper = new MPOSSQLiteHelper(c);
	}
	
	public SQLiteDatabase open(){
		return mSqlite = mSqliteHelper.getWritableDatabase();
	}
	
	public void close() throws SQLException{
		mSqliteHelper.close();
	}
}
