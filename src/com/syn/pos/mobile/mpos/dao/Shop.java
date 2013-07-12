package com.syn.pos.mobile.mpos.dao;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;

import com.j1tth4.mobile.core.sqlite.ISqliteHelper;
import com.syn.pos.mobile.model.ShopData;

public class Shop {
	private final String TB_SHOP_DATA = "shop_property";
	private final String TB_COMPUTER = "computer_property";
	private final String TB_GLOBAL_PROPERTY = "global_property";
	private final String TB_STAFF = "staffs";
	private final String TB_FEATURE = "program_feature";
	private final String TB_LANGUAGE = "language";
	
	
	private ISqliteHelper dbHelper;
	
	public Shop(Context c){
		dbHelper = new MPOSSqliteHelper(c);
	}
	
	public boolean addLanguage(List<ShopData.Language> langLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_LANGUAGE);
		
		for(ShopData.Language lang : langLst){
			ContentValues cv = new ContentValues();
			cv.put("lang_id", lang.getLangID());
			cv.put("lang_name", lang.getLangName());
			cv.put("lang_code", lang.getLangCode());
			
			isSucc = dbHelper.insert(TB_LANGUAGE, cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addProgramFeature(List<ShopData.ProgramFeature> featureLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_FEATURE);
		
		for(ShopData.ProgramFeature feature : featureLst){
			ContentValues cv = new ContentValues();
			cv.put("feature_id", feature.getFeatureID());
			cv.put("feature_name", feature.getFeatureName());
			cv.put("feature_value", feature.getFeatureValue());
			cv.put("feature_text", feature.getFeatureText());
			cv.put("feature_desc", feature.getFeatureDesc());
			
			isSucc = dbHelper.insert(TB_FEATURE, cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addStaff(List<ShopData.Staff> staffLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_STAFF);
		
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put("staff_id", staff.getStaffID());
			cv.put("staff_code", staff.getStaffCode());
			cv.put("staff_name", staff.getStaffName());
			cv.put("staff_password", staff.getStaffPassword());
			
			isSucc = dbHelper.insert(TB_STAFF, cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addGlobalProperty(List<ShopData.GlobalProperty> globalLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_GLOBAL_PROPERTY);
		
		for(ShopData.GlobalProperty global : globalLst){
			ContentValues cv = new ContentValues();
			cv.put("currency_symbol", global.getCurrencySymbol());
			cv.put("currency_code", global.getCurrencyCode());
			cv.put("currency_name", global.getCurrencyName());
			cv.put("currency_format", global.getCurrencyFormat());
			cv.put("date_format", global.getDateFormat());
			cv.put("time_format", global.getTimeFormat());
			cv.put("qty_format", global.getQtyFormat());
			
			isSucc = dbHelper.insert(TB_GLOBAL_PROPERTY, cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addComputerProperty(List<ShopData.ComputerProperty> compLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_COMPUTER);
		
		for(ShopData.ComputerProperty comp : compLst){
			ContentValues cv = new ContentValues();
			cv.put("computer_id", comp.getComputerID());
			cv.put("computer_name", comp.getComputerName());
			cv.put("device_code", comp.getDeviceCode());
			cv.put("registration_number", comp.getRegistrationNumber());
			
			isSucc = dbHelper.insert(TB_COMPUTER, cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
	
	public boolean addShopProperty(List<ShopData.ShopProperty> shopPropLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_SHOP_DATA);
		
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
			
			isSucc = dbHelper.insert(TB_SHOP_DATA, cv);
		}
		dbHelper.close();
		
		return isSucc;
	}
}
