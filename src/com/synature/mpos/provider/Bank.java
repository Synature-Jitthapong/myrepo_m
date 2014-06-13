package com.synature.mpos.provider;

import java.util.ArrayList;
import java.util.List;

import com.synature.pos.BankName;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Bank extends MPOSDatabase{

	public Bank(Context context) {
		super(context);
	}
	
	public List<BankName> listAllBank(){
		List<BankName> bankLst = 
				new ArrayList<BankName>();
		Cursor cursor = getReadableDatabase().query(BankTable.TABLE_BANK, 
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
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(BankTable.TABLE_BANK, null, null);
			for(BankName bank : bankLst){
				ContentValues cv = new ContentValues();
				cv.put(BankTable.COLUMN_BANK_ID, bank.getBankNameId());
				cv.put(BankTable.COLUMN_BANK_NAME, bank.getBankName());
				getWritableDatabase().insertOrThrow(BankTable.TABLE_BANK, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class BankTable {

		public static final String TABLE_BANK = "BankName";
		public static final String COLUMN_BANK_ID = "bank_id";
		public static final String COLUMN_BANK_NAME = "bank_name";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_BANK + " ( " +
				COLUMN_BANK_ID + " INTEGER, " +
				COLUMN_BANK_NAME + " TEXT, " +
				"PRIMARY KEY (" + COLUMN_BANK_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
	}
}
