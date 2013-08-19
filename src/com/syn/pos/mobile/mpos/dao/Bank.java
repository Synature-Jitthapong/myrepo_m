package com.syn.pos.mobile.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.BankName;

public class Bank {
	private ISqliteHelper dbHelper;
	
	public Bank(Context c){
		dbHelper = new MPOSSqliteHelper(c);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		
		String strSql = "SELECT * FROM bank_name";
		
		dbHelper.open();

		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex("bank_name_id")),
						cursor.getString(cursor.getColumnIndex("bank_name")));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		
		dbHelper.close();
		return bankLst;
	}
}
