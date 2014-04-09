package com.syn.mpos.datasource;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.syn.pos.ShopData;

public class Shop extends MPOSDatabase{
	
	public Shop(Context c){
		super(c);
	}
	
	public double getCompanyVatRate(){
		double vatRate = 7;
		Cursor cursor = mSqlite.query(ShopEntry.TABLE_SHOP, 
				new String[]{
				ShopEntry.COLUMN_VAT
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
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + ShopEntry.TABLE_SHOP, null);
		if(cursor.moveToFirst()){
			sp.setShopID(cursor.getInt(cursor.getColumnIndex(ShopEntry.COLUMN_SHOP_ID)));
			sp.setShopCode(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_SHOP_CODE)));
			sp.setShopName(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_SHOP_NAME)));
			sp.setShopType(cursor.getInt(cursor.getColumnIndex(ShopEntry.COLUMN_SHOP_TYPE)));
			sp.setOpenHour(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_OPEN_HOUR)));
			sp.setCloseHour(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_CLOSE_HOUR)));
			sp.setVatType(cursor.getInt(cursor.getColumnIndex(ShopEntry.COLUMN_VAT_TYPE)));
			sp.setCompanyName(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_COMPANY)));
			sp.setCompanyAddress1(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_ADDR1)));
			sp.setCompanyAddress2(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_ADDR2)));
			sp.setCompanyCity(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_CITY)));
			sp.setCompanyProvince(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_PROVINCE_ID)));
			sp.setCompanyZipCode(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_ZIPCODE)));
			sp.setCompanyTelephone(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_TELEPHONE)));
			sp.setCompanyFax(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_FAX)));
			sp.setCompanyTaxID(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_TAX_ID)));
			sp.setCompanyRegisterID(cursor.getString(cursor.getColumnIndex(ShopEntry.COLUMN_REGISTER_ID)));
			sp.setCompanyVat(cursor.getFloat(cursor.getColumnIndex(ShopEntry.COLUMN_VAT)));
			cursor.moveToNext();
		}
		cursor.close();		
		return sp;
	}

	public void insertShopProperty(List<ShopData.ShopProperty> shopPropLst) throws SQLException{
		mSqlite.execSQL("DELETE FROM " + ShopEntry.TABLE_SHOP);
		for(ShopData.ShopProperty shop : shopPropLst){
			ContentValues cv = new ContentValues();
			cv.put(ShopEntry.COLUMN_SHOP_ID, shop.getShopID());
			cv.put(ShopEntry.COLUMN_SHOP_CODE, shop.getShopCode());
			cv.put(ShopEntry.COLUMN_SHOP_NAME, shop.getShopName());
			cv.put(ShopEntry.COLUMN_SHOP_TYPE, shop.getShopType());
			cv.put(ShopEntry.COLUMN_VAT_TYPE, shop.getVatType());
			cv.put(ShopEntry.COLUMN_OPEN_HOUR, shop.getOpenHour());
			cv.put(ShopEntry.COLUMN_CLOSE_HOUR, shop.getCloseHour());
			cv.put(ShopEntry.COLUMN_COMPANY, shop.getCompanyName());
			cv.put(ShopEntry.COLUMN_ADDR1, shop.getCompanyAddress1());
			cv.put(ShopEntry.COLUMN_ADDR2, shop.getCompanyAddress2());
			cv.put(ShopEntry.COLUMN_CITY, shop.getCompanyCity());
			cv.put(ShopEntry.COLUMN_PROVINCE_ID, shop.getCompanyProvince());
			cv.put(ShopEntry.COLUMN_ZIPCODE, shop.getCompanyZipCode());
			cv.put(ShopEntry.COLUMN_TELEPHONE, shop.getCompanyTelephone());
			cv.put(ShopEntry.COLUMN_FAX, shop.getCompanyFax());
			cv.put(ShopEntry.COLUMN_TAX_ID, shop.getCompanyTaxID());
			cv.put(ShopEntry.COLUMN_REGISTER_ID, shop.getCompanyRegisterID());
			cv.put(ShopEntry.COLUMN_VAT, shop.getCompanyVat());
			mSqlite.insertOrThrow(ShopEntry.TABLE_SHOP, null, cv);
		}
	}
	
	public static abstract class ShopEntry{
		public static final String TABLE_SHOP = "Shop";
		public static final String COLUMN_SHOP_ID = "shop_id";
		public static final String COLUMN_SHOP_CODE = "shop_code";
		public static final String COLUMN_SHOP_NAME = "shop_name";
		public static final String COLUMN_SHOP_TYPE = "shop_type";
		public static final String COLUMN_VAT_TYPE = "vat_type";
		public static final String COLUMN_OPEN_HOUR = "open_hour";
		public static final String COLUMN_CLOSE_HOUR = "close_hour";
		public static final String COLUMN_COMPANY = "company";
		public static final String COLUMN_ADDR1 = "addr1";
		public static final String COLUMN_ADDR2 = "addr2";
		public static final String COLUMN_CITY = "city";
		public static final String COLUMN_PROVINCE_ID = "province_id";
		public static final String COLUMN_ZIPCODE = "zip_code";
		public static final String COLUMN_TELEPHONE = "telephone";
		public static final String COLUMN_FAX = "fax";
		public static final String COLUMN_TAX_ID = "tax_id";
		public static final String COLUMN_REGISTER_ID = "register_id";
		public static final String COLUMN_VAT = "vat";
	}
}
