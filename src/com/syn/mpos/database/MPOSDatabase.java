package com.syn.mpos.database;

import java.util.UUID;

import android.database.sqlite.SQLiteDatabase;

public class MPOSDatabase {
	// base uuid column
	public static final String COL_UUID = "UUID";
	
	// base send status column
	public static final int ALREADY_SEND = 1;
	public static final int NOT_SEND = 0;
	public static final String COL_SEND_STATUS = "SendStatus";
	
	protected SQLiteDatabase mSqlite;
	
	public MPOSDatabase(SQLiteDatabase db){
		mSqlite = db;
	}
	
	public String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
