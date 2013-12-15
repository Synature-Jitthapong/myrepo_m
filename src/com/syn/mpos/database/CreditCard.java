package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.syn.pos.CreditCardType;

public class CreditCard extends MPOSDatabase {
	public static final String TB_CREDIT_CARD_TYPE = "CreditCardType";
	public static final String COL_CREDIT_CARD_TYPE_ID = "CreditCardTypeId";
	public static final String COL_CREDIT_CARD_TYPE_NAME = "CreditCardTypeName";
	public static final String COL_CREDIT_CARD_NO = "CreditCardNo";
	public static final String COL_EXP_MONTH = "ExpMonth";
	public static final String COL_EXP_YEAR = "ExpYear";
	
	public CreditCard(Context c){
		super(c);
	}
	
	public List<CreditCardType> listAllCreditCardType(){
		List<CreditCardType> creditCardLst = 
				new ArrayList<CreditCardType>();
		
		open();
		Cursor cursor = mSqlite.query(TB_CREDIT_CARD_TYPE, 
				new String[]{COL_CREDIT_CARD_TYPE_ID, COL_CREDIT_CARD_TYPE_NAME}, 
				null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				CreditCardType credit = new CreditCardType(
						cursor.getInt(cursor.getColumnIndex(COL_CREDIT_CARD_TYPE_ID)),
						cursor.getString(cursor.getColumnIndex(COL_CREDIT_CARD_TYPE_NAME)));
				creditCardLst.add(credit);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return creditCardLst;
	}
}
