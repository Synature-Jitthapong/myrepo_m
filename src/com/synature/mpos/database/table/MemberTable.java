package com.synature.mpos.database.table;

import android.database.sqlite.SQLiteDatabase;

public class MemberTable {
	public static final String TABLE_MEMBER = "Members";
	public static final String COLUMN_MEMBER_ID = "member_id";
	public static final String COLUMN_MEMBER_CODE = "member_code";
	public static final String COLUMN_MEMBER_GENDER = "member_gender";
	public static final String COLUMN_MEMBER_FIRST_NAME = "member_first_name";
	public static final String COLUMN_MEMBER_LAST_NAME = "member_last_name";
	public static final String COLUMN_MEMBER_PASS = "member_password";
	public static final String COLUMN_MEMBER_ADDR1 = "member_addr1";
	public static final String COLUMN_MEMBER_ADDR2 = "member_addr2";
	public static final String COLUMN_MEMBER_CITY = "member_city";
	public static final String COLUMN_MEMBER_PROVINCE = "member_province";
	public static final String COLUMN_MEMBER_ZIPCODE = "member_zipcode";
	public static final String COLUMN_MEMBER_TEL = "member_tel";
	public static final String COLUMN_MEMBER_MOBILE = "member_mobile";
	public static final String COLUMN_MEMBER_EMAIL = "member_email";
	public static final String COLUMN_MEMBER_BIRTHDAY = "member_birthday";
	public static final String COLUMN_MEMBER_ID_NUMBER = "member_id_number";
	public static final String COLUMN_MEMBER_ID_ISSUE_DATE = "member_issue_date";
	public static final String COLUMN_MEMBER_EXPIRE = "member_expire";

	private static final String SQL_CREATE = "create table " + TABLE_MEMBER + " ("
			+ COLUMN_MEMBER_ID + " integer not null, "
			+ MemberGroupTable.COLUMN_MEMBER_GROUP_ID + " integer default 0, "
			+ COLUMN_MEMBER_CODE + " text, "
			+ COLUMN_MEMBER_GENDER + " integer, "
			+ COLUMN_MEMBER_FIRST_NAME + " text, "
			+ COLUMN_MEMBER_LAST_NAME + " text, "
			+ COLUMN_MEMBER_PASS + " text, "
			+ COLUMN_MEMBER_ADDR1 + " text, "
			+ COLUMN_MEMBER_ADDR2 + " text, "
			+ COLUMN_MEMBER_CITY + " text, "
			+ COLUMN_MEMBER_PROVINCE + " integer, "
			+ COLUMN_MEMBER_ZIPCODE + " text, "
			+ COLUMN_MEMBER_TEL + " text, "
			+ COLUMN_MEMBER_MOBILE + " text, "
			+ COLUMN_MEMBER_EMAIL + " text, "
			+ COLUMN_MEMBER_BIRTHDAY + " text, "
			+ COLUMN_MEMBER_ID_NUMBER + " text, "
			+ COLUMN_MEMBER_ID_ISSUE_DATE + " text, "
			+ COLUMN_MEMBER_EXPIRE + " text, "
			+ " primary key (" + COLUMN_MEMBER_ID + "));";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_MEMBER);
		onCreate(db);
	}
}
