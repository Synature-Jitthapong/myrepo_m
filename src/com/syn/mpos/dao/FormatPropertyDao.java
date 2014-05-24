package com.syn.mpos.dao;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class FormatPropertyDao extends MPOSDatabase{
	
	public static final String[] COLUMNS = {
		GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL,
		GlobalPropertyTable.COLUMN_CURRENCY_CODE,
		GlobalPropertyTable.COLUMN_CURRENCY_NAME,
		GlobalPropertyTable.COLUMN_CURRENCY_FORMAT,
		GlobalPropertyTable.COLUMN_QTY_FORMAT,
		GlobalPropertyTable.COLUMN_DATE_FORMAT,
		GlobalPropertyTable.COLUMN_TIME_FORMAT
	};
	
	public FormatPropertyDao(Context context) {
		super(context);
	}

	/**
	 * @param d
	 * @param pattern
	 * @return String format date
	 */
	public String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);	
	}
	
	/**
	 * @param d
	 * @return String format date
	 */
	public String dateFormat(Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		if(!getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getDateFormat());
		return dateFormat.format(d);	
	}
	
	/**
	 * @param date
	 * @return String format date
	 */
	public String dateFormat(String date){
		Calendar calendar = Util.convertStringToCalendar(date);
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		if(!getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getDateFormat());
		return dateFormat.format(calendar.getTime());
	}
	
	/**
	 * @param dateTime
	 * @return String format date time
	 */
	public String dateTimeFormat(String dateTime){
		Calendar calendar = Util.convertStringToCalendar(dateTime);
		SimpleDateFormat dateTimeFormat = getSimpleDateTimeFormat();
		if(!getGlobalProperty().getDateFormat().equals("") && 
				!getGlobalProperty().getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(getGlobalProperty().getDateFormat() + " " +
					getGlobalProperty().getTimeFormat());
		return dateTimeFormat.format(calendar.getTime());
	}
	
	/**
	 * @param time
	 * @return String time format
	 */
	public String timeFormat(String time){
		Calendar calendar = Util.convertStringToCalendar(time);
		SimpleDateFormat timeFormat = getSimpleTimeFormat();
		if(!getGlobalProperty().getTimeFormat().equals(""))
			timeFormat.applyPattern(getGlobalProperty().getTimeFormat());
		return timeFormat.format(calendar.getTime());
	}
	
	/**
	 * @param dateTime
	 * @param pattern
	 * @return String format date time
	 */
	public String dateTimeFormat(String dateTime, String pattern){
		Calendar calendar = Util.convertStringToCalendar(dateTime);
		SimpleDateFormat dateTimeFormat = getSimpleDateTimeFormat();
		dateTimeFormat.applyPattern(pattern);
		return dateTimeFormat.format(calendar.getTime());
	}
	
	/**
	 * @param d
	 * @param pattern
	 * @return String format date time
	 */
	public String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateTimeFormat = getSimpleDateTimeFormat();
		dateTimeFormat.applyPattern(pattern);
		return dateTimeFormat.format(d);
	}
	
	/**
	 * @param d
	 * @return String format date time
	 */
	public String dateTimeFormat(Date d){
		SimpleDateFormat dateTimeFormat = getSimpleDateTimeFormat();
		if(!getGlobalProperty().getDateFormat().equals("") && 
				!getGlobalProperty().getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(getGlobalProperty().getDateFormat() + " " +
					getGlobalProperty().getTimeFormat());
		return dateTimeFormat.format(d);
	}
	
	/**
	 * @param d
	 * @return String format time
	 */
	public String timeFormat(Date d){
		SimpleDateFormat timeFormat = getSimpleTimeFormat();
		if(!getGlobalProperty().getTimeFormat().equals(""))
			timeFormat.applyPattern(getGlobalProperty().getTimeFormat());
		return timeFormat.format(d);
	}
	
	/**
	 * @param qty
	 * @param pattern
	 * @return qty format
	 */
	public String qtyFormat(double qty, String pattern){
		DecimalFormat qtyFormat = getDecimalFormat();
		qtyFormat.applyPattern(pattern);
		return qtyFormat.format(qty);
	}
	
	/**
	 * @param qty
	 * @return qty format
	 */
	public String qtyFormat(double qty){
		DecimalFormat qtyFormat = getDecimalFormat();
		if(!getGlobalProperty().getQtyFormat().equals("")){
			qtyFormat.applyPattern(getGlobalProperty().getQtyFormat());
			return qtyFormat.format(qty);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(qty);
		}
	}
	
	/**
	 * @param currency
	 * @param pattern
	 * @return currency format
	 */
	public String currencyFormat(double currency, String pattern){
		DecimalFormat currencyFormat = getDecimalFormat();
		currencyFormat.applyPattern(pattern);
		return currencyFormat.format(currency);
	}
	
	/**
	 * @param currency
	 * @return currency format
	 */
	public String currencyFormat(double currency){
		DecimalFormat currencyFormat = getDecimalFormat();
		if(!getGlobalProperty().getCurrencyFormat().equals("")){
			currencyFormat.applyPattern(getGlobalProperty().getCurrencyFormat());
			return currencyFormat.format(currency);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(currency);
		}
	}
	
	public ShopData.GlobalProperty getGlobalProperty() {
		ShopData.GlobalProperty gb = new ShopData.GlobalProperty();
		Cursor cursor = getReadableDatabase().query(GlobalPropertyTable.TABLE_GLOBAL_PROPERTY, COLUMNS, 
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

	public void insertProperty(List<ShopData.GlobalProperty> globalLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(GlobalPropertyTable.TABLE_GLOBAL_PROPERTY, null, null);
			for (ShopData.GlobalProperty global : globalLst) {
				ContentValues cv = new ContentValues();
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL, global.getCurrencySymbol());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_CODE, global.getCurrencyCode());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_NAME, global.getCurrencyName());
				cv.put(GlobalPropertyTable.COLUMN_CURRENCY_FORMAT, global.getCurrencyFormat());
				cv.put(GlobalPropertyTable.COLUMN_DATE_FORMAT, global.getDateFormat());
				cv.put(GlobalPropertyTable.COLUMN_TIME_FORMAT, global.getTimeFormat());
				cv.put(GlobalPropertyTable.COLUMN_QTY_FORMAT, global.getQtyFormat());
				getWritableDatabase().insertOrThrow(GlobalPropertyTable.TABLE_GLOBAL_PROPERTY, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	private SimpleDateFormat getSimpleDateFormat(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		return dateFormat;
	}
	
	private SimpleDateFormat getSimpleDateTimeFormat(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		return dateFormat;
	}
	
	private SimpleDateFormat getSimpleTimeFormat(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
		return dateFormat;
	}
	
	private NumberFormat getNumberFormat(){
		NumberFormat numFormat = NumberFormat.getInstance(Locale.US);
		return numFormat;
	}
	
	private DecimalFormat getDecimalFormat(){
		DecimalFormat decFormat = new DecimalFormat();
		return decFormat;
	}
	
	public static class GlobalPropertyTable{

		public static final String TABLE_GLOBAL_PROPERTY = "GlobalProperty";
		public static final String COLUMN_CURRENCY_SYMBOL = "currency_symbol";
		public static final String COLUMN_CURRENCY_CODE = "currency_code";
		public static final String COLUMN_CURRENCY_NAME = "currency_name";
		public static final String COLUMN_CURRENCY_FORMAT = "currency_format";
		public static final String COLUMN_QTY_FORMAT = "qty_format";
		public static final String COLUMN_DATE_FORMAT = "date_format";
		public static final String COLUMN_TIME_FORMAT = "time_format";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_GLOBAL_PROPERTY + " ( " +
				COLUMN_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
				COLUMN_CURRENCY_CODE + " TEXT DEFAULT 'USD', " +
				COLUMN_CURRENCY_NAME + " TEXT, " +
				COLUMN_CURRENCY_FORMAT + " TEXT DEFAULT '#,##0.00', " +
				COLUMN_QTY_FORMAT + " TEXT DEFAULT '#,##0', " +
				COLUMN_DATE_FORMAT + " TEXT DEFAULT 'd MMMM yyyy', " +
				COLUMN_TIME_FORMAT + " TEXT DEFAULT 'HH:mm:ss' );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
	}
}
