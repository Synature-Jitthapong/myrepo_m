package com.syn.mpos.database;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	public static final String[] COLUMNS = {
		COL_CURRENCY_SYMBOL,
		COL_CURRENCY_CODE,
		COL_CURRENCY_NAME,
		COL_CURRENCY_FORMAT,
		COL_QTY_FORMAT,
		COL_DATE_FORMAT,
		COL_TIME_FORMAT
	};
	
	public GlobalProperty(Context c){
		super(c);
	}
	
	public String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat(pattern, Locale.getDefault());
		return dateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		if(!getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getDateFormat());
		return dateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateTimeFormat = 
				new SimpleDateFormat(pattern, Locale.getDefault());
		return dateTimeFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		if(!getGlobalProperty().getDateFormat().equals("") && 
				!getGlobalProperty().getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(getGlobalProperty().getDateFormat() + " " +
					getGlobalProperty().getTimeFormat());
		return dateTimeFormat.format(d);
	}
	
	public String timeFormat(Date d){
		SimpleDateFormat timeFormat = 
				new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		if(!getGlobalProperty().getTimeFormat().equals(""))
			timeFormat.applyPattern(getGlobalProperty().getTimeFormat());
		return timeFormat.format(d);
	}
	
	public String qtyFormat(float qty, String pattern){
		DecimalFormat qtyFormat = new DecimalFormat(pattern);
		return qtyFormat.format(qty);
	}
	
	public String qtyFormat(float qty){
		DecimalFormat qtyFormat = new DecimalFormat("#,##0.####");
		if(!getGlobalProperty().getQtyFormat().equals(""))
			qtyFormat.applyPattern(getGlobalProperty().getQtyFormat());
		return qtyFormat.format(qty);
	}
	
	public String currencyFormat(float currency, String pattern){
		DecimalFormat currencyFormat = new DecimalFormat(pattern);
		return currencyFormat.format(currency);
	}
	
	public String currencyFormat(float currency){
		DecimalFormat currencyFormat = new DecimalFormat("#,##0.####");
		if(!getGlobalProperty().getCurrencyFormat().equals(""))
			currencyFormat.applyPattern(getGlobalProperty().getCurrencyFormat());
		return currencyFormat.format(currency);
	}
	
	public ShopData.GlobalProperty getGlobalProperty() {
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
		open();
		Cursor cursor = mSqlite.query(TB_GLOBAL_PROPERTY, COLUMNS, 
				null, null, null, null, null);
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
		mSqlite.delete(TB_GLOBAL_PROPERTY, null, null);
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
		close();
	}
}
