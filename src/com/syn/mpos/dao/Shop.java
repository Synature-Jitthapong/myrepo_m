package com.syn.mpos.dao;

import java.util.List;

import com.synature.pos.ShopData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Shop extends MPOSDatabase{
	
	public Shop(Context context){
		super(context);
	}
	
	/**
	 * @return Shop Name
	 */
	public String getShopName(){
		return getShopProperty().getShopName();
	}
	
	/**
	 * @return Company Vat Type
	 */
	public int getCompanyVatType(){
		return getShopProperty().getVatType();
	}
	
	/**
	 * @return company vat rate
	 */
	public double getCompanyVatRate(){
		return getShopProperty().getCompanyVat();
	}

	/**
	 * @return shop id
	 */
	public int getShopId(){
		return getShopProperty().getShopID();
	}
	
	/**
	 * @return ShopData.ShopProperty
	 */
	public ShopData.ShopProperty getShopProperty(){
		ShopData.ShopProperty sp = 
				new ShopData.ShopProperty();
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + ShopTable.TABLE_SHOP, null);
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

	/**
	 * @param shopPropLst
	 * @throws SQLException
	 */
	public void insertShopProperty(List<ShopData.ShopProperty> shopPropLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ShopTable.TABLE_SHOP, null, null);
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
				getWritableDatabase().insertOrThrow(ShopTable.TABLE_SHOP, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class ShopTable{

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
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_SHOP + " ( " +
				COLUMN_SHOP_ID + " INTEGER, " +
				COLUMN_SHOP_CODE + " TEXT, " +
				COLUMN_SHOP_NAME + " TEXT, " +
				COLUMN_SHOP_TYPE + " INTEGER, " +
				COLUMN_VAT_TYPE + " INTEGER, " +
				COLUMN_OPEN_HOUR + " TEXT, " +
				COLUMN_CLOSE_HOUR + " TEXT, " +
				COLUMN_COMPANY + " TEXT, " +
				COLUMN_ADDR1 + " TEXT, " +
				COLUMN_ADDR2 + " TEXT, " +
				COLUMN_CITY + " TEXT, " +
				COLUMN_PROVINCE_ID + " INTEGER, " +
				COLUMN_ZIPCODE + " TEXT, " +
				COLUMN_TELEPHONE + " TEXT, " +
				COLUMN_FAX + " TEXT, " +
				COLUMN_TAX_ID + " TEXT, " +
				COLUMN_REGISTER_ID + " TEXT, " +
				COLUMN_VAT + " REAL );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}
