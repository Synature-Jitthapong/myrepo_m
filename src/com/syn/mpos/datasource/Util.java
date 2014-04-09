package com.syn.mpos.datasource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
/**
 * 
 * @author j1tth4
 *
 */
public class Util {
	
	public static Calendar getCalendar(){
		return Calendar.getInstance(Locale.getDefault());
	}
	
	public static Calendar getDate(){
		Calendar c = getCalendar();
		return new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), 
				c.get(Calendar.DAY_OF_MONTH));
	}
	
	public static Calendar getDateTime(){
		Calendar c = getCalendar();
		return new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), 
				c.get(Calendar.DAY_OF_MONTH), 
				c.get(Calendar.HOUR_OF_DAY), 
				c.get(Calendar.MINUTE), 
				c.get(Calendar.SECOND));
	}
	
	public static String dateTimeFormat(String time, String pattern){
		String format = "";
		Calendar calendar;
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		try {
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.parseLong(time));
			format = dateTimeFormat.format(calendar.getTime());
		} catch (NumberFormatException e) {
			calendar = Calendar.getInstance();
			format = dateTimeFormat.format(calendar.getTime());
			e.printStackTrace();
		}
		return format;
	}
	
	public static double calculateVatAmount(double totalPrice, double vatRate, int vatType){
		if(vatType == Products.VAT_TYPE_INCLUDED)
			return totalPrice * vatRate / (100 + vatRate);
		else
			return totalPrice * vatRate / 100;
	}
}
