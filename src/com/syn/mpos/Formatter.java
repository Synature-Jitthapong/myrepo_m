package com.syn.mpos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.syn.mpos.database.GlobalProperty;
import com.syn.pos.ShopData;

import android.content.Context;

public class Formatter {
	private SimpleDateFormat mDateFormat;
	private SimpleDateFormat mDateTimeFormat;
	private SimpleDateFormat mTimeFormat;
	private DecimalFormat mQtyFormat;
	private DecimalFormat mCurrencyFormat;
	
	public Formatter(Context c){
		mDateFormat = new SimpleDateFormat("yyyy/MM-dd", Locale.getDefault());
		mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		mQtyFormat = new DecimalFormat("#,##0.####");
		mCurrencyFormat = new DecimalFormat("#,##0.####");
		
		GlobalProperty global = new GlobalProperty(c);
		ShopData.GlobalProperty gb =
				global.getGlobalProperty();
		
		if(!gb.getDateFormat().equals(""))
			mDateFormat.applyPattern(gb.getDateFormat());
		if(!gb.getTimeFormat().equals(""))
			mTimeFormat.applyPattern(gb.getTimeFormat());
		if(!gb.getQtyFormat().equals(""))
			mQtyFormat.applyPattern(gb.getQtyFormat());
		if(!gb.getCurrencyFormat().equals(""))
			mCurrencyFormat.applyPattern(gb.getCurrencyFormat());
		if(!gb.getDateFormat().equals("") && !gb.getTimeFormat().equals(""))
			mDateTimeFormat.applyPattern(gb.getDateFormat() + " " + gb.getTimeFormat());
	}
	
	public String dateFormat(Date d, String pattern){
		mDateFormat.applyPattern(pattern);
		return mDateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		return mDateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		mDateTimeFormat.applyPattern(pattern);
		return mDateTimeFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		return mDateTimeFormat.format(d);
	}
	
	public String timeFormat(Date d){
		return mTimeFormat.format(d);
	}
	
	public String qtyFormat(float qty, String pattern){
		mQtyFormat.applyPattern(pattern);
		return mQtyFormat.format(qty);
	}
	
	public String qtyFormat(float qty){
		return mQtyFormat.format(qty);
	}
	
	public String currencyFormat(float currency, String pattern){
		mCurrencyFormat.applyLocalizedPattern(pattern);
		return mCurrencyFormat.format(currency);
	}
	
	public String currencyFormat(float currency){
		return mCurrencyFormat.format(currency);
	}
}
