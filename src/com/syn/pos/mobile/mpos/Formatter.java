package com.syn.pos.mobile.mpos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.syn.pos.mobile.model.ShopData;
import com.syn.pos.mobile.mpos.dao.Shop;

import android.content.Context;

public class Formatter {
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat dateTimeFormat;
	private SimpleDateFormat timeFormat;
	private DecimalFormat qtyFormat;
	private DecimalFormat currencyFormat;
	private DecimalFormat defaultFormat;
	
	public Formatter(Context c){
		dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
		qtyFormat = new DecimalFormat("#,###.####");
		currencyFormat = new DecimalFormat("#,###.####");
		defaultFormat = new DecimalFormat("#,###.0000");
		
		Shop s = new Shop(c);
		ShopData.GlobalProperty gb =
				s.getGlobalProperty();
		
		if(!gb.getDateFormat().equals(""))
			dateFormat.applyPattern(gb.getDateFormat());
		if(!gb.getTimeFormat().equals(""))
			timeFormat.applyPattern(gb.getTimeFormat());
		if(!gb.getQtyFormat().equals(""))
			qtyFormat.applyPattern(gb.getQtyFormat());
		if(!gb.getCurrencyFormat().equals(""))
			currencyFormat.applyPattern(gb.getCurrencyFormat());
		if(!gb.getDateFormat().equals("") && !gb.getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(gb.getDateFormat() + " " + gb.getTimeFormat());
	}
	
	public String dateFormat(Date d){
		return dateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d){
		return dateTimeFormat.format(d);
	}
	
	public String timeFormat(Date d){
		return timeFormat.format(d);
	}
	
	public String qtyFormat(double qty){
		return qtyFormat.format(qty);
	}
	
	public String currencyFormat(double currency){
		return currencyFormat.format(currency);
	}
	
	public String defaultFormat(double number){
		return defaultFormat.format(number);
	}
}
