package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.syn.mpos.database.table.CreditCardTable;
import com.syn.pos.CreditCardType;

public class CreditCardDataSource extends MPOSDatabase{
	
	public CreditCardDataSource(SQLiteDatabase db) {
		super(db);
	}

	public List<CreditCardType> listAllCreditCardType(){
		List<CreditCardType> creditCardLst = 
				new ArrayList<CreditCardType>();
		Cursor cursor = mSqlite.query(CreditCardTable.TABLE_NAME, 
				new String[]{CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, 
				CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME}, 
				null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				CreditCardType credit = new CreditCardType(
						cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID)),
						cursor.getString(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME)));
				creditCardLst.add(credit);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return creditCardLst;
	}
	
	public void insertCreditCardType(List<CreditCardType> creditCardLst){
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(CreditCardTable.TABLE_NAME, null, null);
			for(CreditCardType credit : creditCardLst){
				ContentValues cv = new ContentValues();
				cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, credit.getCreditCardTypeId());
				cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME, credit.getCreditCardTypeName());
				mSqlite.insertOrThrow(CreditCardTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
}
