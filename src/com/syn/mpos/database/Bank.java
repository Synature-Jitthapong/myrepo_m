package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.syn.pos.BankName;

public class Bank extends MPOSSQLiteHelper{
	public static final String TB_BANK = "BankName";
	public static final String COL_BANK_ID = "BankId";
	public static final String COL_BANK_NAME = "BankName";
	
	public Bank(Context c){
		super(c);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		
		open();
		Cursor cursor = mSqlite.query(TB_BANK, 
				new String[]{COL_BANK_ID, COL_BANK_NAME}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex(COL_BANK_ID)),
						cursor.getString(cursor.getColumnIndex(COL_BANK_NAME)));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return bankLst;
	}
}
