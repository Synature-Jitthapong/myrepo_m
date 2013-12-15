package com.syn.mpos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.syn.mpos.database.Computer;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.transaction.Transaction;

import android.content.Context;

public class GlobalVar {
	private static GlobalVar instance = null;

	// sqlite db name 
	public static final String DB_NAME = "mpos.db";
	
	// db version
	public static final int DB_VERSION = 1;
	
	//image dir
	public static final String IMG_DIR = "mposimg";
	
	// global property
	public static GlobalProperty sGlobalProperty;
	
	// product
	public static Products sProduct;
	
	// application setting
	public static Setting sSetting;
	
	// shop data property
	public static Shop sShop;
	
	// computer data property
	public static Computer sComputer;
	
	// transaction 
	public static Transaction sTransaction;
	
	private GlobalVar(Context c){
		sGlobalProperty = new GlobalProperty(c);
		sProduct = new Products(c);
		sSetting = new Setting(c);
		sShop = new Shop(c);
		sComputer = new Computer(c);
		sTransaction = new Transaction(c);
	}
	
	public static synchronized GlobalVar newInstance(Context c){
		if(instance == null){
			instance = new GlobalVar(c);
		}
		return instance;
	}
	
	public String dateFormat(Date d, String pattern){
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return dateFormat.format(d);	
	}
	
	public String dateFormat(Date d){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM-dd", Locale.getDefault());
		
		if(!sGlobalProperty.getGlobalProperty().getDateFormat().equals(""))
			dateFormat.applyPattern(sGlobalProperty.getGlobalProperty().getDateFormat());
			
		return dateFormat.format(d);	
	}
	
	public String dateTimeFormat(Date d, String pattern){
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return dateTimeFormat.format(d);
	}
	
	public String dateTimeFormat(Date d){
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		
		if(!sGlobalProperty.getGlobalProperty().getDateFormat().equals("") && 
				!sGlobalProperty.getGlobalProperty().getTimeFormat().equals(""))
			dateTimeFormat.applyPattern(sGlobalProperty.getGlobalProperty().getDateFormat() + " " +
					sGlobalProperty.getGlobalProperty().getTimeFormat());
			
		return dateTimeFormat.format(d);
	}
	
	public String timeFormat(Date d){
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		
		if(!sGlobalProperty.getGlobalProperty().getTimeFormat().equals(""))
			timeFormat.applyPattern(sGlobalProperty.getGlobalProperty().getTimeFormat());
			
		return timeFormat.format(d);
	}
	
	public String qtyFormat(float qty, String pattern){
		DecimalFormat qtyFormat = new DecimalFormat(pattern);
		return qtyFormat.format(qty);
	}
	
	public String qtyFormat(float qty){
		DecimalFormat qtyFormat = new DecimalFormat("#,##0.####");
		
		if(!sGlobalProperty.getGlobalProperty().getQtyFormat().equals(""))
			qtyFormat.applyPattern(sGlobalProperty.getGlobalProperty().getQtyFormat());
		
		return qtyFormat.format(qty);
	}
	
	public String currencyFormat(float currency, String pattern){
		DecimalFormat currencyFormat = new DecimalFormat(pattern);
		return currencyFormat.format(currency);
	}
	
	public String currencyFormat(float currency){
		DecimalFormat currencyFormat = new DecimalFormat("#,##0.####");	
		
		if(!sGlobalProperty.getGlobalProperty().getCurrencyFormat().equals(""))
			currencyFormat.applyPattern(sGlobalProperty.getGlobalProperty().getCurrencyFormat());
			
		return currencyFormat.format(currency);
	}
}
