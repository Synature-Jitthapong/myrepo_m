package com.syn.mpos.database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
	
	public GlobalProperty(SQLiteDatabase db) {
		super(db);
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
		qtyFormat.setRoundingMode(RoundingMode.HALF_UP);
		return qtyFormat.format(qty);
	}
	
	public String qtyFormat(float qty){
		DecimalFormat qtyFormat = new DecimalFormat("#,##0.00");
		qtyFormat.setRoundingMode(RoundingMode.HALF_UP);
		if(!getGlobalProperty().getQtyFormat().equals(""))
			qtyFormat.applyPattern(getGlobalProperty().getQtyFormat());
		return qtyFormat.format(qty);
	}
	
	public String currencyFormat(float currency, String pattern){
		DecimalFormat currencyFormat = new DecimalFormat(pattern);
		currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
		return currencyFormat.format(currency);
	}
	
	public String currencyFormat(float currency){
		DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
		currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
		if(!getGlobalProperty().getCurrencyFormat().equals(""))
			currencyFormat.applyPattern(getGlobalProperty().getCurrencyFormat());
		return currencyFormat.format(currency);
	}
	
	public ShopData.GlobalProperty getGlobalProperty() {
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
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
		return gb;
	}

	public void insertProperty(List<ShopData.GlobalProperty> globalLst) throws SQLException{
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
	}
}
