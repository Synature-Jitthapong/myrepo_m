package com.syn.mpos.database;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.syn.pos.ShopData.ShopProperty;

import android.content.Context;

/**
 * 
 * @author j1tth4
 *
 */
public abstract class Util {
	public static float mVatRate = 7f;
	
	public Util(Context c){
		Shop s = new Shop(c);
		ShopProperty sp = s.getShopProperty();
		mVatRate = sp.getCompanyVat();
	}
	
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
	
	protected float calculateVat(float totalPrice){
		float vatAmount = totalPrice * toVatPercent();
		return vatAmount;
	}
	
	protected float toVatPercent(){
		return mVatRate / 100;
	}
}
