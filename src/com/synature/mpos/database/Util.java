package com.synature.mpos.database;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
/**
 * 
 * @author j1tth4
 *
 */
public class Util {

	public static final int MINIMUM_YEAR = 1900;
	public static final int MINIMUM_MONTH = 0;
	public static final int MINIMUM_DAY = 1;
	
	public static Calendar getCalendar(){
		return Calendar.getInstance(Locale.US);
	}
	
	public static Calendar getMinimum(){
		return new GregorianCalendar(MINIMUM_YEAR, MINIMUM_MONTH, MINIMUM_DAY);
	}
	
	public static Calendar getDate(){
		Calendar c = getCalendar();
		return new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), 
				c.get(Calendar.DAY_OF_MONTH));
	}
	
	public static Calendar convertStringToCalendar(String dateTime){
		Calendar calendar = Calendar.getInstance(Locale.US);
		if(dateTime == null || dateTime.isEmpty()){
			dateTime = String.valueOf(Util.getMinimum().getTimeInMillis());
		}
		calendar.setTimeInMillis(Long.parseLong(dateTime));
		return calendar;
	}
	
	public static double calculateVatPrice(double totalPrice, double vatRate, int vatType){
		if(vatType == Products.VAT_TYPE_EXCLUDE)
			return totalPrice * (100 + vatRate) / 100;
		else
			return totalPrice;
	}
	
	public static double calculateVatAmount(double totalPrice, double vatRate, int vatType){
		if(vatType == Products.VAT_TYPE_INCLUDED)
			return totalPrice * vatRate / (100 + vatRate);
		else if(vatType == Products.VAT_TYPE_EXCLUDE)
			return totalPrice * vatRate / 100;
		else
			return 0;
	}
}
