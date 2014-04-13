package com.syn.mpos.database;

import android.database.sqlite.SQLiteDatabase;

public abstract class TableHelper {
	public abstract void onCreate(SQLiteDatabase db);
	public abstract void onUpgrade(SQLiteDatabase db);
}
