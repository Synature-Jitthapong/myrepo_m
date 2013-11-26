package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;

public class Province extends MPOSSQLiteHelper{
	public static final String TB_PROVINCE = "Province";
	public static final String COL_PROVINCE_ID = "ProvinceId";
	public static final String COL_PROVINCE_NAME = "ProvinceName";
	
	public Province(Context c){
		super(c);
	}
	
	public List<com.syn.pos.Province> listProvince(){
		List<com.syn.pos.Province> pLst =
				new ArrayList<com.syn.pos.Province>();
		
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_PROVINCE, null);
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Province province =
						new com.syn.pos.Province();
				province.setProvinceId(cursor.getInt(cursor.getColumnIndex(COL_PROVINCE_ID)));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex(COL_PROVINCE_NAME)));
				pLst.add(province);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return pLst;
	}
}
