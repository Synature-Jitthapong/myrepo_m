package com.syn.mpos.provider;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Province extends MPOSDatabase{
	public static final String TABLE_PROVINCE = "Province";
	public static final String COLUMN_PROVINCE_ID = "province_id";
	public static final String COLUMN_PROVINCE_NAME = "province_name";
	
	public Province(SQLiteDatabase db){
		super(db);
	}
	
	public List<com.syn.pos.Province> listProvince(){
		List<com.syn.pos.Province> pLst =
				new ArrayList<com.syn.pos.Province>();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TABLE_PROVINCE, null);
		if(cursor.moveToFirst()){
			do{
				com.syn.pos.Province province =
						new com.syn.pos.Province();
				province.setProvinceId(cursor.getInt(cursor.getColumnIndex(COLUMN_PROVINCE_ID)));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex(COLUMN_PROVINCE_NAME)));
				pLst.add(province);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
}
