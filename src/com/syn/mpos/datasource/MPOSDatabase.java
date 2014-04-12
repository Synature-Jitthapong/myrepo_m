package com.syn.mpos.datasource;

import java.util.UUID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MPOSDatabase {
	// base uuid column
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_SEND_STATUS = "send_status";
	
	// send status
	public static final int ALREADY_SEND = 1;
	public static final int NOT_SEND = 0;
	
	protected Context mContext;
	protected SQLiteDatabase mSqlite;
	
	public MPOSDatabase(SQLiteDatabase db){
		mSqlite = db;
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
