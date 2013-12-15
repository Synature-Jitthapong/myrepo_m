package com.syn.mpos.database;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import com.syn.pos.ShopData;

public  class Language extends MPOSDatabase{
	public static final String TB_LANGUAGE = "Language";
	public static final String COL_LANG_ID = "LangId";
	public static final String COL_LANG_NAME = "LangName";
	public static final String COL_LANG_CODE = "LangCode";
	
	public Language(Context c){
		super(c);
	}
	
	public void insertLanguage(List<ShopData.Language> langLst) throws SQLException{	
		open();
		mSqlite.execSQL("DELETE FROM " + TB_LANGUAGE);
		for(ShopData.Language lang : langLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_LANG_ID, lang.getLangID());
			cv.put(COL_LANG_NAME, lang.getLangName());
			cv.put(COL_LANG_CODE, lang.getLangCode());
			mSqlite.insertOrThrow(TB_LANGUAGE, null, cv);
		}
		mSqlite.close();
	}
}