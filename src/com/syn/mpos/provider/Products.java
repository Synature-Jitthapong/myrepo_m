package com.syn.mpos.provider;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.MPOSApplication;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class Products extends MPOSDatabase {
	public static final int NORMAL_TYPE = 0;
	public static final int SET_TYPE = 1;
	public static final int SIZE_TYPE = 2;
	public static final int OPEN_PRICE_TYPE = 5;
	public static final int SET_TYPE_CAN_SELECT = 7;
	
	public static final int VAT_TYPE_INCLUDED = 1;
	public static final int VAT_TYPE_EXCLUDE = 2;
    
	public static final String TABLE_PRODUCT = "Products";
	public static final String COLUMN_PRODUCT_ID = "product_id";
	public static final String COLUMN_PRODUCT_DEPT_ID = "product_dept_id";
	public static final String COLUMN_PRODUCT_GROUP_ID = "product_group_id";
	public static final String COLUMN_PRODUCT_CODE = "product_code";
	public static final String COLUMN_PRODUCT_BAR_CODE = "product_barcode";
	public static final String COLUMN_PRODUCT_NAME = "product_name";
	public static final String COLUMN_PRODUCT_DESC = "product_desc";
	public static final String COLUMN_PRODUCT_TYPE_ID = "product_type_id";
	public static final String COLUMN_PRODUCT_PRICE = "product_price";
	public static final String COLUMN_PRODUCT_UNIT_NAME = "product_unitname";
	public static final String COLUMN_DISCOUNT_ALLOW = "discount_allow";
	public static final String COLUMN_VAT_TYPE = "vat_type";
	public static final String COLUMN_VAT_RATE = "vat_rate";
	public static final String COLUMN_ISOUTOF_STOCK = "isoutof_stock";
	public static final String COLUMN_IMG_URL = "image_url";
	public static final String COLUMN_ACTIVATE = "activate";
	public static final String COLUMN_ORDERING = "ordering";
	public static final String[] ALL_PRODUCT_COLS = {
		COLUMN_PRODUCT_ID, 
		COLUMN_PRODUCT_DEPT_ID,
		COLUMN_PRODUCT_GROUP_ID,
		COLUMN_PRODUCT_CODE,
		COLUMN_PRODUCT_BAR_CODE,
		COLUMN_PRODUCT_NAME,
		COLUMN_PRODUCT_DESC,
		COLUMN_PRODUCT_TYPE_ID,
		COLUMN_PRODUCT_PRICE,
		COLUMN_PRODUCT_UNIT_NAME,
		COLUMN_DISCOUNT_ALLOW,
		COLUMN_VAT_TYPE,
		COLUMN_VAT_RATE,
		COLUMN_ISOUTOF_STOCK,
		COLUMN_IMG_URL
	};
	public static final String TABLE_PRODUCT_DEPT = "ProductDept";
	public static final String COLUMN_PRODUCT_DEPT_CODE = "product_dept_code";
	public static final String COLUMN_PRODUCT_DEPT_NAME = "product_dept_name";
	public static final String[] ALL_PRODUCT_DEPT_COLS = {
		COLUMN_PRODUCT_GROUP_ID,
		COLUMN_PRODUCT_DEPT_ID,
		COLUMN_PRODUCT_DEPT_CODE,
		COLUMN_PRODUCT_DEPT_NAME
	};
	public static final String TABLE_PRODUCT_GROUP = "ProductGroup";
	public static final String COLUMN_PRODUCT_GROUP_CODE = "product_group_code";
	public static final String COLUMN_PRODUCT_GROUP_NAME = "product_group_name";
	public static final String COLUMN_PRODUCT_GROUP_TYPE = "product_group_type";
	public static final String COLUMN_IS_COMMENT = "is_comment";
	
	public static final String TABLE_PCOMP_SET = "PComponentSet";
	public static final String COLUMN_PGROUP_ID = "pgroup_id";
	public static final String COLUMN_CHILD_PRODUCT_ID = "child_product_id";
	public static final String COLUMN_CHILD_PRODUCT_AMOUNT = "child_product_amount";
	public static final String COLUMN_FLEXIBLE_PRODUCT_PRICE = "flexible_product_price";
	public static final String COLUMN_FLEXIBLE_INCLUDE_PRICE = "flexible_include_price";

	public Products(SQLiteDatabase db){
		super(db);
	}
	
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + TABLE_PRODUCT_GROUP, null);
		if(cursor.moveToFirst()){
			do{
				ProductGroup pg = toProductGroup(cursor);
				pgLst.add(pg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pgLst;
	}

	public List<ProductDept> listProductDept(){
		List<ProductDept> pdLst = new ArrayList<ProductDept>();
		Cursor cursor = mSqlite.query(TABLE_PRODUCT_DEPT, 
				ALL_PRODUCT_DEPT_COLS,
				COLUMN_ACTIVATE + "=?", new String[]{"1"}, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				ProductDept pd = toProductDept(cursor);
				pdLst.add(pd);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pdLst;
	}
	
	public List<Product> listProductSize(int proId){
		List<Product> pLst = null;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT b." + COLUMN_PRODUCT_ID + ", " 
				+ " b." + COLUMN_PRODUCT_TYPE_ID + ", "
				+ " b." + COLUMN_PRODUCT_CODE + ", " 
				+ " b." + COLUMN_PRODUCT_BAR_CODE + ", "
				+ " b." + COLUMN_PRODUCT_NAME + ", "
				+ " b." + COLUMN_PRODUCT_PRICE + ", " 
				+ " b." + COLUMN_VAT_TYPE + ", " 
				+ " b." + COLUMN_VAT_RATE
				+ " FROM " + TABLE_PCOMP_SET + " a "
				+ " INNER JOIN " + TABLE_PRODUCT + " b " 
				+ " ON a." + COLUMN_CHILD_PRODUCT_ID + "=b." + COLUMN_PRODUCT_ID
				+ " WHERE a." + COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(proId)});
		if(cursor.moveToFirst()){
			pLst = new ArrayList<Product>();
			do{
				Product p = new Product();
				p.setProductId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
				p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_TYPE_ID)));
				p.setProductCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_CODE)));
				p.setProductBarCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_BAR_CODE)));
				p.setProductName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)));
				p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(COLUMN_PRODUCT_PRICE)));
				p.setVatType(cursor.getInt(cursor.getColumnIndex(COLUMN_VAT_TYPE)));
				p.setVatRate(cursor.getFloat(cursor.getColumnIndex(COLUMN_VAT_RATE)));
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	public List<Product> listProduct(String query){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = mSqlite.query(TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				"(" + COLUMN_PRODUCT_CODE + " LIKE '%" + query + "%' " +
				" OR " + COLUMN_PRODUCT_NAME + " LIKE '%" + query + "%')", 
				null, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	public List<Product> listProduct(int deptId){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = mSqlite.query(TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				COLUMN_PRODUCT_DEPT_ID + "=? " +
						" AND " + COLUMN_ACTIVATE + "=?", 
				new String[]{String.valueOf(deptId), "1"}, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}

	public double getVatRate(int productId){
		double vatRate = 0.0f;
		Cursor cursor = queryProduct(new String[]{COLUMN_VAT_RATE}, COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			vatRate = cursor.getFloat(0);
		}
		cursor.close();
		return vatRate;
	}
	
	public Product getProduct(int proId){
		Product p = null;
		Cursor cursor = mSqlite.query(TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(proId)}, 
				null, null, null);
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		return p;
	}
	
	public ProductDept getProductDept(int deptId){
		ProductDept pd = null;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + TABLE_PRODUCT_DEPT +
				" WHERE " + COLUMN_PRODUCT_DEPT_ID + "=" + deptId, null);
		if(cursor.moveToFirst()){
			pd = toProductDept(cursor);
		}
		cursor.close();
		return pd;
	}
	
	public Cursor queryProduct(String[] columns, String selection, String[] selectionArgs){
		return mSqlite.query(TABLE_PRODUCT, columns, selection, selectionArgs, null, null, null);
	}
	
	private Product toProduct(Cursor cursor){
		Product p = new Product();
		p.setProductId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
		p.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_GROUP_ID)));
		p.setProductDeptId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_DEPT_ID)));
		p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_TYPE_ID)));
		p.setProductCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_CODE)));
		p.setProductBarCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_BAR_CODE)));
		p.setProductName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)));
		p.setProductDesc(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_DESC)));
		p.setProductUnitName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_UNIT_NAME)));
		p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(COLUMN_PRODUCT_PRICE)));
		p.setVatType(cursor.getInt(cursor.getColumnIndex(COLUMN_VAT_TYPE)));
		p.setVatRate(cursor.getFloat(cursor.getColumnIndex(COLUMN_VAT_RATE)));
		p.setDiscountAllow(cursor.getInt(cursor.getColumnIndex(COLUMN_DISCOUNT_ALLOW)));
		p.setImgUrl(cursor.getString(cursor.getColumnIndex(COLUMN_IMG_URL)));
		return p;
	}

	private ProductDept toProductDept(Cursor cursor){
		ProductDept pd = new ProductDept();
		pd.setProductDeptId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_DEPT_ID)));
		pd.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_GROUP_ID)));
		pd.setProductDeptCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_DEPT_CODE)));
		pd.setProductDeptName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_DEPT_NAME)));
		return pd;
	}

	public ProductGroup toProductGroup(Cursor cursor){
		ProductGroup pg = new ProductGroup();
		pg.setProductGroupId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_GROUP_ID)));
		pg.setProductGroupCode(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_GROUP_CODE)));
		pg.setProductGroupName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_GROUP_NAME)));
		return pg;
	}
	
	public void addPComponentSet(List<ProductGroups.PComponentSet> pCompSetLst) throws SQLException{
		mSqlite.delete(TABLE_PCOMP_SET, null, null);
		for(ProductGroups.PComponentSet pCompSet : pCompSetLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PGROUP_ID, pCompSet.getPGroupID());
			cv.put(COLUMN_PRODUCT_ID, pCompSet.getProductID());
			cv.put(COLUMN_CHILD_PRODUCT_ID, pCompSet.getChildProductID());
			cv.put(COLUMN_CHILD_PRODUCT_AMOUNT, pCompSet.getPGroupID());
			cv.put(COLUMN_FLEXIBLE_PRODUCT_PRICE, pCompSet.getFlexibleProductPrice());
			cv.put(COLUMN_FLEXIBLE_INCLUDE_PRICE, pCompSet.getFlexibleIncludePrice());
			mSqlite.insertOrThrow(TABLE_PCOMP_SET, null, cv);
		}
	}
	
	public void addProductGroup(List<ProductGroups.ProductGroup> pgLst,
			List<MenuGroups.MenuGroup> mgLst) throws SQLException{
		mSqlite.execSQL(
				" DELETE FROM " + TABLE_PRODUCT_GROUP);
		for(ProductGroups.ProductGroup pg : pgLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PRODUCT_GROUP_ID, pg.getProductGroupId());
			cv.put(COLUMN_PRODUCT_GROUP_CODE, pg.getProductGroupCode());
			cv.put(COLUMN_PRODUCT_GROUP_NAME, pg.getProductGroupName());
			cv.put(COLUMN_PRODUCT_GROUP_TYPE, pg.getProductGroupType());
			cv.put(COLUMN_IS_COMMENT, pg.getIsComment());
			cv.put(COLUMN_ORDERING, pg.getProductGroupOrdering());
			cv.put(COLUMN_ACTIVATE, 0);
			mSqlite.insertOrThrow(TABLE_PRODUCT_GROUP, null, cv);
		}
		
		for(MenuGroups.MenuGroup mg : mgLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PRODUCT_GROUP_NAME, mg.getMenuGroupName_0());
			cv.put(COLUMN_ORDERING, mg.getMenuGroupOrdering());
			cv.put(COLUMN_ACTIVATE, mg.getActivate());
			mSqlite.update(TABLE_PRODUCT_GROUP, cv, 
					COLUMN_PRODUCT_GROUP_ID + "=?", 
					new String[]{
						String.valueOf(mg.getMenuGroupID())
					});
		}
	}
	
	public void addProductDept(List<ProductGroups.ProductDept> pdLst, 
			List<MenuGroups.MenuDept> mdLst) throws SQLException{
		mSqlite.execSQL(
				" DELETE FROM " + TABLE_PRODUCT_DEPT);
		for(MenuGroups.MenuDept md : mdLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PRODUCT_DEPT_ID, md.getMenuDeptID());
			cv.put(COLUMN_PRODUCT_GROUP_ID, md.getMenuGroupID());
			cv.put(COLUMN_PRODUCT_DEPT_CODE, "");
			cv.put(COLUMN_PRODUCT_DEPT_NAME, md.getMenuDeptName_0());
			cv.put(COLUMN_ORDERING, md.getMenuDeptOrdering());
			cv.put(COLUMN_ACTIVATE, md.getActivate());
			mSqlite.insertOrThrow(TABLE_PRODUCT_DEPT, null, cv);
		}
	}
	
	public void addProducts(List<ProductGroups.Products> productLst,
			List<MenuGroups.MenuItem> menuItemLst) throws SQLException{
		mSqlite.execSQL(
				" DELETE FROM " + Products.TABLE_PRODUCT);
		for(ProductGroups.Products p : productLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PRODUCT_ID, p.getProductID());
			cv.put(COLUMN_PRODUCT_DEPT_ID, p.getProductDeptID());
			cv.put(COLUMN_PRODUCT_GROUP_ID, p.getProductGroupID());
			cv.put(COLUMN_PRODUCT_CODE, p.getProductCode());
			cv.put(COLUMN_PRODUCT_BAR_CODE, p.getProductBarCode());
			cv.put(COLUMN_PRODUCT_TYPE_ID, p.getProductTypeID());
			cv.put(COLUMN_PRODUCT_PRICE, p.getProductPricePerUnit());
			cv.put(COLUMN_PRODUCT_UNIT_NAME, p.getProductUnitName());
			cv.put(COLUMN_PRODUCT_DESC, p.getProductDesc());
			cv.put(COLUMN_DISCOUNT_ALLOW, p.getDiscountAllow());
			cv.put(COLUMN_VAT_TYPE, p.getVatType());
			cv.put(COLUMN_VAT_RATE, p.getVatRate());
			cv.put(COLUMN_ISOUTOF_STOCK, p.getIsOutOfStock());
			
			mSqlite.insertOrThrow(TABLE_PRODUCT, null, cv);
		}
		for(MenuGroups.MenuItem m : menuItemLst){
			try {
				ContentValues cv = new ContentValues();
				cv.put(COLUMN_PRODUCT_NAME, m.getMenuName_0());
				cv.put(COLUMN_IMG_URL, m.getMenuImageLink());
				cv.put(COLUMN_ORDERING, m.getMenuItemOrdering());
				cv.put(COLUMN_ACTIVATE, m.getMenuActivate());
				
				mSqlite.update(TABLE_PRODUCT, cv, COLUMN_PRODUCT_ID + "=?", 
						new String[]{String.valueOf(m.getProductID())});
			} catch (Exception e) {
				Toast toast = Toast.makeText(MPOSApplication.getContext(), e.getMessage(), Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}
	
	public static class Product{
		private int productId;
		private int productGroupId;
		private int productDeptId;
		private String productCode;
		private String productBarCode;
		private String productName;
		private int productTypeId;
		private double productPrice;
		private String productUnitName;
		private String productDesc;
		private int discountAllow;
		private int vatType;
		private double vatRate;
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
		public double getProductPrice() {
			return productPrice;
		}
		public void setProductPrice(double productPrice) {
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
		public double getVatRate() {
			return vatRate;
		}
		public void setVatRate(double vatRate) {
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
