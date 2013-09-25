package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.sqlite.SQLiteHelper;

public class Province {
	private SQLiteHelper mDbHelper;
	
	public Province(Context context){
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public List<com.syn.pos.Province> listProvince(){
		List<com.syn.pos.Province> pLst =
				new ArrayList<com.syn.pos.Province>();
		
		String strSql = "SELECT * FROM provinces ";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Province province =
						new com.syn.pos.Province();
				province.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				
				pLst.add(province);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return pLst;
	}
}
