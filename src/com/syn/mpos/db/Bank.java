package com.syn.mpos.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import com.syn.mpos.model.BankName;

public class Bank {
	private MPOSSQLiteHelper dbHelper;
	
	public Bank(Context c){
		dbHelper = new MPOSSQLiteHelper(c);
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
