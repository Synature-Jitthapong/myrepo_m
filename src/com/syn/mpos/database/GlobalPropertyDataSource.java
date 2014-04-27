package com.syn.mpos.database;

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

import com.syn.mpos.database.table.GlobalPropertyTable;
import com.syn.pos.ShopData;

public class GlobalPropertyDataSource{
	
	public static final String[] COLUMNS = {
		GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL,
		GlobalPropertyTable.COLUMN_CURRENCY_CODE,
		GlobalPropertyTable.COLUMN_CURRENCY_NAME,
		GlobalPropertyTable.COLUMN_CURRENCY_FORMAT,
		GlobalPropertyTable.COLUMN_QTY_FORMAT,
		GlobalPropertyTable.COLUMN_DATE_FORMAT,
		GlobalPropertyTable.COLUMN_TIME_FORMAT
	};

	public static String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);	
	}
	
	public static String dateFormat(SQLiteDatabase sqlite, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		if(!getGlobalProperty(sqlite).getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(sqlite).getDateFormat());
		return dateFormat.format(d);	
	}
	
	public static String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);
	}
	
	public static String dateTimeFormat(SQLiteDatabase sqlite, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("dd/MM/yyyy HH:mm:ss");
		if(!getGlobalProperty(sqlite).getDateFormat().equals("") && 
				!getGlobalProperty(sqlite).getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(sqlite).getDateFormat() + " " +
					getGlobalProperty(sqlite).getTimeFormat());
		return dateFormat.format(d);
	}
	
	public static String timeFormat(SQLiteDatabase sqlite, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("HH:mm:ss");
		if(!getGlobalProperty(sqlite).getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(sqlite).getTimeFormat());
		return dateFormat.format(d);
	}
	
	public static String qtyFormat(double qty, String pattern){
		DecimalFormat decFormat = getDecimalFormat();
		decFormat.applyPattern(pattern);
		return decFormat.format(qty);
	}
	
	public static String qtyFormat(SQLiteDatabase sqlite, double qty){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty(sqlite).getQtyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty(sqlite).getQtyFormat());
			return decFormat.format(qty);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(qty);
		}
	}
	
	public static String currencyFormat(double currency, String pattern){
		DecimalFormat decFormat = getDecimalFormat();
		decFormat.applyPattern(pattern);
		return decFormat.format(currency);
	}
	
	public static String currencyFormat(SQLiteDatabase sqlite, double currency){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty(sqlite).getCurrencyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty(sqlite).getCurrencyFormat());
			return decFormat.format(currency);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(currency);
		}
	}
	
	public static ShopData.GlobalProperty getGlobalProperty(SQLiteDatabase sqlite) {
		ShopData.GlobalProperty gb = new ShopData.GlobalProperty();
		Cursor cursor = sqlite.query(GlobalPropertyTable.TABLE_NAME, COLUMNS, 
				null, null, null, null, null);
		if (cursor.moveToFirst()) {
			gb.setCurrencyCode(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_CURRENCY_CODE)));
			gb.setCurrencySymbol(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL)));
			gb.setCurrencyName(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_CURRENCY_NAME)));
			gb.setCurrencyFormat(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_CURRENCY_FORMAT)));
			gb.setDateFormat(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_DATE_FORMAT)));
			gb.setTimeFormat(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_TIME_FORMAT)));
			gb.setQtyFormat(cursor.getString(cursor
					.getColumnIndex(GlobalPropertyTable.COLUMN_QTY_FORMAT)));
			cursor.moveToNext();
		}
		return gb;
	}

	public static void insertProperty(SQLiteDatabase sqlite, List<ShopData.GlobalProperty> globalLst) throws SQLException{
		sqlite.beginTransaction();
		try {
			sqlite.delete(GlobalPropertyTable.TABLE_NAME, null, null);
			for (ShopData.GlobalProperty global : globalLst) {
				ContentValues cv = new ContentValues();
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL, global.getCurrencySymbol());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_CODE, global.getCurrencyCode());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_NAME, global.getCurrencyName());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_FORMAT, global.getCurrencyFormat());
				cv.put(GlobalPropertyTable.COLUMN_DATE_FORMAT, global.getDateFormat());
				cv.put(GlobalPropertyTable.COLUMN_TIME_FORMAT, global.getTimeFormat());
				cv.put(GlobalPropertyTable.COLUMN_QTY_FORMAT, global.getQtyFormat());
				sqlite.insertOrThrow(GlobalPropertyTable.TABLE_NAME, null, cv);
			}
			sqlite.setTransactionSuccessful();
		} finally {
			sqlite.endTransaction();
		}
	}
	
	private static SimpleDateFormat getSimpleDateFormat(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		return dateFormat;
	}
	
	private static NumberFormat getNumberFormat(){
		NumberFormat numFormat = NumberFormat.getInstance(Locale.getDefault());
		numFormat.setRoundingMode(RoundingMode.HALF_UP);
		return numFormat;
	}
	
	private static DecimalFormat getDecimalFormat(){
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setRoundingMode(RoundingMode.HALF_UP);
		return decFormat;
	}
}
