package com.syn.mpos.database;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class Shop extends MPOSDatabase{
	
	public Shop(SQLiteDatabase db){
		super(db);
	}
	
	public double getCompanyVatRate(){
		double vatRate = 7;
		Cursor cursor = mSqlite.query(ShopTable.TABLE_NAME, 
				new String[]{
				ShopTable.COLUMN_VAT
				}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			vatRate = cursor.getFloat(0);
		}
		cursor.close();
		return vatRate;
	}

	public ShopData.ShopProperty getShopProperty(){
		ShopData.ShopProperty sp = 
				new ShopData.ShopProperty();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + ShopTable.TABLE_NAME, null);
		if(cursor.moveToFirst()){
			sp.setShopID(cursor.getInt(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_ID)));
			sp.setShopCode(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_CODE)));
			sp.setShopName(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_NAME)));
			sp.setShopType(cursor.getInt(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_TYPE)));
			sp.setOpenHour(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_OPEN_HOUR)));
			sp.setCloseHour(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_CLOSE_HOUR)));
			sp.setVatType(cursor.getInt(cursor.getColumnIndex(ShopTable.COLUMN_VAT_TYPE)));
			sp.setCompanyName(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_COMPANY)));
			sp.setCompanyAddress1(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_ADDR1)));
			sp.setCompanyAddress2(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_ADDR2)));
			sp.setCompanyCity(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_CITY)));
			sp.setCompanyProvince(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_PROVINCE_ID)));
			sp.setCompanyZipCode(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_ZIPCODE)));
			sp.setCompanyTelephone(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_TELEPHONE)));
			sp.setCompanyFax(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_FAX)));
			sp.setCompanyTaxID(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_TAX_ID)));
			sp.setCompanyRegisterID(cursor.getString(cursor.getColumnIndex(ShopTable.COLUMN_REGISTER_ID)));
			sp.setCompanyVat(cursor.getFloat(cursor.getColumnIndex(ShopTable.COLUMN_VAT)));
			cursor.moveToNext();
		}
		cursor.close();		
		return sp;
	}

	public void insertShopProperty(List<ShopData.ShopProperty> shopPropLst) throws SQLException{
		mSqlite.delete(ShopTable.TABLE_NAME, null, null);
		for(ShopData.ShopProperty shop : shopPropLst){
			ContentValues cv = new ContentValues();
			cv.put(ShopTable.COLUMN_SHOP_ID, shop.getShopID());
			cv.put(ShopTable.COLUMN_SHOP_CODE, shop.getShopCode());
			cv.put(ShopTable.COLUMN_SHOP_NAME, shop.getShopName());
			cv.put(ShopTable.COLUMN_SHOP_TYPE, shop.getShopType());
			cv.put(ShopTable.COLUMN_VAT_TYPE, shop.getVatType());
			cv.put(ShopTable.COLUMN_OPEN_HOUR, shop.getOpenHour());
			cv.put(ShopTable.COLUMN_CLOSE_HOUR, shop.getCloseHour());
			cv.put(ShopTable.COLUMN_COMPANY, shop.getCompanyName());
			cv.put(ShopTable.COLUMN_ADDR1, shop.getCompanyAddress1());
			cv.put(ShopTable.COLUMN_ADDR2, shop.getCompanyAddress2());
			cv.put(ShopTable.COLUMN_CITY, shop.getCompanyCity());
			cv.put(ShopTable.COLUMN_PROVINCE_ID, shop.getCompanyProvince());
			cv.put(ShopTable.COLUMN_ZIPCODE, shop.getCompanyZipCode());
			cv.put(ShopTable.COLUMN_TELEPHONE, shop.getCompanyTelephone());
			cv.put(ShopTable.COLUMN_FAX, shop.getCompanyFax());
			cv.put(ShopTable.COLUMN_TAX_ID, shop.getCompanyTaxID());
			cv.put(ShopTable.COLUMN_REGISTER_ID, shop.getCompanyRegisterID());
			cv.put(ShopTable.COLUMN_VAT, shop.getCompanyVat());
			mSqlite.insertOrThrow(ShopTable.TABLE_NAME, null, cv);
		}
	}
}
