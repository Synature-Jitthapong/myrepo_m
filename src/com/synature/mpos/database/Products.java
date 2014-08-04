package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.table.ProductComponentGroupTable;
import com.synature.mpos.database.table.ProductComponentTable;
import com.synature.mpos.database.table.ProductDeptTable;
import com.synature.mpos.database.table.ProductGroupTable;
import com.synature.mpos.database.table.ProductTable;
import com.synature.pos.MenuGroups;
import com.synature.pos.ProductGroups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Products extends MPOSDatabase {
	
	/**
	 * Product normal type
	 */
	public static final int NORMAL_TYPE = 0;
	
	/**
	 * Product set 
	 */
	public static final int SET = 1;
	
	/**
	 * Product size
	 */
	public static final int SIZE = 2;
	
	/**
	 * Product open price
	 */
	public static final int OPEN_QTY = 5;
	
	/**
	 * Product set can select
	 */
	public static final int SET_CAN_SELECT = 7;
	
	/**
	 * MenuComment have price
	 */
	public static final int COMMENT_HAVE_PRICE = 15;
	
	/**
	 * Child of product type 7 have price
	 */
	public static final int CHILD_OF_SET_HAVE_PRICE = -6;
	
	/**
	 * Product include vat
	 */
	public static final int VAT_TYPE_INCLUDED = 1;
	
	/**
	 * Product exclude vat
	 */
	public static final int VAT_TYPE_EXCLUDE = 2;
	
	/**
	 * Product no vat
	 */
	public static final int NO_VAT = 0;
	
	/**
	 * Product that no activated
	 */
	public static final int NO_ACTIVATED = 0;
	
	/**
	 * Product that activated
	 */
	public static final int ACTIVATED = 1;
	
	public static final String[] ALL_PRODUCT_COLS = {
		ProductTable.COLUMN_PRODUCT_ID, 
		ProductTable.COLUMN_PRODUCT_DEPT_ID,
		ProductTable.COLUMN_PRODUCT_CODE,
		ProductTable.COLUMN_PRODUCT_BAR_CODE,
		ProductTable.COLUMN_PRODUCT_NAME,
		ProductTable.COLUMN_PRODUCT_NAME1,
		ProductTable.COLUMN_PRODUCT_NAME2,
		ProductTable.COLUMN_PRODUCT_NAME3,
		ProductTable.COLUMN_PRODUCT_DESC,
		ProductTable.COLUMN_PRODUCT_TYPE_ID,
		ProductTable.COLUMN_PRODUCT_PRICE,
		ProductTable.COLUMN_PRODUCT_UNIT_NAME,
		ProductTable.COLUMN_DISCOUNT_ALLOW,
		ProductTable.COLUMN_VAT_TYPE,
		ProductTable.COLUMN_VAT_RATE,
		ProductTable.COLUMN_IS_OUTOF_STOCK,
		ProductTable.COLUMN_IMG_FILE_NAME
	};
	
	public static final String[] ALL_PRODUCT_GROUP_COLS = {
		ProductTable.COLUMN_PRODUCT_GROUP_ID,
		ProductGroupTable.COLUMN_PRODUCT_GROUP_CODE,
		ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME,
		ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME1,
		ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME2,
		ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME3,
	};
	
	public static final String[] ALL_PRODUCT_DEPT_COLS = {
		ProductTable.COLUMN_PRODUCT_GROUP_ID,
		ProductTable.COLUMN_PRODUCT_DEPT_ID,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME1,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME2,
		ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME3
	};

	public Products(Context context){
		super(context);
	}
	
	/**
	 * @param productId
	 * @return List<ProductComponentGroup>
	 */
	public List<ProductComponentGroup> listProductComponentGroup(int productId){
		List<ProductComponentGroup> pCompGroupLst = new ArrayList<ProductComponentGroup>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + ProductTable.COLUMN_PRODUCT_ID + ","
						+ " a." + ProductComponentTable.COLUMN_PGROUP_ID + ","
						+ " a." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ","
						+ " a." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME + ","
						+ " a." + ProductComponentGroupTable.COLUMN_REQ_AMOUNT + ","
						+ " a." + ProductTable.COLUMN_SALE_MODE + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME2 + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME3 + ","
						+ " b." + ProductTable.COLUMN_IMG_FILE_NAME 
						+ " FROM " + ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP + " a "
						+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
						+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b."
						+ ProductTable.COLUMN_PRODUCT_ID 
						+ " WHERE a." + ProductTable.COLUMN_PRODUCT_ID + "=?",
				new String[]{
					String.valueOf(productId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				ProductComponentGroup pCompGroup = new ProductComponentGroup();
				pCompGroup.setProductId(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				pCompGroup
						.setProductGroupId(cursor.getInt(cursor
								.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID)));
				pCompGroup
						.setGroupNo(cursor.getInt(cursor
								.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
				pCompGroup
						.setGroupName(cursor.getString(cursor
								.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME)));
				pCompGroup
						.setRequireAmount(cursor.getDouble(cursor
								.getColumnIndex(ProductComponentGroupTable.COLUMN_REQ_AMOUNT)));
				pCompGroup.setSaleMode(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_SALE_MODE)));
				pCompGroup.setProductName(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
				pCompGroup.setProductName1(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
				pCompGroup.setProductName2(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2)));
				pCompGroup.setProductName3(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3)));
				pCompGroup.setImgName(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_IMG_FILE_NAME)));
				pCompGroupLst.add(pCompGroup);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompGroupLst;
	}
	
	/**
	 * @param groupId
	 * @return List<ProductComponent>
	 */
	public List<ProductComponent> listProductComponent(int groupId){
		List<ProductComponent> pCompLst = new ArrayList<ProductComponent>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + ProductComponentTable.COLUMN_PGROUP_ID + ","
						+ " a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT + ","
						+ " a." + ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE + ","
						+ " a." + ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE + ","
						+ " a." + ProductTable.COLUMN_SALE_MODE + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_ID + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME2 + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME3 + ","
						+ " b." + ProductTable.COLUMN_PRODUCT_PRICE + ","
						+ " b." + ProductTable.COLUMN_IMG_FILE_NAME 
						+ " FROM " + ProductComponentTable.TABLE_PCOMPONENT + " a "
						+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
						+ " ON a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_ID + "=b."
						+ ProductTable.COLUMN_PRODUCT_ID 
						+ " WHERE a." + ProductComponentTable.COLUMN_PGROUP_ID + "=?",
				new String[]{
						String.valueOf(groupId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				ProductComponent pComp = new ProductComponent();
				pComp.setProductGroupId(cursor.getInt(cursor
						.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID)));
				pComp.setProductId(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				pComp.setSaleMode(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_SALE_MODE)));
				pComp.setChildProductAmount(cursor.getInt(cursor
						.getColumnIndex(ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT)));
				pComp.setFlexibleIncludePrice(cursor.getInt(cursor
						.getColumnIndex(ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE)));
				pComp.setFlexibleProductPrice(cursor.getInt(cursor
						.getColumnIndex(ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE)));
				pComp.setProductName(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
				pComp.setProductName1(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
				pComp.setProductName2(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2)));
				pComp.setProductName3(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3)));
				pComp.setImgName(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_IMG_FILE_NAME)));
				pComp.setProductPrice(cursor.getDouble(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
				pCompLst.add(pComp);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pCompLst;
	}
	
	/**
	 * @return List<ProductGroup>
	 */
	public List<ProductGroup> listProductGroup(){
		List<ProductGroup> pgLst = new ArrayList<ProductGroup>();
		Cursor cursor = getReadableDatabase().query(ProductGroupTable.TABLE_PRODUCT_GROUP, 
				ALL_PRODUCT_GROUP_COLS, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				ProductGroup pg = toProductGroup(cursor);
				pgLst.add(pg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pgLst;
	}

	/**
	 * @return List<ProductDept>
	 */
	public List<ProductDept> listProductDept(){
		List<ProductDept> pdLst = new ArrayList<ProductDept>();
		Cursor cursor = getReadableDatabase().query(ProductDeptTable.TABLE_PRODUCT_DEPT, 
				ALL_PRODUCT_DEPT_COLS,
				ProductTable.COLUMN_ACTIVATE + "=?", 
				new String[]{
					String.valueOf(ACTIVATED)
				}, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				ProductDept pd = toProductDept(cursor);
				pdLst.add(pd);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pdLst;
	}
	
	/**
	 * @param proId
	 * @return List<Product>
	 */
	public List<Product> listProductSize(int proId){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT b." + ProductTable.COLUMN_PRODUCT_ID + ", " 
						+ " b." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", " 
						+ " b." + ProductTable.COLUMN_PRODUCT_CODE + ", " 
						+ " b." + ProductTable.COLUMN_PRODUCT_BAR_CODE + ", " 
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ", "  
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ", "  
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME2 + ", "  
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME3 + ", " 
						+ " b." + ProductTable.COLUMN_PRODUCT_PRICE + ", " 
						+ " b." + ProductTable.COLUMN_VAT_TYPE + ", " 
						+ " b." + ProductTable.COLUMN_VAT_RATE + ", "
						+ " b." + ProductTable.COLUMN_IMG_FILE_NAME
						+ " FROM " + ProductComponentTable.TABLE_PCOMPONENT + " a "
						+ " INNER JOIN " + ProductTable.TABLE_PRODUCT + " b "
						+ " ON a." + ProductComponentTable.COLUMN_CHILD_PRODUCT_ID + "=b."
						+ ProductTable.COLUMN_PRODUCT_ID 
						+ " WHERE a."+ ProductTable.COLUMN_PRODUCT_ID + "=?",
				new String[] { 
						String.valueOf(proId) 
				}
		);
		if(cursor.moveToFirst()){
			do{
				Product p = new Product();
				p.setProductId(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				p.setProductTypeId(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID)));
				p.setProductCode(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_CODE)));
				p.setProductBarCode(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_BAR_CODE)));
				p.setProductName(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
				p.setProductName1(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
				p.setProductName2(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2)));
				p.setProductName3(cursor.getString(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3)));
				p.setProductPrice(cursor.getDouble(cursor
						.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
				p.setVatType(cursor.getInt(cursor
						.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
				p.setVatRate(cursor.getDouble(cursor
						.getColumnIndex(ProductTable.COLUMN_VAT_RATE)));
				p.setImgName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_IMG_FILE_NAME)));
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	/**
	 * @param query
	 * @return List<Product>
	 */
	public List<Product> listProduct(String query){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = getReadableDatabase().query(
				ProductTable.TABLE_PRODUCT,
				ALL_PRODUCT_COLS,
				"(" + ProductTable.COLUMN_PRODUCT_CODE + " LIKE '%" + query
						+ "%' " + " OR " + ProductTable.COLUMN_PRODUCT_NAME
						+ " LIKE '%" + query + "%')", null, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}
	
	/**
	 * @param deptId
	 * @return List<Product>
	 */
	public List<Product> listProduct(int deptId){
		List<Product> pLst = new ArrayList<Product>();
		Cursor cursor = getReadableDatabase().query(
				ProductTable.TABLE_PRODUCT,
				ALL_PRODUCT_COLS,
				ProductTable.COLUMN_PRODUCT_DEPT_ID + "=? " + " AND "
						+ ProductTable.COLUMN_ACTIVATE + "=?",
				new String[] { 
					String.valueOf(deptId), 
					String.valueOf(ACTIVATED)
				}, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				Product p = toProduct(cursor);
				pLst.add(p);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return pLst;
	}

	/**
	 * Check allow discount
	 * @param productId
	 * @return true if allow discount
	 */
	public boolean isAllowDiscount(int productId){
		boolean isAllowDiscount = false;
		Cursor cursor = getReadableDatabase().query(ProductTable.TABLE_PRODUCT, 
				new String[]{
					ProductTable.COLUMN_DISCOUNT_ALLOW,
				}, ProductTable.COLUMN_PRODUCT_ID + "=?", 
				new String[]{
					String.valueOf(productId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			if(cursor.getInt(0) == 1)
				isAllowDiscount = true;
		}
		cursor.close();
		return isAllowDiscount;
	}
	
	/**
	 * @param productId
	 * @return vatType include or exclude
 	 */
	public int getVatType(int productId){
		int vatType = VAT_TYPE_INCLUDED;
		Cursor cursor = queryProduct(
				new String[] { 
					ProductTable.COLUMN_VAT_TYPE 
				},
				ProductTable.COLUMN_PRODUCT_ID + "=?",
				new String[] { 
					String.valueOf(productId) 
				}
		);
		if(cursor.moveToFirst()){
			vatType = cursor.getInt(0);
		}
		cursor.close();
		return vatType;
	}
	
	/**
	 * @param productId
	 * @return product vat rate
	 */
	public double getVatRate(int productId){
		double vatRate = 0.0f;
		Cursor cursor = queryProduct(
				new String[] { ProductTable.COLUMN_VAT_RATE },
				ProductTable.COLUMN_PRODUCT_ID + "=?",
				new String[] { 
					String.valueOf(productId) 
				});
		if(cursor.moveToFirst()){
			vatRate = cursor.getDouble(0);
		}
		cursor.close();
		return vatRate;
	}
	
	/**
	 * @param barCode
	 * @return Product
	 */
	public Product getProduct(String barCode){
		Product p = new Product();
		Cursor cursor = getReadableDatabase().query(ProductTable.TABLE_PRODUCT,
				ALL_PRODUCT_COLS, ProductTable.COLUMN_PRODUCT_BAR_CODE + "=?",
				new String[] { 
					barCode 
				}, null, null, null);
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		return p;
	}
	
	/**
	 * @param proId
	 * @return Product
	 */
	public Product getProduct(int proId){
		Product p = new Product();
		Cursor cursor = getReadableDatabase().query(ProductTable.TABLE_PRODUCT,
				ALL_PRODUCT_COLS, ProductTable.COLUMN_PRODUCT_ID + "=?",
				new String[] { 
					String.valueOf(proId) 
				}, null, null, null);
		if(cursor.moveToFirst()){
			p = toProduct(cursor);
		}
		cursor.close();
		return p;
	}
	
	/**
	 * @param deptId
	 * @return ProductDept
	 */
	public ProductDept getProductDept(int deptId){
		ProductDept pd = new ProductDept();
		Cursor cursor = getReadableDatabase().query(
				ProductDeptTable.TABLE_PRODUCT_DEPT, ALL_PRODUCT_DEPT_COLS,
				ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?",
				new String[] { 
					String.valueOf(deptId) 
				}, null, null, null);
		if(cursor.moveToFirst()){
			pd = toProductDept(cursor);
		}
		cursor.close();
		return pd;
	}
	
	/**
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @return Cursor
	 */
	public Cursor queryProduct(String[] columns, String selection, String[] selectionArgs){
		return getReadableDatabase().query(ProductTable.TABLE_PRODUCT, columns,
				selection, selectionArgs, null, null, null);
	}
	
	public Product toProduct(Cursor cursor){
		Product p = new Product();
		p.setProductId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
		p.setProductDeptId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_DEPT_ID)));
		p.setProductTypeId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID)));
		p.setProductCode(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_CODE)));
		p.setProductBarCode(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_BAR_CODE)));
		p.setProductName(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
		p.setProductName1(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1)));
		p.setProductName2(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2)));
		p.setProductName3(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3)));
		p.setProductDesc(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_DESC)));
		p.setProductUnitName(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_UNIT_NAME)));
		p.setProductPrice(cursor.getDouble(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
		p.setVatType(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
		p.setVatRate(cursor.getDouble(cursor
				.getColumnIndex(ProductTable.COLUMN_VAT_RATE)));
		p.setDiscountAllow(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_DISCOUNT_ALLOW)));
		p.setImgName(cursor.getString(cursor
				.getColumnIndex(ProductTable.COLUMN_IMG_FILE_NAME)));
		return p;
	}

	public ProductDept toProductDept(Cursor cursor){
		ProductDept pd = new ProductDept();
		pd.setProductDeptId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_DEPT_ID)));
		pd.setProductGroupId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_GROUP_ID)));
		pd.setProductDeptCode(cursor.getString(cursor
				.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE)));
		pd.setProductDeptName(cursor.getString(cursor
				.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME)));
		pd.setProductDeptName1(cursor.getString(cursor
				.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME1)));
		pd.setProductDeptName2(cursor.getString(cursor
				.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME2)));
		pd.setProductDeptName3(cursor.getString(cursor
				.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME3)));
		return pd;
	}

	public ProductGroup toProductGroup(Cursor cursor){
		ProductGroup pg = new ProductGroup();
		pg.setProductGroupId(cursor.getInt(cursor
				.getColumnIndex(ProductTable.COLUMN_PRODUCT_GROUP_ID)));
		pg.setProductGroupCode(cursor.getString(cursor
				.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_CODE)));
		pg.setProductGroupName(cursor.getString(cursor
				.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
		pg.setProductGroupName1(cursor.getString(cursor
				.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME1)));
		pg.setProductGroupName2(cursor.getString(cursor
				.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME2)));
		pg.setProductGroupName3(cursor.getString(cursor
				.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME3)));
		return pg;
	}
	
	/**
	 * @param pCompGroupLst
	 * @throws SQLException
	 */
	public void insertPComponentGroup(List<ProductGroups.PComponentGroup> pCompGroupLst) throws SQLException{
		getWritableDatabase().delete(ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP, null, null);
		for(ProductGroups.PComponentGroup pCompGroup : pCompGroupLst){
			ContentValues cv = new ContentValues();
			cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pCompGroup.getPGroupID());
			cv.put(ProductTable.COLUMN_PRODUCT_ID, pCompGroup.getProductID());
			cv.put(ProductTable.COLUMN_SALE_MODE, pCompGroup.getSaleMode());
			cv.put(ProductComponentGroupTable.COLUMN_SET_GROUP_NO, pCompGroup.getSetGroupNo());
			cv.put(ProductComponentGroupTable.COLUMN_SET_GROUP_NAME, pCompGroup.getSetGroupName());
			cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, pCompGroup.getRequireAmount());
			getWritableDatabase().insertOrThrow(ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP, null, cv);
		}
	}
	
	/**
	 * @param pCompLst
	 * @throws SQLException
	 */
	public void insertProductComponent(List<ProductGroups.ProductComponent> pCompLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ProductComponentTable.TABLE_PCOMPONENT, null, null);
			for(ProductGroups.ProductComponent pCompSet : pCompLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pCompSet.getPGroupID());
				cv.put(ProductTable.COLUMN_PRODUCT_ID, pCompSet.getProductID());
				cv.put(ProductTable.COLUMN_SALE_MODE, pCompSet.getSaleMode());
				cv.put(ProductComponentTable.COLUMN_CHILD_PRODUCT_ID, pCompSet.getChildProductID());
				cv.put(ProductComponentTable.COLUMN_CHILD_PRODUCT_AMOUNT, pCompSet.getPGroupID());
				cv.put(ProductComponentTable.COLUMN_FLEXIBLE_PRODUCT_PRICE, pCompSet.getFlexibleProductPrice());
				cv.put(ProductComponentTable.COLUMN_FLEXIBLE_INCLUDE_PRICE, pCompSet.getFlexibleIncludePrice());
				getWritableDatabase().insertOrThrow(ProductComponentTable.TABLE_PCOMPONENT, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	/**
	 * @param pgLst
	 * @param mgLst
	 * @throws SQLException
	 */
	public void insertProductGroup(List<ProductGroups.ProductGroup> pgLst,
		List<MenuGroups.MenuGroup> mgLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ProductGroupTable.TABLE_PRODUCT_GROUP, null, null);
			for(ProductGroups.ProductGroup pg : pgLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_GROUP_ID, pg.getProductGroupId());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_CODE, pg.getProductGroupCode());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME, pg.getProductGroupName());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_TYPE, pg.getProductGroupType());
				cv.put(ProductGroupTable.COLUMN_IS_COMMENT, pg.getIsComment());
				cv.put(COLUMN_ORDERING, pg.getProductGroupOrdering());
				cv.put(ProductTable.COLUMN_ACTIVATE, 0);
				getWritableDatabase().insertOrThrow(ProductGroupTable.TABLE_PRODUCT_GROUP, null, cv);
			}
			
			for(MenuGroups.MenuGroup mg : mgLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME, mg.getMenuGroupName_0());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME1, mg.getMenuGroupName_1());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME2, mg.getMenuGroupName_2());
				cv.put(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME3, mg.getMenuGroupName_3());
				cv.put(ProductTable.COLUMN_ACTIVATE, mg.getActivate());
				getWritableDatabase().update(ProductGroupTable.TABLE_PRODUCT_GROUP, 
						cv, 
						ProductTable.COLUMN_PRODUCT_GROUP_ID + "=?", 
						new String[]{
							String.valueOf(mg.getMenuGroupID())
						}
				);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	/**
	 * @param pdLst
	 * @param mdLst
	 * @throws SQLException
	 */
	public void insertProductDept(List<ProductGroups.ProductDept> pdLst,
		List<MenuGroups.MenuDept> mdLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ProductDeptTable.TABLE_PRODUCT_DEPT, null, null);
			for(ProductGroups.ProductDept pd : pdLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_DEPT_ID, pd.getProductDeptID());
				cv.put(ProductTable.COLUMN_PRODUCT_GROUP_ID, pd.getProductGroupID());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_CODE, pd.getProductDeptCode());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME, pd.getProductDeptName());
				cv.put(COLUMN_ORDERING, pd.getProductDeptOrdering());
				cv.put(ProductTable.COLUMN_ACTIVATE, 0);
				getWritableDatabase().insertOrThrow(ProductDeptTable.TABLE_PRODUCT_DEPT, null, cv);
			}
			
			for(MenuGroups.MenuDept md : mdLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME, md.getMenuDeptName_0());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME1, md.getMenuDeptName_1());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME2, md.getMenuDeptName_2());
				cv.put(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME3, md.getMenuDeptName_3());
				cv.put(ProductTable.COLUMN_ACTIVATE, md.getActivate());
				getWritableDatabase().update(ProductDeptTable.TABLE_PRODUCT_DEPT, cv, 
						ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?",
						new String[]{
							String.valueOf(md.getMenuDeptID())
						}
				);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	/**
	 * @param pLst
	 * @param mLst
	 * @throws SQLException
	 */
	public void insertProducts(List<ProductGroups.Products> pLst,
		List<MenuGroups.MenuItem> mLst, List<MenuGroups.MenuComment> mcLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ProductTable.TABLE_PRODUCT, null, null);
			for(ProductGroups.Products p : pLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_ID, p.getProductID());
				cv.put(ProductTable.COLUMN_PRODUCT_DEPT_ID, p.getProductDeptID());
				cv.put(ProductTable.COLUMN_PRODUCT_CODE, p.getProductCode());
				cv.put(ProductTable.COLUMN_PRODUCT_BAR_CODE, p.getProductBarCode());
				cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, p.getProductTypeID());
				cv.put(ProductTable.COLUMN_PRODUCT_PRICE, p.getProductPricePerUnit());
				cv.put(ProductTable.COLUMN_PRODUCT_UNIT_NAME, p.getProductUnitName());
				cv.put(ProductTable.COLUMN_PRODUCT_DESC, p.getProductDesc());
				cv.put(ProductTable.COLUMN_DISCOUNT_ALLOW, p.getDiscountAllow());
				cv.put(ProductTable.COLUMN_VAT_TYPE, p.getVatType());
				cv.put(ProductTable.COLUMN_VAT_RATE, p.getVatRate());
				cv.put(ProductTable.COLUMN_IS_OUTOF_STOCK, p.getIsOutOfStock());
				getWritableDatabase().insertOrThrow(ProductTable.TABLE_PRODUCT, null, cv);
			}
			for(MenuGroups.MenuItem m : mLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_NAME, m.getMenuName_0());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME1, m.getMenuName_1());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME2, m.getMenuName_2());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME3, m.getMenuName_3());
				cv.put(ProductTable.COLUMN_IMG_FILE_NAME, m.getMenuImageLink());
				cv.put(COLUMN_ORDERING, m.getMenuItemOrdering());
				cv.put(ProductTable.COLUMN_ACTIVATE, m.getMenuActivate());
				getWritableDatabase().update(ProductTable.TABLE_PRODUCT, 
						cv, ProductTable.COLUMN_PRODUCT_ID + "=?", 
						new String[]{
							String.valueOf(m.getProductID())
						}
				);
			}
			for(MenuGroups.MenuComment mc : mcLst){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_NAME, mc.getMenuCommentName_0());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME1, mc.getMenuCommentName_1());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME2, mc.getMenuCommentName_2());
				cv.put(ProductTable.COLUMN_PRODUCT_NAME3, mc.getMenuCommentName_3());
				getWritableDatabase().update(ProductTable.TABLE_PRODUCT, 
						cv, ProductTable.COLUMN_PRODUCT_ID + "=?", 
						new String[]{
							String.valueOf(mc.getMenuCommentID())
						}
				);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
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
		private String productName1;
		private String productName2;
		private String productName3;
		private int productTypeId;
		private double productPrice;
		private String productUnitName;
		private String productDesc;
		private int discountAllow;
		private int vatType;
		private double vatRate;
		private int hasServiceCharge;
		private String imgName;
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
		public String getProductName1() {
			return productName1;
		}
		public void setProductName1(String productName1) {
			this.productName1 = productName1;
		}
		public String getProductName2() {
			return productName2;
		}
		public void setProductName2(String productName2) {
			this.productName2 = productName2;
		}
		public String getProductName3() {
			return productName3;
		}
		public void setProductName3(String productName3) {
			this.productName3 = productName3;
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
		public String getImgName() {
			return imgName;
		}
		public void setImgName(String imgName) {
			this.imgName = imgName;
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
		private String productDeptName1;
		private String productDeptName2;
		private String productDeptName3;
		
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
		public String getProductDeptName1() {
			return productDeptName1;
		}
		public void setProductDeptName1(String productDeptName1) {
			this.productDeptName1 = productDeptName1;
		}
		public String getProductDeptName2() {
			return productDeptName2;
		}
		public void setProductDeptName2(String productDeptName2) {
			this.productDeptName2 = productDeptName2;
		}
		public String getProductDeptName3() {
			return productDeptName3;
		}
		public void setProductDeptName3(String productDeptName3) {
			this.productDeptName3 = productDeptName3;
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
		private String productGroupName1;
		private String productGroupName2;
		private String productGroupName3;
		
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
		public String getProductGroupName1() {
			return productGroupName1;
		}
		public void setProductGroupName1(String productGroupName1) {
			this.productGroupName1 = productGroupName1;
		}
		public String getProductGroupName2() {
			return productGroupName2;
		}
		public void setProductGroupName2(String productGroupName2) {
			this.productGroupName2 = productGroupName2;
		}
		public String getProductGroupName3() {
			return productGroupName3;
		}
		public void setProductGroupName3(String productGroupName3) {
			this.productGroupName3 = productGroupName3;
		}
	}
}
