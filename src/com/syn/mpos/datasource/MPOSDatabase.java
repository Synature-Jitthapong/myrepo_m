package com.syn.mpos.datasource;

import java.util.UUID;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MPOSDatabase {
	// base uuid column
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_SEND_STATUS = "send_status";
	
	// send status
	public static final int ALREADY_SEND = 1;
	public static final int NOT_SEND = 0;
	
	protected Context mContext;
	protected MPOSSQLiteHelper mDbHelper;
	protected SQLiteDatabase mSqlite;
	
	public MPOSDatabase(Context c){
		mDbHelper = new MPOSSQLiteHelper(c);
	}
	
	public void open() throws SQLException{
		mSqlite = mDbHelper.getWritableDatabase();
	}
	
	public void close(){
		mSqlite.close();
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
