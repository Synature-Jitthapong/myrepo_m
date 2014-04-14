package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.syn.pos.BankName;

public class Bank extends MPOSDatabase{

	public Bank(Context c) {
		super(c);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		Cursor cursor = mSqlite.query(BankTable.TABLE_NAME, 
				new String[]{BankTable.COLUMN_BANK_ID, BankTable.COLUMN_BANK_NAME}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				BankName bank = new BankName(
						cursor.getInt(cursor.getColumnIndex(BankTable.COLUMN_BANK_ID)),
						cursor.getString(cursor.getColumnIndex(BankTable.COLUMN_BANK_NAME)));
				bankLst.add(bank);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return bankLst;
	}
	
	public void insertBank(List<BankName> bankLst){
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(BankTable.TABLE_NAME, null, null);
			for(BankName bank : bankLst){
				ContentValues cv = new ContentValues();
				cv.put(BankTable.COLUMN_BANK_ID, bank.getBankNameId());
				cv.put(BankTable.COLUMN_BANK_NAME, bank.getBankName());
				mSqlite.insert(BankTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		}finally{
			mSqlite.endTransaction();
		}
	}
}
