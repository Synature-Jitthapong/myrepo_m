package com.syn.mpos.database;

import java.util.UUID;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class MPOSDatabase {
	// base uuid column
	public static final String COL_UUID = "UUID";
	
	protected MPOSSQLiteHelper mSqliteHelper;
	protected SQLiteDatabase mSqlite;
	
	public MPOSDatabase(Context c){
		mSqliteHelper = new MPOSSQLiteHelper(c);
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
	public SQLiteDatabase open(){
		return mSqlite = mSqliteHelper.getWritableDatabase();
	}
	
	public void close() throws SQLException{
		mSqliteHelper.close();
	}
}
