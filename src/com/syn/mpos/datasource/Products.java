package com.syn.mpos.datasource;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Products extends MPOSDatabase {
	public static final int NORMAL_TYPE = 0;
	public static final int SET_TYPE = 1;
	public static final int SIZE_TYPE = 2;
	public static final int OPEN_PRICE_TYPE = 5;
	public static final int SET_TYPE_CAN_SELECT = 7;
	public static final int VAT_TYPE_INCLUDED = 1;
	public static final int VAT_TYPE_EXCLUDE = 2;
	public static final String[] ALL_PRODUCT_COLS = {
		ProductEntry.COLUMN_PRODUCT_ID, 
		ProductEntry.COLUMN_PRODUCT_DEPT_ID,
		ProductEntry.COLUMN_PRODUCT_CODE,
		ProductEntry.COLUMN_PRODUCT_BAR_CODE,
		ProductEntry.COLUMN_PRODUCT_NAME,
		ProductEntry.COLUMN_PRODUCT_DESC,
		ProductEntry.COLUMN_PRODUCT_TYPE_ID,
		ProductEntry.COLUMN_PRODUCT_PRICE,
		ProductEntry.COLUMN_PRODUCT_UNIT_NAME,
		ProductEntry.COLUMN_DISCOUNT_ALLOW,
		ProductEntry.COLUMN_VAT_TYPE,
		ProductEntry.COLUMN_VAT_RATE,
		ProductEntry.COLUMN_ISOUTOF_STOCK,
		ProductEntry.COLUMN_IMG_URL
	};
	public static final String[] ALL_PRODUCT_DEPT_COLS = {
		ProductEntry.COLUMN_PRODUCT_GROUP_ID,
		ProductEntry.COLUMN_PRODUCT_DEPT_ID,
		ProductDeptEntry.COLUMN_PRODUCT_DEPT_CODE,
		ProductDeptEntry.COLUMN_PRODUCT_DEPT_NAME
	};

	public Products(SQLiteDatabase db){
		super(db);
	}
	
	public List<ProductComponentGroup> listProductComponentGroup(int productId){
		List<ProductComponentGroup> pCompGroupLst = null;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT a." + ProductEntry.COLUMN_PRODUCT_ID
				+ ",a." + ProductComponentEntry.COLUMN_PGROUP_ID
				+ ",a." + ProductComponentGroupEntry.COLUMN_SET_GROUP_NO
				+ ",a." + ProductComponentGroupEntry.COLUMN_SET_GROUP_NAME
				+ ",a." + ProductComponentGroupEntry.COLUMN_REQ_AMOUNT
				+ ",a." + ProductEntry.COLUMN_SALE_MODE
				+ ",b." + ProductEntry.COLUMN_PRODUCT_NAME
				+ ",b." + ProductEntry.COLUMN_IMG_URL
				+ " FROM " + ProductComponentGroupEntry.TABLE_PCOMP_GROUP + " a "
				+ " LEFT JOIN " + ProductEntry.TABLE_PRODUCT + " b "
				+ " ON a." + ProductEntry.COLUMN_PRODUCT_ID + "=b." + ProductEntry.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductEntry.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			pCompGroupLst = new ArrayList<ProductComponentGroup>();
			do{
				ProductComponentGroup pCompGroup = new ProductComponentGroup();
				pCompGroup.setProductId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ID)));
				pCompGroup.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_PGROUP_ID)));
				pCompGroup.setGroupNo(cursor.getInt(cursor.getColumnIndex(ProductComponentGroupEntry.COLUMN_SET_GROUP_NO)));
				pCompGroup.setGroupName(cursor.getString(cursor.getColumnIndex(ProductComponentGroupEntry.COLUMN_SET_GROUP_NAME)));
				pCompGroup.setRequireAmount(cursor.getDouble(cursor.getColumnIndex(ProductComponentGroupEntry.COLUMN_REQ_AMOUNT)));
				pCompGroup.setSaleMode(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_SALE_MODE)));
				pCompGroup.setProductName(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
				pCompGroup.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMG_URL)));
				pCompGroupLst.add(pCompGroup);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompGroupLst;
	}
	
	public List<ProductComponent> listProductComponent(int groupId){
		List<ProductComponent> pCompLst = null;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT a." + ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID
				+ ",a." + ProductComponentEntry.COLUMN_PGROUP_ID
				+ ",a." + ProductComponentEntry.COLUMN_CHILD_PRODUCT_AMOUNT
				+ ",a." + ProductComponentEntry.COLUMN_FLEXIBLE_INCLUDE_PRICE
				+ ",a." + ProductComponentEntry.COLUMN_FLEXIBLE_PRODUCT_PRICE
				+ ",a." + ProductEntry.COLUMN_SALE_MODE
				+ ",b." + ProductEntry.COLUMN_PRODUCT_NAME
				+ ",b." + ProductEntry.COLUMN_PRODUCT_PRICE
				+ ",b." + ProductEntry.COLUMN_IMG_URL
				+ " FROM " + ProductComponentEntry.TABLE_PCOMP + " a "
				+ " LEFT JOIN " + ProductEntry.TABLE_PRODUCT + " b "
				+ " ON a." + ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID + "=b." + ProductEntry.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductComponentEntry.COLUMN_PGROUP_ID + "=?", 
				new String[]{String.valueOf(groupId)});
		if(cursor.moveToFirst()){
			pCompLst = new ArrayList<ProductComponent>();
			do{
				ProductComponent pComp = new ProductComponent();
				pComp.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_PGROUP_ID)));
				pComp.setProductId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ID)));
				pComp.setSaleMode(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_SALE_MODE)));
				pComp.setChildProductId(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID)));
				pComp.setChildProductAmount(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_CHILD_PRODUCT_AMOUNT)));
				pComp.setFlexibleIncludePrice(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_FLEXIBLE_INCLUDE_PRICE)));
				pComp.setFlexibleProductPrice(cursor.getInt(cursor.getColumnIndex(ProductComponentEntry.COLUMN_FLEXIBLE_PRODUCT_PRICE)));
				pComp.setProductName(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
				pComp.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMG_URL)));
				pCompLst.add(pComp);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompLst;
	}
	
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + ProductGroupEntry.TABLE_PRODUCT_GROUP, null);
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
		Cursor cursor = mSqlite.query(ProductDeptEntry.TABLE_PRODUCT_DEPT, 
				ALL_PRODUCT_DEPT_COLS,
				ProductEntry.COLUMN_ACTIVATE + "=?", new String[]{"1"}, null, null, ProductEntry.COLUMN_ORDERING);
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
				"SELECT b." + ProductEntry.COLUMN_PRODUCT_ID + ", " 
				+ " b." + ProductEntry.COLUMN_PRODUCT_TYPE_ID + ", "
				+ " b." + ProductEntry.COLUMN_PRODUCT_CODE + ", " 
				+ " b." + ProductEntry.COLUMN_PRODUCT_BAR_CODE + ", "
				+ " b." + ProductEntry.COLUMN_PRODUCT_NAME + ", "
				+ " b." + ProductEntry.COLUMN_PRODUCT_PRICE + ", " 
				+ " b." + ProductEntry.COLUMN_VAT_TYPE + ", " 
				+ " b." + ProductEntry.COLUMN_VAT_RATE
				+ " FROM " + ProductComponentEntry.TABLE_PCOMP + " a "
				+ " INNER JOIN " + ProductEntry.TABLE_PRODUCT + " b " 
				+ " ON a." + ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID + "=b." + ProductEntry.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductEntry.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(proId)});
		if(cursor.moveToFirst()){
			pLst = new ArrayList<Product>();
			do{
				Product p = new Product();
				p.setProductId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ID)));
				p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_TYPE_ID)));
				p.setProductCode(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CODE)));
				p.setProductBarCode(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_BAR_CODE)));
				p.setProductName(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
				p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)));
				p.setVatType(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_VAT_TYPE)));
				p.setVatRate(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_VAT_RATE)));
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	public List<Product> listProduct(String query){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = mSqlite.query(ProductEntry.TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				"(" + ProductEntry.COLUMN_PRODUCT_CODE + " LIKE '%" + query + "%' " +
				" OR " + ProductEntry.COLUMN_PRODUCT_NAME + " LIKE '%" + query + "%')", 
				null, null, null, ProductEntry.COLUMN_ORDERING);
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
		Cursor cursor = mSqlite.query(ProductEntry.TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				ProductEntry.COLUMN_PRODUCT_DEPT_ID + "=? " +
						" AND " + ProductEntry.COLUMN_ACTIVATE + "=?", 
				new String[]{String.valueOf(deptId), "1"}, null, null, ProductEntry.COLUMN_ORDERING);
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
		Cursor cursor = queryProduct(new String[]{ProductEntry.COLUMN_VAT_RATE}, ProductEntry.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			vatRate = cursor.getFloat(0);
		}
		cursor.close();
		return vatRate;
	}
	
	public Product getProduct(String barCode){
		Product p = null;
		Cursor cursor = mSqlite.query(ProductEntry.TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				ProductEntry.COLUMN_PRODUCT_BAR_CODE + "=?", new String[]{barCode}, 
				null, null, "1");
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		return p;
	}
	
	public Product getProduct(int proId){
		Product p = null;
		Cursor cursor = mSqlite.query(ProductEntry.TABLE_PRODUCT, ALL_PRODUCT_COLS, 
				ProductEntry.COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(proId)}, 
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
				" SELECT * FROM " + ProductDeptEntry.TABLE_PRODUCT_DEPT +
				" WHERE " + ProductEntry.COLUMN_PRODUCT_DEPT_ID + "=" + deptId, null);
		if(cursor.moveToFirst()){
			pd = toProductDept(cursor);
		}
		cursor.close();
		return pd;
	}
	
	public Cursor queryProduct(String[] columns, String selection, String[] selectionArgs){
		return mSqlite.query(ProductEntry.TABLE_PRODUCT, columns, selection, selectionArgs, null, null, null);
	}
	
	private Product toProduct(Cursor cursor){
		Product p = new Product();
		p.setProductId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ID)));
		p.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DEPT_ID)));
		p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_TYPE_ID)));
		p.setProductCode(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CODE)));
		p.setProductBarCode(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_BAR_CODE)));
		p.setProductName(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
		p.setProductDesc(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESC)));
		p.setProductUnitName(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_UNIT_NAME)));
		p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)));
		p.setVatType(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_VAT_TYPE)));
		p.setVatRate(cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_VAT_RATE)));
		p.setDiscountAllow(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_DISCOUNT_ALLOW)));
		p.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMG_URL)));
		return p;
	}

	private ProductDept toProductDept(Cursor cursor){
		ProductDept pd = new ProductDept();
		pd.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DEPT_ID)));
		pd.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_GROUP_ID)));
		pd.setProductDeptCode(cursor.getString(cursor.getColumnIndex(ProductDeptEntry.COLUMN_PRODUCT_DEPT_CODE)));
		pd.setProductDeptName(cursor.getString(cursor.getColumnIndex(ProductDeptEntry.COLUMN_PRODUCT_DEPT_NAME)));
		return pd;
	}

	public ProductGroup toProductGroup(Cursor cursor){
		ProductGroup pg = new ProductGroup();
		pg.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_GROUP_ID)));
		pg.setProductGroupCode(cursor.getString(cursor.getColumnIndex(ProductGroupEntry.COLUMN_PRODUCT_GROUP_CODE)));
		pg.setProductGroupName(cursor.getString(cursor.getColumnIndex(ProductGroupEntry.COLUMN_PRODUCT_GROUP_NAME)));
		return pg;
	}
	
	public void insertPComponentGroup(List<ProductGroups.PComponentGroup> pCompGroupLst) throws SQLException{
		mSqlite.delete(ProductComponentGroupEntry.TABLE_PCOMP_GROUP, null, null);
		for(ProductGroups.PComponentGroup pCompGroup : pCompGroupLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductComponentEntry.COLUMN_PGROUP_ID, pCompGroup.getPGroupID());
			cv.put(ProductEntry.COLUMN_PRODUCT_ID, pCompGroup.getProductID());
			cv.put(ProductEntry.COLUMN_SALE_MODE, pCompGroup.getSaleMode());
			cv.put(ProductComponentGroupEntry.COLUMN_SET_GROUP_NO, pCompGroup.getSetGroupNo());
			cv.put(ProductComponentGroupEntry.COLUMN_SET_GROUP_NAME, pCompGroup.getSetGroupName());
			cv.put(ProductComponentGroupEntry.COLUMN_REQ_AMOUNT, pCompGroup.getRequireAmount());
			mSqlite.insertOrThrow(ProductComponentGroupEntry.TABLE_PCOMP_GROUP, null, cv);
		}
	}
	
	public void insertProductComponent(List<ProductGroups.ProductComponent> pCompLst) throws SQLException{
		mSqlite.delete(ProductComponentEntry.TABLE_PCOMP, null, null);
		for(ProductGroups.ProductComponent pCompSet : pCompLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductComponentEntry.COLUMN_PGROUP_ID, pCompSet.getPGroupID());
			cv.put(ProductEntry.COLUMN_PRODUCT_ID, pCompSet.getProductID());
			cv.put(ProductEntry.COLUMN_SALE_MODE, pCompSet.getSaleMode());
			cv.put(ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID, pCompSet.getChildProductID());
			cv.put(ProductComponentEntry.COLUMN_CHILD_PRODUCT_AMOUNT, pCompSet.getPGroupID());
			cv.put(ProductComponentEntry.COLUMN_FLEXIBLE_PRODUCT_PRICE, pCompSet.getFlexibleProductPrice());
			cv.put(ProductComponentEntry.COLUMN_FLEXIBLE_INCLUDE_PRICE, pCompSet.getFlexibleIncludePrice());
			mSqlite.insertOrThrow(ProductComponentEntry.TABLE_PCOMP, null, cv);
		}
	}
	
	public void insertProductGroup(List<ProductGroups.ProductGroup> pgLst,
			List<MenuGroups.MenuGroup> mgLst) throws SQLException{
		mSqlite.delete(ProductGroupEntry.TABLE_PRODUCT_GROUP, null, null);
		for(ProductGroups.ProductGroup pg : pgLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_PRODUCT_GROUP_ID, pg.getProductGroupId());
			cv.put(ProductGroupEntry.COLUMN_PRODUCT_GROUP_CODE, pg.getProductGroupCode());
			cv.put(ProductGroupEntry.COLUMN_PRODUCT_GROUP_NAME, pg.getProductGroupName());
			cv.put(ProductGroupEntry.COLUMN_PRODUCT_GROUP_TYPE, pg.getProductGroupType());
			cv.put(ProductGroupEntry.COLUMN_IS_COMMENT, pg.getIsComment());
			cv.put(ProductEntry.COLUMN_ORDERING, pg.getProductGroupOrdering());
			cv.put(ProductEntry.COLUMN_ACTIVATE, 0);
			mSqlite.insertOrThrow(ProductGroupEntry.TABLE_PRODUCT_GROUP, null, cv);
		}
		
		for(MenuGroups.MenuGroup mg : mgLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_ACTIVATE, mg.getActivate());
			mSqlite.update(ProductGroupEntry.TABLE_PRODUCT_GROUP, cv, 
					ProductEntry.COLUMN_PRODUCT_GROUP_ID + "=?", 
					new String[]{
						String.valueOf(mg.getMenuGroupID())
					});
		}
	}
	
	public void insertProductDept(List<ProductGroups.ProductDept> pdLst,
			List<MenuGroups.MenuDept> mdLst) throws SQLException{
		mSqlite.delete(ProductDeptEntry.TABLE_PRODUCT_DEPT, null, null);
		for(ProductGroups.ProductDept pd : pdLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_PRODUCT_DEPT_ID, pd.getProductDeptID());
			cv.put(ProductEntry.COLUMN_PRODUCT_GROUP_ID, pd.getProductGroupID());
			cv.put(ProductDeptEntry.COLUMN_PRODUCT_DEPT_CODE, pd.getProductDeptCode());
			cv.put(ProductDeptEntry.COLUMN_PRODUCT_DEPT_NAME, pd.getProductDeptName());
			cv.put(ProductEntry.COLUMN_ORDERING, pd.getProductDeptOrdering());
			cv.put(ProductEntry.COLUMN_ACTIVATE, 0);
			mSqlite.insertOrThrow(ProductDeptEntry.TABLE_PRODUCT_DEPT, null, cv);
		}
		
		for(MenuGroups.MenuDept md : mdLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_ACTIVATE, md.getActivate());
			mSqlite.update(ProductDeptEntry.TABLE_PRODUCT_DEPT, cv, 
					ProductEntry.COLUMN_PRODUCT_DEPT_ID + "=?",
					new String[]{
						String.valueOf(md.getMenuDeptID())
					}
			);
		}
	}
	
	public void insertProducts(List<ProductGroups.Products> productLst,
			List<MenuGroups.MenuItem> menuItemLst) throws SQLException{
		mSqlite.delete(ProductEntry.TABLE_PRODUCT, null, null);
		for(ProductGroups.Products p : productLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_PRODUCT_ID, p.getProductID());
			cv.put(ProductEntry.COLUMN_PRODUCT_DEPT_ID, p.getProductDeptID());
			cv.put(ProductEntry.COLUMN_PRODUCT_CODE, p.getProductCode());
			cv.put(ProductEntry.COLUMN_PRODUCT_BAR_CODE, p.getProductBarCode());
			cv.put(ProductEntry.COLUMN_PRODUCT_TYPE_ID, p.getProductTypeID());
			cv.put(ProductEntry.COLUMN_PRODUCT_PRICE, p.getProductPricePerUnit());
			cv.put(ProductEntry.COLUMN_PRODUCT_UNIT_NAME, p.getProductUnitName());
			cv.put(ProductEntry.COLUMN_PRODUCT_DESC, p.getProductDesc());
			cv.put(ProductEntry.COLUMN_DISCOUNT_ALLOW, p.getDiscountAllow());
			cv.put(ProductEntry.COLUMN_VAT_TYPE, p.getVatType());
			cv.put(ProductEntry.COLUMN_VAT_RATE, p.getVatRate());
			cv.put(ProductEntry.COLUMN_ISOUTOF_STOCK, p.getIsOutOfStock());
			mSqlite.insertOrThrow(ProductEntry.TABLE_PRODUCT, null, cv);
		}
		
		for(MenuGroups.MenuItem m : menuItemLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductEntry.COLUMN_PRODUCT_NAME, m.getMenuName_0());
			cv.put(ProductEntry.COLUMN_IMG_URL, m.getMenuImageLink());
			cv.put(ProductEntry.COLUMN_ORDERING, m.getMenuItemOrdering());
			cv.put(ProductEntry.COLUMN_ACTIVATE, m.getMenuActivate());
			mSqlite.update(ProductEntry.TABLE_PRODUCT, cv, ProductEntry.COLUMN_PRODUCT_ID + "=?", 
					new String[]{String.valueOf(m.getProductID())});
		}
	}
	
	public static abstract class ProductComponentEntry{
		public static final String TABLE_PCOMP = "ProductComponent";
		public static final String COLUMN_PGROUP_ID = "pgroup_id";
		public static final String COLUMN_CHILD_PRODUCT_ID = "child_product_id";
		public static final String COLUMN_CHILD_PRODUCT_AMOUNT = "child_product_amount";
		public static final String COLUMN_FLEXIBLE_PRODUCT_PRICE = "flexible_product_price";
		public static final String COLUMN_FLEXIBLE_INCLUDE_PRICE = "flexible_include_price";
	}
	
	public static abstract class ProductComponentGroupEntry{
		public static final String TABLE_PCOMP_GROUP = "PComponentGroup";
		public static final String COLUMN_SET_GROUP_NO = "set_group_no";
		public static final String COLUMN_SET_GROUP_NAME = "set_group_name";
		public static final String COLUMN_REQ_AMOUNT = "req_amount";
	}
	
	public static abstract class ProductGroupEntry{
		public static final String TABLE_PRODUCT_GROUP = "ProductGroup";
		public static final String COLUMN_PRODUCT_GROUP_CODE = "product_group_code";
		public static final String COLUMN_PRODUCT_GROUP_NAME = "product_group_name";
		public static final String COLUMN_PRODUCT_GROUP_TYPE = "product_group_type";
		public static final String COLUMN_IS_COMMENT = "is_comment";	
	}
	
	public static abstract class ProductDeptEntry{
		public static final String TABLE_PRODUCT_DEPT = "ProductDept";
		public static final String COLUMN_PRODUCT_DEPT_CODE = "product_dept_code";
		public static final String COLUMN_PRODUCT_DEPT_NAME = "product_dept_name";	
	}
	
	public static abstract class ProductEntry{
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
		public static final String COLUMN_SALE_MODE = "sale_mode";
		public static final String COLUMN_ORDERING = "ordering";	
	}
	
	public static class ProductComponentGroup extends ProductComponent{
		private int groupNo;
		private String groupName;
		private double requireAmount;
		
		public int getGroupNo() {
			return groupNo;
		}
		public void setGroupNo(int groupNo) {
			this.groupNo = groupNo;
		}
		public String getGroupName() {
			return groupName;
		}
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}
		public double getRequireAmount() {
			return requireAmount;
		}
		public void setRequireAmount(double requireAmount) {
			this.requireAmount = requireAmount;
		}
	}
	
	public static class ProductComponent extends Product{
        private int childProductId;
        private double childProductAmount;
        private double flexibleProductPrice;
        private int flexibleIncludePrice;
		
        public int getChildProductId() {
			return childProductId;
		}
		public void setChildProductId(int childProductId) {
			this.childProductId = childProductId;
		}
		public double getChildProductAmount() {
			return childProductAmount;
		}
		public void setChildProductAmount(double childProductAmount) {
			this.childProductAmount = childProductAmount;
		}
		public double getFlexibleProductPrice() {
			return flexibleProductPrice;
		}
		public void setFlexibleProductPrice(double flexibleProductPrice) {
			this.flexibleProductPrice = flexibleProductPrice;
		}
		public int getFlexibleIncludePrice() {
			return flexibleIncludePrice;
		}
		public void setFlexibleIncludePrice(int flexibleIncludePrice) {
			this.flexibleIncludePrice = flexibleIncludePrice;
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
		private int saleMode;
		
		public int getSaleMode() {
			return saleMode;
		}
		public void setSaleMode(int saleMode) {
			this.saleMode = saleMode;
		}
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
