package com.syn.mpos.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.syn.pos.ShopData;

public  class Language extends MPOSDatabase{

	public Language(Context c){
		super(c);
	}
	
	public void insertLanguage(List<ShopData.Language> langLst) throws SQLException{	
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(LanguageTable.TABLE_NAME, null, null);
			for(ShopData.Language lang : langLst){
				ContentValues cv = new ContentValues();
				cv.put(LanguageTable.COLUMN_LANG_ID, lang.getLangID());
				cv.put(LanguageTable.COLUMN_LANG_NAME, lang.getLangName());
				cv.put(LanguageTable.COLUMN_LANG_CODE, lang.getLangCode());
				mSqlite.insertOrThrow(LanguageTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally{
			mSqlite.endTransaction();
		}
	}
}