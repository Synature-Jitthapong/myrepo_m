package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import org.kobjects.util.Strings;

import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Products extends MPOSSQLiteHelper {
	public static final int VAT_TYPE_INCLUDED = 1;
	public static final int VAT_TYPE_EXCLUDE = 2;
	
	public static final String TB_PRODUCT = "Products";
	public static final String COL_PRODUCT_ID = "ProductId";
	public static final String COL_PRODUCT_DEPT_ID = "ProductDeptId";
	public static final String COL_PRODUCT_GROUP_ID = "ProductGroupId";
	public static final String COL_PRODUCT_CODE = "ProductCode";
	public static final String COL_PRODUCT_BAR_CODE = "ProductBarCode";
	public static final String COL_PRODUCT_NAME = "ProductName";
	public static final String COL_PRODUCT_DESC = "ProductDesc";
	public static final String COL_PRODUCT_TYPE_ID = "ProductTypeId";
	public static final String COL_PRODUCT_PRICE = "ProductPrice";
	public static final String COL_PRODUCT_UNIT_NAME = "ProductUnitName";
	public static final String COL_DISCOUNT_ALLOW = "DiscountAllow";
	public static final String COL_VAT_TYPE = "VatType";
	public static final String COL_VAT_RATE = "VatRate";
	public static final String COL_IS_OUTOF_STOCK = "IsOutOfStock";
	public static final String COL_IMG_URL = "ImageUrl";
	public static final String[] ALL_PRODUCT_COLS = {
		COL_PRODUCT_ID, 
		COL_PRODUCT_DEPT_ID,
		COL_PRODUCT_GROUP_ID,
		COL_PRODUCT_CODE,
		COL_PRODUCT_BAR_CODE,
		COL_PRODUCT_NAME,
		COL_PRODUCT_DESC,
		COL_PRODUCT_TYPE_ID,
		COL_PRODUCT_PRICE,
		COL_PRODUCT_UNIT_NAME,
		COL_DISCOUNT_ALLOW,
		COL_VAT_TYPE,
		COL_VAT_RATE,
		COL_IS_OUTOF_STOCK,
		COL_IMG_URL
	};
	
	public static final String TB_PRODUCT_DEPT = "ProductDept";
	public static final String COL_PRODUCT_DEPT_CODE = "ProductDeptCode";
	public static final String COL_PRODUCT_DEPT_NAME = "ProductDeptName";
	public static final String[] ALL_PRODUCT_DEPT_COLS = {
		COL_PRODUCT_GROUP_ID,
		COL_PRODUCT_DEPT_ID,
		COL_PRODUCT_DEPT_CODE,
		COL_PRODUCT_DEPT_NAME
	};
	
	public static final String TB_PRODUCT_GROUP = "ProductGroup";
	public static final String COL_PRODUCT_GROUP_CODE = "ProductGroupCode";
	public static final String COL_PRODUCT_GROUP_NAME = "ProductGroupName";
	public static final String COL_PRODUCT_GROUP_TYPE = "ProductGroupType";
	public static final String COL_IS_COMMENT = "IsComment";

	public Products(Context c){
		super(c);
	}
	
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + TB_PRODUCT_GROUP, null);
		if(cursor.moveToFirst()){
			do{
				ProductGroup pg = toProductGroup(cursor);
				pgLst.add(pg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return pgLst;
	}

	public List<ProductDept> listProductDept(){
		List<ProductDept> pdLst = new ArrayList<ProductDept>();
		
		open();
		Cursor cursor = mSqlite.query(TB_PRODUCT_DEPT, ALL_PRODUCT_DEPT_COLS,
				null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				ProductDept pd = toProductDept(cursor);
				pdLst.add(pd);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return pdLst;
	}

	public List<Product> listProduct(String query){
		List<Product> pLst = new ArrayList<Product>();
	
		open();
		Cursor cursor = mSqlite.query(TB_PRODUCT, ALL_PRODUCT_COLS, 
				"(" + COL_PRODUCT_CODE + " LIKE '%?%' " +
				" OR " + COL_PRODUCT_NAME + " LIKE '%?%')", 
				new String[]{}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return pLst;
	}
	
	public List<Product> listProduct(int deptId){
		List<Product> pLst = new ArrayList<Product>();
		
		open();
		Cursor cursor = mSqlite.query(TB_PRODUCT, ALL_PRODUCT_COLS, COL_PRODUCT_DEPT_ID + "=?", 
				new String[]{String.valueOf(deptId)}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return pLst;
	}

	public float getVatRate(int productId){
		float vatRate = 0.0f;
		open();
		Cursor cursor = getProductColumn(new String[]{COL_VAT_RATE}, COL_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			vatRate = cursor.getFloat(0);
		}
		cursor.close();
		close();
		return vatRate;
	}
	
	public Product getProduct(int proId){
		Product p = null;
		open();
		Cursor cursor = mSqlite.query(TB_PRODUCT, ALL_PRODUCT_COLS, 
				COL_PRODUCT_ID + "=?", new String[]{String.valueOf(proId)}, 
				null, null, null);
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		close();
		return p;
	}
	
	public ProductDept getProductDept(int deptId){
		ProductDept pd = null;
		
		open();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + TB_PRODUCT_DEPT +
				" WHERE " + COL_PRODUCT_DEPT_ID + "=" + deptId, null);
		if(cursor.moveToFirst()){
			pd = toProductDept(cursor);
		}
		cursor.close();
		close();
		return pd;
	}
	
	private Cursor getProductColumn(String[] columns, String selection, String[] selectionArgs){
		return mSqlite.query(TB_PRODUCT, columns, selection, selectionArgs, null, null, null);
	}

	private Product toProduct(Cursor cursor){
		Product p = new Product();
		p.setProductId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_ID)));
		p.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_GROUP_ID)));
		p.setProductDeptId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_DEPT_ID)));
		p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_TYPE_ID)));
		p.setProductCode(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_CODE)));
		p.setProductBarCode(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_BAR_CODE)));
		p.setProductName(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_NAME)));
		p.setProductDesc(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_DESC)));
		p.setProductUnitName(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_UNIT_NAME)));
		p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(COL_PRODUCT_PRICE)));
		p.setVatType(cursor.getInt(cursor.getColumnIndex(COL_VAT_TYPE)));
		p.setVatRate(cursor.getFloat(cursor.getColumnIndex(COL_VAT_RATE)));
		p.setDiscountAllow(cursor.getInt(cursor.getColumnIndex(COL_DISCOUNT_ALLOW)));
		p.setImgUrl(cursor.getString(cursor.getColumnIndex(COL_IMG_URL)));
		return p;
	}

	private ProductDept toProductDept(Cursor cursor){
		ProductDept pd = new ProductDept();
		pd.setProductDeptId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_DEPT_ID)));
		pd.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_GROUP_ID)));
		pd.setProductDeptCode(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_DEPT_CODE)));
		pd.setProductDeptName(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_DEPT_NAME)));
		return pd;
	}

	public ProductGroup toProductGroup(Cursor cursor){
		ProductGroup pg = new ProductGroup();
		pg.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_GROUP_ID)));
		pg.setProductGroupCode(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_GROUP_CODE)));
		pg.setProductGroupName(cursor.getString(cursor.getColumnIndex(COL_PRODUCT_GROUP_NAME)));
		return pg;
	}
	
	public void addProductGroup(List<ProductGroups.ProductGroup> pgLst,
			List<MenuGroups.MenuGroup> mgLst) throws SQLException{
		
		open();
		mSqlite.execSQL(
				" DELETE FROM " + TB_PRODUCT_GROUP);
		for(MenuGroups.MenuGroup mg : mgLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_PRODUCT_GROUP_ID, mg.getMenuGroupID());
			cv.put(COL_PRODUCT_GROUP_CODE, "x");
			cv.put(COL_PRODUCT_GROUP_NAME, mg.getMenuGroupName_0());
			cv.put(COL_PRODUCT_GROUP_TYPE, mg.getMenuGroupType());
			cv.put(COL_IS_COMMENT, 0);
			mSqlite.insertOrThrow(TB_PRODUCT_GROUP, null, cv);
		}
		close();
	}
	
	public void addProductDept(List<ProductGroups.ProductDept> pdLst, 
			List<MenuGroups.MenuDept> mdLst) throws SQLException{
		
		open();
		mSqlite.execSQL(
				" DELETE FROM " + TB_PRODUCT_DEPT);
		for(MenuGroups.MenuDept md : mdLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_PRODUCT_DEPT_ID, md.getMenuDeptID());
			cv.put(COL_PRODUCT_GROUP_ID, md.getMenuGroupID());
			cv.put(COL_PRODUCT_DEPT_CODE, "");
			cv.put(COL_PRODUCT_DEPT_NAME, md.getMenuDeptName_0());
			mSqlite.insertOrThrow(TB_PRODUCT_DEPT, null, cv);
		}
		close();
	}
	
	public void addProducts(List<ProductGroups.Products> productLst,
			List<MenuGroups.MenuItem> menuItemLst) throws SQLException{
		
		open();
		mSqlite.execSQL(
				" DELETE FROM " + Products.TB_PRODUCT);
		for(ProductGroups.Products p : productLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_PRODUCT_ID, p.getProductID());
			cv.put(COL_PRODUCT_DEPT_ID, p.getProductDeptID());
			cv.put(COL_PRODUCT_GROUP_ID, p.getProductGroupID());
			cv.put(COL_PRODUCT_CODE, p.getProductCode());
			cv.put(COL_PRODUCT_BAR_CODE, p.getProductBarCode());
			cv.put(COL_PRODUCT_TYPE_ID, p.getProductTypeID());
			cv.put(COL_PRODUCT_PRICE, p.getProductPricePerUnit());
			cv.put(COL_PRODUCT_UNIT_NAME, p.getProductUnitName());
			cv.put(COL_PRODUCT_DESC, p.getProductDesc());
			cv.put(COL_DISCOUNT_ALLOW, p.getDiscountAllow());
			cv.put(COL_VAT_TYPE, p.getVatType());
			cv.put(COL_VAT_RATE, p.getVatRate());
			cv.put(COL_IS_OUTOF_STOCK, p.getIsOutOfStock());
			
			mSqlite.insertOrThrow(TB_PRODUCT, null, cv);
		}
		for(MenuGroups.MenuItem m : menuItemLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_PRODUCT_NAME, m.getMenuDesc_0());
			cv.put(COL_IMG_URL, m.getMenuImageLink());
			
			mSqlite.update(TB_PRODUCT, cv, COL_PRODUCT_ID + "=?", 
					new String[]{String.valueOf(m.getProductID())});
		}
		close();
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
		private String imgUrl;
		
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
		public String getImgUrl() {
			return imgUrl;
		}
		public void setImgUrl(String imgUrl) {
			this.imgUrl = imgUrl;
		}
		@Override
		public String toString() {
			return productName;
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
		
		@Override
		public String toString() {
			return productDeptName;
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
