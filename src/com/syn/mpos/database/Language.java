package com.syn.mpos.database;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import com.syn.pos.ShopData;

public  class Language{
	public static final String TB_NAME = "Language";
	public static final String COL_LANG_ID = "LangId";
	public static final String COL_LANG_NAME = "LangName";
	public static final String COL_LANG_CODE = "LangCode";
	
	private MPOSSQLiteHelper mSqlite;
	
	public Language(Context c){
		mSqlite = new MPOSSQLiteHelper(c);
	}
	
	public boolean insertLanguage(List<ShopData.Language> langLst) throws SQLException{
		boolean isSucc = false;	
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM " + TB_NAME);
		for(ShopData.Language lang : langLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_LANG_ID, lang.getLangID());
			cv.put(COL_LANG_NAME, lang.getLangName());
			cv.put(COL_LANG_CODE, lang.getLangCode());
			isSucc = mSqlite.insert(TB_NAME, cv);
		}
		mSqlite.close();
		return isSucc;
	}
}