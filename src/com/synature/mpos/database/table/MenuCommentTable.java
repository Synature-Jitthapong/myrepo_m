package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author j1tth4
 * MenuComment Table
 */
public class MenuCommentTable extends BaseColumn{

	public static final String TABLE_MENU_COMMENT = "MenuComment";
	public static final String COLUMN_COMMENT_ID = "menu_comment_id";
	public static final String COLUMN_COMMENT_GROUP_ID = "menu_comment_group_id";
	public static final String COLUMN_COMMENT_NAME = "menu_comment_name";
	public static final String COLUMN_COMMENT_NAME1 = "menu_comment_name1";
	public static final String COLUMN_COMMENT_NAME2 = "menu_comment_name2";
	public static final String COLUMN_COMMENT_NAME3 = "menu_comment_name3";
	
	private static final String SQL_CREATE = 
			" create table " + TABLE_MENU_COMMENT + " ( "
			+ COLUMN_COMMENT_ID + " integer not null, "
			+ COLUMN_COMMENT_GROUP_ID + " integer not null, "
			+ COLUMN_COMMENT_NAME + " text not null, "
			+ COLUMN_COMMENT_NAME1 + " text, "
			+ COLUMN_COMMENT_NAME2 + " text, "
			+ COLUMN_COMMENT_NAME3 + " text, "
			+ COLUMN_ORDERING + " integer default 0, "
			+ " primary key (" + COLUMN_COMMENT_ID + "));";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_MENU_COMMENT);
		onCreate(db);
	}
	
}
