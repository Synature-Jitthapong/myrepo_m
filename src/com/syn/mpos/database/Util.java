package com.syn.mpos.database;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
	
	public static float calculateVat(float totalPrice, float vatRate){
		float vatAmount = totalPrice * toVatPercent(vatRate);
		return vatAmount;
	}
	
	public static float toVatPercent(float vatRate){
		return vatRate / 100;
	}
}
