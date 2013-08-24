package com.syn.mpos.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.syn.mpos.model.ShopData;

public class Shop {
	private MPOSSQLiteHelper dbHelper;
	
	public Shop(Context c){
		dbHelper = new MPOSSQLiteHelper(c);
	}
	
	public ShopData.ComputerProperty getComputerProperty(){
		ShopData.ComputerProperty computer = 
				new ShopData.ComputerProperty();
		
		String strSql = "SELECT * FROM computer_property";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			computer.setComputerID(cursor.getInt(cursor.getColumnIndex("computer_id")));
			computer.setComputerName(cursor.getString(cursor.getColumnIndex("computer_name")));
			computer.setDeviceCode(cursor.getString(cursor.getColumnIndex("device_code")));
			computer.setRegistrationNumber(cursor.getString(cursor.getColumnIndex("registration_number")));
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return computer;
	}
	
	public ShopData.ShopProperty getShopProperty(){
		ShopData.ShopProperty sp = 
				new ShopData.ShopProperty();
		
		String strSql = "SELECT * FROM shop_property";
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			sp.setShopID(cursor.getInt(cursor.getColumnIndex("shop_id")));
			sp.setShopCode(cursor.getString(cursor.getColumnIndex("shop_code")));
			sp.setShopName(cursor.getString(cursor.getColumnIndex("shop_name")));
			sp.setShopType(cursor.getInt(cursor.getColumnIndex("shop_type")));
			sp.setCalculateServiceChargeWhenFreeBill(
					cursor.getInt(cursor.getColumnIndex("calculate_service_charge_when_freebill")));
			sp.setCloseHour(cursor.getString(cursor.getColumnIndex("close_hour")));
			sp.setFastFoodType(cursor.getInt(cursor.getColumnIndex("fast_food_type")));
			sp.setTableType(cursor.getInt(cursor.getColumnIndex("table_type")));
			sp.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
			sp.setServiceCharge(cursor.getDouble(cursor.getColumnIndex("service_charge")));
			sp.setServiceChargeType(cursor.getInt(cursor.getColumnIndex("service_charge_type")));
			sp.setOpenHour(cursor.getString(cursor.getColumnIndex("open_hour")));
			sp.setCompanyName(cursor.getString(cursor.getColumnIndex("company_name")));
			sp.setCompanyAddress1(cursor.getString(cursor.getColumnIndex("company_address_1")));
			sp.setCompanyAddress2(cursor.getString(cursor.getColumnIndex("company_address_2")));
			sp.setCompanyCity(cursor.getString(cursor.getColumnIndex("company_city")));
			sp.setCompanyProvince(cursor.getString(cursor.getColumnIndex("company_province")));
			sp.setCompanyZipCode(cursor.getString(cursor.getColumnIndex("company_zip_code")));
			sp.setCompanyTelephone(cursor.getString(cursor.getColumnIndex("company_tel")));
			sp.setCompanyFax(cursor.getString(cursor.getColumnIndex("company_fax")));
			sp.setCompanyTaxID(cursor.getString(cursor.getColumnIndex("company_tax_id")));
			sp.setCompanyRegisterID(cursor.getString(cursor.getColumnIndex("company_register_id")));
			sp.setCompanyVat(cursor.getDouble(cursor.getColumnIndex("company_vat")));
			cursor.moveToNext();
		}
		cursor.close();
		dbHelper.close();
		
