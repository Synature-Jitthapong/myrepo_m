package com.syn.mpos.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import com.syn.mpos.model.ProductGroups;

public class Product {
	private MPOSSQLiteHelper dbHelper;
	
	public Product(Context c){
		dbHelper = new MPOSSQLiteHelper(c);
	}
	
	public boolean addProducts(List<ProductGroups.Products> productLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM products");
		
		for(ProductGroups.Products p : productLst){
			ContentValues cv = new ContentValues();
			cv.put("product_id", p.getProductID());
			cv.put("product_dept_id", p.getProductDeptID());
			cv.put("product_group_id", p.getProductGroupID());
			cv.put("product_code", p.getProductCode());
			cv.put("product_bar_code", p.getProductBarCode());
			cv.put("product_type_id", p.getProductTypeID());
			cv.put("product_price", p.getProductPricePerUnit());
			cv.put("product_unit_name", p.getProductUnitName());
			cv.put("product_desc", p.getProductDesc());
			cv.put("discount_allow", p.getDiscountAllow());
			cv.put("vat_type", p.getVatType());
			cv.put("vat_rate", p.getVatRate());
			cv.put("has_service_charge", p.getHasServiceCharge());
			cv.put("activate", p.getActivate());
			cv.put("is_out_of_stock", p.getIsOutOfStock());
			cv.put("sale_mode_1", p.getSaleMode1());
			cv.put("product_price_1", p.getProductPricePerUnit1());
			cv.put("sale_mode_2", p.getSaleMode2());
			cv.put("product_price_2", p.getProductPricePerUnit2());
			cv.put("sale_mode_3", p.getSaleMode3());
			cv.put("product_price_3", p.getProductPricePerUnit3());
			cv.put("sale_mode_4", p.getSaleMode4());
			cv.put("product_price_4", p.getProductPricePerUnit4());
			cv.put("sale_mode_5", p.getSaleMode5());
			cv.put("product_price_5", p.getProductPricePerUnit5());
			cv.put("updatedate", p.getUpdateDate());
			
			isSucc = dbHelper.insert("products", cv);
		}
		
		dbHelper.close();
		
		return isSucc;
	}
}
