package com.syn.mpos.datasource;

import java.util.List;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public  class Language extends MPOSDatabase{
	
	public Language(SQLiteDatabase db){
		super(db);
	}
	
	public void insertLanguage(List<ShopData.Language> langLst) throws SQLException{	
		mSqlite.delete(LanguageEntry.TABLE_LANGUAGE, null, null);
		for(ShopData.Language lang : langLst){
			ContentValues cv = new ContentValues();
			cv.put(LanguageEntry.COLUMN_LANG_ID, lang.getLangID());
			cv.put(LanguageEntry.COLUMN_LANG_NAME, lang.getLangName());
			cv.put(LanguageEntry.COLUMN_LANG_CODE, lang.getLangCode());
			mSqlite.insertOrThrow(LanguageEntry.TABLE_LANGUAGE, null, cv);
		}
	}
	
	public static abstract class LanguageEntry{
		public static final String TABLE_LANGUAGE = "Language";
		public static final String COLUMN_LANG_ID = "lang_id";
		public static final String COLUMN_LANG_NAME = "lang_name";
		public static final String COLUMN_LANG_CODE = "lang_code";
	}
}