		return sp;
	}
	
	public ShopData.GlobalProperty getGlobalProperty(){
		ShopData.GlobalProperty gb = 
				new ShopData.GlobalProperty();
		
		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery("SELECT * FROM global_property");
		if(cursor.moveToFirst()){
			gb.setCurrencyCode(cursor.getString(cursor.getColumnIndex("currency_code")));
			gb.setCurrencySymbol(cursor.getString(cursor.getColumnIndex("currency_symbol")));
			gb.setCurrencyName(cursor.getString(cursor.getColumnIndex("currency_name")));
			gb.setCurrencyFormat(cursor.getString(cursor.getColumnIndex("currency_format")));
			gb.setDateFormat(cursor.getString(cursor.getColumnIndex("date_format")));
			gb.setTimeFormat(cursor.getString(cursor.getColumnIndex("time_format")));
			gb.setQtyFormat(cursor.getString(cursor.getColumnIndex("qty_format")));
			cursor.moveToNext();
		}
		dbHelper.close();
		
		return gb;
	}
	
	public boolean addLanguage(List<ShopData.Language> langLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM language");
		
		for(ShopData.Language lang : langLst){
			ContentValues cv = new ContentValues();
			cv.put("lang_id", lang.getLangID());
			cv.put("lang_name", lang.getLangName());
			cv.put("lang_code", lang.getLangCode());
			
			isSucc = dbHelper.insert("language", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addProgramFeature(List<ShopData.ProgramFeature> featureLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM program_feature");
		
		for(ShopData.ProgramFeature feature : featureLst){
			ContentValues cv = new ContentValues();
			cv.put("feature_id", feature.getFeatureID());
			cv.put("feature_name", feature.getFeatureName());
			cv.put("feature_value", feature.getFeatureValue());
			cv.put("feature_text", feature.getFeatureText());
			cv.put("feature_desc", feature.getFeatureDesc());
			
			isSucc = dbHelper.insert("program_feature", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addStaff(List<ShopData.Staff> staffLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM staffs");
		
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put("staff_id", staff.getStaffID());
			cv.put("staff_code", staff.getStaffCode());
			cv.put("staff_name", staff.getStaffName());
			cv.put("staff_password", staff.getStaffPassword());
			
			isSucc = dbHelper.insert("staffs", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addGlobalProperty(List<ShopData.GlobalProperty> globalLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM global_property");
		
		for(ShopData.GlobalProperty global : globalLst){
			ContentValues cv = new ContentValues();
			cv.put("currency_symbol", global.getCurrencySymbol());
			cv.put("currency_code", global.getCurrencyCode());
			cv.put("currency_name", global.getCurrencyName());
			cv.put("currency_format", global.getCurrencyFormat());
			cv.put("date_format", global.getDateFormat());
			cv.put("time_format", global.getTimeFormat());
			cv.put("qty_format", global.getQtyFormat());
			
			isSucc = dbHelper.insert("global_property", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addComputerProperty(List<ShopData.ComputerProperty> compLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM computer_property");
		
		for(ShopData.ComputerProperty comp : compLst){
			ContentValues cv = new ContentValues();
			cv.put("computer_id", comp.getComputerID());
			cv.put("computer_name", comp.getComputerName());
			cv.put("device_code", comp.getDeviceCode());
			cv.put("registration_number", comp.getRegistrationNumber());
			
			isSucc = dbHelper.insert("computer_property", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addShopProperty(List<ShopData.ShopProperty> shopPropLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM shop_property");
		
		for(ShopData.ShopProperty shop : shopPropLst){
			ContentValues cv = new ContentValues();
			cv.put("shop_id", shop.getShopID());
			cv.put("shop_code", shop.getShopCode());
			cv.put("shop_name", shop.getShopName());
			cv.put("shop_type", shop.getShopType());
			cv.put("fast_food_type", shop.getFastFoodType());
			cv.put("table_type", shop.getTableType());
			cv.put("vat_type", shop.getVatType());
			cv.put("service_charge", shop.getServiceCharge());
			cv.put("service_charge_type", shop.getServiceChargeType());
			cv.put("open_hour", shop.getOpenHour());
			cv.put("close_hour", shop.getCloseHour());
			cv.put("calculate_service_charge_when_freebill", shop.getCalculateServiceChargeWhenFreeBill());
			cv.put("company_name", shop.getCompanyName());
			cv.put("company_address_1", shop.getCompanyAddress1());
			cv.put("company_address_2", shop.getCompanyAddress2());
			cv.put("company_city", shop.getCompanyCity());
			cv.put("company_province", shop.getCompanyProvince());
			cv.put("company_zip_code", shop.getCompanyZipCode());
			cv.put("company_tel", shop.getCompanyTelephone());
			cv.put("company_fax", shop.getCompanyFax());
			cv.put("company_tax_id", shop.getCompanyTaxID());
			cv.put("company_register_id", shop.getCompanyRegisterID());
			cv.put("company_vat", shop.getCompanyVat());
			
			isSucc = dbHelper.insert("shop_property", cv);
		}
		dbHelper.close();
		
		return isSucc;
	}
}
