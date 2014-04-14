package com.syn.mpos.database;

import java.util.UUID;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MPOSDatabase{
	// base uuid column
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_SEND_STATUS = "send_status";
	
	// send status
	public static final int ALREADY_SEND = 1;
	public static final int NOT_SEND = 0;
	
	protected Context mContext;
	
	private MPOSSQLiteHelper mSqliteHelper;
	
	protected SQLiteDatabase mSqlite = null;
	
	public MPOSDatabase(Context c){
		mContext = c;
		mSqliteHelper = new MPOSSQLiteHelper(c);
	}
	
	public void open() throws SQLException{
		mSqlite = mSqliteHelper.getWritableDatabase();
	}
	
	public void close(){
		mSqliteHelper.close();
	}
	
	public SQLiteDatabase getDatabase(){
		return mSqlite;
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
