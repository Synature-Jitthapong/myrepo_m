package com.syn.pos.mobile.mpos;

import java.text.SimpleDateFormat;

import android.content.Context;

import com.syn.pos.mobile.model.ShopData;
import com.syn.pos.mobile.mpos.dao.Shop;

public class MPOSVar {
	public static int shopId;
	public static int computerId;
	public static SimpleDateFormat dateFormat;
	public static SimpleDateFormat dateTimeFormat;
	public static SimpleDateFormat timeFormat;
	
	public MPOSVar(Context c){
		Shop s = new Shop(c);
		
		ShopData.GlobalProperty gb = s.getGlobalProperty();
		
		String datePattern = "yyyy-MM-dd";
		String timePattern = "HH:mm:ss";
		if(!gb.getDateFormat().equals(""))
			datePattern = gb.getDateFormat();
		if(!gb.getTimeFormat().equals(""))
			timePattern = gb.getTimeFormat();
		
		dateFormat = new SimpleDateFormat(datePattern);
		dateTimeFormat = new SimpleDateFormat(datePattern + " " + timePattern);
		timeFormat = new SimpleDateFormat(timePattern);
	}
}
