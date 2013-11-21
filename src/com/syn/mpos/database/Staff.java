package com.syn.mpos.database;

import java.util.List;
import com.syn.pos.ShopData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Staff {
	public static final String TB_NAME = "Staff";
	public static final String COL_STAFF_ID = "StaffID";
	public static final String COL_STAFF_CODE = "StaffCode";
	public static final String COL_STAFF_NAME = "StaffName";
	public static final String COL_STAFF_PASS = "StaffPassword";
	
	private MPOSSQLiteHelper mSqlite;
	
	public Staff(Context c) {
		mSqlite = new MPOSSQLiteHelper(c);
	}

	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_NAME +
				" WHERE " + COL_STAFF_ID + "=" + staffId);
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(COL_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(COL_STAFF_NAME)));
		}
		cursor.close();
		mSqlite.close();
		return s;
	}
	
	public boolean insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		boolean isSucc = false;
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM " + TB_NAME);
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_STAFF_ID, staff.getStaffID());
			cv.put(COL_STAFF_CODE, staff.getStaffCode());
			cv.put(COL_STAFF_NAME, staff.getStaffName());
			cv.put(COL_STAFF_PASS, staff.getStaffPassword());
			isSucc = mSqlite.insert(TB_NAME, cv);
		}
		mSqlite.close();
		return isSucc;
	}
	
}
