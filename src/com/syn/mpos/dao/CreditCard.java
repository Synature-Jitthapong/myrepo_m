package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import com.synature.pos.CreditCardType;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CreditCard extends MPOSDatabase{
	
	public CreditCard(Context context) {
		super(context);
	}

	public String getCreditCardType(int typeId){
		String cardType = "";
		Cursor cursor = getReadableDatabase().query(CreditCardTable.TABLE_CREDIT_CARD_TYPE, 
				new String[]{ 
					CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME
				}, 
				CreditCardTable.COLUMN_CREDITCARD_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(typeId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			cardType = cursor.getString(
					cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME));
		}
		cursor.close();
		return cardType;
	}
	
	public List<CreditCardType> listAllCreditCardType(){
		List<CreditCardType> creditCardLst = 
				new ArrayList<CreditCardType>();
		Cursor cursor = getReadableDatabase().query(CreditCardTable.TABLE_CREDIT_CARD_TYPE, 
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
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(CreditCardTable.TABLE_CREDIT_CARD_TYPE, null, null);
			for(CreditCardType credit : creditCardLst){
				ContentValues cv = new ContentValues();
				cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, credit.getCreditCardTypeId());
				cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_NAME, credit.getCreditCardTypeName());
				getWritableDatabase().insertOrThrow(CreditCardTable.TABLE_CREDIT_CARD_TYPE, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class CreditCardTable{

		public static final String TABLE_CREDIT_CARD_TYPE = "CreditCardType";
		public static final String COLUMN_CREDITCARD_TYPE_ID = "creditcard_type_id";
		public static final String COLUMN_CREDITCARD_TYPE_NAME = "creditcard_type_name";
		public static final String COLUMN_CREDITCARD_NO = "creditcard_no";
		public static final String COLUMN_EXP_MONTH = "exp_month";
		public static final String COLUMN_EXP_YEAR = "exp_year";

		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_CREDIT_CARD_TYPE + " ( " +
				COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
				COLUMN_CREDITCARD_TYPE_NAME + " TEXT, " +
				" PRIMARY KEY (" + COLUMN_CREDITCARD_TYPE_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}
