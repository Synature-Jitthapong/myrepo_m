package com.syn.mpos.database;

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
	
	public static Calendar getDate(){
		Calendar calendar = Calendar.getInstance();
		return calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), 
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	public static Calendar getDateTime(){
		Calendar calendar = Calendar.getInstance();
		return calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), 
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), 
				calendar.get(Calendar.SECOND));
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
	
	public static double calculateVat(double totalPrice, double vatRate){
		double vatAmount = totalPrice * toVatPercent(vatRate);
		return vatAmount;
	}
	
	public static double toVatPercent(double vatRate){
		return vatRate / 100;
	}
}
