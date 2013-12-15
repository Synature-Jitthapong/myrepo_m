package com.syn.mpos.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.syn.pos.ShopData;

public class GlobalProperty extends MPOSDatabase{
	public static final String TB_GLOBAL_PROPERTY = "GlobalProperty";
	public static final String COL_CURRENCY_SYMBOL = "CurrencySymbol";
	public static final String COL_CURRENCY_CODE = "CurrencyCode";
	public static final String COL_CURRENCY_NAME = "CurrencyName";
	public static final String COL_CURRENCY_FORMAT = "CurrencyFormat";
	public static final String COL_QTY_FORMAT = "QtyFormat";
	public static final String COL_DATE_FORMAT = "DateFormat";
	public static final String COL_TIME_FORMAT = "TimeFormat";
	
	public GlobalProperty(Context c){
		super(c);
	}
	
	public ShopData.GlobalProperty getGlobalProperty() {
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_GLOBAL_PROPERTY, null);
		if (cursor.moveToFirst()) {
			gb.setCurrencyCode(cursor.getString(cursor
					.getColumnIndex(COL_CURRENCY_CODE)));
			gb.setCurrencySymbol(cursor.getString(cursor
					.getColumnIndex(COL_CURRENCY_SYMBOL)));
			gb.setCurrencyName(cursor.getString(cursor
					.getColumnIndex(COL_CURRENCY_NAME)));
			gb.setCurrencyFormat(cursor.getString(cursor
					.getColumnIndex(COL_CURRENCY_FORMAT)));
			gb.setDateFormat(cursor.getString(cursor
					.getColumnIndex(COL_DATE_FORMAT)));
			gb.setTimeFormat(cursor.getString(cursor
					.getColumnIndex(COL_TIME_FORMAT)));
			gb.setQtyFormat(cursor.getString(cursor
					.getColumnIndex(COL_QTY_FORMAT)));
			cursor.moveToNext();
		}
		close();
		return gb;
	}

	public void insertProperty(List<ShopData.GlobalProperty> globalLst) throws SQLException{
		open();
		mSqlite.execSQL("DELETE FROM " + TB_GLOBAL_PROPERTY);
		for (ShopData.GlobalProperty global : globalLst) {
			ContentValues cv = new ContentValues();
			cv.put(COL_CURRENCY_SYMBOL, global.getCurrencySymbol());
			cv.put(COL_CURRENCY_CODE, global.getCurrencyCode());
			cv.put(COL_CURRENCY_NAME, global.getCurrencyName());
			cv.put(COL_CURRENCY_FORMAT, global.getCurrencyFormat());
			cv.put(COL_DATE_FORMAT, global.getDateFormat());
			cv.put(COL_TIME_FORMAT, global.getTimeFormat());
			cv.put(COL_QTY_FORMAT, global.getQtyFormat());
			mSqlite.insertOrThrow(TB_GLOBAL_PROPERTY, null, cv);
		}
		mSqlite.close();
	}
}
