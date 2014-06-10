package com.synature.mpos.dao;

import java.util.List;

import com.synature.pos.ShopData;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public  class Language extends MPOSDatabase{

	public Language(Context context){
		super(context);
	}
	
	public void insertLanguage(List<ShopData.Language> langLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(LanguageTable.TABLE_LANGUAGE, null, null);
			for(ShopData.Language lang : langLst){
				ContentValues cv = new ContentValues();
				cv.put(LanguageTable.COLUMN_LANG_ID, lang.getLangID());
				cv.put(LanguageTable.COLUMN_LANG_NAME, lang.getLangName());
				cv.put(LanguageTable.COLUMN_LANG_CODE, lang.getLangCode());
				getWritableDatabase().insertOrThrow(LanguageTable.TABLE_LANGUAGE, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class LanguageTable{
		
		public static final String TABLE_LANGUAGE = "Language";
		public static final String COLUMN_LANG_ID = "lang_id";
		public static final String COLUMN_LANG_NAME = "lang_name";
		public static final String COLUMN_LANG_CODE = "lang_code";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_LANGUAGE + " ( " +
				COLUMN_LANG_ID + " INTEGER DEFAULT 1, " +
				COLUMN_LANG_NAME + " TEXT, " +
				COLUMN_LANG_CODE + " TEXT DEFAULT 'en', " +
				"PRIMARY KEY (" + COLUMN_LANG_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}