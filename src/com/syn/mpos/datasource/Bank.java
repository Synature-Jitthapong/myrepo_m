package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.syn.pos.BankName;

public class Bank extends MPOSDatabase{

	public static final String TABLE_BANK = "BankName";
	public static final String COLUMN_BANK_ID = "bank_id";
	public static final String COLUMN_BANK_NAME = "bank_name";
	
	public Bank(Context c) {
		super(c);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		Cursor cursor = mSqlite.query(TABLE_BANK, 
				new String[]{COLUMN_BANK_ID, COLUMN_BANK_NAME}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex(COLUMN_BANK_ID)),
						cursor.getString(cursor.getColumnIndex(COLUMN_BANK_NAME)));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return bankLst;
	}
	
	public void insertBank(List<BankName> bankLst){
		mSqlite.delete(TABLE_BANK, null, null);
		for(BankName bank : bankLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_BANK_ID, bank.getBankNameId());
			cv.put(COLUMN_BANK_NAME, bank.getBankName());
			mSqlite.insert(TABLE_BANK, null, cv);
		}
	}
}
