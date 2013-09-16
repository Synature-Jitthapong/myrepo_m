package com.syn.mpos.database;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 
 * @author j1tth4
 *
 */
public abstract class Util {
	
	protected Calendar getDate(){
		Calendar calendar = Calendar.getInstance();
		return calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), 
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	protected Calendar getDateTime(){
		Calendar calendar = Calendar.getInstance();
		return calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), 
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), 
				calendar.get(Calendar.SECOND));
	}
	
	protected float calculateVat(float totalPrice, float vatRate){
		float vatAmount = totalPrice * toVatPercent(vatRate);
		return vatAmount;
	}
	
	protected float toVatPercent(float vat){
		return vat / 100;
	}
}
