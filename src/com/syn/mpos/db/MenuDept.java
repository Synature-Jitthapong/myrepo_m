package com.syn.mpos.db;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.model.MenuGroups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MenuDept{
	private final String tbMenuDept = "menu_dept";
	
	private MPOSSQLiteHelper dbHelper;
	
	public MenuDept(Context c) {
		dbHelper = new MPOSSQLiteHelper(c);
	}
	
	public List<MenuGroups.MenuDept> listMenuDept() {
		List<MenuGroups.MenuDept> mdl = 
				new ArrayList<MenuGroups.MenuDept>();

		dbHelper.open();
		
		String strSql = "SELECT * FROM " + tbMenuDept +
				" ORDER BY menu_dept_ordering";
		
		Cursor cursor = dbHelper.rawQuery(strSql);

		if(cursor.moveToFirst()){
			do{
				MenuGroups.MenuDept md = new MenuGroups.MenuDept();

				md.setMenuDeptID(cursor.getInt(cursor.getColumnIndex("menu_dept_id")));
				md.setMenuGroupID(cursor.getInt(cursor.getColumnIndex("menu_group_id")));
				md.setMenuDeptName_0(cursor.getString(cursor.getColumnIndex("menu_dept_name_0")));
				md.setMenuDeptName_1(cursor.getString(cursor.getColumnIndex("menu_dept_name_1")));
				md.setMenuDeptName_2(cursor.getString(cursor.getColumnIndex("menu_dept_name_2")));
				md.setMenuDeptName_3(cursor.getString(cursor.getColumnIndex("menu_dept_name_3")));
				md.setMenuDeptOrdering(cursor.getInt(cursor.getColumnIndex("menu_dept_ordering")));
				md.setUpdateDate(cursor.getString(cursor.getColumnIndex("updatedate")));

				mdl.add(md);

			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();

		return mdl;
	}
	
	public boolean addMenuDept(List<MenuGroups.MenuDept> mdLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + tbMenuDept);
		for(MenuGroups.MenuDept md : mdLst){
			ContentValues cv = new ContentValues();
			cv.put("menu_dept_id", md.getMenuDeptID());
			cv.put("menu_group_id", md.getMenuGroupID());
			cv.put("menu_dept_name_0", md.getMenuDeptName_0());
			cv.put("menu_dept_name_1", md.getMenuDeptName_1());
			cv.put("menu_dept_name_2", md.getMenuDeptName_2());
			cv.put("menu_dept_name_3", md.getMenuDeptName_3());
			cv.put("menu_dept_ordering", md.getMenuDeptOrdering());
			cv.put("updatedate", md.getUpdateDate());
			
			isSucc = dbHelper.insert(tbMenuDept, cv);
		}
		dbHelper.close();
		
		return isSucc;
	}
	
	
}
