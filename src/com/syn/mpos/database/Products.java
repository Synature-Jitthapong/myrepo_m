package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;

public class Products {
	private MPOSSQLiteHelper mSqlite;
	
	public Products(Context c){
		mSqlite = new MPOSSQLiteHelper(c);
	}
	
	public List<Product> listProduct(int deptId){
		List<Product> pLst = new ArrayList<Product>();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM products" +
				" WHERE product_dept_id=" + deptId +
				" AND activated=1" +
				" ORDER BY product_ordering");
		if(cursor.moveToFirst()){
			do{
				Product p = new Product();
				p.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				p.setProductCode(cursor.getString(cursor.getColumnIndex("product_code")));
				p.setProductBarCode(cursor.getString(cursor.getColumnIndex("product_barcode")));
				p.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
				p.setProductPrice(cursor.getFloat(cursor.getColumnIndex("product_price")));
				p.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
				p.setVatRate(cursor.getFloat(cursor.getColumnIndex("vat_rate")));
				p.setDiscountAllow(cursor.getInt(cursor.getColumnIndex("discount_allow")));
				p.setPicName(cursor.getString(cursor.getColumnIndex("pic_name")));
				p.setHasServiceCharge(cursor.getInt(cursor.getColumnIndex("has_service_charge")));
				
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return pLst;
	}
	
	public List<ProductDept> listProductDept(){
		List<ProductDept> pdLst = new ArrayList<ProductDept>();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM product_dept " +
				" ORDER BY product_dept_ordering");
		if(cursor.moveToFirst()){
			do{
				ProductDept pd = new ProductDept();
				pd.setProductDeptId(cursor.getInt(cursor.getColumnIndex("product_dept_id")));
				pd.setProductGroupId(cursor.getInt(cursor.getColumnIndex("product_group_id")));
				pd.setProductDeptCode(cursor.getString(cursor.getColumnIndex("product_dept_code")));
				pd.setProductDeptName(cursor.getString(cursor.getColumnIndex("product_dept_name")));
				
				pdLst.add(pd);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return pdLst;
	}
	
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM product_group" +
				" ORDER BY product_group_ordering");
		if(cursor.moveToFirst()){
			do{
				ProductGroup pg = new ProductGroup();
				pg.setProductGroupId(cursor.getInt(cursor.getColumnIndex("product_group_id")));
				pg.setProductGroupCode(cursor.getString(cursor.getColumnIndex("product_group_code")));
				pg.setProductGroupName(cursor.getString(cursor.getColumnIndex("product_group_name")));
				
				pgLst.add(pg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return pgLst;
	}
	
	public boolean insertProductGroup(List<ProductGroups.ProductGroup> pgLst){
		boolean isSuccess = false;
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM product_group WHERE create_from_device=0");
		for(ProductGroups.ProductGroup pg : pgLst){
			ContentValues cv = new ContentValues();
			cv.put("product_group_id", pg.getProductGroupId());
			cv.put("product_group_code", pg.getProductGroupCode());
			cv.put("product_group_name", pg.getProductGroupName());
			cv.put("product_group_type", pg.getProductGroupType());
			cv.put("is_comment", pg.getIsComment());
			cv.put("product_group_ordering", pg.getProductGroupOrdering());
			
			isSuccess = mSqlite.insert("product_group", cv);
		}
		mSqlite.close();
		return isSuccess;
	}
	
	public boolean insertProductDept(List<ProductGroups.ProductDept> pdLst){
		boolean isSuccess = false;
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM product_dept WHERE create_from_device=0");
		for(ProductGroups.ProductDept pd : pdLst){
			ContentValues cv = new ContentValues();
			cv.put("product_dept_id", pd.getProductDeptID());
			cv.put("product_group_id", pd.getProductGroupID());
			cv.put("product_dept_code", pd.getProductDeptCode());
			cv.put("product_dept_name", pd.getProductDeptName());
			cv.put("product_dept_ordering", pd.getProductDeptOrdering());
			
			isSuccess = mSqlite.insert("product_dept", cv);
		}
		mSqlite.close();
		return isSuccess;
	}
	
	public boolean insertProducts(List<ProductGroups.Products> productLst,
			List<MenuGroups.MenuItem> menuItemLst){
		boolean isSucc = false;
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM products WHERE create_from_device=0");
		
		for(ProductGroups.Products p : productLst){
			ContentValues cv = new ContentValues();
			cv.put("product_id", p.getProductID());
			cv.put("product_dept_id", p.getProductDeptID());
			cv.put("product_group_id", p.getProductGroupID());
			cv.put("product_code", p.getProductCode());
			cv.put("product_barcode", p.getProductBarCode());
			cv.put("product_type_id", p.getProductTypeID());
			cv.put("product_price", p.getProductPricePerUnit());
			cv.put("product_unit_name", p.getProductUnitName());
			cv.put("product_desc", p.getProductDesc());
			cv.put("discount_allow", p.getDiscountAllow());
			cv.put("vat_type", p.getVatType());
			cv.put("vat_rate", p.getVatRate());
			cv.put("has_service_charge", p.getHasServiceCharge());
			cv.put("activated", p.getActivate());
			cv.put("is_out_of_stock", p.getIsOutOfStock());
			
			isSucc = mSqlite.insert("products", cv);
		}
		
		for(MenuGroups.MenuItem m : menuItemLst){
			mSqlite.execSQL("UPDATE products SET " +
					" product_name='" + m.getMenuName_0() + "', " +
					" pic_name='" + m.getMenuImageLink() + "'," +
					" product_ordering=" + m.getMenuItemOrdering() +
					" WHERE product_id=" + m.getProductID());
		}
		
		mSqlite.close();
		
		return isSucc;
	}
	
	public static class Product{
		private int productId;
		private int productGroupId;
		private int productDeptId;
		private String productCode;
		private String productBarCode;
		private String productName;
		private int productTypeId;
		private float productPrice;
		private String productUnitName;
		private String productDesc;
		private int discountAllow;
		private int vatType;
		private float vatRate;
		private int hasServiceCharge;
		private String picName;
		
		public int getProductGroupId() {
			return productGroupId;
		}
		public void setProductGroupId(int productGroupId) {
			this.productGroupId = productGroupId;
		}
		public int getProductDeptId() {
			return productDeptId;
		}
		public void setProductDeptId(int productDeptId) {
			this.productDeptId = productDeptId;
		}
		public int getProductId() {
			return productId;
		}
		public void setProductId(int productId) {
			this.productId = productId;
		}
		public String getProductCode() {
			return productCode;
		}
		public void setProductCode(String productCode) {
			this.productCode = productCode;
		}
		public String getProductBarCode() {
			return productBarCode;
		}
		public void setProductBarCode(String productBarCode) {
			this.productBarCode = productBarCode;
		}
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public int getProductTypeId() {
			return productTypeId;
		}
		public void setProductTypeId(int productTypeId) {
			this.productTypeId = productTypeId;
		}
		public float getProductPrice() {
			return productPrice;
		}
		public void setProductPrice(float productPrice) {
			this.productPrice = productPrice;
		}
		public String getProductUnitName() {
			return productUnitName;
		}
		public void setProductUnitName(String productUnitName) {
			this.productUnitName = productUnitName;
		}
		public String getProductDesc() {
			return productDesc;
		}
		public void setProductDesc(String productDesc) {
			this.productDesc = productDesc;
		}
		public int getDiscountAllow() {
			return discountAllow;
		}
		public void setDiscountAllow(int discountAllow) {
			this.discountAllow = discountAllow;
		}
		public int getVatType() {
			return vatType;
		}
		public void setVatType(int vatType) {
			this.vatType = vatType;
		}
		public float getVatRate() {
			return vatRate;
		}
		public void setVatRate(float vatRate) {
			this.vatRate = vatRate;
		}
		public int getHasServiceCharge() {
			return hasServiceCharge;
		}
		public void setHasServiceCharge(int hasServiceCharge) {
			this.hasServiceCharge = hasServiceCharge;
		}
		public String getPicName() {
			return picName;
		}
		public void setPicName(String picName) {
			this.picName = picName;
		}
	}
	
	public static class ProductDept{
		private int productDeptId;
		private int productGroupId;
		private String productDeptCode;
		private String productDeptName;
		
		public int getProductDeptId() {
			return productDeptId;
		}
		public void setProductDeptId(int productDeptId) {
			this.productDeptId = productDeptId;
		}
		public int getProductGroupId() {
			return productGroupId;
		}
		public void setProductGroupId(int productGroupId) {
			this.productGroupId = productGroupId;
		}
		public String getProductDeptCode() {
			return productDeptCode;
		}
		public void setProductDeptCode(String productDeptCode) {
			this.productDeptCode = productDeptCode;
		}
		public String getProductDeptName() {
			return productDeptName;
		}
		public void setProductDeptName(String productDeptName) {
			this.productDeptName = productDeptName;
		}
	}
	
	public static class ProductGroup{
		private int productGroupId;
		private String productGroupCode;
		private String productGroupName;
		
		public int getProductGroupId() {
			return productGroupId;
		}
		public void setProductGroupId(int productGroupId) {
			this.productGroupId = productGroupId;
		}
		public String getProductGroupCode() {
			return productGroupCode;
		}
		public void setProductGroupCode(String productGroupCode) {
			this.productGroupCode = productGroupCode;
		}
		public String getProductGroupName() {
			return productGroupName;
		}
		public void setProductGroupName(String productGroupName) {
			this.productGroupName = productGroupName;
		}
	}
}
