package com.syn.mpos.database;

import java.util.UUID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MPOSDatabase{
	// base uuid column
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_SEND_STATUS = "send_status";
	
	// send status
	public static final int ALREADY_SEND = 1;
	public static final int NOT_SEND = 0;
	
	protected MPOSSQLiteHelper mDatabaseHelper;
	
	public MPOSDatabase(Context context){
		mDatabaseHelper = MPOSSQLiteHelper.getInstance(context);
	}
	
	public SQLiteDatabase getWritableDatabase(){
		return mDatabaseHelper.getWritableDatabase();
	}
	
	public SQLiteDatabase getReadableDatabase(){
		return mDatabaseHelper.getReadableDatabase();
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
