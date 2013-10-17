package com.syn.mpos.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSSQLiteDatabase extends SQLiteOpenHelper{
	private static final String DB_NAME = "mpos.db";
	private static final int DB_VER = 1;
	
	private final String[] sqlCreate = {
		MPOSSql.TB_BANK,
		MPOSSql.TB_CREDIT,
		MPOSSql.TB_DOC,
		MPOSSql.TB_DOC_DETAIL,
		MPOSSql.TB_DOC_TYPE,
		MPOSSql.TB_DOC_TYPE_GROUP,
		MPOSSql.TB_DOC_TYPE_GROUP_VAL,
		MPOSSql.TB_LANG,
		MPOSSql.TB_MEMBER_GROUP,
		MPOSSql.TB_MEMBER,
		MPOSSql.TB_ORDER,
		MPOSSql.TB_ORDER_TMP,
		MPOSSql.TB_PAY_DETAIL,
		MPOSSql.TB_PAY_TYPE,
		MPOSSql.TB_PRO_DEPT,
		MPOSSql.TB_PRO_GROUP,
		MPOSSql.TB_PRODUCT,
		MPOSSql.TB_PROVINCE,
		MPOSSql.TB_PROGRAM_FEATURE,
		MPOSSql.TB_SESSION,
		MPOSSql.TB_SESSION_END,
		MPOSSql.TB_SHOP,
		MPOSSql.TB_STAFF,
		MPOSSql.TB_STAFF_PERMISS,
		MPOSSql.TB_TRANS,
	};

	public MPOSSQLiteDatabase(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String strSql : sqlCreate){
			db.execSQL(strSql);
		}
		
		/**
		 * testing
		 */
		for(String strSql : MPOSSql.BANK_SQL){
			db.execSQL(strSql);
		}

		for(String strSql : MPOSSql.CARD_SQL){
			db.execSQL(strSql);
		}
		
		for(String strSql : MPOSSql.DOC_TYPE){
			db.execSQL(strSql);
		}
		
		for(String strSql : MPOSSql.PROVINCE){
			db.execSQL(strSql);
		}
		
		for(String strSql : MPOSSql.MG){
			db.execSQL(strSql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP DATABASE " + DB_NAME);
		onCreate(db);
	}
}
