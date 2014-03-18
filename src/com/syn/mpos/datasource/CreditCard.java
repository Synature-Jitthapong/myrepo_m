package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.CreditCardType;

public class CreditCard extends MPOSDatabase{
	
	public CreditCard(SQLiteDatabase db) {
		super(db);
	}

	public List<CreditCardType> listAllCreditCardType(){
		List<CreditCardType> creditCardLst = 
				new ArrayList<CreditCardType>();
		Cursor cursor = mSqlite.query(CreditCardEntry.TABLE_CREDITCARD_TYPE, 
				new String[]{CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID, 
				CreditCardEntry.COLUMN_CREDITCARD_TYPE_NAME}, 
				null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				CreditCardType credit = new CreditCardType(
						cursor.getInt(cursor.getColumnIndex(CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID)),
						cursor.getString(cursor.getColumnIndex(CreditCardEntry.COLUMN_CREDITCARD_TYPE_NAME)));
				creditCardLst.add(credit);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return creditCardLst;
	}
	
	public void insertCreditCardType(List<CreditCardType> creditCardLst){
		mSqlite.delete(CreditCardEntry.TABLE_CREDITCARD_TYPE, null, null);
		for(CreditCardType credit : creditCardLst){
			ContentValues cv = new ContentValues();
			cv.put(CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID, credit.getCreditCardTypeId());
			cv.put(CreditCardEntry.COLUMN_CREDITCARD_TYPE_NAME, credit.getCreditCardTypeName());
			mSqlite.insert(CreditCardEntry.TABLE_CREDITCARD_TYPE, null, cv);
		}
	}

	public static abstract class CreditCardEntry{
		public static final String TABLE_CREDITCARD_TYPE = "CreditCardType";
		public static final String COLUMN_CREDITCARD_TYPE_ID = "creditcard_type_id";
		public static final String COLUMN_CREDITCARD_TYPE_NAME = "creditcard_type_name";
		public static final String COLUMN_CREDITCARD_NO = "creditcard_no";
		public static final String COLUMN_EXP_MONTH = "exp_month";
		public static final String COLUMN_EXP_YEAR = "exp_year";
	}
}
