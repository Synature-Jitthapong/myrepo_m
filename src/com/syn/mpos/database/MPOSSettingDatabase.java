package com.syn.mpos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MPOSSettingDatabase extends SQLiteOpenHelper {
	public static final String DB_NAME = "setting.db";
	public static final int DB_VER = 1;
	private String[] mSqlCreate = {
			MPOSSql.TB_CONN_SETTING,
			MPOSSql.TB_PRINTER_SETTING,
			MPOSSql.TB_SYNC_ITEM
	}; 
	
	public MPOSSettingDatabase(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String sql : mSqlCreate){
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
