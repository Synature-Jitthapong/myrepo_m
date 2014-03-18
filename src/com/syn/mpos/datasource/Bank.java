package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.BankName;

public class Bank extends MPOSDatabase{
	
	public Bank(SQLiteDatabase db) {
		super(db);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		Cursor cursor = mSqlite.query(BankEntry.TABLE_BANK, 
				new String[]{BankEntry.COLUMN_BANK_ID, BankEntry.COLUMN_BANK_NAME}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex(BankEntry.COLUMN_BANK_ID)),
						cursor.getString(cursor.getColumnIndex(BankEntry.COLUMN_BANK_NAME)));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return bankLst;
	}
	
	public void insertBank(List<BankName> bankLst){
		mSqlite.delete(BankEntry.TABLE_BANK, null, null);
		for(BankName bank : bankLst){
			ContentValues cv = new ContentValues();
			cv.put(BankEntry.COLUMN_BANK_ID, bank.getBankNameId());
			cv.put(BankEntry.COLUMN_BANK_NAME, bank.getBankName());
			mSqlite.insert(BankEntry.TABLE_BANK, null, cv);
		}
	}
	
	public static abstract class BankEntry{
		public static final String TABLE_BANK = "BankName";
		public static final String COLUMN_BANK_ID = "bank_id";
		public static final String COLUMN_BANK_NAME = "bank_name";
	}
}
