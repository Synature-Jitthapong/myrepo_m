package com.syn.mpos.provider;

import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class Shop extends MPOSDatabase{
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
	
	public Shop(SQLiteDatabase db){
		super(db);
	}
	
	public double getCompanyVatRate(){
		double vatRate = 7;
		Cursor cursor = mSqlite.query(TABLE_SHOP, 
				new String[]{
					COLUMN_VAT
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
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TABLE_SHOP, null);
		if(cursor.moveToFirst()){
			sp.setShopID(cursor.getInt(cursor.getColumnIndex(COLUMN_SHOP_ID)));
			sp.setShopCode(cursor.getString(cursor.getColumnIndex(COLUMN_SHOP_CODE)));
			sp.setShopName(cursor.getString(cursor.getColumnIndex(COLUMN_SHOP_NAME)));
			sp.setShopType(cursor.getInt(cursor.getColumnIndex(COLUMN_SHOP_TYPE)));
			sp.setOpenHour(cursor.getString(cursor.getColumnIndex(COLUMN_OPEN_HOUR)));
			sp.setCloseHour(cursor.getString(cursor.getColumnIndex(COLUMN_CLOSE_HOUR)));
			sp.setVatType(cursor.getInt(cursor.getColumnIndex(COLUMN_VAT_TYPE)));
			sp.setCompanyName(cursor.getString(cursor.getColumnIndex(COLUMN_COMPANY)));
			sp.setCompanyAddress1(cursor.getString(cursor.getColumnIndex(COLUMN_ADDR1)));
			sp.setCompanyAddress2(cursor.getString(cursor.getColumnIndex(COLUMN_ADDR2)));
			sp.setCompanyCity(cursor.getString(cursor.getColumnIndex(COLUMN_CITY)));
			sp.setCompanyProvince(cursor.getString(cursor.getColumnIndex(COLUMN_PROVINCE_ID)));
			sp.setCompanyZipCode(cursor.getString(cursor.getColumnIndex(COLUMN_ZIPCODE)));
			sp.setCompanyTelephone(cursor.getString(cursor.getColumnIndex(COLUMN_TELEPHONE)));
			sp.setCompanyFax(cursor.getString(cursor.getColumnIndex(COLUMN_FAX)));
			sp.setCompanyTaxID(cursor.getString(cursor.getColumnIndex(COLUMN_TAX_ID)));
			sp.setCompanyRegisterID(cursor.getString(cursor.getColumnIndex(COLUMN_REGISTER_ID)));
			sp.setCompanyVat(cursor.getFloat(cursor.getColumnIndex(COLUMN_VAT)));
			cursor.moveToNext();
		}
		cursor.close();		
		return sp;
	}

	public void insertShopProperty(List<ShopData.ShopProperty> shopPropLst) throws SQLException{
		mSqlite.execSQL("DELETE FROM " + TABLE_SHOP);
		for(ShopData.ShopProperty shop : shopPropLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_SHOP_ID, shop.getShopID());
			cv.put(COLUMN_SHOP_CODE, shop.getShopCode());
			cv.put(COLUMN_SHOP_NAME, shop.getShopName());
			cv.put(COLUMN_SHOP_TYPE, shop.getShopType());
			cv.put(COLUMN_VAT_TYPE, shop.getVatType());
			cv.put(COLUMN_OPEN_HOUR, shop.getOpenHour());
			cv.put(COLUMN_CLOSE_HOUR, shop.getCloseHour());
			cv.put(COLUMN_COMPANY, shop.getCompanyName());
			cv.put(COLUMN_ADDR1, shop.getCompanyAddress1());
			cv.put(COLUMN_ADDR2, shop.getCompanyAddress2());
			cv.put(COLUMN_CITY, shop.getCompanyCity());
			cv.put(COLUMN_PROVINCE_ID, shop.getCompanyProvince());
			cv.put(COLUMN_ZIPCODE, shop.getCompanyZipCode());
			cv.put(COLUMN_TELEPHONE, shop.getCompanyTelephone());
			cv.put(COLUMN_FAX, shop.getCompanyFax());
			cv.put(COLUMN_TAX_ID, shop.getCompanyTaxID());
			cv.put(COLUMN_REGISTER_ID, shop.getCompanyRegisterID());
			cv.put(COLUMN_VAT, shop.getCompanyVat());
			mSqlite.insertOrThrow(TABLE_SHOP, null, cv);
		}
	}
}
