package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.MenuGroups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * 
 * @author j1tth4
 *
 */
public class MenuItem {
	private String mSaleModeParam = "sale_mode_1 = 1";
	private String mColSaleModePrice = "product_price";
	
	private MPOSSQLiteHelper mDbHelper;
	
	public MenuItem(Context c) {
		mDbHelper = new MPOSSQLiteHelper(c);
	}
	
	public List<MenuGroups.MenuItem> listMenuItem(String query){
		List<MenuGroups.MenuItem> menuLst 
			= new ArrayList<MenuGroups.MenuItem>();
		
		String strSql = "SELECT a.menu_name_0, a.menu_image_link, " +
				" b.product_id, b.product_code, " +
				" b.product_bar_code, b.product_price " +
				" FROM menu_item a" +
				" LEFT JOIN products b" +
				" ON a.product_id=b.product_id" +
				" WHERE (b.product_code LIKE '%" + query + "%' " +
				" OR a.menu_name_0 LIKE '%" + query + "%') " +
				" AND a.menu_activate=1 " +
				" AND b.activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				MenuGroups.MenuItem menu = 
						new MenuGroups.MenuItem();
				menu.setProductID(cursor.getInt(cursor.getColumnIndex("product_id")));
				menu.setProductCode(cursor.getString(cursor.getColumnIndex("product_code")));
				menu.setProductBarCode(cursor.getString(cursor.getColumnIndex("product_bar_code")));
				menu.setMenuName_0(cursor.getString(cursor.getColumnIndex("menu_name_0")));
				menu.setMenuImageLink(cursor.getString(cursor.getColumnIndex("menu_image_link")));
				menu.setProductPricePerUnit(cursor.getFloat(cursor.getColumnIndex("product_price")));
				menuLst.add(menu);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return menuLst;
	}
	
	public List<MenuGroups.MenuItem> listMenuItem(int menuDeptId, int saleMode){
		List<MenuGroups.MenuItem> miLst = 
				new ArrayList<MenuGroups.MenuItem>();
		
		filterSaleMode(saleMode);
		
		String strSql = " SELECT a.menu_name_0, a.menu_name_1, " +
				" a.menu_name_2, a.menu_desc_0, " +
				" a.menu_short_name_0, a.menu_short_name_1, " +
				" a.menu_image_link, " +
				" b.product_id, b.product_code, b.product_bar_code, " +
				" b." + mColSaleModePrice + ", " +
				" b.discount_allow, b.is_out_of_stock, " +
				" b.vat_type, b.product_unit_name " +
				" FROM menu_item a " +
				" LEFT JOIN products b " +
				" ON a.product_id=b.product_id " +
				" WHERE a.menu_dept_id=" + menuDeptId +
				" AND b." + mSaleModeParam + 
				" AND a.menu_activate=1 " +
				" ORDER BY a.menu_ordering";	
		
		mDbHelper.open();
		
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				MenuGroups.MenuItem mi = new MenuGroups.MenuItem();
				mi.setProductID(cursor.getInt(cursor.getColumnIndex("product_id")));
				mi.setProductBarCode(cursor.getString(cursor.getColumnIndex("product_bar_code")));
				mi.setProductPricePerUnit(cursor.getFloat(cursor.getColumnIndex(mColSaleModePrice)));
				mi.setIsOutOfStock(cursor.getInt(cursor.getColumnIndex("is_out_of_stock")));
				mi.setVatType(cursor.getInt(cursor.getColumnIndex("vat_type")));
				mi.setProductUnitName(cursor.getString(cursor.getColumnIndex("product_unit_name")));
				mi.setMenuName_0(cursor.getString(cursor.getColumnIndex("menu_name_0")));
				mi.setMenuName_1(cursor.getString(cursor.getColumnIndex("menu_name_1")));
				mi.setMenuName_2(cursor.getString(cursor.getColumnIndex("menu_name_2")));
				mi.setMenuShortName_0(cursor.getString(cursor.getColumnIndex("menu_short_name_0")));
				mi.setMenuImageLink(cursor.getString(cursor.getColumnIndex("menu_image_link")));
				
				miLst.add(mi);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		
		return miLst;
	}
	
	public boolean addMenuItem(List<MenuGroups.MenuItem> mgLst) {
		boolean isSucc = false;
		
		mDbHelper.open();
		mDbHelper.execSQL("DELETE FROM menu_item");
		
		ContentValues cv = new ContentValues();
		for (MenuGroups.MenuItem mi : mgLst) {
			cv.put("menu_item_id", mi.getMenuItemID());
			cv.put("product_id", mi.getProductID());
			cv.put("menu_dept_id", mi.getMenuDeptID());
			cv.put("menu_group_id", mi.getMenuGroupID());
			cv.put("menu_name_0", mi.getMenuName_0());
			cv.put("menu_name_1", mi.getMenuName_1());
			cv.put("menu_name_2", mi.getMenuName_2());
			cv.put("menu_name_3", mi.getMenuName_3());
			cv.put("menu_desc_0", mi.getMenuDesc_0());
			cv.put("menu_desc_1", mi.getMenuDesc_1());
			cv.put("menu_desc_2", mi.getMenuDesc_2());
			cv.put("menu_desc_3", mi.getMenuDesc_3());
			cv.put("menu_short_name_0", mi.getMenuShortName_0());
			cv.put("menu_short_name_1", mi.getMenuShortName_1());
			cv.put("menu_short_name_2", mi.getMenuShortName_2());
			cv.put("menu_short_name_3", mi.getMenuShortName_3());
			cv.put("menu_image_link", mi.getMenuImageLink());
			cv.put("menu_ordering", mi.getMenuItemOrdering());
			cv.put("updatedate", mi.getUpdateDate());
			cv.put("menu_activate", mi.getMenuActivate());

			isSucc = mDbHelper.insert("menu_item", cv);
		}
		mDbHelper.close();
		return isSucc;
	}

	private void filterSaleMode(int saleMode){
		switch(saleMode){
		case 1:
			mSaleModeParam = "sale_mode_1 = 1";
			mColSaleModePrice = "product_price";
			break;
		case 2:
			mSaleModeParam = "sale_mode_2 = 1";
			mColSaleModePrice = "product_price_2";
			break;
		case 3:
			mSaleModeParam = "sale_mode_3 = 1";
			mColSaleModePrice = "product_price_3";
			break;
		}
	}
}
