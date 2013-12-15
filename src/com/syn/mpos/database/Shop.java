package com.syn.mpos.database;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.syn.pos.ShopData;

public class Shop extends MPOSDatabase{
	public static final String TB_SHOP = "Shop";
	public static final String COL_SHOP_ID = "ShopID";
	public static final String COL_SHOP_CODE = "ShopCode";
	public static final String COL_SHOP_NAME = "ShopName";
	public static final String COL_SHOP_TYPE = "ShopType";
	public static final String COL_VAT_TYPE = "VatType";
	public static final String COL_OPEN_HOUR = "OpenHour";
	public static final String COL_CLOSE_HOUR = "CloseHour";
	public static final String COL_COMPANY = "Company";
	public static final String COL_ADDR1 = "Addr1";
	public static final String COL_ADDR2 = "Addr2";
	public static final String COL_CITY = "City";
	public static final String COL_PROVINCE_ID = "ProvinceId";
	public static final String COL_ZIPCODE = "ZipCode";
	public static final String COL_TELEPHONE = "Telephone";
	public static final String COL_FAX = "Fax";
	public static final String COL_TAX_ID = "TaxId";
	public static final String COL_REGISTER_ID = "RegisterId";
	public static final String COL_VAT = "Vat";
	
	public Shop(Context c){
		super(c);
	}
	
	public ShopData.ShopProperty getShopProperty(){
		ShopData.ShopProperty sp = 
				new ShopData.ShopProperty();
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_SHOP, null);
		if(cursor.moveToFirst()){
			sp.setShopID(cursor.getInt(cursor.getColumnIndex(COL_SHOP_ID)));
			sp.setShopCode(cursor.getString(cursor.getColumnIndex(COL_SHOP_CODE)));
			sp.setShopName(cursor.getString(cursor.getColumnIndex(COL_SHOP_NAME)));
			sp.setShopType(cursor.getInt(cursor.getColumnIndex(COL_SHOP_TYPE)));
			sp.setOpenHour(cursor.getString(cursor.getColumnIndex(COL_OPEN_HOUR)));
			sp.setCloseHour(cursor.getString(cursor.getColumnIndex(COL_CLOSE_HOUR)));
			sp.setVatType(cursor.getInt(cursor.getColumnIndex(COL_VAT_TYPE)));
			sp.setCompanyName(cursor.getString(cursor.getColumnIndex(COL_COMPANY)));
			sp.setCompanyAddress1(cursor.getString(cursor.getColumnIndex(COL_ADDR1)));
			sp.setCompanyAddress2(cursor.getString(cursor.getColumnIndex(COL_ADDR2)));
			sp.setCompanyCity(cursor.getString(cursor.getColumnIndex(COL_CITY)));
			sp.setCompanyProvince(cursor.getString(cursor.getColumnIndex(COL_PROVINCE_ID)));
			sp.setCompanyZipCode(cursor.getString(cursor.getColumnIndex(COL_ZIPCODE)));
			sp.setCompanyTelephone(cursor.getString(cursor.getColumnIndex(COL_TELEPHONE)));
			sp.setCompanyFax(cursor.getString(cursor.getColumnIndex(COL_FAX)));
			sp.setCompanyTaxID(cursor.getString(cursor.getColumnIndex(COL_TAX_ID)));
			sp.setCompanyRegisterID(cursor.getString(cursor.getColumnIndex(COL_REGISTER_ID)));
			sp.setCompanyVat(cursor.getFloat(cursor.getColumnIndex(COL_VAT)));
			cursor.moveToNext();
		}
		cursor.close();
		close();		
		return sp;
	}

	public void addShopProperty(List<ShopData.ShopProperty> shopPropLst) throws SQLException{
		open();
		mSqlite.execSQL("DELETE FROM " + TB_SHOP);
		for(ShopData.ShopProperty shop : shopPropLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_SHOP_ID, shop.getShopID());
			cv.put(COL_SHOP_CODE, shop.getShopCode());
			cv.put(COL_SHOP_NAME, shop.getShopName());
			cv.put(COL_SHOP_TYPE, shop.getShopType());
			cv.put(COL_VAT_TYPE, shop.getVatType());
			cv.put(COL_OPEN_HOUR, shop.getOpenHour());
			cv.put(COL_CLOSE_HOUR, shop.getCloseHour());
			cv.put(COL_COMPANY, shop.getCompanyName());
			cv.put(COL_ADDR1, shop.getCompanyAddress1());
			cv.put(COL_ADDR2, shop.getCompanyAddress2());
			cv.put(COL_CITY, shop.getCompanyCity());
			cv.put(COL_PROVINCE_ID, shop.getCompanyProvince());
			cv.put(COL_ZIPCODE, shop.getCompanyZipCode());
			cv.put(COL_TELEPHONE, shop.getCompanyTelephone());
			cv.put(COL_FAX, shop.getCompanyFax());
			cv.put(COL_TAX_ID, shop.getCompanyTaxID());
			cv.put(COL_REGISTER_ID, shop.getCompanyRegisterID());
			cv.put(COL_VAT, shop.getCompanyVat());
			mSqlite.insertOrThrow(TB_SHOP, null, cv);
		}
		close();
	}
}
