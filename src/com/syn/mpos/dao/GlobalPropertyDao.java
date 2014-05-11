package com.syn.mpos.dao;

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

public class GlobalPropertyDao extends MPOSDatabase{
	
	public static final String[] COLUMNS = {
		GlobalPropertyTable.COLUMN_CURRENCY_SYMBOL,
		GlobalPropertyTable.COLUMN_CURRENCY_CODE,
		GlobalPropertyTable.COLUMN_CURRENCY_NAME,
		GlobalPropertyTable.COLUMN_CURRENCY_FORMAT,
		GlobalPropertyTable.COLUMN_QTY_FORMAT,
		GlobalPropertyTable.COLUMN_DATE_FORMAT,
		GlobalPropertyTable.COLUMN_TIME_FORMAT
	};
	
//	private static GlobalPropertyDataSource sInstance;
//	
//	public static synchronized GlobalPropertyDataSource getInstance(Context context){
//		if(sInstance == null){
//			sInstance = new GlobalPropertyDataSource(context);
//		}
//		return sInstance;
//	}
	
	public GlobalPropertyDao(Context context) {
		super(context);
	}

	public String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		if(!getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getDateFormat());
		return dateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("dd/MM/yyyy HH:mm:ss");
		if(!getGlobalProperty().getDateFormat().equals("") && 
				!getGlobalProperty().getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getDateFormat() + " " +
					getGlobalProperty().getTimeFormat());
		return dateFormat.format(d);
	}
	
	public String timeFormat(Date d){
		SimpleDateFormat dateFormat = getSimpleDateFormat();
		dateFormat.applyPattern("HH:mm:ss");
		if(!getGlobalProperty().getTimeFormat().equals(""))
			dateFormat.applyPattern(getGlobalProperty().getTimeFormat());
		return dateFormat.format(d);
	}
	
	public String qtyFormat(double qty, String pattern){
		DecimalFormat decFormat = getDecimalFormat();
		decFormat.applyPattern(pattern);
		return decFormat.format(qty);
	}
	
	public String qtyFormat(double qty){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty().getQtyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty().getQtyFormat());
			return decFormat.format(qty);
		}else{
			NumberFormat numFormat = getNumberFormat();
			return numFormat.format(qty);
		}
	}
	
	public String currencyFormat(double currency, String pattern){
		DecimalFormat decFormat = getDecimalFormat();
		decFormat.applyPattern(pattern);
		return decFormat.format(currency);
	}
	
	public String currencyFormat(double currency){
		DecimalFormat decFormat = getDecimalFormat();
		if(!getGlobalProperty().getCurrencyFormat().equals("")){
			decFormat.applyPattern(getGlobalProperty().getCurrencyFormat());
			return decFormat.format(currency);
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		return dateFormat;
	}
	
	private NumberFormat getNumberFormat(){
		NumberFormat numFormat = NumberFormat.getInstance(Locale.getDefault());
		numFormat.setRoundingMode(RoundingMode.HALF_UP);
		return numFormat;
	}
	
	private DecimalFormat getDecimalFormat(){
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setRoundingMode(RoundingMode.HALF_UP);
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
