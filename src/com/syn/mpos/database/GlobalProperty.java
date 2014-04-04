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

import com.syn.pos.ShopData;

public class GlobalProperty extends MPOSDatabase{

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
	
	private NumberFormat mNumFormat;
	private DecimalFormat mDecFormat;
	private SimpleDateFormat mDateFormat;
	
	public GlobalProperty(SQLiteDatabase db) {
		super(db);
		mNumFormat = NumberFormat.getInstance(Locale.getDefault());
		mNumFormat.setRoundingMode(RoundingMode.HALF_UP);
		mDecFormat = new DecimalFormat();
		mDecFormat.setRoundingMode(RoundingMode.HALF_UP);
		mDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	}

	public String dateFormat(Date d, String pattern){
		mDateFormat.applyPattern(pattern);
		return mDateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		if(!getGlobalProperty().getDateFormat().equals(""))
			mDateFormat.applyPattern(getGlobalProperty().getDateFormat());
		return mDateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		mDateFormat.applyPattern(pattern);
		return mDateFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		mDateFormat.applyPattern("dd/MM/yyyy HH:mm:ss");
		if(!getGlobalProperty().getDateFormat().equals("") && 
				!getGlobalProperty().getTimeFormat().equals(""))
			mDateFormat.applyPattern(getGlobalProperty().getDateFormat() + " " +
					getGlobalProperty().getTimeFormat());
		return mDateFormat.format(d);
	}
	
	public String timeFormat(Date d){
		mDateFormat.applyPattern("HH:mm:ss");
		if(!getGlobalProperty().getTimeFormat().equals(""))
			mDateFormat.applyPattern(getGlobalProperty().getTimeFormat());
		return mDateFormat.format(d);
	}
	
	public String qtyFormat(double qty, String pattern){
		mDecFormat.applyPattern(pattern);
		return mDecFormat.format(qty);
	}
	
	public String qtyFormat(double qty){
		if(!getGlobalProperty().getQtyFormat().equals("")){
			mDecFormat.applyPattern(getGlobalProperty().getQtyFormat());
			return mDecFormat.format(qty);
		}else{
			return mNumFormat.format(qty);
		}
	}
	
	public String currencyFormat(double currency, String pattern){
		mDecFormat.applyPattern(pattern);
		return mDecFormat.format(currency);
	}
	
	public String currencyFormat(double currency){
		if(!getGlobalProperty().getCurrencyFormat().equals("")){
			mDecFormat.applyPattern(getGlobalProperty().getCurrencyFormat());
			return mDecFormat.format(currency);
		}else{
			return mNumFormat.format(currency);
		}
	}
	
	public ShopData.GlobalProperty getGlobalProperty() {
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
		Cursor cursor = mSqlite.query(TABLE_GLOBAL_PROPERTY, COLUMNS, 
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

	public void insertProperty(List<ShopData.GlobalProperty> globalLst) throws SQLException{
		mSqlite.delete(TABLE_GLOBAL_PROPERTY, null, null);
		for (ShopData.GlobalProperty global : globalLst) {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_CURRENCY_SYMBOL, global.getCurrencySymbol());
			cv.put(COLUMN_CURRENCY_CODE, global.getCurrencyCode());
			cv.put(COLUMN_CURRENCY_NAME, global.getCurrencyName());
			cv.put(COLUMN_CURRENCY_FORMAT, global.getCurrencyFormat());
			cv.put(COLUMN_DATE_FORMAT, global.getDateFormat());
			cv.put(COLUMN_TIME_FORMAT, global.getTimeFormat());
			cv.put(COLUMN_QTY_FORMAT, global.getQtyFormat());
			mSqlite.insertOrThrow(TABLE_GLOBAL_PROPERTY, null, cv);
		}
	}
}
