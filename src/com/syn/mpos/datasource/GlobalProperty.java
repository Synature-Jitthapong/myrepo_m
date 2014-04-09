package com.syn.mpos.datasource;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class GlobalProperty{

	public static final String TABLE_GLOBAL_PROPERTY = "GlobalProperty";
	public static final String COLUMN_CURRENCY_SYMBOL = "currency_symbol";
	public static final String COLUMN_CURRENCY_CODE = "currency_code";
	public static final String COLUMN_CURRENCY_NAME = "currency_name";
	public static final String COLUMN_CURRENCY_FORMAT = "currency_format";
	public static final String COLUMN_QTY_FORMAT = "qty_format";
	public static final String COLUMN_DATE_FORMAT = "date_format";
	public static final String COLUMN_TIME_FORMAT = "time_format";
	
	public static final String[] COLUMNS = {
		COLUMN_CURRENCY_SYMBOL,
		COLUMN_CURRENCY_CODE,
		COLUMN_CURRENCY_NAME,
		COLUMN_CURRENCY_FORMAT,
		COLUMN_QTY_FORMAT,
		COLUMN_DATE_FORMAT,
		COLUMN_TIME_FORMAT
	};

	public static String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);	
	}
	
	public static String dateFormat(Context c, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		if(!getGlobalProperty(c).getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(c).getDateFormat());
		return dateFormat.format(d);	
	}
	
	public static String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);
	}
	
	public static String dateTimeFormat(Context c, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("dd/MM/yyyy HH:mm:ss");
		if(!getGlobalProperty(c).getDateFormat().equals("") && 
				!getGlobalProperty(c).getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(c).getDateFormat() + " " +
					getGlobalProperty(c).getTimeFormat());
		return dateFormat.format(d);
	}
	
	public static String timeFormat(Context c, Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("HH:mm:ss");
		if(!getGlobalProperty(c).getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty(c).getTimeFormat());
		return dateFormat.format(d);
	}
	
	public static String qtyFormat(double qty, String pattern){
		DecimalFormat decFormat = getDecimalFormat();
		decFormat.applyPattern(pattern);
		return decFormat.format(qty);
	}
	
	public static String qtyFormat(Context c, double qty){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty(c).getQtyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty(c).getQtyFormat());
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
	
	public static String currencyFormat(Context c, double currency){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty(c).getCurrencyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty(c).getCurrencyFormat());
			return decFormat.format(currency);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(currency);
		}
	}
	
	public static ShopData.GlobalProperty getGlobalProperty(Context c) {
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
		Cursor cursor = getDatabase(c).query(TABLE_GLOBAL_PROPERTY, COLUMNS, 
				null, null, null, null, null);
		if (cursor.moveToFirst()) {
			gb.setCurrencyCode(cursor.getString(cursor
					.getColumnIndex(COLUMN_CURRENCY_CODE)));
			gb.setCurrencySymbol(cursor.getString(cursor
					.getColumnIndex(COLUMN_CURRENCY_SYMBOL)));
			gb.setCurrencyName(cursor.getString(cursor
					.getColumnIndex(COLUMN_CURRENCY_NAME)));
			gb.setCurrencyFormat(cursor.getString(cursor
					.getColumnIndex(COLUMN_CURRENCY_FORMAT)));
			gb.setDateFormat(cursor.getString(cursor
					.getColumnIndex(COLUMN_DATE_FORMAT)));
			gb.setTimeFormat(cursor.getString(cursor
					.getColumnIndex(COLUMN_TIME_FORMAT)));
			gb.setQtyFormat(cursor.getString(cursor
					.getColumnIndex(COLUMN_QTY_FORMAT)));
			cursor.moveToNext();
		}
		return gb;
	}

	public static void insertProperty(Context c, List<ShopData.GlobalProperty> globalLst) throws SQLException{
		getDatabase(c).delete(TABLE_GLOBAL_PROPERTY, null, null);
		for (ShopData.GlobalProperty global : globalLst) {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_CURRENCY_SYMBOL, global.getCurrencySymbol());
			cv.put(COLUMN_CURRENCY_CODE, global.getCurrencyCode());
			cv.put(COLUMN_CURRENCY_NAME, global.getCurrencyName());
			cv.put(COLUMN_CURRENCY_FORMAT, global.getCurrencyFormat());
			cv.put(COLUMN_DATE_FORMAT, global.getDateFormat());
			cv.put(COLUMN_TIME_FORMAT, global.getTimeFormat());
			cv.put(COLUMN_QTY_FORMAT, global.getQtyFormat());
			getDatabase(c).insertOrThrow(TABLE_GLOBAL_PROPERTY, null, cv);
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
	
	private static SQLiteDatabase getDatabase(Context c){
		MPOSSQLiteHelper dbHelper = new MPOSSQLiteHelper(c);
		return dbHelper.getWritableDatabase();
	}
}
