package com.syn.pos.mobile.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.CreditCardType;

public class CreditCard {
	private ISqliteHelper dbHelper;
	
	public CreditCard(Context c){
		dbHelper = new MPOSSqliteHelper(c);
	}
	
	public List<CreditCardType> listAllCreditCardType(){
		List<CreditCardType> creditCardLst = 
				new ArrayList<CreditCardType>();
		
		String strSql = "SELECT * FROM creditcard_type";
		
		dbHelper.open();
		
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				CreditCardType credit = new CreditCardType(
						cursor.getInt(cursor.getColumnIndex("creditcard_type_id")),
						cursor.getString(cursor.getColumnIndex("creditcard_type_name")));
				creditCardLst.add(credit);
			}while(cursor.moveToNext());
		}
		cursor.close();
		
		dbHelper.close();
		
		return creditCardLst;
	}
}
