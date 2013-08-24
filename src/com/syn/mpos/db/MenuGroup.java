package com.syn.mpos.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.syn.mpos.model.MenuGroups;

public class MenuGroup {
	private final String TB_MENU_GROUP = "menu_group";

	private MPOSSQLiteHelper dbHelper;
	
	public MenuGroup(Context c) {
		dbHelper = new MPOSSQLiteHelper(c);
	}
	
	public List<MenuGroups.MenuGroup> listAllMenuGroup() {
		List<MenuGroups.MenuGroup> mgl = 
				new ArrayList<MenuGroups.MenuGroup>();

		String strSql = " SELECT * FROM " +
				TB_MENU_GROUP + 
				" WHERE menu_group_type_id=0 " +
				" ORDER BY menu_group_ordering";

		dbHelper.open();
		Cursor cursor = dbHelper.rawQuery(strSql);

		if(cursor.moveToFirst()){
			do{
				MenuGroups.MenuGroup mg = new MenuGroups.MenuGroup();

				mg.setMenuGroupID(cursor.getInt(cursor.getColumnIndex("menu_group_id")));
				mg.setMenuGroupName_0(cursor.getString(cursor.getColumnIndex("menu_group_name_0")));
				mg.setMenuGroupName_1(cursor.getString(cursor.getColumnIndex("menu_group_name_1")));
				mg.setMenuGroupName_2(cursor.getString(cursor.getColumnIndex("menu_group_name_2")));
				mg.setMenuGroupName_3(cursor.getString(cursor.getColumnIndex("menu_group_name_3")));
				mg.setMenuGroupOrdering(cursor.getInt(cursor.getColumnIndex("menu_group_ordering")));
				mg.setMenuGroupType(cursor.getInt(cursor.getColumnIndex("menu_group_type_id")));
				mg.setUpdateDate(cursor.getString(cursor.getColumnIndex("updatedate")));

				mgl.add(mg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		dbHelper.close();

		return mgl;
	}
	
	public boolean addMenuGroup(List<MenuGroups.MenuGroup> mgLst){
		boolean isSucc = false;
		
		dbHelper.open();
		dbHelper.execSQL("DELETE FROM " + TB_MENU_GROUP);
		
		for (MenuGroups.MenuGroup mg : mgLst) {
			ContentValues cv = new ContentValues();
			cv.put("menu_group_id", mg.getMenuGroupID());
			cv.put("menu_group_name_0", mg.getMenuGroupName_0());
			cv.put("menu_group_name_1", mg.getMenuGroupName_1());
			cv.put("menu_group_name_2", mg.getMenuGroupName_2());
			cv.put("menu_group_name_3", mg.getMenuGroupName_3());
			cv.put("menu_group_type_id", mg.getMenuGroupType());
			cv.put("menu_group_ordering", mg.getMenuGroupOrdering());
			cv.put("updatedate", mg.getUpdateDate());
			
			isSucc = dbHelper.insert(TB_MENU_GROUP, cv);
		}
		dbHelper.close();
		return isSucc;
	}
}
