package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class MemberGroupTable {
	public static final String TABLE_MEMBER_GROUP = "MemberGroup";
	public static final String COLUMN_MEMBER_GROUP_ID = "member_group_id";
	
	public static void onCreate(SQLiteDatabase db) {
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
