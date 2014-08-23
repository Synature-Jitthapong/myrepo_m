package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author j1tth4
 * MenuCommentGroup Table
 */
public class MenuCommentGroupTable{
	
	public static final String TABLE_MENU_COMMENT_GROUP = "MenuCommentGroup";
	public static final String COLUMN_COMMENT_GROUP_NAME = "menu_comment_group_name";
	public static final String COLUMN_COMMENT_GROUP_NAME1 = "menu_comment_group_name1";
	
	private static final String SQL_CREATE = 
			" create table " + TABLE_MENU_COMMENT_GROUP + " ( "
			+ MenuCommentTable.COLUMN_COMMENT_GROUP_ID + " integer not null, "
			+ COLUMN_COMMENT_GROUP_NAME + " text, "
			+ COLUMN_COMMENT_GROUP_NAME1 + " text, "
			+ " primary key (" + MenuCommentTable.COLUMN_COMMENT_GROUP_ID + "));";
	
	public static void onCreate(SQLiteDatabase db){
		db.execSQL(SQL_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("drop table if exists " + TABLE_MENU_COMMENT_GROUP);
		onCreate(db);
	}
}
