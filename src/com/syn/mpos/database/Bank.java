package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.syn.pos.BankName;

public class Bank {
	public static final String TB_NAME = "BankName";
	public static final String COL_BANK_ID = "BankID";
	public static final String COL_BANK_NAME = "BankName";
	
	private MPOSSQLiteHelper dbHelper;
	
	public Bank(Context c){
		dbHelper = new MPOSSQLiteHelper(c);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		
		String strSql = "SELECT * FROM " + TB_NAME;
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex(COL_BANK_ID)),
						cursor.getString(cursor.getColumnIndex(COL_BANK_NAME)));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();
		return bankLst;
	}
}
