package com.syn.mpos.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.syn.mpos.database.table.LanguageTable;
import com.syn.pos.ShopData;

public  class LanguageDataSource extends MPOSDatabase{

	public LanguageDataSource(Context context){
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
}