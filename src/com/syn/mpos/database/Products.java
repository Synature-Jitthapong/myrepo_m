package com.syn.mpos.database;

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
		ProductsTable.COLUMN_PRODUCT_ID, 
		ProductsTable.COLUMN_PRODUCT_DEPT_ID,
		ProductsTable.COLUMN_PRODUCT_CODE,
		ProductsTable.COLUMN_PRODUCT_BAR_CODE,
		ProductsTable.COLUMN_PRODUCT_NAME,
		ProductsTable.COLUMN_PRODUCT_DESC,
		ProductsTable.COLUMN_PRODUCT_TYPE_ID,
		ProductsTable.COLUMN_PRODUCT_PRICE,
		ProductsTable.COLUMN_PRODUCT_UNIT_NAME,
		ProductsTable.COLUMN_DISCOUNT_ALLOW,
		ProductsTable.COLUMN_VAT_TYPE,
		ProductsTable.COLUMN_VAT_RATE,
		ProductsTable.COLUMN_ISOUTOF_STOCK,
		ProductsTable.COLUMN_IMG_URL
	};
	
	public static final String[] ALL_PRODUCT_DEPT_COLS = {
		ProductsTable.COLUMN_PRODUCT_GROUP_ID,
		ProductsTable.COLUMN_PRODUCT_DEPT_ID,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME
	};

	public Products(SQLiteDatabase db){
		super(db);
	}
	
	public List<ProductComponentGroup> listProductComponentGroup(int productId){
		List<ProductComponentGroup> pCompGroupLst = null;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT a." + ProductsTable.COLUMN_PRODUCT_ID
				+ ",a." + ProductComponentTable.COLUMN_PGROUP_ID
				+ ",a." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO
				+ ",a." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME
				+ ",a." + ProductComponentGroupTable.COLUMN_REQ_AMOUNT
				+ ",a." + ProductsTable.COLUMN_SALE_MODE
				+ ",b." + ProductsTable.COLUMN_PRODUCT_NAME
				+ ",b." + ProductsTable.COLUMN_IMG_URL
				+ " FROM " + ProductComponentGroupTable.TABLE_NAME + " a "
				+ " LEFT JOIN " + ProductsTable.TABLE_NAME + " b "
				+ " ON a." + ProductsTable.COLUMN_PRODUCT_ID + "=b." + ProductsTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductsTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			pCompGroupLst = new ArrayList<ProductComponentGroup>();
			do{
				ProductComponentGroup pCompGroup = new ProductComponentGroup();
				pCompGroup.setProductId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
				pCompGroup.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID)));
				pCompGroup.setGroupNo(cursor.getInt(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
				pCompGroup.setGroupName(cursor.getString(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME)));
				pCompGroup.setRequireAmount(cursor.getDouble(cursor.getColumnIndex(ProductComponentGroupTable.COLUMN_REQ_AMOUNT)));
				pCompGroup.setSaleMode(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_SALE_MODE)));
				pCompGroup.setProductName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
				pCompGroup.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_IMG_URL)));
				pCompGroupLst.add(pCompGroup);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompGroupLst;
	}
	
	public List<ProductComponent> listProductComponent(int groupId){
		List<ProductComponent> pCompLst = null;
		Cursor cursor = mSqlite.rawQuery(
				"SELECT a." + ProductsTable.COLUMN_PRODUCT_ID
				+ ",a." + ProductComponentTable.COLUMN_PGROUP_ID
				+ ",a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT
				+ ",a." + ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE
				+ ",a." + ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE
				+ ",a." + ProductsTable.COLUMN_SALE_MODE
				+ ",b." + ProductsTable.COLUMN_PRODUCT_NAME
				+ ",b." + ProductsTable.COLUMN_PRODUCT_PRICE
				+ ",b." + ProductsTable.COLUMN_IMG_URL
				+ " FROM " + ProductComponentTable.TABLE_NAME + " a "
				+ " LEFT JOIN " + ProductsTable.TABLE_NAME + " b "
				+ " ON a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_ID + "=b." + ProductsTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductComponentTable.COLUMN_PGROUP_ID + "=?", 
				new String[]{String.valueOf(groupId)});
		if(cursor.moveToFirst()){
			pCompLst = new ArrayList<ProductComponent>();
			do{
				ProductComponent pComp = new ProductComponent();
				pComp.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID)));
				pComp.setProductId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
				pComp.setSaleMode(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_SALE_MODE)));
				pComp.setChildProductAmount(cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT)));
				pComp.setFlexibleIncludePrice(cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE)));
				pComp.setFlexibleProductPrice(cursor.getInt(cursor.getColumnIndex(ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE)));
				pComp.setProductName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
				pComp.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_IMG_URL)));
				pCompLst.add(pComp);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompLst;
	}
	
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		Cursor cursor = mSqlite.rawQuery(
				" SELECT * FROM " + ProductGroupTable.TABLE_NAME, null);
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
		Cursor cursor = mSqlite.query(ProductDeptTable.TABLE_NAME, 
				ALL_PRODUCT_DEPT_COLS,
				ProductsTable.COLUMN_ACTIVATE + "=?", new String[]{"1"}, null, null, 
				ProductsTable.COLUMN_ORDERING);
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
				"SELECT b." + ProductsTable.COLUMN_PRODUCT_ID + ", " 
				+ " b." + ProductsTable.COLUMN_PRODUCT_TYPE_ID + ", "
				+ " b." + ProductsTable.COLUMN_PRODUCT_CODE + ", " 
				+ " b." + ProductsTable.COLUMN_PRODUCT_BAR_CODE + ", "
				+ " b." + ProductsTable.COLUMN_PRODUCT_NAME + ", "
				+ " b." + ProductsTable.COLUMN_PRODUCT_PRICE + ", " 
				+ " b." + ProductsTable.COLUMN_VAT_TYPE + ", " 
				+ " b." + ProductsTable.COLUMN_VAT_RATE
				+ " FROM " + ProductComponentTable.TABLE_NAME + " a "
				+ " INNER JOIN " + ProductsTable.TABLE_NAME + " b " 
				+ " ON a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_ID + "=b." + ProductsTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + ProductsTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(proId)});
		if(cursor.moveToFirst()){
			pLst = new ArrayList<Product>();
			do{
				Product p = new Product();
				p.setProductId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
				p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_TYPE_ID)));
				p.setProductCode(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_CODE)));
				p.setProductBarCode(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_BAR_CODE)));
				p.setProductName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
				p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_PRICE)));
				p.setVatType(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_VAT_TYPE)));
				p.setVatRate(cursor.getFloat(cursor.getColumnIndex(ProductsTable.COLUMN_VAT_RATE)));
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	public List<Product> listProduct(String query){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = mSqlite.query(ProductsTable.TABLE_NAME, ALL_PRODUCT_COLS, 
				"(" + ProductsTable.COLUMN_PRODUCT_CODE + " LIKE '%" + query + "%' " +
				" OR " + ProductsTable.COLUMN_PRODUCT_NAME + " LIKE '%" + query + "%')", 
				null, null, null, ProductsTable.COLUMN_ORDERING);
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
		Cursor cursor = mSqlite.query(ProductsTable.TABLE_NAME, ALL_PRODUCT_COLS, 
				ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=? " +
						" AND " + ProductsTable.COLUMN_ACTIVATE + "=?", 
				new String[]{String.valueOf(deptId), "1"}, null, null, ProductsTable.COLUMN_ORDERING);
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
		Cursor cursor = queryProduct(new String[]{ProductsTable.COLUMN_VAT_RATE}, ProductsTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{String.valueOf(productId)});
		if(cursor.moveToFirst()){
			vatRate = cursor.getFloat(0);
		}
		cursor.close();
		return vatRate;
	}
	
	public Product getProduct(String barCode){
		Product p = null;
		Cursor cursor = mSqlite.query(ProductsTable.TABLE_NAME, ALL_PRODUCT_COLS, 
				ProductsTable.COLUMN_PRODUCT_BAR_CODE + "=?", new String[]{barCode}, 
				null, null, "1");
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		return p;
	}
	
	public Product getProduct(int proId){
		Product p = null;
		Cursor cursor = mSqlite.query(ProductsTable.TABLE_NAME, ALL_PRODUCT_COLS, 
				ProductsTable.COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(proId)}, 
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
				" SELECT * FROM " + ProductDeptTable.TABLE_NAME +
				" WHERE " + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=" + deptId, null);
		if(cursor.moveToFirst()){
			pd = toProductDept(cursor);
		}
		cursor.close();
		return pd;
	}
	
	public Cursor queryProduct(String[] columns, String selection, String[] selectionArgs){
		return mSqlite.query(ProductsTable.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
	}
	
	private Product toProduct(Cursor cursor){
		Product p = new Product();
		p.setProductId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
		p.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_DEPT_ID)));
		p.setProductTypeId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_TYPE_ID)));
		p.setProductCode(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_CODE)));
		p.setProductBarCode(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_BAR_CODE)));
		p.setProductName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
		p.setProductDesc(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_DESC)));
		p.setProductUnitName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_UNIT_NAME)));
		p.setProductPrice(cursor.getFloat(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_PRICE)));
		p.setVatType(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_VAT_TYPE)));
		p.setVatRate(cursor.getFloat(cursor.getColumnIndex(ProductsTable.COLUMN_VAT_RATE)));
		p.setDiscountAllow(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_DISCOUNT_ALLOW)));
		p.setImgUrl(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_IMG_URL)));
		return p;
	}

	private ProductDept toProductDept(Cursor cursor){
		ProductDept pd = new ProductDept();
		pd.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_DEPT_ID)));
		pd.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_GROUP_ID)));
		pd.setProductDeptCode(cursor.getString(cursor.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE)));
		pd.setProductDeptName(cursor.getString(cursor.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME)));
		return pd;
	}

	public ProductGroup toProductGroup(Cursor cursor){
		ProductGroup pg = new ProductGroup();
		pg.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_GROUP_ID)));
		pg.setProductGroupCode(cursor.getString(cursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_CODE)));
		pg.setProductGroupName(cursor.getString(cursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
		return pg;
	}
	
	/**
	 * @param pCompGroupLst
	 * @throws SQLException
	 */
	public void insertPComponentGroup(List<ProductGroups.PComponentGroup> pCompGroupLst) throws SQLException{
		mSqlite.delete(ProductComponentGroupTable.TABLE_NAME, null, null);
		for(ProductGroups.PComponentGroup pCompGroup : pCompGroupLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pCompGroup.getPGroupID());
			cv.put(ProductsTable.COLUMN_PRODUCT_ID, pCompGroup.getProductID());
			cv.put(ProductsTable.COLUMN_SALE_MODE, pCompGroup.getSaleMode());
			cv.put(ProductComponentGroupTable.COLUMN_SET_GROUP_NO, pCompGroup.getSetGroupNo());
			cv.put(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME, pCompGroup.getSetGroupName());
			cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, pCompGroup.getRequireAmount());
			mSqlite.insertOrThrow(ProductComponentGroupTable.TABLE_NAME, null, cv);
		}
	}
	
	/**
	 * @param pCompLst
	 * @throws SQLException
	 */
	public void insertProductComponent(List<ProductGroups.ProductComponent> pCompLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(ProductComponentTable.TABLE_NAME, null, null);
			for(ProductGroups.ProductComponent pCompSet : pCompLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pCompSet.getPGroupID());
				cv.put(ProductsTable.COLUMN_PRODUCT_ID, pCompSet.getProductID());
				cv.put(ProductsTable.COLUMN_SALE_MODE, pCompSet.getSaleMode());
				cv.put(ProductComponentTable.COLUMN_CHILD_PRODUCT_ID, pCompSet.getChildProductID());
				cv.put(ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT, pCompSet.getPGroupID());
				cv.put(ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE, pCompSet.getFlexibleProductPrice());
				cv.put(ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE, pCompSet.getFlexibleIncludePrice());
				mSqlite.insertOrThrow(ProductComponentTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
	
	/**
	 * @param pgLst
	 * @param mgLst
	 * @throws SQLException
	 */
	public void insertProductGroup(List<ProductGroups.ProductGroup> pgLst,
		List<MenuGroups.MenuGroup> mgLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(ProductGroupTable.TABLE_NAME, null, null);
			for(ProductGroups.ProductGroup pg : pgLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_PRODUCT_GROUP_ID, pg.getProductGroupId());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_CODE, pg.getProductGroupCode());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME, pg.getProductGroupName());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_TYPE, pg.getProductGroupType());
				cv.put(ProductGroupTable.COLUMN_IS_COMMENT, pg.getIsComment());
				cv.put(ProductsTable.COLUMN_ORDERING, pg.getProductGroupOrdering());
				cv.put(ProductsTable.COLUMN_ACTIVATE, 0);
				mSqlite.insertOrThrow(ProductGroupTable.TABLE_NAME, null, cv);
			}
			
			for(MenuGroups.MenuGroup mg : mgLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_ACTIVATE, mg.getActivate());
				mSqlite.update(ProductGroupTable.TABLE_NAME, cv, 
						ProductsTable.COLUMN_PRODUCT_GROUP_ID + "=?", 
						new String[]{
							String.valueOf(mg.getMenuGroupID())
						});
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
	
	/**
	 * @param pdLst
	 * @param mdLst
	 * @throws SQLException
	 */
	public void insertProductDept(List<ProductGroups.ProductDept> pdLst,
		List<MenuGroups.MenuDept> mdLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(ProductDeptTable.TABLE_NAME, null, null);
			for(ProductGroups.ProductDept pd : pdLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_PRODUCT_DEPT_ID, pd.getProductDeptID());
				cv.put(ProductsTable.COLUMN_PRODUCT_GROUP_ID, pd.getProductGroupID());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE, pd.getProductDeptCode());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME, pd.getProductDeptName());
				cv.put(ProductsTable.COLUMN_ORDERING, pd.getProductDeptOrdering());
				cv.put(ProductsTable.COLUMN_ACTIVATE, 0);
				mSqlite.insertOrThrow(ProductDeptTable.TABLE_NAME, null, cv);
			}
			
			for(MenuGroups.MenuDept md : mdLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_ACTIVATE, md.getActivate());
				mSqlite.update(ProductDeptTable.TABLE_NAME, cv, 
						ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=?",
						new String[]{
							String.valueOf(md.getMenuDeptID())
						}
				);
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
	
	/**
	 * @param productLst
	 * @param menuItemLst
	 * @throws SQLException
	 */
	public void insertProducts(List<ProductGroups.Products> productLst,
		List<MenuGroups.MenuItem> menuItemLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(ProductsTable.TABLE_NAME, null, null);
			for(ProductGroups.Products p : productLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_PRODUCT_ID, p.getProductID());
				cv.put(ProductsTable.COLUMN_PRODUCT_DEPT_ID, p.getProductDeptID());
				cv.put(ProductsTable.COLUMN_PRODUCT_CODE, p.getProductCode());
				cv.put(ProductsTable.COLUMN_PRODUCT_BAR_CODE, p.getProductBarCode());
				cv.put(ProductsTable.COLUMN_PRODUCT_TYPE_ID, p.getProductTypeID());
				cv.put(ProductsTable.COLUMN_PRODUCT_PRICE, p.getProductPricePerUnit());
				cv.put(ProductsTable.COLUMN_PRODUCT_UNIT_NAME, p.getProductUnitName());
				cv.put(ProductsTable.COLUMN_PRODUCT_DESC, p.getProductDesc());
				cv.put(ProductsTable.COLUMN_DISCOUNT_ALLOW, p.getDiscountAllow());
				cv.put(ProductsTable.COLUMN_VAT_TYPE, p.getVatType());
				cv.put(ProductsTable.COLUMN_VAT_RATE, p.getVatRate());
				cv.put(ProductsTable.COLUMN_ISOUTOF_STOCK, p.getIsOutOfStock());
				mSqlite.insertOrThrow(ProductsTable.TABLE_NAME, null, cv);
			}
			
			for(MenuGroups.MenuItem m : menuItemLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_PRODUCT_NAME, m.getMenuName_0());
				cv.put(ProductsTable.COLUMN_IMG_URL, m.getMenuImageLink());
				cv.put(ProductsTable.COLUMN_ORDERING, m.getMenuItemOrdering());
				cv.put(ProductsTable.COLUMN_ACTIVATE, m.getMenuActivate());
				mSqlite.update(ProductsTable.TABLE_NAME, cv, ProductsTable.COLUMN_PRODUCT_ID + "=?", 
						new String[]{String.valueOf(m.getProductID())});
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
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
